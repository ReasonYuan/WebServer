package com.yiyihealth.ai.dsmain.logic;

import java.util.ArrayList;

import com.yiyihealth.ds.esearch.fol.Formula;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class TimeLineLogic extends ContextLogic {

	public TimeLineLogic(String projectDir, InherenceEngine engine, ArrayList<Formula> formulas) {
		super(projectDir, engine, formulas);
	}

}
