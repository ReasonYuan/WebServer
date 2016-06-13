package com.yiyihealth.ai.dsmain.medicine.wx;
import java.io.File;
import java.io.IOException;

import org.bson.Document;
import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;


public class MongoDBUtils {
	
	private static MongoClient mongoClient = null;
	private static MongoCollection<Document> collection = null;
	
	public static void downLoadDic() throws IOException{
		JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
		JSONObject mObject = config.getJSONObject("dicInfomation");
		String dbName = mObject.getString("dbName");
		String tableName = mObject.getString("tableName");
		getCollection(dbName,tableName);
		String savePath = mObject.getString("DicSavePath");
		writeToJsonFile(savePath);
		System.out.println("================更新字典完毕===================");
		closeClient();
	}

	public static MongoCollection<Document> getCollection(String dbName,String tableName) throws IOException{
		JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
		JSONObject mObject = config.getJSONObject("dicInfomation");
		String serverIp = mObject.getString("mongoServerIp");
		mongoClient  = new MongoClient(serverIp,27017);
		MongoDatabase database = getDataBase(dbName);
        collection = database.getCollection(tableName);
        return collection;
	}
	
	private static MongoDatabase getDataBase(String dataBaseName){
		return mongoClient.getDatabase(dataBaseName);
	}
	
	
	private static void writeToJsonFile(String path){
		JSONArray mArray = queryAllDocment();
		FileManager.writeToFile(path, format(mArray.toJSONString()));
	}
	
	/**
	 * 查询所有的Document信息 并转化为jsonarray
	 */
	public static JSONArray queryAllDocment(){
		JSONArray mArray = new JSONArray();
		for (Document cur : collection.find()) {
			JSONObject mObject =  new JSONObject();
			for (String key : cur.keySet()) {
				if (!key.equals("_id") && !key.equals("naNum")) {
					mObject.put(key, cur.get(key));
				}
			}
			mArray.add(mObject);
		}
		 return mArray;
	}
	
	/**
	   * 得到格式化json数据  退格用\t 换行用\r
	   */
	  public static String format(String jsonStr) {
	    int level = 0;
	    StringBuffer jsonForMatStr = new StringBuffer();
	    for(int i=0;i<jsonStr.length();i++){
	      char c = jsonStr.charAt(i);
	      if(level>0&&'\n'==jsonForMatStr.charAt(jsonForMatStr.length()-1)){
	        jsonForMatStr.append(getLevelStr(level));
	      }
	      switch (c) {
	      case '{': 
	      case '[':
	        jsonForMatStr.append(c+"\n");
	        level++;
	        break;
	      case ',': 
	        jsonForMatStr.append(c+"\n");
	        break;
	      case '}':
	      case ']':
	        jsonForMatStr.append("\n");
	        level--;
	        jsonForMatStr.append(getLevelStr(level));
	        jsonForMatStr.append(c);
	        break;
	      default:
	        jsonForMatStr.append(c);
	        break;
	      }
	    }
	    
	    return jsonForMatStr.toString();

	  }
	  
	  private static String getLevelStr(int level){
	    StringBuffer levelStr = new StringBuffer();
	    for(int levelI = 0;levelI<level ; levelI++){
	      levelStr.append("\t");
	    }
	    return levelStr.toString();
	  }
	  
	  /**
		 * 关闭mongoclient
		 */
		public static void closeClient(){
			mongoClient.close();
		}
}
