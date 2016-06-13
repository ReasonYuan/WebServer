package com.yiyihealth.nlp.deepstruct.dict;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 某个词的词义, 根据上下文决定
 * @author qiangpeng
 *
 */
public class WordMeaning implements Cloneable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2300886418088567731L;

	public static final int DIR_LEFT = -1, DIR_NONE = 0, DIR_RIGHT = 1, DIR_BOTH = 2;
	
	private Word word;
	
	/**
	 * 医疗系统归纳的词性
	 */
	private WordNature wordNature;
	
	/**
	 * 同义词的字符串, 多个同义词用","隔开
	 */
	private String textNearSynonyms = null;
	
	/**
	 * 近义词
	 */
	private ArrayList<WordMeaning> nearSynonyms = new ArrayList<WordMeaning>();
	
	/**
	 * 对于灵活多变型词，根据类别自定义解析格式
	 */
	private String parseFormat;
	
	/**
	 * 样本, 语句或测试词
	 */
	private String sample;
	
	public WordMeaning() {
	}
	
	public String getSample() {
		return sample;
	}

	public void setSample(String sample) {
		this.sample = sample;
	}
	
	public void setWord(Word word){
		this.word = word;
	}

	public WordNature getWordNature() {
		return wordNature;
	}

	public void setWordNature(WordNature wordClassM) {
		this.wordNature = wordClassM;
	}

	public ArrayList<WordMeaning> getNearSynonyms() {
		return nearSynonyms;
	}

	public void setNearSynonyms(ArrayList<WordMeaning> nearSynonyms) {
		this.nearSynonyms = nearSynonyms;
	}

	public Word getWord() {
		return word;
	}

	public String getTextNearSynonyms() {
		return textNearSynonyms == null ? "" : textNearSynonyms;
	}

	public void setTextNearSynonyms(String textNearSynonyms) {
		this.textNearSynonyms = textNearSynonyms;
	}
	
	public String getParseFormat() {
		return parseFormat;
	}

	public void setParseFormat(String parseFormat) {
		this.parseFormat = parseFormat;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
