package com.yiyihealth.ai.dsmain.exports;

import java.util.ArrayList;

import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class DiagExportModule extends ExportModule {

	@Override
	public ArrayList<Object> export(InherenceEngine engine) {
		return ExporterUtils.getDiagList(engine);
	}

}
