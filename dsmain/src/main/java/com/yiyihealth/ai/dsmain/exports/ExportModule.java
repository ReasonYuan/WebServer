package com.yiyihealth.ai.dsmain.exports;

import java.util.ArrayList;

import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public abstract class ExportModule {

	public abstract ArrayList<Object> export(InherenceEngine engine);
	
}
