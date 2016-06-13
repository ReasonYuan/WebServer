package com.yiyihealth.nlp.deepstruct.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

/**
 * 把单词按词性分类存储到excel里，方便人工核对
 * @author qiangpeng
 *
 */
public class ExcelByNatureWriter {

	private static Hashtable<String, ArrayList<Word>> natureWords = new Hashtable<String, ArrayList<Word>>();
	
	/**
	 * 把单词按词性分类存储到excel里，方便人工核对, 只按单词的第一个词性分类
	 * @param words
	 */
	public static void writeNatureWords(ArrayList<Word> words){
		for (int i = 0; i < words.size(); i++) {
			Word word = words.get(i);
			doClassify(word);
		}
		for (String key : natureWords.keySet()) {
			String readableName = WordNatures.getNatureReadableName(key);
			try {
				//writeOneNatureWords(readableName, natureWords.get(key));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void writeExcel(String filename, String sheetname,  ArrayList<ArrayList<String>> contents) {
		try {
			File excelFile = new File(filename); // 创建文件对象
			FileOutputStream out = new FileOutputStream(excelFile); // 文件流
			// 创建对Excel工作簿文件的引用
			HSSFWorkbook wbs = new HSSFWorkbook();
			Sheet sheet = wbs.createSheet(sheetname);
			for (int i = 0; i < contents.size(); i++) {
				Row row = sheet.createRow(i);
				for (int k = 0; k < contents.get(i).size(); k++) {
					row.createCell(k).setCellValue(contents.get(i).get(k));
				}
			}
			wbs.write(out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void writeOneNatureWords(String readableName, ArrayList<Word> words) throws Exception {
		File excelFile = new File("exceldict/" + readableName + "_dict.xlsx"); // 创建文件对象
		FileOutputStream out = new FileOutputStream(excelFile); // 文件流
		// 创建对Excel工作簿文件的引用
		HSSFWorkbook wbs = new HSSFWorkbook();
		Sheet sheet = wbs.createSheet(readableName);
		
		for (int i = 0; i <= words.size(); i++) {
			if (i == 0) {
				Row row = sheet.createRow(i);
				int cidx = 0;
				row.createCell(cidx++).setCellValue("单词");
				for (int j = 1; j <= 3; j++) {
					row.createCell(cidx++).setCellValue("词性" + j);
					row.createCell(cidx++).setCellValue("近义词" + j);
					row.createCell(cidx++).setCellValue("前置词性" + j);
					row.createCell(cidx++).setCellValue("后置词性" + j);
				}
			} else {
				Row row = sheet.createRow(i);
				Word word = words.get(i-1);
				int cidx = 0;
				//格式: 单词, [词性，近义词, 前置词性, 后置词性], [...],...
				Cell w0 = row.createCell(cidx++);
				w0.setCellValue(word.getText());
				for (int j = 0; j < word.getMeanings().size(); j++) {
					//词性
					Cell wd0 = row.createCell(cidx++);
					wd0.setCellValue(word.getMeanings().get(j).getWordNature().getName());
					//近义词
					Cell wd1 = row.createCell(cidx++);
					wd1.setCellValue(word.getMeanings().get(j).getTextNearSynonyms());
					//前置词性
					//后置词性
				}
			}
		}
		wbs.write(out);
		out.flush();
		out.close();
	}
	
	private static void doClassify(Word word){
		if (word.getMeanings().size() == 0) {
			throw new RuntimeException("word: " + word.getText() + " does not have any natures!!!");
		}
		//只按大类
		String rootNature = word.getMeanings().get(0).getWordNature().getName();
		ArrayList<Word> oneNature = natureWords.get(rootNature);
		if (oneNature == null) {
			oneNature = new ArrayList<Word>();
			natureWords.put(rootNature, oneNature);
		}
		oneNature.add(word);
	}
	
}

