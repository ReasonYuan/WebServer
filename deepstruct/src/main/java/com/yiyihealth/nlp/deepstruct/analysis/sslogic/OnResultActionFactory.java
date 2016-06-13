package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

public class OnResultActionFactory {

	public static OnResultAction createAction(String actionName, String actionContent, String tag) {
		if (actionName.equals("combine")) {
			return new OnCombineAction(actionName, actionContent, tag);
		} if (actionName.equals("evidence")) {
			return new OnEvidenceAction(actionName, actionContent, tag);
		}
		return null;
	}
	
}
