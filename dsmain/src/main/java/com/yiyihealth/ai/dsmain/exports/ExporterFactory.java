package com.yiyihealth.ai.dsmain.exports;

import com.alibaba.fastjson.JSONObject;

public class ExporterFactory {

	public static Exporter createExporter(JSONObject config){
		String exportFormat = config.getString("format");
		if (exportFormat.equals("json")) {
			try {
				return (Exporter)Class.forName(config.getString("exporter")).newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		//TODO 实现其他exporter
		
		return null;
	}
	
}
