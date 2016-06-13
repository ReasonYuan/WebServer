package com.yiyihealth.ds.esearch.fol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;

public class MemsDumper {
	
	/**
	 * 导出内存数据，以便以后跟踪错误
	 * @param memDumpFilename
	 */
	public static void dumpMems(AtomManager atomManager, String memDumpFilename) {
		PredicateManager predicateManager = PredicateManager.getInstance();
		FormulaManager formulaManager = FormulaManager.getInstance();
		try {
			// 读取配置文件
			JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("../dsmain/conf/config.json")));
			final String outputFile = config.getString("projectDir") + "/output/" + memDumpFilename;
			ObjectOutputStream bw = new ObjectOutputStream(new FileOutputStream(new File(outputFile)));
			bw.writeObject(atomManager);
			bw.writeObject(formulaManager);
			bw.writeObject(predicateManager);
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ProvedMems loadDumpMems(String memDumpFilename){
		try {
			ProvedMems result = new ProvedMems();
			// 读取配置文件
			JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("../dsmain/conf/config.json")));
			final String outputFile = config.getString("projectDir") + "/output/" + memDumpFilename;
			ObjectInputStream br = new ObjectInputStream(new FileInputStream(new File(outputFile)));
			result.atomManager = (AtomManager) br.readObject();
			result.formulaManager = (FormulaManager) br.readObject();
			result.predicateManager = (PredicateManager) br.readObject();
			br.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
