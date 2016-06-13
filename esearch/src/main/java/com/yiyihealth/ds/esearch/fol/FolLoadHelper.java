package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;

import com.yiyihealth.ds.esearch.fol.FolParser.FormulaOrPredicate;

public class FolLoadHelper {

	public static class LoadResult {
		public ArrayList<Predicate> predicates;
		public ArrayList<Formula> formulas;
	}
	
	public static LoadResult load(String logicFile){
		LoadResult result = new LoadResult();
		LogicLoader<FolParser, FormulaOrPredicate> formulaLoader = new LogicLoader<FolParser, FormulaOrPredicate>(
				new FolParser());
		ArrayList<FormulaOrPredicate> folOrPres = formulaLoader.loadLogics(logicFile);
		Object[] fs = (Object[]) folOrPres.toArray();
		ArrayList<Formula> formulas = new ArrayList<Formula>();
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		for (int i = 0; i < fs.length; i++) {
			FormulaOrPredicate formulaOrPredicate = (FormulaOrPredicate) fs[i];
			if (formulaOrPredicate.formula != null) {
				formulas.add(formulaOrPredicate.formula);
			} else if (formulaOrPredicate.predicate != null) {
				predicates.add(formulaOrPredicate.predicate);
				PredicateManager.getInstance().addPredicate(formulaOrPredicate.predicate);
			}
		}
		result.formulas = formulas;
		result.predicates = predicates;
		return result;
	}
	
	public static ArrayList<Predicate> findIllegalEvidences(ArrayList<Predicate> evidences,
			ArrayList<Predicate> predicates) {
		ArrayList<Predicate> illegalEvidences = new ArrayList<Predicate>();
		for (int j = 0; j < evidences.size(); j++) {
			Predicate evidence = evidences.get(j);
			boolean legal = false;
			for (int i = 0; i < predicates.size(); i++) {
				if (predicates.get(i).getName().equals(evidence.getName())) {
					if (predicates.get(i).getParams().size() == evidence.getParams().size()) {
						legal = true;
						break;
					}
				}
			}
			if (!legal) {
				illegalEvidences.add(evidence);
			}
		}
		return illegalEvidences;
	}
	
}
