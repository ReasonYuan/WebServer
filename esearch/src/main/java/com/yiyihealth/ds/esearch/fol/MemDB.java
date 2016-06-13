package com.yiyihealth.ds.esearch.fol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.NewEvidenceWritter;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class MemDB {
	
	private ArrayList<WordPos> words = new ArrayList<SimpleEvidenceSearcher.WordPos>();
	
	private ArrayList<EWord> allWords = new ArrayList<EWord>();
	
	private ArrayList<ArrayList<EWord>> oneRecord = new ArrayList<ArrayList<EWord>>();
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	
	@SuppressWarnings("serial")
	private Hashtable<Integer, Hashtable<String, String>> sentenceQueryByNature = new Hashtable<Integer, Hashtable<String,String>>(){
		public synchronized Hashtable<String,String> get(Object key) {
			Hashtable<String,String> result = super.get(key);
			if (result == null) {
				result = new Hashtable<String,String>();
				super.put((Integer) key, result);
			}
			return result;
		};
	};
	
	@SuppressWarnings("serial")
	private Hashtable<Integer, Hashtable<String, String>> sentenceQueryByWord = new Hashtable<Integer, Hashtable<String,String>>(){
		public synchronized Hashtable<String,String> get(Object key) {
			Hashtable<String,String> result = super.get(key);
			if (result == null) {
				result = new Hashtable<String,String>();
				super.put((Integer) key, result);
			}
			return result;
		};
	};
	
	private WordPos[] wordsArr = null;
	
	private MemDB(String file) {
		try {
			String text = FileUtils.fileRead(file);
			JSONArray jsonArray = JSONArray.parseArray(text);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				JSONObject word = object.getJSONObject("eWord");
				EWord eWord = new EWord(word.getString("word"), word.getString("nature"));
				JSONObject attributes = word.getJSONObject("attributes");
				if (attributes != null) {
					for(String key : attributes.keySet()){
						eWord.putAttribute(key, attributes.getString(key));
					}
				}
				eWord.parseDateOnInit();
				WordPos wordPos = new WordPos(eWord, object.getIntValue("pos"), object.getIntValue("sentencePos"), object.getIntValue("recordPos"), object.getIntValue("blockPos"));
				words.add(wordPos);
				allWords.add(eWord);
			}
			oneRecord.add(allWords);
		} catch (Exception e) {
			e.printStackTrace();
		}
		wordsArr = new WordPos[words.size()];
		for (WordPos pos : words) {
			wordsArr[pos.pos] = pos;
		}
	}

	public static MemDB load(String file){
		return new MemDB(file);
	}
	
	public WordPos findMaxDate(){
		long maxTime = 0;
		WordPos wordPosCan = null;
		for (int i = 0; i < wordsArr.length; i++) {
			WordPos wordPos = wordsArr[i];
			if (wordPos.eWord.getAttribute(EWord.ATTR_TIMESTAMP) != null) {
				if ((Long)wordPos.eWord.getAttribute(EWord.ATTR_TIMESTAMP) > maxTime) {
					maxTime = Math.max(maxTime, (Long)wordPos.eWord.getAttribute(EWord.ATTR_TIMESTAMP));
					wordPosCan = wordPos;
				}
			}
		}
		return wordPosCan;
	}
	
	public WordPos findExistsWord(String word, String nature, String attrkey, String attrvalue){
		for (int i = 0; i < wordsArr.length; i++) {
			WordPos wordPos = wordsArr[i];
			boolean passed = true;
			boolean checked = false;
			if (!word.equals("null")) {
				if (!wordPos.eWord.getWord().equals(word)) {
					passed = false;
				}
				checked = true;
			}
			if (!nature.equals("null")) {
				if (!wordPos.eWord.getNature().equals(nature)) {
					passed = false;
				}
				checked = true;
			}
			if (!attrkey.equals("null")) {
				if (wordPos.eWord.getAttribute(attrkey) == null) {
					passed = false;
				} else {
					if (!attrvalue.equals("null")){
						if (!wordPos.eWord.getAttribute(attrkey).equals(attrvalue)) {
							passed = false;
						}
					}
				}
				checked = true;
			}
			if (checked && passed) {
				return wordPos;
			}
		}
		return null;
	}
	
	/**
	 * 向前或向后查找第一个出现的满足条件的词
	 * @param isBefore
	 * @param posRef
	 * @param range
	 * @param wordCompare
	 * @param natureCompare
	 * @param attrKeyCompare
	 * @param attrValueCompare
	 * @return
	 */
	public WordPos findBeforeOrAfter(String predicateName, int posRef, Object range, Object wordCompare, Object natureCompare, Object attrKeyCompare, Object attrValueCompare){
		boolean inSentence = range.equals("IN_SENTENCE");
		boolean inSection = range.equals("IN_SECTION");
		int searchPos = 0;
		int endPos = wordsArr.length - 1;
		//是否在一个指定数字范围内查找
		boolean isSearchInRange = false;
		if (!(inSection || inSentence)) {
			String rangeStr = range.toString();
			int intPosStart = 0;
			if (rangeStr.charAt(0) == '~') {
				intPosStart = 1;
				isSearchInRange = true;
			}
			//指定位置查找
			try {
				searchPos = Integer.parseInt(range.toString().substring(intPosStart));
				if (searchPos == 0) {
					throw new RuntimeException("findBeforeOrAfter#param range不能为0!");
				}
			} catch (Exception e) {
				throw new RuntimeException("findBeforeOrAfter#param range应该是个数值!", e);
			}
			if (searchPos > 0 && predicateName.equals(Predicate.BUILDIN_FINDFIRSTBEFORE)) {
				throw new RuntimeException("范围参数为正时必需用FindFirstAfter! range: " + rangeStr);
			}
			if (searchPos < 0 && predicateName.equals(Predicate.BUILDIN_FINDFIRSTAFTER)) {
				throw new RuntimeException("范围参数为负时必需用FindFirstBefore! range: " + rangeStr);
			}
		}
		int startPos = posRef;
		int dir = 0;
		if (searchPos != 0) {
			if (isSearchInRange) {
				dir = searchPos > 0 ? 1 : -1;
				endPos = startPos + searchPos;
				startPos += dir;
				inSentence = true;
			} else {
				startPos += searchPos;
			}
		} else {
			dir = predicateName.equals(Predicate.BUILDIN_FINDFIRSTBEFORE) ? -1 : 1;
			startPos += dir;
			if (dir == -1) {
				endPos = 0;
			}
		}
		
		if (startPos < 0 || startPos >= wordsArr.length) {
			if (isSearchInRange) {
				if (startPos < 0) {
					startPos = 0;
					if (startPos == posRef) {
						return null;
					}
				}
				if(startPos >= wordsArr.length){
					startPos = wordsArr.length - 1;
					if (startPos == posRef) {
						return null;
					}
				}
			} else {
				return null;
			}
		}
		boolean onlySearchOnce = searchPos != 0 && !isSearchInRange;//或者dir == 0;
		//TODO 需要重构，否则难以理解一次查找和循环查找
		for (int i = startPos; i >= 0 && i < wordsArr.length && (onlySearchOnce || (dir == 1 ? i <= endPos : i >= endPos)); i = i + dir) {
			WordPos wordPos = wordsArr[i];
			if (inSentence) {
				if (wordPos.sentencePos != wordsArr[posRef].sentencePos) {
					//找完整句未发现
					return null;
				}
			} else if(inSection) {
				if (wordPos.blockPos != wordsArr[posRef].blockPos) {
					//找完整段未发现
					return null;
				}
			}
			boolean satisified = true;
			boolean checked = false;
			if (!wordCompare.equals("null")) {
				if (wordCompare.equals("*")) {
					satisified = true;
				} else if(!wordPos.eWord.getWord().equals(wordCompare)){
					satisified = false;
				}
				checked = true;
			}
			if (satisified && !natureCompare.equals("null")) {
				if (natureCompare.equals("*")) {
					satisified = true;
				} else if(!wordPos.eWord.getNature().equals(natureCompare)){
					satisified = false;
				}
				checked = true;
			}
			if (satisified && !attrKeyCompare.equals("null")) {
				Object value = wordPos.eWord.getAttribute(attrKeyCompare.toString());
				if (!attrValueCompare.equals("null")) {
					if(value == null || !value.equals(attrValueCompare)){
						satisified = false;
					}
				} else {
					if (value == null) {
						satisified = false;
					}
				}
				checked = true;
			}
			if (!checked) {
				throw new RuntimeException("compareWord, compareNature, compareAttrKey必有一项非空, 如果compareAttrValue不为空，则compareAttrKey必须不为空！");
			}
			if (satisified) {
				return wordPos;
			}
			//指定位置查找只一次
			if (onlySearchOnce) {
				break;
			}
		}
		return null;
	}
	
	
	//TOOD
	public ArrayList<String[]> findSequence(String predicateName, int posRef, Object range,Object txt){
		
		// 读取配置文件
		JSONObject config = null;
		try {
			config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		final String projectDir = config.getString("projectDir");
		
		NewEvidenceWritter writter = new NewEvidenceWritter();
		SimpleEvidenceSearcher searcher = new SimpleEvidenceSearcher(
				projectDir + "/../../simple_syntax/"+txt, writter);
		searcher.searchSimpleEvidence(oneRecord);
		ArrayList<String> evidences = writter.getEvidences();
		
		ArrayList<String[]> list = new ArrayList<String[]>();
		for (int i = 0; i < evidences.size(); i++) {
			String evdence = evidences.get(i);
			StringTokenizer tokenizer = new StringTokenizer(evdence,"(,) ");
			String[] tokens = new String[tokenizer.countTokens()];
			for (int j = 0; j < tokens.length; j++) {
				tokens[j] = tokenizer.nextToken();
			}
			list.add(tokens);
			System.out.println(tokens[0]+tokens[1]+tokens[2]);
		}
		
		
		ArrayList<String[]> wordPoss = new ArrayList<String[]>();
		boolean inSentence = range.equals("IN_SENTENCE");
		boolean inSection = range.equals("IN_SECTION");
		int searchPos = 0;
		if (!(inSection || inSentence)) {
			//指定位置查找
			try {
				searchPos = Integer.parseInt(range.toString());
				if (searchPos == 0) {
					throw new RuntimeException("findSequence#param range不能为0!");
				}
			} catch (Exception e) {
				throw new RuntimeException("findSequence#param range应该是个数值!", e);
			}
		}
		int startPos = posRef;
		int dir = 0;
		if (searchPos != 0) {
			startPos += searchPos;
		} else {
			dir = 1;
			startPos += dir;
		}
		
		if (startPos < 0 || startPos >= wordsArr.length) {
			return null;
		}
		
		
		//TOOD 直接使用txt证出的结论
		for (int i = 0; i < list.size(); i++) {
			String[] strs  = list.get(i);
			if (Integer.parseInt(strs[1]) - startPos < 2 && Integer.parseInt(strs[1]) - startPos > 0) {
				wordPoss.add(strs);
			}
		}
		
//		
//		//TODO 需要重构，否则难以理解一次查找和循环查找
//		for (int i = startPos; i >= 0 && i < wordsArr.length; i = i + dir) {
//			WordPos wordPos = wordsArr[i];
//			if (inSentence) {
//				if (wordPos.sentencePos != wordsArr[posRef].sentencePos) {
//					//找完整句
//					break;
//				}
//			} else if(inSection) {
//				if (wordPos.blockPos != wordsArr[posRef].blockPos) {
//					//找完整段
//					break;
//				}  
//			}
//			boolean satisified = true;
//			if (!wordPos.eWord.getNature().equals("Value") && !wordPos.eWord.getNature().equals("ValueUnit") && !wordPos.eWord.getNature().equals("DirVerb")) {
//				satisified = false;
//			}
//			
//			if(satisified){
//				wordPoss.add(wordPos);
//			}
//
//			
//		}
		return wordPoss;
	}
	
	
	/**
	 * 根据位置返回对应词
	 * @param pos
	 * @return
	 */
	public EWord getEWord(int pos){
		return wordsArr[pos].eWord;
	}
	
	public int getWordsSize(){
		return wordsArr.length;
	}
	
	public WordPos[] getWords(){
		return wordsArr;
	}
	
	//TODO 临时方法, 不应该放在这里，需要重构
	public void tagDate(Atom atom, String date){
		for (int i = 0; i < wordsArr.length; i++) {
			WordPos wordPos = wordsArr[i];
			if (wordPos.pos == Integer.parseInt(atom.getParams().get(0).toString())) {
				if(!wordPos.eWord.getTaggedDates().contains(atom.getParams().get(3).toString())){
					wordPos.eWord.tagDate(atom.getParams().get(3).toString());
					wordPos.eWord.tagActualDate(date);
				}
				
				break;
			}
		}
	}
	
	//TODO 临时方法, 不应该放在这里，需要重构
	public ArrayList<EWord> toEWords(){
		ArrayList<EWord> results = new ArrayList<EWord>();
		for (int i = 0; i < wordsArr.length; i++) {
			WordPos wordPos = wordsArr[i];
			results.add(wordPos.eWord);
		}
		return results;
	}
	
	//TODO 临时方法，需要提取出去
	public void writeDrugExcel(String eviFile, Atom[] atoms, Atom[] admissionDate, String filename) throws Exception {
		Hashtable<String, Atom> founds = new Hashtable<String, Atom>();
		FileOutputStream fOutputStream = new FileOutputStream(filename, true);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fOutputStream));
		bw.newLine();
		bw.write(eviFile);
		bw.newLine();
		for (int i = 0; i < atoms.length; i++) {
			founds.put(atoms[i].getParams().get(0) + "_" + atoms[i].getParams().get(2), atoms[i]);
			bw.write(atoms[i].getParams().get(1) + "\t" + atoms[i].getParams().get(2)
					 + "\t" + atoms[i].getParams().get(3) + atoms[i].getParams().get(4));
			int lastPos = Integer.parseInt(atoms[i].getParams().get(5).toString());
			String pinci = "";
			for (int j = lastPos+1; j < wordsArr.length; j++) {
				if (wordsArr[j].eWord.getNature().equals(WordNatures.VALUE) || wordsArr[j].eWord.getNature().equals(WordNatures.VALUEUNIT)) {
					pinci += wordsArr[j].eWord.getWord();
				} else {
					break;
				}
			}
			bw.write("\t" + pinci);
			bw.newLine();
		}
		
		String defaultDate = admissionDate.length > 0 ? admissionDate[0].getParams().get(1).toString() : sdf.format(new Date(getMinDate()));
		
		for (int i = 0; i < wordsArr.length; i++) {
			if (wordsArr[i].eWord.getNature().equals(WordNatures.DRUG)) {
				if (founds.get(i + "_" + wordsArr[i].eWord.getWord()) == null) {
					String pinci = "";
					String jiliang = "";
					int cnt = 0;
					for (int j = i+1; j < wordsArr.length; j++) {
						if (wordsArr[j].eWord.getNature().equals(WordNatures.VALUE) || wordsArr[j].eWord.getNature().equals(WordNatures.VALUEUNIT)) {
							if (cnt >= 2) {
								if (pinci.length() > 0 && wordsArr[j].eWord.getNature().equals(WordNatures.VALUE)) {
									break;
								}
								pinci += wordsArr[j].eWord.getWord();
							} else {
								jiliang += wordsArr[j].eWord.getWord();
							}
							cnt++;
						} else {
							break;
						}
					}
					bw.write(defaultDate + "\t" + wordsArr[i].eWord.getWord() + "\t" + jiliang+ "\t" + pinci);
					bw.newLine();
				}
			}
		}
		
		bw.flush();
		fOutputStream.close();
	}
	
	public boolean isNoWordSentence(String word, String nature, int sentencePos){
		if (word == null && nature == null) {
			throw new RuntimeException("isNoWordBetween谓词中word和nature必有一个不为null且存在!");
		}
		boolean hasWord = false;
		if (word != null) {
			hasWord |= sentenceQueryByWord.get(sentencePos).containsKey(word);
		}
		if (nature != null) {
			hasWord |= sentenceQueryByNature.get(sentencePos).containsKey(nature);
		}
		return !hasWord;
	}
	
	/**
	 * 输出时如果有未标记时间的，用最小时间, 通常是指没有入院日期，也没有参照日期的病历内容
	 * @return
	 */
	public long getMinDate(){
		long min = 0;
		for (int i = 0; i < wordsArr.length; i++) {
			WordPos wordPos = wordsArr[i];
			if (wordPos.eWord.isDate()) {
				if (min == 0){
					if(wordPos.eWord.getTimestamp() > 0) {
						min = wordPos.eWord.getTimestamp();
					}
				} else {
					if(wordPos.eWord.getTimestamp() > 0) {
						min = Math.min(min, wordPos.eWord.getTimestamp());
					}
				}
			}
		}
		return min;
	}
	
	/**
	 * 输出时如果有未标记时间的，用最大时间, 通常是指没有入院日期，也没有参照日期的病历内容
	 * @return
	 */
	public long getMaxDate(){
		long max = 0;
		for (int i = 0; i < wordsArr.length; i++) {
			WordPos wordPos = wordsArr[i];
			if (wordPos.eWord.isDate()) {
				if (max == 0){
					if(wordPos.eWord.getTimestamp() > 0) {
						max = wordPos.eWord.getTimestamp();
					}
				} else {
					if(wordPos.eWord.getTimestamp() > 0) {
						max = Math.max(max, wordPos.eWord.getTimestamp());
					}
				}
			}
		}
		return max;
	}
	
	public boolean onlyWordBetween(String word, String nature, int startPos, int endPos){
		if (word == null && nature == null) {
			throw new RuntimeException("isNoWordBetween谓词中word和nature必有一个不为null且存在!");
		}
		boolean hasWordNotEquals = false;
		boolean mustExistsOne = false;
		for(int i=startPos+1; i<endPos; i++){
			boolean equals = true;
			if (word != null) {
				if (!word.equals(wordsArr[i].eWord.getWord())) {
					equals = false;
				}
			}
			if (nature != null) {
				if (!nature.equals(wordsArr[i].eWord.getNature())) {
					equals = false;
				}
			}
			if (!equals) {
				//发现一个不是，证明不是只有
				hasWordNotEquals = true;
				break;
			} else {
				mustExistsOne = true;
			}
		}
		return !hasWordNotEquals && mustExistsOne;
	}
	
	public boolean hasFeature(String feature, int...poses){
		if (poses.length == 0) {
			throw new RuntimeException("HasFeature至少需要一个位置信息！");
		}
		boolean allHave = true;
		for (int i = 0; i < poses.length; i++) {
			if (!wordsArr[poses[i]].eWord.hasFeature(feature)) {
				allHave = false;
				break;
			}
		}
		return allHave;
	}
	
	/**
	 * 查找文本内容
	 * @param posStart
	 * @param posEnd
	 * @param includeStart
	 * @param includeEnd
	 * @return
	 */
	public String findText(int posStart, int posEnd, boolean includeStart, boolean includeEnd){
		String text = "";
		int sentencePos = -1;
		for (int i = (includeStart ? posStart : posStart + 1); i < wordsArr.length && (includeEnd ? i <= posEnd : i < posEnd); i++) {
			if (sentencePos == -1) {
				sentencePos = wordsArr[i].sentencePos;
			} else if (sentencePos != wordsArr[i].sentencePos) {
				break;
			} else if (wordsArr[i].eWord.isSentenceEnd()) {
				break;
			}
			text += wordsArr[i].eWord.getWord();
		}
		return text;
	}
	
	public boolean dateAfter(int pos1, int pos2){
		WordPos wordPos1 = wordsArr[pos1];
		WordPos wordPos2 = wordsArr[pos2];
		if (!wordPos1.eWord.isDate() || !wordPos2.eWord.isDate()) {
			throw new RuntimeException("dateAfter比较的两个元素必须都是date: " + wordPos1.eWord.getWord() + ", " + wordPos2.eWord.getWord());
		}
		return wordPos1.eWord.getTimestamp() < wordPos2.eWord.getTimestamp();
	}
	
	public boolean isNoWordBetween(String word, String nature, int startPos, int endPos){
		if (word == null && nature == null) {
			throw new RuntimeException("isNoWordBetween谓词中word和nature必有一个不为null且存在!");
		}
		boolean hasWord = false;
		for(int i=startPos+1; i<endPos; i++){
			boolean equals = true;
			if (word != null) {
				if (!word.equals(wordsArr[i].eWord.getWord())) {
					equals = false;
				}
			}
			if (nature != null) {
				if (!nature.equals(wordsArr[i].eWord.getNature())) {
					equals = false;
				}
			}
			if (equals) {
				//发现一个，证明不是一个都没有
				hasWord = true;
				break;
			}
		}
		return !hasWord;
	}
	
	public boolean hasAttr(String pos, String attrKey){
		int intPos = Integer.parseInt(pos);
		if (intPos < 0 || intPos >= wordsArr.length) {
			throw new RuntimeException("Index out of bounds, pos: " + pos + ", attribute: " + attrKey);
		}
		if (wordsArr[intPos] == null) {
			//TODO warning, 这里应该必须有内容
			System.out.println("Warning: 这里应该必须有内容, pos: " + pos + ", attribute: " + attrKey);
			return false;
		}
		return wordsArr[intPos].eWord.getAttributes().containsKey(attrKey);
	}
	
	/**
	 * 有属性key，并属性值相同
	 * @param pos
	 * @param attrKey
	 * @param attrValue
	 * @return
	 */
	public boolean equalsAttr(String pos, String attrKey, String attrValue){
		int intPos = Integer.parseInt(pos);
		if (intPos < 0 || intPos >= wordsArr.length) {
			throw new RuntimeException("Index out of bounds, pos: " + pos + ", attribute: " + attrKey);
		}
		if (wordsArr[intPos] == null) {
			//TODO warning, 这里应该必须有内容
			System.out.println("Warning: 这里应该必须有内容, pos: " + pos + ", attribute: " + attrKey);
			return false;
		}
		return attrValue.equals(wordsArr[intPos].eWord.getAttribute(attrKey));
	}
	
}
