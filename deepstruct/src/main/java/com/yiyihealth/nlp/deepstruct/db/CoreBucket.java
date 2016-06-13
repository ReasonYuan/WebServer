package com.yiyihealth.nlp.deepstruct.db;

import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSONObject;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQuery;

public class CoreBucket {
	
	public static interface OnQueryEachRecordInfo {
		public void onEachRecordInfo(JsonObject object);
	}
	
	private Bucket bucket;
	
	private String bucketName;
	
	public CoreBucket(Bucket bucket) {
		this.bucket = bucket;
		bucketName = bucket.name();
	}
	
	public Bucket getBucket() {
		return bucket;
	}
	
	public JsonDocument getDocument(String documentID){
		return bucket.get(documentID);
	}
	
	/**
	 * 所有记录都会被加上前缀: struct_
	 * @param documentID
	 * @param version
	 * @param jsonObject
	 */
	public void saveStructDocument(String documentID, String version, JSONObject jsonObject){
		String structDocID = "struct_" + version + "_" + documentID;
		JsonDocument jsonDocument = JsonDocument.create(structDocID, JsonObject.fromJson(jsonObject.toJSONString()));
		JsonDocument result = bucket.upsert(jsonDocument);
		
		
		
		
		System.out.println("saved: " + structDocID + ", " + result.cas());
	}
	
	public void queryN1sql(String n1sql, OnQueryEachRecordInfo callback){
		System.out.println("n1sql: " + n1sql);
		bucket
	    .async()
	    .query(N1qlQuery.simple(n1sql))
	    .flatMap(AsyncN1qlQueryResult::rows)
	    //.timeout(30, TimeUnit.SECONDS)
	    .toBlocking()
	    .forEach(row -> callback.onEachRecordInfo(row.value()));
	}
	
	public void listNoteInfos(String doctors, int offset, int limit, String types, OnQueryEachRecordInfo callback){
		String n1sql = "SELECT meta().id, info[0].record_type, info[0].record_title, info[0].note_info FROM `" + bucketName
	    		+ "` where type = 'Record' and ( " + types + " ) and info[0].note_info is not null and (" + doctors + ") order by meta().id, info[0].create_time offset " + offset + " limit " + limit;
		System.out.println("n1sql: " + n1sql);
		bucket
	    .async()
	    .query(N1qlQuery.simple(n1sql))
	    .flatMap(AsyncN1qlQueryResult::rows)
	    //.timeout(30, TimeUnit.SECONDS)
	    .toBlocking()
	    .forEach(row -> callback.onEachRecordInfo(row.value()));
	}
}
