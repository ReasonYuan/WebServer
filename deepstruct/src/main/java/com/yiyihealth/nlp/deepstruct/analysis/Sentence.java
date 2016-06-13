package com.yiyihealth.nlp.deepstruct.analysis;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.utils.Constants;

public class Sentence implements Cloneable {

	private Graph graph;

	private int start;

	private int end;
	
	private ArrayList<EWord> words = new ArrayList<EWord>(){
		public boolean contains(Object object){
			if (object instanceof String) {
				for(int i=0; i<size(); i++){
					if (get(i).getWord().equals(object)) {
						return true;
					}
				}
				return false;
			} else {
				return super.contains(object);
			}
		}
	};
	
	private static HealthAnalysis cutAnalysis = new HealthAnalysis();
	
	protected Sentence(Graph graph, int start, int end) {
		this.graph = graph;
		this.start = start;
		this.end = end;
	}

	public String debugRestoreToString(){
		String result = "";
		for (int i = 0; i < words.size(); i++) {
			result += words.get(i).getWord();
		}
		return result;
	}
	
	@Override
	public String toString() {
		return debugRestoreToString();
	}
	
	public boolean contains(String word){
		for (int i = 0; i < words.size(); i++) {
			if (words.get(i).getWord().equals(word)) {
				return true;
			}
		}
		return false;
	}
	
	public void toSentence() {
		if (words.size() > 0) {
			words.clear();
		}
		Term[][] terms = graph.getTerms();
		for (int i = start; i <= end;) {
			int maxLength = 0;
			Term maxLenTerm = null;
			for (int t = 0; t < terms.length; t++) {
				if (terms[t][i] != null) {
					if (terms[t][i].getWord().length() >= maxLength) {
						if (terms[t][i].getWord().length() == maxLength) {
							maxLenTerm.addCandidateNatures(terms[t][i].getCandidateNatures());
						} else {
							maxLength = terms[t][i].getWord().length();
							maxLenTerm = terms[t][i];
						}
					}
				}
			}
			if (maxLenTerm != null) {
				if (maxLenTerm.getCandidateNatures().size() > 1 && maxLenTerm.getCandidateNatures().contains(WordNatures.UNREC)) {
					maxLenTerm.getCandidateNatures().remove(WordNatures.UNREC);
					if (maxLenTerm.getNature().equals(WordNatures.UNREC)) {
						//任意先选取一个词性
						maxLenTerm.setNature(maxLenTerm.getCandidateNatures().get(0));
					}
				}
				words.add(maxLenTerm);
				//TODO 临时方案，跳过的部分的所有词都切割需要在这个词的末尾切割，比如下面"补体C3"和"31.080", 应该把"31.080"切割成"1.080"
				//"补体C31.080g/L,"
				int nextWordPos = i + maxLength;
				for(int j=i+1; j < nextWordPos; j++){
					for (int t = 0; t < terms.length; t++) {
						if (terms[t][j] != null && j + terms[t][j].getWord().length() > nextWordPos) {
							String cutStr = terms[t][j].getWord().substring(nextWordPos - j, terms[t][j].getWord().length());
							if (cutStr.length() > 0) {
								//被切掉的词默认为是unrec的，TODO 以后再识别一下
								Term ct = HealthAnalysis.parseCutStr(cutStr);
								terms[t][j].setWord(cutStr);
								terms[t][j].setNature(WordNatures.UNREC);
								terms[t][j].setTimeformat(null);
								terms[t][j].clearCandidateNatures();
								if (ct != null) {
									terms[t][j].setNature(ct.getNature());
									terms[t][j].addCandidateNatures(ct.getCandidateNatures());
								}
								terms[t][nextWordPos] = terms[t][j];
								terms[t][j] = null;
								int nextRecDistance = getNextRecWordDistance(terms, cutStr, nextWordPos);
								if (nextRecDistance != -1) {
									if (nextRecDistance == 0) {
										terms[t][nextWordPos] = null;
									} else {
										terms[t][nextWordPos].setWord(terms[t][nextWordPos].getWord().substring(0, nextRecDistance));
									}
								}
							}
						}
					}
				}
				//跳到下一个词
				i = i + maxLength;
			} else {
				i++;
			}
		}
//		System.out.print("句子div: ");
//		for (int i = 0; i < words.size(); i++) {
//			System.out.print(words.get(i).text + " | ");
//		}
//		//System.out.println("");
//		//System.out.print("句子ne: ");
//		for (int i = 0; i < words.size(); i++) {
//			System.out.print(words.get(i).text + "/" + words.get(i).nature + " | ");
//		}
		//System.out.println("");
		//System.out.print("句子nc: ");
	}
	
	/**
	 * 查找下一个被识别的词的距离
	 * @param terms
	 * @param startPos
	 * @param end
	 * @return
	 */
	private int getNextRecWordDistance(Term[][] terms, String cutStr, int startPos){
		for (int t = 0; t < terms.length; t++) {
			for (int i = startPos; i < terms[t].length && i < startPos + cutStr.length(); i++) {
				if (terms[t][i] != null && !terms[t][i].getNature().equals(WordNatures.UNREC)) {
					//有已识别的词
					return i - startPos;
				}
			}
		}
		return -1;
	}
	
	public void debugWithNatures(){
		for (int i = 0; i < words.size(); i++) {
			String cname = WordNatures.getNatureReadableName(words.get(i).getNature());
			if (Constants.debugPrint) System.out.print(words.get(i).getWord() + "/" + cname + " | ");
		}
	}
	
	public void debugWithoutNatures(){
		for (int i = 0; i < words.size(); i++) {
			if (Constants.debugPrint) System.out.print(words.get(i).getWord() + " | ");
		}
	}

	public ArrayList<EWord> getWords() {
		return words;
	}
	
//	/**
//	 * 首先进行单行验证
//	 */
//	public void rowLogicVerify() {
//		Term[][] terms = graph.getTerms();
//		boolean[][] invalides = new boolean[terms.length][terms[0].length];
//		// TODO 优化，优化，只验证数值
//		for (int t = 0; t < terms.length; t++) {
//			for (int i = start; i < end - 1; i++) {
//				if (terms[t][i] != null && terms[t][i + 1] != null && terms[t][i].nature == "_v"
//						&& terms[t][i + 1].nature == "_v") {
//					invalides[t][i] = true;
//					invalides[t][i + 1] = true;
//				}
//			}
//		}
//		for (int t = 0; t < terms.length; t++) {
//			for (int i = start; i < end; i++) {
//				if (invalides[t][i]) {
//					terms[t][i] = null;// 删除不正确的term
//				}
//			}
//		}
//	}

}
