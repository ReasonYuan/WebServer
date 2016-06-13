package com.yiyihealth.ai.dsmain.nlp;

public abstract class RecordParser {

	/**
	 * 
	 * @param projectDir 项目目录
	 * @param outputWordsDir nlp parser结束后的目录
	 */
	public abstract void parseWord(String projectDir) throws Exception ;
	
}
