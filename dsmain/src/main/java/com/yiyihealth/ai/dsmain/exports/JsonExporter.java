package com.yiyihealth.ai.dsmain.exports;

import java.util.ArrayList;
import java.util.Hashtable;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ai.dsmain.medicine.wx.UnDefAndUnRecTittles;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;

public class JsonExporter extends Exporter {
	
	
	JsonExporter() {
	}

	@Override
	public void export(InherenceEngine engine, String filename) {
		System.out.println("exporting...");
		//TODO exports
		
		Hashtable<String, ArrayList<Object>> exportData = new Hashtable<>();
		
		DrugExportModule drugExportModule = new DrugExportModule();
		ArrayList<Object> drugs = ExporterUtils.sortList(drugExportModule.export(engine));
		
		TestItemExportModule testItemExportModule = new TestItemExportModule();
		ArrayList<Object> testItems = ExporterUtils.sortList(testItemExportModule.export(engine));
		
		SymptomExportModule symptomExportModule = new SymptomExportModule();
		ArrayList<Object> symptoms = ExporterUtils.sortList(symptomExportModule.export(engine));
		
		DiagExportModule diagExportModule = new DiagExportModule();
		ArrayList<Object> diags = ExporterUtils.sortList(diagExportModule.export(engine));
		
		ExamExportModule examExportModule = new ExamExportModule();
		ArrayList<Object> exams = ExporterUtils.sortList(examExportModule.export(engine));
		
		UnRecExportModule unRecExportModule = new UnRecExportModule();
		ArrayList<Object> unRec = unRecExportModule.export(engine);
		
		exportData.put("用药", drugs);
		exportData.put("化验", testItems);
		exportData.put("症状", symptoms);
		exportData.put("诊断", diags);
		exportData.put("检查", exams);
		exportData.put(UnDefAndUnRecTittles.wordsUnDefAndUnRec, unRec);
		writeToJsonFile(exportData, filename);
	}
	
	private void writeToJsonFile(Hashtable<String, ArrayList<Object>> exportData, String filename){
		String content = JSONObject.toJSONString(exportData, true);
		FileManager.writeToFile(filename, content);
	}
	
}
