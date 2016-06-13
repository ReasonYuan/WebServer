package com.yiyihealth.ds.esearch.fol;

import java.io.Serializable;
import java.util.ArrayList;

public class PredicateManager implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6509274699467656454L;

	private ArrayList<Predicate> predicates = new ArrayList<Predicate>();
	
	private static PredicateManager _instance = null;

	public static PredicateManager getInstance(){
		if (_instance == null) {
			_instance = new PredicateManager();
		}
		return _instance;
	}
	
	public Predicate getPredicateByName(String name){
		for (int i = 0; i < predicates.size(); i++) {
			if (predicates.get(i).getName().equals(name)) {
				return predicates.get(i);
			}
		}
		return null;
	}
	
	public void addPredicate(Predicate predicate){
		if (getPredicateByName(predicate.getName()) == null) {
			predicates.add(predicate);
		}
	}
	
	
	
}
