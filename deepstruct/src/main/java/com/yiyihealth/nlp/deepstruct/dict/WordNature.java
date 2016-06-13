package com.yiyihealth.nlp.deepstruct.dict;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class WordNature implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static int NATURE = 1, MEDICALCARE = 2, DETAIL_MEDICALCARE = 3;
	
	protected String name;

	protected int level = 0;
	
	//private ArrayList<WordClass> subclasses = new ArrayList<WordClass>();
	
	private static ConcurrentHashMap<String, WordNature> wordNatures = new ConcurrentHashMap<String, WordNature>();
	
	public static WordNature getWordNature(String className){
		WordNature nature = wordNatures.get(className);
		if (nature != null) {
			return nature;
		} else {
			nature = new WordNature(className);
			wordNatures.put(className, nature);
			return nature;
		}
	}
	
	public static WordNature getWordNatureByReadableName(String readbleName){
		return getWordNature(WordNatures.getNatureByReadableName(readbleName));
	}
	
	private WordNature(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

//	public ArrayList<WordClass> getSubclasses() {
//		return subclasses;
//	}
//
//	public void setSubclasses(ArrayList<WordClass> subclasses) {
//		this.subclasses = subclasses;
//	}
	
}
