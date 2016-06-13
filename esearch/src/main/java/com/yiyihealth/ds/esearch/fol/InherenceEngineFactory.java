package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;

public class InherenceEngineFactory {

	/**
	 * 创建推理引擎
	 * @param evidenceFile 证据文件
	 * @param dbFile	病历数据文件
	 * @param formulas	公式
	 * @param predefinePredicates 预定义的谓词, 用来规范语法
	 * @return
	 */
	public static InherenceEngine create(String evidenceFile, String dbFile, ArrayList<Formula> formulas, ArrayList<Predicate> predefinePredicates){
		LogicLoader<PredicateParser, Predicate> evidencesLoader = new LogicLoader<PredicateParser, Predicate>(
				new PredicateParser());
		//带问好的谓词直接也可以是不带问号的
		ArrayList<Predicate> predefinedUnsureOrNot = new ArrayList<Predicate>();
		ArrayList<String> prenames = new ArrayList<String>();
		for(Predicate predicate : predefinePredicates){
			if (predicate.getName().endsWith("?")) {
				Predicate preSure = new Predicate();
				preSure.setName(predicate.getName().substring(0, predicate.getName().lastIndexOf("?")));
				preSure.getParams().addAll(predicate.getParams());
				predefinedUnsureOrNot.add(preSure);
			} else {
				//同理，不带问号的都可以加一个问号，验证合法性时才能仍然通过
				Predicate preUnsure = new Predicate();
				preUnsure.setName(predicate.getName() + "?");
				preUnsure.getParams().addAll(predicate.getParams());
				predefinedUnsureOrNot.add(preUnsure);
			}
			prenames.add(predicate.getName());
		}
		for(Predicate predicate : predefinedUnsureOrNot){
			if (!prenames.contains(predicate.getName())) {
				predefinePredicates.add(predicate);
			}
		}
		
		ArrayList<Predicate> evidences = evidencesLoader.loadLogics(evidenceFile);
		ArrayList<Predicate> illegalEvidences = FolLoadHelper.findIllegalEvidences(evidences, predefinePredicates);
		if (illegalEvidences.size() > 0) {
			System.out.println("illegal evidences: " + illegalEvidences);
			throw new RuntimeException("证据谓词不合法!");
		}
		// TODO 必须验证公式里的谓词也符合谓词定义
		InherenceEngine engine = new InherenceEngine(formulas, evidences, dbFile);
		return engine;
	}
	
}
