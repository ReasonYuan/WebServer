package com.yiyihealth.nlp.deepstruct.utils;

import java.io.FileOutputStream;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.dict.Paper;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class JSONUtil {
	
    public static void writeJson4ESearch(JSONObject jsonObject, String documentID, String dir){
    	try {
			FileOutputStream fOutputStream = new FileOutputStream(dir + documentID + ".json");
			fOutputStream.write(JSONObject.toJSONString(jsonObject, true).getBytes());
			fOutputStream.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
	
	/**
	 * 把一份病历放在一个一维数组里
	 * @param paper
	 * @param jsonArray
	 */
    public static void toJson(Paper paper, JSONArray jsonArray){
    	ArrayList<Term> words = paper.getWords();
		for (int k = 0; k < words.size(); k++) {
			JSONObject item = new JSONObject();
			Term word = words.get(k);
			item.put("word", word.getWord());
			item.put("nature", word.getNature());
			item.put("natureChinese", WordNatures.getNatureReadableName(word.getNature()));
			item.put("natureFull", WordNatures.getNatureFullByName(word.getNature()));
			item.put("isTimeline", word.isTimeline);
			if (word.shouldCheckTimeContext()) {
				item.put("checkTimeContext", true);
				item.put("timeContextType", word.getTimeformat());
			}
			if (word.getAttributes().size() > 0) {
				item.put("attributes", word.getAttributes());
			}
			if (word.getNormalDate() != null) {
				item.put("normalDate", word.getNormalDate());
			}
			jsonArray.add(item);
		}
    }
    
}
