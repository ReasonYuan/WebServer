package com.yiyihealth.ai.dsmain.db;

import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.db.CoreBucket;
import com.yiyihealth.nlp.deepstruct.db.CouchbaseDBManager;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;

/**
 * 结构化完后的病历记录，保存到服务器上
 *
 */
public class StructRecords {
	
	private static CoreBucket coreBucket;
	
	private String host;
	private String bucket;
	
	public StructRecords(String host, String bucket) {
		this.host = host;
		this.bucket = bucket;
	}
	
	public void closeBucket(){
		if (coreBucket != null) {
			try {
				coreBucket.getBucket().close();
			} catch (Exception e) {
			}
		}
	}
	
	public static void writeNlpWords4Human(String documentID, String toFileDir, ArrayList<EWord> words){
		StringBuffer toHumanRec = new StringBuffer();
		StringBuffer origLine = new StringBuffer();
		StringBuffer belowLine = new StringBuffer();
		String outFilename = documentID + "_out.txt";
		for (int k = 0; k < words.size(); k++) {
			EWord word = words.get(k);
			if (word.getNature().equals(WordNatures.HEADING)) {
				toHumanRec.append(origLine.toString());
				origLine.setLength(0);
				toHumanRec.append("\n");
				toHumanRec.append(belowLine.toString());
				belowLine.setLength(0);
				toHumanRec.append("\n");
				toHumanRec.append("\n");
			}
			origLine.append(word.getWord());
			belowLine.append(word.getWord()).append("[" + WordNatures.getNatureReadableName(word.getNature()) + "]("+k+") ");
			//toHumanRec.append();
		}
		toHumanRec.append(origLine.toString());
		origLine.setLength(0);
		toHumanRec.append("\n");
		toHumanRec.append(belowLine.toString());
		belowLine.setLength(0);
		toHumanRec.append("\n");
		FileManager.writeToFile(toFileDir + "/" + outFilename, toHumanRec.toString());
	}

	/**
	 * 
	 * @param documentID
	 * @param version 结构化版本
	 * @param words
	 */
	public void saveStructDocument(String documentID, String version, ArrayList<EWord> words, String toFileDir, boolean toDB) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonWords = new JSONArray();
		jsonObject.put("data", jsonWords);
		jsonObject.put("documentID", documentID);
		
		for (int k = 0; k < words.size(); k++) {
			JSONObject item = new JSONObject();
			EWord word = words.get(k);
			item.put("word", word.getWord());
			item.put("nature", word.getNature());
			item.put("natureChinese", WordNatures.getNatureReadableName(word.getNature()));
			if (word.getAttributes().size() > 0) {
				item.put("attributes", word.getAttributes());
			}
			if (word.getTaggedDates().size() > 0) {
				item.put("refDates", JSONArray.toJSON(word.getTaggedDates()));
			}
			if (word.getTaggedDates().size() > 0) {
				item.put("refActualDates", JSONArray.toJSON(word.getTaggedActualDates()));
			}
			jsonWords.add(item);
		}

		if (toFileDir != null) {
			String outFilename = documentID + "_out.json";
			FileManager.writeToFile(toFileDir + "/" + outFilename, JSONObject.toJSONString(jsonObject, true));
		}
		
		if (toDB) {
			try {
				if (coreBucket == null) {
					coreBucket = new CoreBucket(CouchbaseDBManager.getBucket(host, bucket));
				}
				coreBucket.saveStructDocument(documentID, version, jsonObject);
			} catch (Exception e) {
				coreBucket = new CoreBucket(CouchbaseDBManager.getBucket(host, bucket));
				try {
					coreBucket.saveStructDocument(documentID, version, jsonObject);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}

}
