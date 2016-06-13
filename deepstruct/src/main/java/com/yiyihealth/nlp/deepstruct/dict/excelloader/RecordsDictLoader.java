package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.WordMeaning;
import com.yiyihealth.nlp.deepstruct.dict.WordNature;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.WordsLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.CommonExcelDictLoader.RowParser;

public class RecordsDictLoader implements WordsLoader {

	@Override
	public ArrayList<Word> loadDict() {
		CommonExcelDictLoader loader = new CommonExcelDictLoader();
		ArrayList<Word> resutls = loader.loadDict("dict/风湿科1-6069.xlsx", 1, 3, new RowParser() {
			public Word parseRow(String[] rowValues) {
				if (rowValues[0] != null && rowValues[0].trim().length() > 0) {
					String text = rowValues[0].trim();
					if (rowValues[2] != null && rowValues[2].trim().length() > 0) {
						WordMeaning meaning = new WordMeaning();
						meaning.setWordNature(WordNature.getWordNatureByReadableName(rowValues[2].trim()));
						Word word = WordFactory.createWord(text, meaning);
						if (rowValues[1] != null) {
							String synTxt = rowValues[2].trim(); 
							meaning.setTextNearSynonyms(synTxt);
						}
						return word;
					}
				}
				return null;
			}
		});
		return resutls;
	}

}
