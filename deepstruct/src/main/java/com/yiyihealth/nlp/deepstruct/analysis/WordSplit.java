package com.yiyihealth.nlp.deepstruct.analysis;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.csense.NBRec;
import com.yiyihealth.nlp.deepstruct.csense.NBRec.IllegalCSListener;
import com.yiyihealth.nlp.deepstruct.csense.NBRec.NBRecResult;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.TrieNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.NodeMatcher;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.NormalNodeMatcher;
import com.yiyihealth.nlp.deepstruct.utils.Constants;

public class WordSplit {
	
	private NodeMatcher[] matchers;
	
	private char[] chars;
	
	/**
	 * 标记每一个char的位置是否已有已被标记的词
	 */
	private boolean[] hasWords;
	
	private Term[][] terms;
	
	private final int UNRECONGNIZED_ROW = 1;
	
	private ArrayList<Sentence> sentences = new ArrayList<Sentence>();
	
	private static HealthAnalysis reparseAnalysis = new HealthAnalysis();
	
	/**
	 * 重新shen yu
	 */
	private static NormalNodeMatcher<CharNode> gooverMatcher = new NormalNodeMatcher<>(HealthAnalysis.getNormalForest());
	
	private NBRec nbRec;
	
	/**
	 * 临时为构建动态树查询已有节点
	 */
	//private Hashtable<String, PathNode> pathNodesMap = new Hashtable<String, PathNode>();
	
	protected WordSplit(String text, IllegalCSListener illegalCSListener, NodeMatcher... matchers) {
		this.matchers = matchers;
		char[] chars = new char[text.length() + 1];
    	text.getChars(0, text.length(), chars, 0);
    	chars[chars.length - 1] = '。';//以防没有句末
    	this.chars = chars;
    	terms = new Term[matchers.length+UNRECONGNIZED_ROW][chars.length];
    	hasWords = new boolean[chars.length];
    	
    	nbRec = new NBRec("../dsknowledgebase/low_level/辅助性质.txt");
    	nbRec.loadCommonSense("../dsknowledgebase/low_level/常识-简单键值.txt", "../dsknowledgebase/low_level/常识-可以.txt", "../dsknowledgebase/low_level/常识-行为.txt");
    	nbRec.loadRepeatSense("../dsknowledgebase/low_level/重复.txt");
    	nbRec.setIllegalCSListener(illegalCSListener);
	}
	
	public void split(){
		for (int i = 0; i < matchers.length; i++) {
			NodeMatcher matcher = matchers[i];
			for (int j = 0; j < chars.length; j++) {
				char c = chars[j];
				Term term = matcher.nextChar(c, j);
				if (term != null && term.isDefined) {
					terms[i][term.startPos] = term;
					for (int k = term.startPos; k < term.getWord().length() + term.startPos; k++) {
						if (!hasWords[k]) {
							hasWords[k] = true;
						}
					}
					//字符串回看，实际是上的位置在j的前面，所以需要回到实际位置重新开始
					if (j - term.startPos > term.getWord().length()) {
						int backoffset = j - term.startPos - term.getWord().length() + 1;
						j -= backoffset;
						matcher.reset();
					}
				}
			}
		}
		
		//debug
//		String[] types = {"普通", "日期", "数值", "数值单位"};
//		for (int i = 0; i < terms.length - UNRECONGNIZED_ROW; i++) {
//			System.out.print(types[i] + ": ");
//			for (int j = 0; j < terms[i].length; j++) {
//				if (terms[i][j] != null) {
//					System.out.print(terms[i][j].text + " | ");
//				}
//			}
//			System.out.println();
//		}
		//最后一个字符是辅助字符，不算
		int offset = hasWords.length == 0 ? 0 : (hasWords[0] ? -1 : 0);
		if(Constants.debugPrint) System.out.print("未识别: ");
		for (int i = 0; i < hasWords.length - 1; i++) {
			if(!hasWords[i] && offset == -1){
				offset = i;
			}
			if ((hasWords[i] || i == hasWords.length - 2) && offset != -1){
				// 未识别的字符结束
				int len = i - offset + (i == hasWords.length - 2 ? 1 : 0);
				if(chars[offset + len - 1] == Punctuation.FULLSTOP_C && len > 1){
					len -= 1;
					terms[0][offset + len - 1] = new Term(""+Punctuation.FULLSTOP_C, WordNatures.PUNC, true, offset + len - 1);
				}
				String unrecongnized = new String(chars, offset, len);
				Term term = new Term(unrecongnized, WordNatures.UNREC, true, offset);
				terms[terms.length - 1][offset] = term;
				if(Constants.debugPrint) System.out.print(unrecongnized + " | ");
				offset = -1;
			}
		}
		Graph graph = new Graph(chars, terms);
		graph.debugWithoutNatures();
		graph.debugWithNatures();
		sentences = graph.getSentences();
		//System.out.println("\n=================");//if(Constants.debugPrint) 
	}
	
