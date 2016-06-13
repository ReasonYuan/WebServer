package com.yiyihealth.ai.dsmain.medicine.wx;

public class SearchWord implements Cloneable{

	
	private String searchWord = "";
	private int wordPos;
	public String getSearchWord() {
		return searchWord;
	}
	public void setSearchWord(String searchWord) {
		this.searchWord = searchWord;
	}
	public int getWordPos() {
		return wordPos;
	}
	public void setWordPos(int wordPos) {
		this.wordPos = wordPos;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
