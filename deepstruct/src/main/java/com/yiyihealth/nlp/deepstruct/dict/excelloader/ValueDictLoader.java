package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.WordMeaning;
import com.yiyihealth.nlp.deepstruct.dict.WordNature;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.WordsLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.CommonExcelDictLoader.RowParser;

public class ValueDictLoader implements WordsLoader {

	@Override
	public ArrayList<Word> loadDict() {
		CommonExcelDictLoader loader = new CommonExcelDictLoader();
		ArrayList<Word> resutls = loader.loadDict("dict/values.xlsx", 1, 4, new RowParser() {
			public Word parseRow(String[] rowValues) {
				if (rowValues[0] != null) {
					String text = rowValues[0].trim();
					WordMeaning meaning = new WordMeaning();
					
					//TODO 处理购买量，先转换成undef, 暂不做处理
					if (rowValues[2] != null && rowValues[2].trim().equals("购买量")) {
						meaning.setWordNature(WordNature.getWordNature(WordNatures.UNDEF));
					} else {
						meaning.setWordNature(WordNature.getWordNature(WordNatures.VALUE));
					}
					
					Word word = WordFactory.createWord(text, meaning);
					if (rowValues[2] != null) {
						String sample = rowValues[2].trim(); 
						meaning.setSample(sample);
					}
					return word;
				}
				return null;
			}
		});
		return resutls;
	}

}
