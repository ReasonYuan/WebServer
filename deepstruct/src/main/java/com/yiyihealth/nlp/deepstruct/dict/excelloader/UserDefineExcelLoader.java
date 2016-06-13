package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.WordMeaning;
import com.yiyihealth.nlp.deepstruct.dict.WordNature;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.WordsLoader;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;

public class UserDefineExcelLoader implements WordsLoader {

//	public static void main(String args[]) throws ClassNotFoundException, IOException {
//		ArrayList<Word> words = loadJsonDict("dict/病历分词字典.json");
//		
//		System.out.println(words.size());
//	}
	  
	
	@SuppressWarnings({ "resource", "unused" })
	private static ArrayList<Word> readJsonFile(String file,Hashtable<String, String> naturesMap,ArrayList<Word> results) throws IOException, ClassNotFoundException{
		String jsonstr = FileManager.BufferedReaderLargeFile(file);
		
		JSONArray mArray = JSONArray.parseArray(jsonstr);
		for (int i = 0; i < mArray.size(); i++) {
			JSONObject mObject = mArray.getJSONObject(i);
			Set<String> keys = mObject.keySet();
			Word word  = null;
			ArrayList<WordMeaning> meaningList = new ArrayList<>();
			for (String key : keys) {
				WordMeaning meaning = new WordMeaning();
				if(keys.size() == 1 && key.equals("word")){
					meaning.setWordNature(WordNature.getWordNature("Unrec"));
					meaning.setWord(word);
					meaningList.add(meaning);
				}
				if(key.contains("nature") && !mObject.getString(key).equals("")){
					String nature = naturesMap.get(mObject.getString(key));
					if(nature != null){
						meaning.setWordNature(WordNature.getWordNature(nature));
						meaning.setWord(word);
						meaningList.add(meaning);
					}else{
						JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
						final String projectDir = config.getString("projectDir");
						FileWriter bw =  new FileWriter(projectDir + "/findWord/" + "newWord.txt", true);
						String tmp = new String(("nature：" + mObject.getString(key) + "word： " + key + "\r\n\n").getBytes(), "UTF-8");
						bw.write(tmp);
						bw.flush();
						bw.close();
						meaning.setWordNature(WordNature.getWordNature("Unrec"));
						meaning.setWord(word);
						meaningList.add(meaning);
					}
				}
			}
			WordMeaning[] list = new WordMeaning[meaningList.size()];
			meaningList.toArray(list);
			word = WordFactory.createWord(mObject.getString("word"), list);
			if(word != null){
				results.add(word);
			}
		}
		
		return results;
	}
	
	public static ArrayList<Word> loadJsonDict(String file) throws ClassNotFoundException, IOException { 
		Hashtable<String, String> naturesMap = new Hashtable<String, String>();
		for (int i = 0; i < WordNatures.natureNames.length; i++) {
			naturesMap.put(WordNatures.natureNames[i][0], WordNatures.natureNames[i][1]);
		}
		ArrayList<Word> results = new ArrayList<Word>();
		results = readJsonFile(file,naturesMap,results);
		return results;
	}
	

	public static  ArrayList<Word> loadDict(String file) {
		try {
			
			//WordClassNames.classNames
			Hashtable<String, String> naturesMap = new Hashtable<String, String>();
			for (int i = 0; i < WordNatures.natureNames.length; i++) {
				naturesMap.put(WordNatures.natureNames[i][0], WordNatures.natureNames[i][1]);
			}
			
			File excelFile = new File(file); // 创建文件对象
			FileInputStream is = new FileInputStream(excelFile); // 文件流
			// 创建对Excel工作簿文件的引用
			Workbook wbs = WorkbookFactory.create(excelFile);
			// int sheetCount = workbook.getNumberOfSheets(); //Sheet的数量
			Sheet dictSheet = wbs.getSheetAt(0);
			Hashtable<String, String> wordsExists = new Hashtable<String, String>();
			int lineOffset = 5;
			ArrayList<Word> results = new ArrayList<Word>();
			for (int r = lineOffset; r < dictSheet.getLastRowNum()+1; r++) {// 循环该子sheetrow
				Row row = dictSheet.getRow(r);
				//System.out.print(r + "-> ");
				String[] values = new String[7];
				for (int c = 0; c < 7; c++) {// 循环该子sheet行对应的单元格项
					Cell cell = row.getCell(c);
					String value = null;
					boolean error = false;
					if (cell != null) {
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_STRING:
							value = cell.getStringCellValue().trim();
							break;
						case Cell.CELL_TYPE_BLANK:
							value = "";
							break;
						case Cell.CELL_TYPE_NUMERIC:
							value = "" + cell.getNumericCellValue();
							break;
						default:
							error = true;
							System.out.print("Error: unknown cell type: " + cell.getCellType());
							break;
						}
						if (!error) {
							values[c] = value;
							//System.out.print(getCellName(c) + ": " + value + " | ");
						}
					}
				}
				if (values[0] != null && !values[0].equals("")) {
					if (wordsExists.containsKey(values[0])) {
						//System.out.println("Error: 单词 " + values[0] + " 已经存在");
					} else {
						Word word = null;
						final int ML = 3;
						for (int i = 0; i < (values.length-1)/ML; i++) {
							int start = i*ML + 1;
							//同义词
							String nearSynonyms = values[start] == null ? "" : values[start];
							//词性
							String nature = values[start + 1] == null ? null : naturesMap.get(values[start + 1]);
							//词义
							//String wmean = values[start + 2] == null ? "" : values[start + 2];
							if (nature != null) {
								WordMeaning meaning = new WordMeaning();
								meaning.setWordNature(WordNature.getWordNature(nature));
								//TODO meaning.setNearSynonyms(nearSynonyms);
								meaning.setWord(word);
								meaning.setTextNearSynonyms(nearSynonyms);
								word = WordFactory.createWord(values[0], meaning);
							}
						}
						if (word != null && word.getMeanings().size() > 0) {
							wordsExists.put(word.getText(), word.getText());
							results.add(word);
						}
					}
				}
				//System.out.println("");
			}
			is.close();
			return results;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unused")
	private String getCellName(int colIndex){
		if (colIndex == 0) {
			return "词";
		} else if(colIndex <= 8){
			final String[] means = {"同义词", "作用方向", "词性", "词义"};
			return means[(colIndex-1)%4];
		} else {
			return "Unknown";
		}
	}
	
	/**
	 * TODO 目前只支持2个意思解释
	 * @param colIndex
	 * @return
	 */
	@SuppressWarnings("unused")
	private static int getCellType(int colIndex){
		if (colIndex == 0) {
			return -1;
		} else if(colIndex <= 8){
			final int[] means = {0, 1, 2, 3};
			return means[(colIndex-1)%4];
		} else {
			return -2;
		}
	}

	public ArrayList<Word> loadDict() {
		ArrayList<Word> words = null;
		try {
			words = loadJsonDict("dict/病历分词字典.json");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return words;
	}
	
}
