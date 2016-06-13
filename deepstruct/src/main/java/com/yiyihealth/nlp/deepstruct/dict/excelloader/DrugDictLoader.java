package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordNature;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.WordMeaning;
import com.yiyihealth.nlp.deepstruct.dict.WordsLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.CommonExcelDictLoader.RowParser;

public class DrugDictLoader implements WordsLoader {

	public ArrayList<Word> loadDict() {
		CommonExcelDictLoader loader = new CommonExcelDictLoader();
		ArrayList<Word> resutls = loader.loadDict("dict/drug.xlsx", 1, 3, new RowParser() {
			public Word parseRow(String[] rowValues) {
				if (rowValues[1] != null) {
					String text = rowValues[1].trim();
					WordMeaning meaning = new WordMeaning();
					meaning.setWordNature(WordNature.getWordNature(WordNatures.DRUG));
					Word word = WordFactory.createWord(text, meaning);
					if (rowValues[2] != null) {
						String synTxt = rowValues[2].trim(); 
						meaning.setTextNearSynonyms(synTxt);
					}
					return word;
				}
				return null;
			}
		});
		ArrayList<Word> resutls2 = loader.loadDict("dict/药品数据库大全.xlsx", 1, 4, new RowParser() {
			public Word parseRow(String[] rowValues) {
				if (rowValues[1] != null) {
					String text = rowValues[2].trim();
					WordMeaning meaning = new WordMeaning();
					meaning.setWordNature(WordNature.getWordNature(WordNatures.DRUG));
					Word word = WordFactory.createWord(text, meaning);
					if (rowValues[3] != null) {
						String synTxt = rowValues[3].trim(); 
						meaning.setTextNearSynonyms(synTxt);
					}
					return word;
				}
				return null;
			}
		});
		resutls.addAll(resutls2);
		return resutls;
	}

}
