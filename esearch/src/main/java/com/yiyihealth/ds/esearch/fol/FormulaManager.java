package com.yiyihealth.ds.esearch.fol;

import java.io.Serializable;
import java.util.ArrayList;

public class FormulaManager implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2682267327622598671L;

	private static FormulaManager _instance = null;
	
	private ArrayList<Formula> formulas = new ArrayList<Formula>();
	
	public static FormulaManager getInstance(){
		if (_instance == null) {
			_instance = new FormulaManager();
		}
		return _instance;
	}

	public void addFormula(Formula formula){
		formulas.add(formula);
	}
	
}
