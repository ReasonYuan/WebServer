package com.yiyihealth.ai.dsmain.db;

import org.codehaus.plexus.util.FileUtils;

import com.couchbase.client.java.document.json.JsonObject;
import com.yiyihealth.nlp.deepstruct.db.CoreBucket;
import com.yiyihealth.nlp.deepstruct.db.CoreBucket.OnQueryEachRecordInfo;
import com.yiyihealth.nlp.deepstruct.db.CouchbaseDBManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;

public class CouchbaseRecordFetcher {
	
	private String n1sqlFile;
	
	private String projectDir;
	
	private String host;
	private String bucket;

	public CouchbaseRecordFetcher(String projectDir, String n1sqlFile, String host, String bucket){
		this.n1sqlFile = n1sqlFile;
		this.projectDir = projectDir;
		this.host = host;
		this.bucket = bucket;
	}
	
	public String getN1sqlFile() {
		return n1sqlFile;
	}
	
	public void fetchRecords() {
		try {
			OnQueryEachRecordInfo callback = new OnQueryEachRecordInfo() {
				public void onEachRecordInfo(JsonObject object) {
					FileManager.writeToFile(projectDir + "/original/" + object.getString("id") + ".json", object.toString());
					System.out.println("save original file: " + projectDir + "/original/" + object.getString("id") + ".json");
				}
			};
			CoreBucket bucketCon = new CoreBucket(CouchbaseDBManager.getBucket(host, bucket));
	    	System.out.println("正在查询数据...");
	    	bucketCon.queryN1sql(FileUtils.fileRead(n1sqlFile).replace("__BUCKET_NAME__", bucket), callback);
	    	bucketCon.getBucket().close();
	    	System.out.println("获取原始数据结束...");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
