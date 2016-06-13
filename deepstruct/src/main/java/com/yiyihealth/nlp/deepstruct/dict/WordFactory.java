package com.yiyihealth.nlp.deepstruct.dict;

public class WordFactory {

	public static Word createWord(String text, WordMeaning... meaning){
		Word word = new Word(text);
		for (int i = 0; i < meaning.length; i++) {
			word.addMeaning(meaning[i]);
			meaning[i].setWord(word);
		}
		return word;
	}
	
	public static Word createWordWithNatures(String text, String...natures){
		Word word = new Word(text);
		for (int i = 0; i < natures.length; i++) {
			WordMeaning meaning = new WordMeaning();
			meaning.setWordNature(WordNature.getWordNature(natures[i]));
			word.addMeaning(meaning);
			meaning.setWord(word);
		}
		return word;
	}
	
//	WordMeaning meaning = new WordMeaning();
//	word.addMeaning(meaning);
	
}
