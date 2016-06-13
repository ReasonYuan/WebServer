package com.yiyihealth.ai.dsmain.nlp;

import java.util.ArrayList;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleSyntaxCorrect;
import com.yiyihealth.nlp.deepstruct.dict.Paper;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.utils.JSONUtil;

public class NlpJsonFileParser {
	
	private SimpleSyntaxCorrect checker;
	
	public NlpJsonFileParser(String projectDir){
		checker = new SimpleSyntaxCorrect(projectDir + "/../../simple_syntax/simple_syntax_correct.txt");
	}

	/**
	 * 对原始数据分词, 然后存入证据文件里
	 * @param jsonArray
	 * @param documentID
	 * @param dir2save 存放分词结果的目录
	 * @param save2File TODO 这是词典很小的时候的临时方案，根据简单语法规则推测新词并在下轮用新词重新分词
	 * @return
	 */
	public ArrayList<Term> parse(JSONObject jsonObject, String documentID, String dir2save, boolean save2File){
    	Paper paper = new Paper();
    	Set<String> keys = jsonObject.keySet();
    	for(String name : keys){
    		String content = jsonObject.getString(name);
    		ArrayList<Sentence> sentences = new HealthAnalysis().parse(content);
			checker.correctBasicWords(sentences);
			paper.addSection(name, sentences);
    	}
    	if (save2File) {
    		JSONArray jsonWords = new JSONArray();
        	jsonObject.put("data", jsonWords);
        	jsonObject.put("documentID", documentID);
        	JSONUtil.toJson(paper, jsonWords);
        	JSONUtil.writeJson4ESearch(jsonObject, documentID, dir2save);
		}
    	return paper.getWords();
    }
	
}
