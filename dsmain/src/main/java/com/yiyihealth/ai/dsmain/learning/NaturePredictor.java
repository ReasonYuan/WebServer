package com.yiyihealth.ai.dsmain.learning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ds.esearch.syntaxtrie.SynSeqNode;
import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.TrieNode;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager.OnRecordLoadListener;

public class NaturePredictor {
	
	private static class ProbValueUnitOrTestItem {
		String word;
		String aroundText;
		String aroundWithNature;
		@Override
		public boolean equals(Object obj) {
			return word.equals(((ProbValueUnitOrTestItem)obj).word);
		}
	}
	
	/**
	 * 打印出前后多少个词供参考
	 */
	private static final int PRE_NEXT_WORDS_4REF = 6;
	
	public static void main(String[] args) throws Exception {
		
		/**
		 * 要预测的新词的词性
		 */
		final String predictNature = "Drug";
		//过滤掉可能是数值单位或化验项的词, 并输出到文件ProbBeValueUnitOrTestIem.txt
		final boolean filterProbValueUnit = true;
		JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
		final String projectDir = config.getString("projectDir");
		final String probValueUnitFile = projectDir + "/predicts/"+predictNature+"/ProbBeValueUnitOrTestIem.txt";
		final SynSeqNode zxRoot = (SynSeqNode) FileManager.readObject(projectDir + "/predicts/"+predictNature+"/StatsTree.obj");
		final SynSeqNode dxRoot = (SynSeqNode) FileManager.readObject(projectDir + "/predicts/"+predictNature+"/StatsTreeDaoxu.obj");
		
		//数据中还有很多未识别的
		final ArrayList<ProbValueUnitOrTestItem> unRecValueUnits = new ArrayList<ProbValueUnitOrTestItem>();
		
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(projectDir + "/predicts/"+predictNature+"/"+predictNature+"Predicts.txt"))));
		final int[] recordsCounter = {0};
		new FileManager().loadWords(projectDir + "/nlpwords/", new OnRecordLoadListener() {
			public void onRecordLoad(String filename, ArrayList<EWord> allWords) {
				//if(filename.equals("doctor_13671609763_record_2237.json")){
					if (allWords.size() != 0) {
						EWord[] eWords = new EWord[allWords.size()];
						allWords.toArray(eWords);
						new NaturePredictor().predict(recordsCounter[0], eWords, false, zxRoot, predictNature, writer, unRecValueUnits, filterProbValueUnit);
						new NaturePredictor().predict(recordsCounter[0], eWords, true, dxRoot, predictNature, writer, unRecValueUnits, filterProbValueUnit);
						recordsCounter[0]++;
					}
				//}
			}
		});
		writer.flush();
		writer.close();
		
		writeProbBeValueUnit(unRecValueUnits, probValueUnitFile);
		System.out.println("recordsCounter: " + recordsCounter[0]);
		
	}
	
	/**
	 * 记录下很可能是数值单位的内容
	 * @param unRecValueUnits
	 */
	private static void writeProbBeValueUnit(ArrayList<ProbValueUnitOrTestItem> unRecValueUnits, String file) throws Exception {
		BufferedWriter bWriter = null;
		try {
			bWriter = new  BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(file))));
			for(ProbValueUnitOrTestItem item : unRecValueUnits){
				bWriter.write(item.word + "\t\t\t\t" + item.aroundText + "\t\t\t\t" + item.aroundWithNature);
				bWriter.newLine();
			}
			bWriter.flush();
		} finally {
			if (bWriter != null) {
				bWriter.close();
			}
		}
	}
	
	/**
	 * 把数组内容倒序
	 * @param eWords
	 */
	private void reverseArray(EWord[] eWords) {
		// 生产一个倒序ewords
		EWord[] eWordsDaoxu = new EWord[eWords.length];
		for (int i = 0; i < eWordsDaoxu.length; i++) {
			eWordsDaoxu[i] = eWords[eWordsDaoxu.length - i - 1];
		}
		System.arraycopy(eWordsDaoxu, 0, eWords, 0, eWords.length);
	}
	
	public void predict(int recordIndex, EWord[] eWords, boolean isDaoxu, SynSeqNode synRoot,String tagNature, BufferedWriter writer, ArrayList<ProbValueUnitOrTestItem> unRecValueUnits, boolean filterProbValueUnit){
		if (isDaoxu) {
			reverseArray(eWords);
		}
		@SuppressWarnings("rawtypes")
		
		ArrayList[] taggedInfo = tagNatureSequence(eWords, synRoot, tagNature);
		ArrayList<int[]> sumsMap = new ArrayList<>();
		for (int i = 0; i < taggedInfo.length; i++) {
			if (taggedInfo[i] != null && SequanceStats.filterWord(eWords[i])) {
				int sum = 0;
				for (int j = 0; j < taggedInfo[i].size(); j++) {
					SynSeqNode node = (SynSeqNode)taggedInfo[i].get(j);
					sum += node.getCounter();
				}
				int[] sumMap = new int[3];
				sumMap[0] = sum;
				sumMap[1] = i;
				sumMap[2] = taggedInfo[i].size();
				sumsMap.add(sumMap);
			}
		}
		sumsMap.sort(new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				return o2[0] - o1[0];
			}
		});
		int filteredSum = 0;
		int filteredCnt = 0;
		if (sumsMap.size() > 0) {
			String zxdx = isDaoxu ? "倒序" : "正序";
			writeOneLine2File(writer, "====" + zxdx + "====");
		}
		//一些通配词性，比如数值、数值单位、日期等就不要列出来了
		final String[] anyMatchesNatures = {WordNatures.PUNC, WordNatures.VALUE, WordNatures.VALUEUNIT, WordNatures.DATE, WordNatures.PERIOD};
		for (int i = 0; i < sumsMap.size(); i++) {
			int[] map = sumsMap.get(i);
			String around = getStringAround(eWords, map[1], PRE_NEXT_WORDS_4REF, PRE_NEXT_WORDS_4REF, true, isDaoxu);
			String aroundWithoutNature = getStringAround(eWords, map[1], PRE_NEXT_WORDS_4REF, PRE_NEXT_WORDS_4REF, false, isDaoxu);
			EWord word = eWords[map[1]];
			boolean foundCorrect = tagNature.equals(word.getNature());
			boolean needReport = true;
			//是否已经在词典里了，如果已经在词典里的就不需要找出来
			if (HealthAnalysis.getNormalForest().searchWord(word.getWord())) {
				needReport = false;
			}
			//进行词典过滤，过滤掉已有词和通配词（时间、数值，单位、标点符号、时间长度
			//一些通配词性，比如数值、数值单位、日期等就不要列出来了
			for (int j = 0; j < anyMatchesNatures.length; j++) {
				if (word.getNature().equals(anyMatchesNatures[j])) {
					needReport = false;
					break;
				}
			}
			
			//TODO 临时的，我们再把药报告出来, 为了统计已经词典过滤的正确率
			if(word.getNature().equals(tagNature)){
				needReport = true;
			}
			if (needReport) {
				if (filterProbValueUnit && isProbBeValueUnitOrTestItem(word)) {
					ProbValueUnitOrTestItem item = new ProbValueUnitOrTestItem();
					item.word = word.getWord();
					item.aroundText = aroundWithoutNature;
					item.aroundWithNature = around;
					if(!unRecValueUnits.contains(item)){
						unRecValueUnits.add(item);
					}
				} else {
					filteredSum++;
					if(foundCorrect) filteredCnt++;
					String predictInfo = filteredSum + ": " + map[0] + ", " + map[2] + ", right: "+ (foundCorrect ? "^T^" : "*F*") + ", " +  eWords[map[1]] + ", 原文: " + aroundWithoutNature + ", " + around;
					System.out.println(predictInfo);
					writeOneLine2File(writer, predictInfo);
				}
			}
		}
		
		if (sumsMap.size() > 0) {
			String zxdx = isDaoxu ? "倒序" : "正序";
//			String unfiltered = zxdx + "未经词典过滤的正确率: " + ((float)correctCnt)/sumsMap.size()*100;
//			System.out.println(unfiltered);
//			writeOneLine2File(writer, unfiltered);
			String filtered = zxdx + "已经词典过滤的正确率: " + ((float)filteredCnt)/filteredSum*100;
			System.out.println(filtered);
			writeOneLine2File(writer, filtered);
			writeOneLine2File(writer, "第" + recordIndex + "个病历记录完成!");
		}
	}
	
	/**
	 * 是否很可能是数值单位、化验项
	 * @param eWord
	 * @return
	 */
	private boolean isProbBeValueUnitOrTestItem(EWord eWord){
		if (!eWord.getNature().equals(WordNatures.UNREC)) {
			return false;
		}
		String word = eWord.getWord();
		if (word.length() == 0) {
			return false;
		}
		char[] chars = word.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			boolean isDigital = c >= '0' && c <= '9';
			boolean isSpecial = c == '-' || c == '^' || c == '/' || c == '.';
			boolean isEnChar = c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
			if (!(isEnChar || isDigital || isSpecial)) {
				return false;
			}
		}
		return true;
	}
	
	private void writeOneLine2File(BufferedWriter writer, String line){
		if (writer != null) {
			try {
				writer.write(line);
				writer.newLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getStringAround(EWord[] eWords, int pos, int pre, int next, boolean withNature, boolean isDaoxu){
		int start = pos - pre;
		int end = pos + next+1;
		if (start <= 0) {
			start = 0;
		}
		if (end >= eWords.length) {
			end = eWords.length - 1;
		}
		String string = "";
		if (isDaoxu) {
			for (int i = end - 1; i >= start; i--) {
				string += withNature ? "[" : "";
				string += eWords[i].getWord() + (withNature ? ("|" + eWords[i].getNature()) : "") + (pos == i && withNature ? "*" : "");
				string += withNature ? "]" : "";
			}
		} else {
			for (int i = start; i < end; i++) {
				string += withNature ? "[" : "";
				string += eWords[i].getWord() + (withNature ? ("|" + eWords[i].getNature()) : "") + (pos == i && withNature ? "*" : "");
				string += withNature ? "]" : "";
			}
		}
		return string;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList[] tagNatureSequence(EWord[] eWords, SynSeqNode synSeqRoot,String tagNature){
		ArrayList[] matchedNodes = new ArrayList[eWords.length];
		for (int i = 0; i < eWords.length; i++) {
			SynSeqNode currentNode = synSeqRoot;
			//被预测那个位置不需要匹配
			boolean skipTagNatureMatch = false;
			for (int j = i; j < eWords.length; j++) {
				EWord eWord = eWords[j];
				if (Punctuation.isSentenceEnd(eWord)) {
					break;
				}
				if (SequanceStats.filterWord(eWord)) {
					String nature = eWord.getNature();
					boolean foundWord = false;
					if (skipTagNatureMatch) {
						skipTagNatureMatch = false;
						currentNode = currentNode.getNode(tagNature);
						//模糊匹配不论是否找到词都算成功
						foundWord = true;
					} else {
						currentNode = currentNode.getNode(nature);
						if (currentNode != null && currentNode.getWordTrieRoot() != null) {
							TrieNode wordNode = currentNode.getWordTrieRoot().getNode(eWord.getWord());
							if (wordNode != null || isAnyMatchNode(eWord)) {
								//找到了对应的词
								foundWord = true;
							}
						}
					}
					if (currentNode != null && foundWord) {
						SynSeqNode tagNode = currentNode.getNode(tagNature);
						if(tagNode != null){
							//找个一个匹配, 预计接下来存在需要查找的词性
							int predictPos = findNextNoPuncPos(j, eWords);
							if (predictPos != -1) {
								if (predictPos < eWords.length) {
									if (continueMatch(eWords, tagNode, predictPos) > 0) {
										if (matchedNodes[predictPos] == null) {
											matchedNodes[predictPos] = new ArrayList<SynSeqNode>();
										}
										matchedNodes[predictPos].add(tagNode);
									}
									skipTagNatureMatch = true;
								}
							} else {
								break;
							}
						}
					} else {
						break;
					}
				}
			}
		}
		return matchedNodes;
	}
	
	/**
	 * 对于数值等可以采用任意匹配的，用任意匹配
	 * @param eWord
	 * @return
	 */
	private boolean isAnyMatchNode(EWord eWord){
		final String[] anyMatchNatures = {
				WordNatures.VALUE
			};
		for (int i = 0; i < anyMatchNatures.length; i++) {
			if (anyMatchNatures[i].equals(eWord.getNature())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 继续匹配预测词后的词
	 * @param eWords
	 * @param currentNode
	 * @param predictPos
	 * @return
	 */
	private int continueMatch(EWord[] eWords, SynSeqNode predictNode, int predictPos){
		int step = 0;
		int start = predictPos+1;
		if (start < eWords.length) {
			SynSeqNode currentNode = predictNode;
			for (int i = start; i < eWords.length; i++) {
				EWord eWord = eWords[i];
				if (Punctuation.isSentenceEnd(eWord)) {
					break;
				}
				if (SequanceStats.filterWord(eWord)) {
					String nature = eWord.getNature();
					currentNode = currentNode.getNode(nature);
					if (currentNode != null &&
							(isAnyMatchNode(eWord) || currentNode.getWordTrieRoot() != null && currentNode.getWordTrieRoot().getNode(eWord.getWord()) != null)) {
						step++;
					} else {
						break;
					}
				}
			}
		}
		return step;
	}
	
	private int findNextNoPuncPos(int currentPos, EWord[] eWords){
		int pos = -1;
		for (int j = currentPos+1; j < eWords.length; j++) {
			EWord eWord = eWords[j];
			if (Punctuation.isSentenceEnd(eWord)) {
				break;
			}
			if (SequanceStats.filterWord(eWord)) {
				return j;
			}
		}
		return pos;
	}
	
}
