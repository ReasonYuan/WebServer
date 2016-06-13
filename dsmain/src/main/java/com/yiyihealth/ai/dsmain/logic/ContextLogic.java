package com.yiyihealth.ai.dsmain.logic;

import java.util.ArrayList;

import com.yiyihealth.ds.esearch.fol.Formula;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class ContextLogic extends LogicFlow {
	
	private ArrayList<Formula> formulas;
	
	private boolean called = false;
	
	public ContextLogic(String projectDir, InherenceEngine engine, ArrayList<Formula> formulas) {
		super(projectDir, engine);
		this.formulas = formulas;
	}

	@Override
	public void inherence() {
		if (!called) {
			called = true;
			for (int i = 0; i < this.formulas.size(); i++) {
				for(Formula formula : this.formulas){
					engine.addFormula(formula);
				}
			}
			engine.doInherence();
		} else {
			throw new RuntimeException("目前仅支持调用一次!!!");
		}
	}

}
