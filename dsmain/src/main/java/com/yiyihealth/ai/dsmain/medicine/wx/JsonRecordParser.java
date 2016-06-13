package com.yiyihealth.ai.dsmain.medicine.wx;

import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ai.dsmain.nlp.RecordParser;

public class JsonRecordParser extends RecordParser {

	@Override
	public void parseWord(String projectDir) throws Exception {
		String text = FileUtils.fileRead(projectDir + "/conf.json");
		JSONObject config = JSONObject.parseObject(text);
		ArrayList<String> orgJsonFiles = NlpDrugParser.listFiles(config,projectDir + "/original");
		
		for (int i = 0; i < orgJsonFiles.size(); i++) {
			NlpDrugParser nlpParser = new NlpDrugParser(projectDir);
			nlpParser.parseOneFile(orgJsonFiles.get(i), projectDir + "/nlpwords/");
		}
	}

}


