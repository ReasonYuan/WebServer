package com.yiyihealth.nlp.deepstruct.dict;

import java.io.Serializable;
import java.util.ArrayList;

public class Word implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 864541326882135080L;

	private int freq = 10;
	
	private String text;
	
	/**
	 * 用分词工具得到的词性
	 */
	private String orgNature;
	
	private ArrayList<WordMeaning> meanings = new ArrayList<WordMeaning>();
	
	protected Word(String text){
		this.text = text;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public void addMeaning(WordMeaning meaning){
		meanings.add(meaning);
	}
	
	public void loadMeanings(){
		
	}
	
	public boolean containNature(String nature){
		for (int i = 0; i < meanings.size(); i++) {
			if (meanings.get(i).getWordNature().name.equals(nature)) {
				return true;
			}
		}
		return false;
	}

	public ArrayList<WordMeaning> getMeanings() {
		return meanings;
	}
	
	public void copyNatures(ArrayList<String> natures){
		for(WordMeaning meaning : meanings){
			if (!natures.contains(meaning.getWordNature())) {
				natures.add(meaning.getWordNature().getName());
			}
		}
	}

	public void setMeanings(ArrayList<WordMeaning> meanings) {
		this.meanings = meanings;
	}

	public String getText() {
		return text;
	}

	public int getFreq() {
		return freq;
	}

	public void setFreq(int freq) {
		this.freq = freq;
	}

	public String getOrgNature() {
		return orgNature;
	}

	public void setOrgNature(String orgNature) {
		this.orgNature = orgNature;
	}

}
