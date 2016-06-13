package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.WordMeaning;
import com.yiyihealth.nlp.deepstruct.dict.WordNature;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.WordsLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.CommonExcelDictLoader.RowParser;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.DateOrPreriodGenerator.DPItem;

public class DateDictLoader implements WordsLoader {

	@Override
	public ArrayList<Word> loadDict() {
		CommonExcelDictLoader loader = new CommonExcelDictLoader();
		ArrayList<Word> resutls = loader.loadDict("dict/dates.xlsx", 1, 5, new RowParser() {
			public Word parseRow(String[] rowValues) {
				if (rowValues[0] != null) {
					String text = rowValues[0].trim();
					WordMeaning meaning = new WordMeaning();
					String nature = rowValues[3] == null ? WordNatures.DATE
							: (rowValues[3].trim().equals("时间长度") ? WordNatures.PERIOD : WordNatures.DATE);
					meaning.setWordNature(WordNature.getWordNature(nature));
					Word word = WordFactory.createWord(text, meaning);
					if (rowValues[1] != null) {
						String fromat = rowValues[1].trim();
						meaning.setParseFormat(fromat);
					}
					if (rowValues[2] != null) {
						String sample = rowValues[2].trim();
						meaning.setSample(sample);
					}
					return word;
				}
				return null;
			}
		});
		
		//增加对分秒的支持
		ArrayList<Word> originAllWords = new ArrayList<>(resutls);
		for(Word word : originAllWords){
			ArrayList<String>[] msSupports = getMSSupports(word.getText(), word.getMeanings().get(0).getParseFormat());
			if (msSupports != null) {
				for (int i = 0; i < msSupports[0].size(); i++) {
					String pattern = msSupports[0].get(i);
					String format = msSupports[1].get(i);
					WordMeaning meaning = new WordMeaning();
					meaning.setWordNature(WordNature.getWordNature(WordNatures.DATE));
					Word dword = WordFactory.createWord(pattern, meaning);
					meaning.setParseFormat(format);
					resutls.add(dword);
				}
			}
		}
		
		ArrayList<DPItem> dpItems = DateOrPreriodGenerator.generateDatesAndPeriods();
		for (int i = 0; i < dpItems.size(); i++) {
			DPItem item = dpItems.get(i);
			String text = item.text;
			WordMeaning meaning = new WordMeaning();
			String nature = item.isDate ? WordNatures.DATE : WordNatures.PERIOD;
			meaning.setWordNature(WordNature.getWordNature(nature));
			Word word = WordFactory.createWord(text, meaning);

			if (item.attr != null && !"".equals(item.attr)) {
				String fromat = item.attr.trim();
				meaning.setParseFormat(fromat);
			}

			resutls.add(word);
		}
		
		//目前仅发现这三种时间具有多词性，暂硬编码到代码里
		String[] dateOrPeriod = {"d月", "dd月", "dd年"};
		String[] timeformats = {"MONTH_OR_PERIOD", "MONTH_OR_PERIOD", "YEAR_OR_PERIOD"};
		ArrayList<String> multiNatures = new ArrayList<>();
		for (int i = 0; i < dateOrPeriod.length; i++) {
			multiNatures.add(dateOrPeriod[i]);
		}
		for (int i = 0; i < resutls.size(); i++) {
			Word word = resutls.get(i);
			if (multiNatures.contains(word.getText())){
				if(!word.containNature(WordNatures.DATE)) {
					WordMeaning meaning = new WordMeaning();
					meaning.setWord(word);
					meaning.setParseFormat(timeformats[multiNatures.indexOf(word.getText())]);
					meaning.setWordNature(WordNature.getWordNature(WordNatures.DATE));
					word.addMeaning(meaning);
				}
			}
		}

		return resutls;
	}

	/**
	 * 支持分秒的
	 * @param datePattern
	 * @param format
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<String>[] getMSSupports(String datePattern, String format) {
		final String[] canHaveMS = { 
				"yyyy-MM-dd", "yy-MM-dd",
				"yy－MM－dd", "yyyy－MM－dd",
				"yy.MM.dd", "yyyy.MM.dd"
			};
		ArrayList<String> formats = new ArrayList<>();
		ArrayList<String> datePatterns = new ArrayList<>();
		for (int i = 0; i < canHaveMS.length; i++) {
			if (format.equals(canHaveMS[i])) {
				formats.add(canHaveMS[i] + "mm:ss");
				datePatterns.add(datePattern + "dd:dd");
				
				formats.add(canHaveMS[i] + "mm：ss");
				datePatterns.add(datePattern + "dd：dd");
				
				formats.add(canHaveMS[i] + "mm::ss");
				datePatterns.add(datePattern + "dd::dd");
				
				formats.add(canHaveMS[i] + " mm:ss");
				datePatterns.add(datePattern + " dd:dd");
				
				formats.add(canHaveMS[i] + " mm：ss");
				datePatterns.add(datePattern + " dd：dd");
				
				formats.add(canHaveMS[i] + " mm::ss");
				datePatterns.add(datePattern + " dd::dd");
				break;
			}
		}
		return datePatterns.size() > 0 ? new ArrayList[]{datePatterns, formats} : null;
	}
}