	public void predicateNatures(String logTag){
		for(Sentence sentence : sentences){
			ArrayList<EWord> words = sentence.getWords();
			int startIndex = 0;
			boolean rearranged = true;
			while (rearranged) {
				rearranged = false;
				for (int i = startIndex; i < words.size(); i++) {
					Term word = (Term) words.get(i);
					//TODO 目前由于时间关系，先用笨办法仅验证诊断是否需要拆分成部位和症状的问题，此解决方案不具有普遍性
					if (word.getCandidateNatures().contains(WordNatures.DIAG)) {
						//临时假设改词具有部位的词性，根据常识推断出是否真的可以为症状，如果可以则尝试拆分成部位和症状
						ArrayList<String> assumeNatures = new ArrayList<>();
						assumeNatures.add(WordNatures.SYMPTOM);
						assumeNatures.add(WordNatures.REGION);
						word.addCandidateNatures(assumeNatures);
						NBRecResult nRecResult = nbRec.recNature(word, i, words, logTag);
						String naturePicked = nRecResult.naturePicked;
						//不能区分或者可以被区分成假设的词性时都认为需要分成部位＋症状
						if (naturePicked == null || naturePicked != null && assumeNatures.contains(naturePicked)) {
							//的确很可能是部位
							//尝试拆分
							Term[] terms = gooverRegionSymptomWords(word.getWord(), word.startPos);
							if (terms != null) {
								//把拆分后的词加入，删除原来的长词
								words.remove(i);
								for (int j = 0; j < terms.length; j++) {
									words.add(j+i, terms[j]);
								}
								startIndex = i+terms.length;
								rearranged = true;
								break;
							} else {
								//TODO 输出到提示，让字典管理人员查看该词然后调整字典
								
							}
						}
						for(String nature : assumeNatures){
							word.removeCandidateNature(nature);
						}
					}
					
					if (word.getWord().equals("98年")) {
						System.out.println("ddddd");
					}
					if (word.getCandidateNatures().size() > 1) {
						//拿这个词和句子到常识里去判断，如果能选出唯一词性，则通过，否则向系统报告矛盾，需要人为干预!
						//人为干预即学习过程，让人告诉系统增加或调整新的常识
						
//						System.out.println("多词性判断: " + word.getCandidateNatures() + ", word: " + word);
						
						String naturePicked = nbRec.recNature(word, i, words, logTag).naturePicked;
						if (naturePicked == null) {
							//TODO 先不管，以后处理不符合常识的东西
						} else {
							word.setNature(naturePicked);
						}
					}
				}
			}
		}
	}
	
	public ArrayList<Sentence> getSentences() {
		return sentences;
	}
	
	/**
	 * 重新看一次症状/诊断词, 由于部分诊断是由部位和症状合成的，所以在不该出现诊断地方出现的部位和症状都是部位＋症状
	 * @param word
	 * @return
	 */
	private Term[] gooverRegionSymptomWords(String word, int posStart) {
		gooverMatcher.reset();
		//TODO 重新写算法，向前查找最长部位, 找到了就
		char[] chars = word.toCharArray();
		
		Term maxRegionTerm = null;
		Term symptomTerm = null;
		for (int i = 0; i < chars.length; i++) {
			gooverMatcher.reset();
			for (int j = 0; j <= i + 1; j++) {
				Term term = null;
				if (j <= i) {
					term = gooverMatcher.nextChar(chars[j], j);
				} else {
					term = gooverMatcher.nextChar(Punctuation.FULLSTOP_C, j);
				}
				if (j == i+ 1 && term != null) {
					if (term.getCandidateNatures().contains(WordNatures.REGION)) {
						if (maxRegionTerm == null) {
							maxRegionTerm = term;
						} else {
							if (term.getWord().length() > maxRegionTerm.getWord().length()) {
								maxRegionTerm = term;
							}
						}
					}
				}
			}
		}
		if (maxRegionTerm != null) {
			gooverMatcher.reset();
			String rightStr = word.substring(maxRegionTerm.getWord().length());
			TrieNode node = gooverMatcher.getRootNode().getNode(rightStr);
			if (node != null && node.isWordEnd() && node.getDictWord().containNature(WordNatures.SYMPTOM)) {
				Term symptomWord = new Term(rightStr, WordNatures.SYMPTOM, true, 0);
				symptomWord.addCandidateNature(WordNatures.SYMPTOM);
				symptomTerm = symptomWord;
			}
		}
		
		if (maxRegionTerm != null && symptomTerm != null) {
			maxRegionTerm.startPos = posStart;
			symptomTerm.startPos = posStart + maxRegionTerm.getWord().length();
			Term[] resutls = {maxRegionTerm, symptomTerm};
			return resutls;
		}
		return null;
	}
	
	//原型版不支持异议切割，只线性切割
	public void nextWord(char c, int pos){
		//1. 分别value和date识别，谁长以谁为最终单词，如果一样长，兼具2种词性
		//2. 如果单词在普通词典里能找到，在普通词性到token
		//3. 再重新回到普通单词识别
		
		//另外，用提问方式，哪一个matcher匹配得长，就用谁的，如果一样则都用
		
		
//		for (int i = 0; i < tmpTokens.length; i++) {
//			tmpTokens[i] = null;
//		}
//		for (int i = 0; i < matchers.length; i++) {
//			NodeMatcher matcher = matchers[i];
//			Token token = matcher.nextChar(c, pos);
//			if (token != null) {
//				
//			}
//		}
//		
//		//PK
//		int wordNum = 0;
//		for (int i = 0; i < tmpTokens.length; i++) {
//			if (tmpTokens[i] != null && tmpTokens[i].isDefined) {
//				wordNum++;
//			}
//		}
//		
	}
	
}
