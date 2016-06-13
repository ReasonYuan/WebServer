package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class NewWordDict {
	
	private static NewWordDict _instance;
	
	private static ArrayList<NewWord> newWords = new ArrayList<>();

	public static NewWordDict getInstance(){
		if (_instance == null) {
			_instance = new NewWordDict();
		}
		return _instance;
	}
	
	public void addNewWord(String word, String nature, String tag){
		if (nature.equals(WordNatures.DATE) || nature.equals(WordNatures.VALUE)) {
			//日期，数值不作为单词
			return;
		}
		newWords.add(new NewWord(word, nature, tag));
	}
	
	public void debugPrintAll(){
		int cnt = 0;
		for(NewWord newWord : newWords){
			System.out.println(cnt++ + ": " + newWord.word + ", " + WordNatures.getNatureReadableName(newWord.nature) + ", tag: " + newWord.tag);
		}
	}
	
	private class NewWord {
		String word;
		String nature;
		String tag;

		public NewWord(String word, String nature, String tag) {
			this.word = word;
			this.nature = nature;
			this.tag = tag;
		}
	}
	
}
