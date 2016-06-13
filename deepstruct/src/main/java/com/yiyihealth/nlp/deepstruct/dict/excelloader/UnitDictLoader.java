package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.WordMeaning;
import com.yiyihealth.nlp.deepstruct.dict.WordNature;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.WordsLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.CommonExcelDictLoader.RowParser;

public class UnitDictLoader implements WordsLoader {

	@Override
	public ArrayList<Word> loadDict() {
		CommonExcelDictLoader loader = new CommonExcelDictLoader();
		ArrayList<Word> resutls = loader.loadDict("dict/valueunits.xlsx", 1, 4, new RowParser() {
			public Word parseRow(String[] rowValues) {
				if (rowValues[0] != null) {
					String text = rowValues[0].trim();
					WordMeaning meaning = new WordMeaning();
					meaning.setWordNature(WordNature.getWordNature(WordNatures.getNatureByReadableName(rowValues[2])));
					Word word = WordFactory.createWord(text, meaning);
					if (rowValues[3] != null) {
						String sample = rowValues[3].trim(); 
						meaning.setSample(sample);
					}
					if (rowValues[1] != null) {
						String synTxt = rowValues[1].trim(); 
						meaning.setTextNearSynonyms(synTxt);
					}
					return word;
				}
				return null;
			}
		});
		return resutls;
	}

}
