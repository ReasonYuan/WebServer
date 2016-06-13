package com.yiyihealth.ai.dsmain.medicine.wx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.FileUtils;

import java.util.StringTokenizer;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class ParseStrutsConfig {
	private static final String parseRule =  "=>";
	/**
	 * 读取解析配置文件
	 * @param path
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> readConfigFile(String path,String fileName) throws IOException{
		ArrayList<String> configStringList = new ArrayList<>();
		File mFile = new File(path);
		if(!mFile.exists()){
			mFile.mkdirs();
		}
		File mFile2 = new File(path + fileName);
		if(!mFile.exists()){
			mFile2.createNewFile();
		}
		
		FileInputStream mInputStream = new FileInputStream(mFile2);
		InputStreamReader mReader = new InputStreamReader(mInputStream,"UTF-8");
		BufferedReader mBufferedReader =  new BufferedReader(mReader);
		String oneLine = "";
		while((oneLine = mBufferedReader.readLine())!= null){
//			System.out.println(oneLine);
			configStringList.add(oneLine);
		}
		mBufferedReader.close();
		mReader.close();
		mInputStream.close();
		return configStringList;
	}
	
	/**
	 * 将每一行文字拆分开
	 * @param oneLine
	 */
	public static ArrayList<String> parseOneLineString(String oneLine){
		ArrayList<String> listStr = new ArrayList<>();
		String s = new String(oneLine);
		StringTokenizer st = new StringTokenizer(s,parseRule,false);
		String string = "";
		while( st.hasMoreElements() ){
			string = st.nextToken();
			listStr.add(string);
		}
		return listStr;
	}
	
	/**
	 * 将一个string按规则拆开
	 * @param oneLine
	 * @param rules
	 */
	public static ArrayList<String> parseOneLineString(String oneLine,String rules){
		ArrayList<String> listStr = new ArrayList<>();
		String s = new String(oneLine);
		StringTokenizer st = new StringTokenizer(s,rules,false);
		String string = "";
		//目前只考虑了当前这种比较简单的格式
		while( st.hasMoreElements() ){
			string = st.nextToken();
			listStr.add(string);
		}
//		System.out.println(string);
		return listStr;
	}
	
	
	public static void search(InherenceEngine engine) throws ClassNotFoundException, IOException{
		JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
		final String projectDir = config.getString("projectDir");
		ArrayList<String> configStringList = readConfigFile(projectDir + "/struct/", "struct.txt");
		
		ArrayList<StructObject> objects = new ArrayList<>();
		for (int i = 0; i < configStringList.size(); i++) {
			StructObject mObject = new StructObject();
			ArrayList<String> list = parseOneLineString(configStringList.get(i));//解析每一行文字
			for (int j = 0; j < list.size(); j++) {
				ArrayList<String> oneLineList = parseOneLineString(list.get(j));//一行文字第二次拆开
				String left = oneLineList.get(0).substring(0, oneLineList.get(0).indexOf("("));
				String right = oneLineList.get(0).substring(oneLineList.get(0).indexOf("("), oneLineList.get(0).length());
				ArrayList<String> rightLineList =   parseOneLineString(right, "()");
					if(j == 0){
						ArrayList<String> rightLine =  parseOneLineString(rightLineList.get(0), ",");
						ArrayList<String> predicateParams = getPredicateParams(rightLine);
						mObject.setPredicateName(left);
						mObject.setPredicateParams(predicateParams);
					}else{
						mObject.setPridicateFuc(left);
						mObject.setResult(rightLineList.get(0));
					}
			}
			objects.add(mObject);
		}
		searchStruct(objects,engine);
	}
	
	private static ArrayList<String> getPredicateParams(ArrayList<String> paramsList){
		ArrayList<String> params = new ArrayList<>();
		for (int i = 0; i < paramsList.size(); i++) {
			String str  = paramsList.get(i).trim();
			if(!str.equals("")){
				params.add(str);
			}
		}
		return params;
	}
	
	/**
	 * 根据配置文件查询结果
	 * @param objects
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void searchStruct(ArrayList<StructObject> objects,InherenceEngine engine) throws ClassNotFoundException, IOException{
		for (int i = 0; i < objects.size(); i++) {
			StructObject structObject = objects.get(i);
			String name = structObject.getPredicateName();
			SearchWord[] searchParams = new SearchWord[structObject.getSearchPamras().size()];
//			SearchWord[] results = new SearchWord[structObject.getResultPamras().size()];
			structObject.getSearchPamras().toArray(searchParams);
			ArrayList<SearchWord> results = structObject.getResultPamras();
			
			JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
			final String projectDir = config.getString("projectDir");
			ArrayList<ArrayList<SearchWord>> allSearList = structParamsGroup(searchParams,projectDir + "/struct/Synonyms.json");
			
			
			HashMap<String, Integer> searchWordMap = new HashMap<>();
			HashMap<String, Integer> resultsWordMap = new HashMap<>();
			for (int m = 0; m < allSearList.size(); m++) {
				String string = "";
				for (int n = 0; n < allSearList.get(m).size(); n++) {
					string += "   " + allSearList.get(m).get(n).getSearchWord();
					searchWordMap.put(allSearList.get(m).get(n).getSearchWord(), allSearList.get(m).get(n).getWordPos());
				}
				System.out.println("查找的内容" + string);
			}
			
			for (int j = 0; j < results.size(); j++) {
				resultsWordMap.put(results.get(j).getSearchWord(), results.get(j).getWordPos());
			}
			
			resultsWordMap  = (HashMap<String, Integer>) sortMapByValue(resultsWordMap);
			ArrayList<ArrayList<String>> resultsList = engine.queryAtom(name,2,searchWordMap,resultsWordMap);
			String resultStr = "";
			for (int j = 0; j < resultsList.size(); j++) {
				ArrayList<String> oneList  = resultsList.get(j);
				TestItemObject mItemObject = new TestItemObject();
				mItemObject.setName(oneList.get(0));
				mItemObject.setValue(oneList.get(2) + oneList.get(3));
				mItemObject.setException(oneList.get(4));
				mItemObject.setDate(oneList.get(1));
				mItemObject.setBelongSentence(oneList.get(5));
				resultStr = resultsList.get(j).toString() + "   ";
				System.out.println("-----results-----" + mItemObject.toString());
			}

			
//			structObject.setResult(result);
		}
	}
	
	public static Map<String, Integer> sortMapByValue(Map<String, Integer> oriMap) {  
	    Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();  
	    if (oriMap != null && !oriMap.isEmpty()) {  
	        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(oriMap.entrySet());  
	        Collections.sort(entryList,  
	                new Comparator<Map.Entry<String, Integer>>() {  
	                    public int compare(Entry<String, Integer> entry1,  
	                            Entry<String, Integer> entry2) {  
	                        int value1 = 0, value2 = 0;  
	                        try {  
	                            value1 = entry1.getValue();  
	                            value2 = entry2.getValue();  
	                        } catch (NumberFormatException e) {  
	                            value1 = 0;  
	                            value2 = 0;  
	                        }  
	                        return value1 - value2;  
	                    }  
	                });  
	        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();  
	        Map.Entry<String, Integer> tmpEntry = null;  
	        while (iter.hasNext()) {  
	            tmpEntry = iter.next();  
	            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());  
	        }  
	    }  
	    return sortedMap;  
	} 
	
	/**
	 * 构造查询参数的可能组合
	 * @param params
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static ArrayList<ArrayList<SearchWord>> structParamsGroup(SearchWord[] params,String path) throws ClassNotFoundException, IOException{
		String jsonStr  = readFile(path);
		JSONObject mObject = JSONObject.parseObject(jsonStr);
		ArrayList<ArrayList<SearchWord>> searchList = new ArrayList<>();
		for(int i = 0;i < params.length;i++){
			ArrayList<SearchWord> list = new ArrayList<>();
			SearchWord param = params[i];
			JSONArray array = mObject.getJSONArray(param.getSearchWord());
			list.add(param);
			if(array != null){
				for (int j = 0; j < array.size(); j++) {
					SearchWord word = new SearchWord();
					word.setSearchWord(array.getString(j));
					word.setWordPos(param.getWordPos());
					list.add(word);
				}
			}
			searchList.add(list);	
		}
		ArrayList<ArrayList<SearchWord>> allSearList = new ArrayList<>();
		ArrayList<SearchWord> tmpList = null;
		
		for (int i = 0; i < searchList.size(); i++) {
			int size = searchList.get(i).size();
			if(size > 1){
				ArrayList<ArrayList<SearchWord>> tmpAddAllSearList  = new ArrayList<>();
				for (int j = 0; j < searchList.get(i).size(); j++) {
					SearchWord word = searchList.get(i).get(j);
//					System.out.println("-------" + searchList.get(i).get(j).getSearchWord());
					if(allSearList.size() == 0){
						tmpList = new ArrayList<>();
						tmpList.add(word);
						tmpAddAllSearList.add(tmpList);
					}else{
						ArrayList<ArrayList<SearchWord>> tmpAllSearList = cloneArrayList(allSearList);
						for (int m = 0; m < tmpAllSearList.size(); m++) {
							tmpList =  tmpAllSearList.get(m);
							tmpList.add(word);
						}
						tmpAddAllSearList.addAll(tmpAllSearList);
					}
				}
				
				if(tmpAddAllSearList.size() != 0){
					allSearList.clear();
					allSearList.addAll(tmpAddAllSearList);
				}
				
			}else{
				SearchWord word = searchList.get(i).get(0);
				if(allSearList.size() == 0){
					tmpList = new ArrayList<>();
					tmpList.add(word);
					allSearList.add(tmpList);
				}else{
					for (int m = 0; m < allSearList.size(); m++) {
						tmpList =  allSearList.get(m);
						tmpList.add(word);
					}
				}
				
			}
			
		}
		return allSearList;
	}
	
	/**
	 * 深拷贝arraylist
	 * @param list
	 * @return
	 */
	public static ArrayList<ArrayList<SearchWord>> cloneArrayList(ArrayList<ArrayList<SearchWord>> list){
		ArrayList<ArrayList<SearchWord>> tmplist = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			ArrayList<SearchWord> tmp = new ArrayList<>();
			for (int j = 0; j < list.get(i).size(); j++) {
				tmp.add(list.get(i).get(j));
			}
			tmplist.add(tmp);
		}
		return tmplist;
	}
	
	/**
	 * 读取查询配置文件
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String path) throws IOException{
		File  mFile = new File(path);
		FileReader mFileReader = new FileReader(mFile);
		BufferedReader mReader = new BufferedReader(mFileReader);
		String jsonstr = "";
		String oneLine = "";
		while ((oneLine = mReader.readLine()) != null) {
			jsonstr += oneLine;
		}
		mReader.close();
		mFileReader.close();
		return jsonstr;
	}
}
