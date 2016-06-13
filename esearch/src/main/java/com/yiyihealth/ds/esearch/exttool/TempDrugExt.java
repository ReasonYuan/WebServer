package com.yiyihealth.ds.esearch.exttool;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.EvidenceWritter;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleSyntaxCorrect;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Paper;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager.OnRecordLoadListener;
import com.yiyihealth.nlp.deepstruct.utils.JSONUtil;

public class TempDrugExt {
	
	private static JSONObject config;
	
	private static int counter = 0;
	
	private static final SimpleSyntaxCorrect checker = new SimpleSyntaxCorrect("./tmp_drug_proj/sys/simple_syntaxc_orrect.txt");

	public static void preProcess() {
		try {
			String text = FileUtils.fileRead("./tmp_drug_proj/conf/conf.json");
			config = JSONObject.parseObject(text);
			ArrayList<String> files = listFiles(config);
			for (int i = 0; i < files.size(); i++) {
				parseOneFile(files.get(i));
			}
			toEvidences();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ArrayList<String> listFiles(JSONObject config){
		ArrayList<String> results = new ArrayList<String>();
		File dir = new File("./tmp_drug_proj/org_files");
		findFiles(dir, config, results);
		return results;
	}
	
	private static void findFiles(File file, final JSONObject config, ArrayList<String> results){
		if (file.isDirectory()) {
			File[] files = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (new File(dir.getPath() + "/" + name).isDirectory()) {
						return true;
					}
					boolean isLegal = false;
					for (int i = 0; i < config.getJSONArray("fileFilter").size(); i++) {
						String prefix = config.getJSONArray("fileFilter").getString(i);
						if (name.startsWith(prefix)) {
							isLegal = true;
							break;
						}
					}
					return isLegal;
				}
			});
			for (int i = 0; i < files.length; i++) {
				findFiles(files[i], config, results);
			}
		} else {
			System.out.println("符合过滤条件的文件: " + file.getPath());
			results.add(file.getPath());
		}
	}
	
	public static void parseOneFile(String file) throws IOException {
		String text = FileUtils.fileRead(file);
    	JSONObject jsonObject = JSONObject.parseObject(text);
    	Paper paper = new Paper();
    	Set<String> keys = jsonObject.keySet();
    	for(String key : keys){
    		String blockString = jsonObject.getString(key);
    		blockString = blockString.replace("\n", "");
    		blockString = blockString.replace(" ", "");
    		String title = key;
    		ArrayList<Sentence> sentences = new HealthAnalysis().parse(blockString);
    		checker.correctBasicWords(sentences);
    		paper.addSection(title, sentences);
    	}
    	
    	JSONArray jsonWords = new JSONArray();
    	JSONObject output = new JSONObject();
    	output.put("data", jsonWords);
    	output.put("filename", counter + "_" + new File(file).getName());
    	JSONUtil.toJson(paper, jsonWords);
    	String fileName = new File(file).getName();
    	JSONUtil.writeJson4ESearch(output, counter++ + "_" + fileName.substring(0, fileName.indexOf(".")), "./tmp_drug_proj/tmp_json/");
	}
	
	public static void toEvidences(){
		try {
			final int[] cnt = new int[1];
			new FileManager().loadWords("./tmp_drug_proj/tmp_json", new OnRecordLoadListener() {
				public void onRecordLoad(String filename, ArrayList<EWord> allWords) {
					if (allWords.size() != 0) {
						ArrayList<ArrayList<EWord>> oneRecord = new ArrayList<ArrayList<EWord>>();
						oneRecord.add(allWords);
						EvidenceWritter writter = new EvidenceWritter("./tmp_drug_proj/evidences/" + filename + ".evi");
						SimpleEvidenceSearcher searcher = new SimpleEvidenceSearcher("./tmp_drug_proj/sys/esearch.txt", writter);
						ArrayList<WordPos> wordPoses = searcher.searchSimpleEvidence(oneRecord);
						writter.writeToFile();
						writter.appendToFile(wordPoses);
						//进入推理查询库
						Object object = JSON.toJSON(wordPoses);
						String wordsJson = object.toString();
						FileManager.writeToFile("./tmp_drug_proj/evidences/" + filename + ".evi" + ".db", wordsJson);
						cnt[0]++;
					}
				}
			});
			System.out.println("total: " + cnt[0]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
