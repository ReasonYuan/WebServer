package com.yiyihealth.ai.dsmain.medicine.wx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JsonParseMain {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		File  mFile = new File("/Users/wangxi/Downloads/DIC5.json");
		FileReader mFileReader = new FileReader(mFile);
		BufferedReader mReader = new BufferedReader(mFileReader);
		String jsonstr = "";
		String oneLine = "";
		while ((oneLine = mReader.readLine()) != null) {
			if(oneLine.equals("}")){
				oneLine += ",";
			}
			
			System.out.println(oneLine);
			if(!oneLine.contains("_id") && !oneLine.contains("naNum")){
				jsonstr +=oneLine;
			}
			
		}
		jsonstr = "[" + jsonstr.substring(0, jsonstr.length() - 1) +"]";
		String json = format(jsonstr); 
		System.out.println(json);
		ExcelByUser.writeYiJuToFile("../dsmain/dict", json, "病历分词字典.json");
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
}
