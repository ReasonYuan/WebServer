package com.yiyihealth.nlp.deepstruct.db;

import java.util.Hashtable;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

public class CouchbaseDBManager {
	
	private static Hashtable<String, Bucket> connections = new Hashtable<String, Bucket>();
	private static CouchbaseCluster cluster;

	private CouchbaseDBManager() {
	}
	
	public static Bucket getBucket(String host, String bucketName){
		Bucket bucket = connections.get(bucketName);
		if (bucket == null) {
			//this tunes the SDK (to customize connection timeout)
	        CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
	        		.connectTimeout(60000*1) //10000ms = 10s, default is 5s
	                .queryTimeout(60000*30)
	                .kvTimeout(60000*30)
	                .viewTimeout(60000*30)
	                .build();
	        System.out.println("Create connection");
	        //use the env during cluster creation to apply
	        Cluster cluster = CouchbaseCluster.create(env, host);
	        System.out.println("Try to openBucket");
	        bucket = cluster.openBucket(bucketName); //you can also f
			
			// Connect to localhost
			//cluster = CouchbaseCluster.create(host);
			//bucket = cluster.openBucket(bucketName);
			connections.put(bucketName, bucket);
		}
		return bucket;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		try {
			cluster.disconnect();
		} catch (Exception e) {
		}
	}
}
