package com.yiyihealth.ai.dsmain.exports;

import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public abstract class Exporter {

	/**
	 * @param engine
	 * @param filename - 如果实现不需要，可为null
	 */
	public abstract void export(InherenceEngine engine, String filename);
	
}
