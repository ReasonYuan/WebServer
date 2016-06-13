package com.yiyihealth.ai.dsmain.nlp;

import java.io.File;
import java.io.FilenameFilter;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

public class CouchbaseRecordParser extends RecordParser {

	public void parseWord(String projectDir) throws Exception {
		//2.1 读取保存好的原始数据的json文件
		File dir = new File(projectDir + "/original");
		String[] orgJsonFiles = dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});
		//2.2 第二轮才真正写入分词结果(n=2)，TODO 这是分词不完善的临时解决方案
		for (int n = 0; n < 2; n++) {
			for (int i = 0; i < orgJsonFiles.length; i++) {
				JSONObject object = JSONObject.parseObject(FileUtils.fileRead(new File(projectDir + "/original/" + orgJsonFiles[i])));
				String recordTitle = object.getString("record_title");
				String noteinfo = object.getString("note_info").replace("\\n", "");
	    		JsonArray jsonArray = JsonArray.fromJson(noteinfo);
	    		JsonObject titleObject = JsonObject.create();
	    		titleObject.put("record_title", recordTitle);
	    		jsonArray.add(titleObject);
	    		NlpParser nlpParser = new NlpParser(projectDir);
	    		nlpParser.parse(jsonArray, orgJsonFiles[i].substring(0, orgJsonFiles[i].indexOf(".json")), projectDir + "/nlpwords/", n == 1);
			}
		}
	}
	
}
