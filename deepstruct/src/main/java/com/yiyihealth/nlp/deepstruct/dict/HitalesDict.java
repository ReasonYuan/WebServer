package com.yiyihealth.nlp.deepstruct.dict;

import java.util.ArrayList;
import java.util.Hashtable;

import com.yiyihealth.nlp.deepstruct.dict.excelloader.DateDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.DrugDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.UnitDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.UserDefineExcelLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.ValueDictLoader;
import com.yiyihealth.nlp.deepstruct.utils.ExcelByNatureWriter;

public class HitalesDict {

	private ArrayList<Word> normalWords;
	
	private ArrayList<Word> dateWords;
	
	private ArrayList<Word> valueWords;
	
	private ArrayList<Word> unitWords;
	
	private static HitalesDict dict = null;

	private HitalesDict() {
	}

	public static HitalesDict getHealthCareDict() {
		if (dict == null) {
			dict = new HitalesDict();
			dict.loadDict();
		}
		return dict;
	}
	
	private void loadDict(){
		//普通词汇
		normalWords = loadNormalDict();
		checkSynonyms();
//		try {
//			normalWords.sort(new Comparator<Word>() {
//				public int compare(Word w1, Word w2) {
//					//仅按第一个词性分类
//					WordMeaning meaning1 = w1.getMeanings().get(0);
//					WordMeaning meaning2 = w2.getMeanings().get(0);
//					System.out.println(meaning1.getWordNature().getName() + "=========" + meaning2.getWordNature().getName());
//					return meaning1.getWordNature().getName().compareTo(meaning2.getWordNature().getName());
//				}
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		
		//日期词汇
		dateWords = new DateDictLoader().loadDict();
		
		//数值词汇
		valueWords = new ValueDictLoader().loadDict();
		
		//数值单位词汇
		unitWords = new UnitDictLoader().loadDict();
		
		//输出, TODO 需要重新规划输出内容
		//format2ExcelDict();
	}
	
	public ArrayList<Word> getNormalWords() {
		return normalWords;
	}

	public ArrayList<Word> getDateWords() {
		return dateWords;
	}

	public ArrayList<Word> getValueWords() {
		return valueWords;
	}

	public ArrayList<Word> getUnitWords() {
		return unitWords;
	}

	public void setUnitWords(ArrayList<Word> unitWords) {
		this.unitWords = unitWords;
	}

	private void checkSynonyms(){
		Hashtable<String, Word> checks = new Hashtable<String, Word>();
		for (int i = 0; i < normalWords.size(); i++) {
			checks.put(normalWords.get(i).getText(), normalWords.get(i));
		}
		ArrayList<Word> newWords = new ArrayList<Word>();
		for (int i = 0; i < normalWords.size(); i++) {
			ArrayList<WordMeaning> meanings = normalWords.get(i).getMeanings();
			for (int j = 0; j < meanings.size(); j++) {
				WordMeaning meaning = meanings.get(j);
				String textNearSynonyms = meaning.getTextNearSynonyms().trim();
				String[] tokens = textNearSynonyms.split("[、|,|;| |，]");
				if (tokens.length > 0) {
					for (int k = 0; k < tokens.length; k++) {
						if (tokens[k].length() > 0) {
							try {
								WordMeaning meaningSyn = (WordMeaning) meaning.clone();
								Word synWord = WordFactory.createWord(tokens[k], meaningSyn);
								newWords.add(synWord);
								meaning.getNearSynonyms().add(meaningSyn);
								meaningSyn.getNearSynonyms().add(meaning);
							} catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		normalWords.addAll(newWords);
		//所有同义词互为同义词
		//TODO 所有同义词互为同义词?
//		for (int i = 0; i < words.size(); i++) {
//			ArrayList<WordMeaning> meanings = words.get(i).getMeanings();
//			for (int j = 0; j < meanings.size(); j++) {
//				WordMeaning meaning = meanings.get(j);
//				//所有和我间接同义的也同义
//				
//			}
//		}
	}
	
	public void format2ExcelDict(){
		ExcelByNatureWriter.writeNatureWords(normalWords);
	}
	
	//private==
	public static ArrayList<Word> loadNormalDict(){
		ArrayList<Word> results = new ArrayList<Word>();
		
		//标点符号
		String punctuation = Punctuation.getPunctuation();
		for (int i = 0; i < punctuation.length(); i++) {
			String ch = punctuation.charAt(i) + "";
			WordMeaning meaning = new WordMeaning();
			meaning.setWordNature(WordNature.getWordNatureByReadableName("标点符号"));
			Word word = WordFactory.createWord(ch, meaning);
			results.add(word);
		}
		
		//最初定义的草稿词典
		ArrayList<Word> wordsMyDef = new UserDefineExcelLoader().loadDict();
		if (wordsMyDef != null) {
			results.addAll(wordsMyDef);
		}
		
		//药品词典
		ArrayList<Word> wordsDrugs = new DrugDictLoader().loadDict();
		if (wordsDrugs != null) {
			results.addAll(wordsDrugs);
		}
		
		//风湿科的词典
//		ArrayList<Word> wordsFromRecords = new RecordsDictLoader().loadDict();
//		if (wordsFromRecords != null) {
//			results.addAll(wordsFromRecords);
//		}
		
		return results;
	}
	
}
