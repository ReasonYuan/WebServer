package com.yiyihealth.ai.dsmain.medicine.wx;

import com.alibaba.fastjson.JSONObject;

public class UnRecObject {
	
	/**
	 * 词
	 */
	private String word = "";
	
	/**
	 * 现有的词性
	 */
	private String nature = "";
	
	/**
	 * 上下文
	 */
	private String longContent = "";
	
	/**
	 * 词的位置
	 */
	private String wordPos = "";

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getNature() {
		if (nature.equals("Unrec")) {
			return "未识别";
		}
		
		if (nature.equals("Undef")) {
			return "未定义";
		}
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public String getLongContent() {
		return longContent;
	}

	public void setLongContent(String longContent) {
		this.longContent = longContent;
	}

	public String getWordPos() {
		return wordPos;
	}

	public void setWordPos(String wordPos) {
		this.wordPos = wordPos;
	}
	
	public JSONObject toJsonObject(){
		JSONObject object = new JSONObject();
		object.put(UnDefAndUnRecTittles.word, word);
		object.put(UnDefAndUnRecTittles.nature, getNature());
		object.put(UnDefAndUnRecTittles.longContext, longContent);
		object.put(UnDefAndUnRecTittles.wordPos, wordPos);
		object.put(UnDefAndUnRecTittles.suggestion, "");
		return object;
	}
}
