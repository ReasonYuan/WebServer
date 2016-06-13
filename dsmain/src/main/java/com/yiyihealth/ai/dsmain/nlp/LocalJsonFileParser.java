package com.yiyihealth.ai.dsmain.nlp;

import java.io.File;
import java.io.FilenameFilter;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;

public class LocalJsonFileParser extends RecordParser {

	@Override
	public void parseWord(String projectDir) throws Exception {
		// 2.1 读取保存好的原始数据的json文件
		File dir = new File(projectDir + "/original");
		String[] orgJsonFiles = dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".json");
			}
		});
		// 2.2 第二轮才真正写入分词结果(n=2)，TODO 这是分词不完善的临时解决方案
		for (int n = 0; n < 2; n++) {
			for (int i = 0; i < orgJsonFiles.length; i++) {
				JSONObject object = JSONObject
						.parseObject(FileUtils.fileRead(new File(projectDir + "/original/" + orgJsonFiles[i])));
				NlpJsonFileParser nlpJsonFileParser = new NlpJsonFileParser(projectDir);
				nlpJsonFileParser.parse(object, orgJsonFiles[i].substring(0, orgJsonFiles[i].indexOf(".json")),
						projectDir + "/nlpwords/", n == 1);
			}
		}
	}

}
