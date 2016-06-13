package com.yiyihealth.ai.dsmain.logic;

import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public abstract class LogicFlow {
	
	protected InherenceEngine engine;
	protected String projectDir;
	
	/**
	 * 
	 * @param engine 推理引擎
	 */
	public LogicFlow(String projectDir, InherenceEngine engine) {
		this.engine = engine;
		this.projectDir = projectDir;
	}

	public abstract void inherence();
	
	public InherenceEngine getEngine() {
		return engine;
	}
	
	public String getProjectDir() {
		return projectDir;
	}
	
}
