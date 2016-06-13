package com.yiyihealth.nlp.deepstruct.dict;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.Sentence;

/**
 * 一份完整的病历
 * @author qiangpeng
 *
 */
public class Paper {

	private ArrayList<Term> words = new ArrayList<>();
	
	private int charSize = 0;
	
	public Paper() {

	}
	
	public ArrayList<Term> getWords() {
		return words;
	}
	
	public void addSection(String sectionName, ArrayList<Sentence> sentences){
		words.add(new Term(sectionName, WordNatures.HEADING, true, charSize));
		charSize += sectionName.length();
		for (int i = 0; i < sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			ArrayList<EWord> orgWords = sentence.getWords();
			for (int j = 0; j < orgWords.size(); j++) {
				Term word = (Term) ((Term)orgWords.get(j)).clone();
				this.words.add(word);
				word.startPos = charSize;
				charSize += word.getWord().length();
			}
		}
		if (!words.get(words.size() - 1).getWord().equals(Punctuation.FULLSTOP_C)) {
			words.add(new Term(Punctuation.FULLSTOP_C+"", WordNatures.PUNC, true, charSize));
			charSize += 1;
		}
	}
	
}

