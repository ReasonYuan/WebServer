package com.yiyihealth.nlp.deepstruct.dict;

import java.util.ArrayList;

public class SentenceTmp {
	
	private ArrayList<WordInContext> words = new ArrayList<WordInContext>();

	private SentenceTmp previous;
	
	private SentenceTmp next;
	
	/**
	 * 根据上下文选择最适合的词义
	 */
	public void pickWordMeaning(){
		
	}
	
	public void addWord(Word word){
		WordInContext wordInContext = new WordInContext(word);
		if (words.size() > 0) {
			WordInContext pre = words.get(words.size()-1);
			wordInContext.previous = pre;
			pre.next = wordInContext;
		}
		words.add(wordInContext);
	}
	
}

class WordInContext {
	public WordInContext(Word word) {
		this.word = word;
	}
	Word word;
	WordMeaning meaning;
	WordInContext previous;
	WordInContext next;
}
