package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.MatchResult;
import com.yiyihealth.nlp.deepstruct.dict.EWord;

public class OnLabelAction extends OnResultAction {

	public OnLabelAction(String actionName, String actionCmd, String tag) {
		super(actionName, actionCmd, tag);
	}

	@Override
	public void doAction(ArrayList<EWord> words, MatchResult matchResult, SearchOffset offset) {
		
	}

}
