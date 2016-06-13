package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.MatchResult;
import com.yiyihealth.nlp.deepstruct.dict.EWord;

public abstract class OnResultAction {
	
	protected String actionName;
	
	protected String actionCmd;
	
	protected String tag;
	
	public OnResultAction(String actionName, String actionCmd, String tag) {
		this.actionCmd = actionCmd;
		this.actionName = actionName;
		this.tag = tag;
	}
	
	public abstract void doAction(ArrayList<EWord> words, MatchResult matchResult, SearchOffset offset);
	
}
