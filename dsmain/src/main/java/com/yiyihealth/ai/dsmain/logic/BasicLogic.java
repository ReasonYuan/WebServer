package com.yiyihealth.ai.dsmain.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ds.esearch.fol.FolLoadHelper;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class BasicLogic extends LogicFlow {
	
	private ArrayList<LogicFlow> nextLogics = new ArrayList<LogicFlow>();
	
	private boolean called = false;

	public BasicLogic(String projectDir, InherenceEngine engine) {
		super(projectDir, engine);
	}

	@Override
	public void inherence() {
		engine.doInherence();
		if (!called) {
			called = true;
//			final String[] contextLogics = {
//					projectDir + "/beliefbase/tag_admission_date.logic"
//				};
			JSONObject config;
			try {
				config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
				final String contextPath = config.getString("contextLogics");
				JSONArray contexts = JSONArray.parseArray(FileUtils.fileRead(new File(projectDir + "/"+ contextPath)));
				final String[] contextLogics = getJsonToStringArray(contexts);
				
				for (int i = 0; i < contextLogics.length; i++) {
					FolLoadHelper.LoadResult result = FolLoadHelper.load(projectDir + "/" +contextLogics[i]);
					ContextLogic logic = new ContextLogic(projectDir, engine, result.formulas);
					nextLogics.add(logic);
					logic.inherence();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("目前仅支持调用一次!!!");
		}
	}
	
	 /**
       * 将json数组转化为String型
       * @param str
	   * @return
	   */
	public static String[] getJsonToStringArray(JSONArray jsonArray) {
		String[] arr=new String[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			arr[i] = jsonArray.getString(i);
			System.out.println(arr[i]);
		}
		return arr;
	}
}
