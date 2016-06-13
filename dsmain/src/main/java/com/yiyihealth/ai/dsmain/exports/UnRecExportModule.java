package com.yiyihealth.ai.dsmain.exports;

import java.util.ArrayList;

import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class UnRecExportModule extends ExportModule {

	@Override
	public ArrayList<Object> export(InherenceEngine engine) {
		ArrayList<Object> list = new ArrayList<>();
		list.add(ExporterUtils.getUndefAndUnRecToJson(engine));
		return list;
	}

}
