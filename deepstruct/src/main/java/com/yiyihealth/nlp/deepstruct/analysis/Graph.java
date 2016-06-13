package com.yiyihealth.nlp.deepstruct.analysis;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.utils.Constants;

public class Graph implements Cloneable {

	private char[] chars;
	
	private Term[][] terms;
	
	/**
	 * 有些语句不是以。号结尾的，可能根本没有标点符号，所以需要分别记录起始和结束位置
	 */
	private int[] sentenceEnds = new int[32], sentenceStarts = new int[32];
	
	private int sentenceCounter = 0;
	
	private ArrayList<Sentence> sentences = new ArrayList<Sentence>();
	
	private String debugVerify = "";
	
	/**
	 * 把从字典理查到的词作为图处理
	 * tokens[n].length == chars.length
	 * @param chars
	 * @param terms
	 */
	public Graph(char[] chars, Term[][] terms) {
		this.chars = chars;
		this.terms = terms;
		debugVerify = new String(chars, 0, chars.length);
		//先用最简单的断句规则：。
		int start = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == Punctuation.FULLSTOP_C) {
				if (sentenceEnds.length == sentenceCounter) {
					int[] bufEnd = new int[sentenceEnds.length + 16];
					int[] bufStart = new int[sentenceStarts.length + 16];
					System.arraycopy(sentenceEnds, 0, bufEnd, 0, sentenceEnds.length);
					System.arraycopy(sentenceStarts, 0, bufStart, 0, sentenceStarts.length);
					sentenceEnds = bufEnd;
					sentenceStarts = bufStart;
				}
				sentenceEnds[sentenceCounter] = i;
				sentenceStarts[sentenceCounter++] = start;
				start = i+1;
			}
		}
		//TODO 处理最后一个不是句号的情况
//		for (int i = start; i < chars.length; i++) {
//			if (terms[row][i] != null) {
//				sentenceEnds[sentenceCounter] = chars.length - 1;
//				sentenceStarts[sentenceCounter++] = start;
//				break;
//			}
//		}
		for (int i = 0; i < sentenceCounter; i++) {
			Sentence sentence = new Sentence(this, sentenceStarts[i], sentenceEnds[i]);
			sentences.add(sentence);
			//sentence.rowLogicVerify();
			sentence.toSentence();
			//TODO 验证完整性, 不要删除下面的代码
//			String debugRestore = sentence.debugRestoreToString();
//			String debugNotMatchBeginAt = "";
//			if (debugVerify.indexOf(debugRestore) == -1) {
//				for (int j = 1; j <= debugRestore.length(); j++) {
//					if(debugVerify.indexOf(debugRestore.substring(0, j)) == -1){
//						debugNotMatchBeginAt = debugRestore.substring(0, j);
//						break;
//					}
//				}
//				//throw new RuntimeException("\n"+debugNotMatchBeginAt+"\n语句复原错误: " + debugRestore);
//			}
		}
	}
	
	public void debugWithNatures(){
		if (Constants.debugPrint) System.out.print("\n分词(带词性): ");
		for (int i = 0; i < sentences.size(); i++) {
			sentences.get(i).debugWithNatures();
		}
	}
	
	public void debugWithoutNatures(){
		if (Constants.debugPrint) System.out.print("分词: ");
		for (int i = 0; i < sentences.size(); i++) {
			sentences.get(i).debugWithoutNatures();
		}
	}

	public ArrayList<Sentence> getSentences() {
		return sentences;
	}
	
	public char[] getChars() {
		return chars;
	}

	public void setChars(char[] chars) {
		this.chars = chars;
	}

	public Term[][] getTerms() {
		return terms;
	}

	public void setTerms(Term[][] terms) {
		this.terms = terms;
	}
	
}
