package wx.mongodb;

import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class DBUtils { 
	private static MongoClient mongoClient = new MongoClient( App.serverIp , 27017 );
//	private static MongoClient mongoClient = new MongoClient( localIp , 27017 );
	private static MongoCollection<Document> collection = null;
	private static onLoadDbOK callback = null;
	/**
	 * 将word和nature插入数据库中
	 * @param word
	 * @param nature1
	 * @param nature2
	 */
	public static void inserWord(String word,ArrayList<String> natureList){
		if(!getOneDocumentIsExist(word)){
			Document mDocument = new Document();
			mDocument.put("word", word);
			for (int i = 0; i < natureList.size(); i++) {
				mDocument.put("nature" + (i+1), natureList.get(i));
			}
			
			collection.insertOne(mDocument);
		}else {
			Document mDocument = getOneExistDocument(word);
			int size = parseSize(mDocument);
			natureList = removeExistNature(size, mDocument, natureList);
			for(int i = 0  ; i < natureList.size();i++){
				collection.updateOne(Filters.eq("word", word), new Document("$set",new Document("nature" + (i + size + 1), natureList.get(i))));
			}
		}
	}
//	db.course.update({},{$unset:{"lectures.lectures_count":""}},{multi:true})
	public static void deleOneColumn() {
		Document query = new Document("_id", "");
		Document update = new Document();
		update.put("$unset", new Document("word", "炎症性病"));
		collection.updateOne(query, update);
	}
	
	/**
	 * 更新document中指定的nature
	 * @param word
	 * @param nature
	 * @param natureWord
	 */
	public static void updateOneDocument(String word,String nature,String natureWord){
		collection.updateOne(Filters.eq("word", word), new Document("$set",new Document(nature,natureWord)));
	}
	
	
	private static int parseSize(Document mDocument){
		int count = 0;
		for(String key:mDocument.keySet()){
			if(!key.equals("_id") && !key.equals("naNum")){
				if(key.contains("nature")){
					count ++;
				}
			}
		}
		return count;
	}
	
	private static ArrayList<String> removeExistNature(int natureCount,Document mDocument,ArrayList<String> natureList){
		ArrayList<String> newList = (ArrayList<String>) natureList.clone();
		for(int i = 1;i<= natureCount;i++){
			String nature = mDocument.getString("nature" + i);
			for(int m = 0;m < natureList.size();m++){
				String tmpNature = natureList.get(m);
				if(nature.equals(tmpNature)){
					for(int n = 0;n < newList.size();n++){
						if(newList.get(n).toString().equals(nature)){
							newList.remove(n);
						}
					}
				}
			}
		}
		return newList;
	}
	
	public static String format(String value){
		int length = value.length();
		int addEmptyStrSize = 40 - length;
		String string = "";
		for (int i = 0; i < addEmptyStrSize; i++) {
			string +=" ";
		}
		value = value + string;
		return value;
	}
	
	/**
	 * 查询所有的Document信息
	 */
	public static String queryAllDocment(){
		String allStr = "";
		 for (Document cur : collection.find()) {
			 System.out.println("====start===="+ cur.getString("word")+"=======");
			 String str = "";
	            for (String key : cur.keySet()) {
	            	if(!key.equals("_id") && !key.equals("naNum")){
	            		str += "" + key + ":" + format(cur.get(key).toString());
	            	}
				}
	            allStr += str + "\r\n";
	         System.out.println("====end===="+ cur.getString("word")+"=======");  
	       }
		 return allStr;
	}
	
	/**
	 * 查询一个Document
	 * @param word
	 * @return
	 */
	public static String queryOneDocument(String word){
		Document mTmpDoc  = null;
		String allText = "";
		for (Document cur : collection.find()) {
			String string = (String) cur.get("word");
        	if(string.contains(word)){
        		mTmpDoc = cur;
        		String str = "";
    			for (String key : mTmpDoc.keySet()) {
    				if(!key.equals("_id") && !key.equals("naNum")){
    					str += "   " + key + ":" +  format(mTmpDoc.get(key).toString());
    				}
    			}
    			allText += str + "\r\n";
        	}
		}
		return allText;
	}
	
	
	public static MongoCollection<Document> getCollection(String dbName,String tableName){
		MongoDatabase database = getDataBase(dbName);
        collection = database.getCollection(tableName);
        if(callback != null){
        	callback.callBakc();
        }
        return collection;
	}
	
	private static MongoDatabase getDataBase(String dataBaseName){
		return mongoClient.getDatabase(dataBaseName);
	}
	
	/**
	 * 查询数据库中是否已经存在该word的Docment
	 * @param word
	 * @param natureList
	 * @return
	 */
	private static boolean getOneDocumentIsExist(String word){
		
		for (Document doc : collection.find()) {
            String string = (String) doc.get("word");
            	if(string.equals(word)){
            		return true;
            	}
			}
		return false;
	}
	
	/**
	 * 找到数据库中已经存在的Document
	 * @param word
	 * @return
	 */
	private static Document getOneExistDocument(String word){
		for (Document doc : collection.find()) {
			String string = (String) doc.get("word");
         	if(string.equals(word)){
         		return doc;
         	}
        }
		return null;
	}
	
	/**
	 * 删除word的document
	 * @param word
	 */
	public static void DeleteOneWord(String word){
		collection.deleteOne(Filters.eq("word", word));  
	}
	
	/**
	 * 关闭mongoclient
	 */
	public static void closeClient(){
		mongoClient.close();
	}
	
	public interface onLoadDbOK{
		public void callBakc();
	}
	
	public static void setLoadCallBack(onLoadDbOK callBack){
		callback = callBack;
	}
}
