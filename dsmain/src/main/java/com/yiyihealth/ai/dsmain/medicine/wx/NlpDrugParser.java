package com.yiyihealth.ai.dsmain.medicine.wx;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleSyntaxCorrect;
import com.yiyihealth.nlp.deepstruct.dict.Paper;
import com.yiyihealth.nlp.deepstruct.utils.JSONUtil;

public class NlpDrugParser {
	
	private static SimpleSyntaxCorrect checker;
	private static JSONObject config;
	private static int counter = 0;

	public NlpDrugParser(String projectDir){
		checker = new SimpleSyntaxCorrect(projectDir + "/../../simple_syntax/simple_syntax_correct.txt");
	}
	
	public void parseOneFile(String file,String dir2save) throws IOException {
		String text = FileUtils.fileRead(file);
		String person = file.substring(file.indexOf("original/")+"original/".length(), file.indexOf('/', file.indexOf("original/")+"original/".length()+1));
		String fileName = new File(file).getName();
		JSONObject jsonObject = JSONObject.parseObject(text);
		if(fileName.contains("RY")){
			jsonObject.put("record_title", "入院记录");
		}
		if(fileName.contains("CY")){
			jsonObject.put("record_title", "出院记录");
		}
		String adminDate = jsonObject.getString("admission_date");
		String outAdminDate = jsonObject.getString("out_hospital_date");
		if(adminDate != null){
			jsonObject.put("入院日期", adminDate);
		}
		if(outAdminDate != null){
			jsonObject.put("出院日期", outAdminDate);
		}
		
		
		
		Paper paper = new Paper();
		Set<String> keys = jsonObject.keySet();
		for (String key : keys) {
			String blockString = jsonObject.getString(key);
			blockString = blockString.replace("\n", "");
			blockString = blockString.replace(" ", "");
			String title = key;
			ArrayList<Sentence> sentences = new HealthAnalysis().parse(file + "\n", blockString);
			checker.correctBasicWords(sentences);
			paper.addSection(title, sentences);
		}

		JSONArray jsonWords = new JSONArray();
		JSONObject output = new JSONObject();
		output.put("data", jsonWords);
		output.put("filename", counter + "_" + new File(file).getName());
		JSONUtil.toJson(paper, jsonWords);
		JSONUtil.writeJson4ESearch(output, counter++ + "_" + person + "_" + fileName.substring(0, fileName.indexOf(".")),
				dir2save);
	}
	
	public static ArrayList<String> listFiles(JSONObject config,String path){
		ArrayList<String> results = new ArrayList<String>();
		File dir = new File(path);
		findFiles(dir, config, results);
		results.sort(new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		return results;
	}
	
	private static void findFiles(File file, final JSONObject config, ArrayList<String> results){
		if (file.isDirectory()) {
			File[] files = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if(!(dir.getPath() + name).contains(".svn")){
						if (new File(dir.getPath() + "/" + name).isDirectory()) {
							return true;
						}
					}else{
						return false;
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
}
