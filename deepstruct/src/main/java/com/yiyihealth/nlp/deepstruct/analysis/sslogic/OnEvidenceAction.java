package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.MatchResult;
import com.yiyihealth.nlp.deepstruct.dict.EWord;

public class OnEvidenceAction extends OnResultAction {
	
	private String evidenceName;
	
	private String[] params;
	
	private EvidenceWritter writter;
	
	/**
	 * 特殊参数，需要替换成句的为止
	 */
	private static final String SENTENCE_POS = "SENTENCE_POS";
	
	public OnEvidenceAction(String actionName, String actionCmd, String tag) {
		super(actionName, actionCmd, tag);
		StringTokenizer tokenizer = new StringTokenizer(actionCmd, "/");
		String[] tokens = new String[tokenizer.countTokens()];
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokenizer.nextToken();
		}
		evidenceName = tokens[0];
		params = new String[tokens.length - 1];
		for (int i = 1; i < tokens.length; i++) {
			params[i-1] = tokens[i];
		}
	}

	public void setWritter(EvidenceWritter writter) {
		this.writter = writter;
	}
	
	@Override
	public void doAction(ArrayList<EWord> words, MatchResult matchResult, SearchOffset offset) {
		//TODO 可能需要重构，不一定处理器是writter
		if (writter != null) {
			String[] ps = new String[params.length];
			System.arraycopy(params, 0, ps, 0, params.length);
			if (!matchResult.matchFailed) {
				for (int i = 0; i < matchResult.variables.length; i++) {
					if (matchResult.variables[i] != null && matchResult.variables[i].length() > 0) {
						boolean found = false;
						for (int j = 0; j < ps.length; j++) {
							if (matchResult.variables[i].equals(ps[j])) {
								ps[j] = "" + i;
								found = true;
								break;
							}
						}
						if (!found) {
							throw new RuntimeException("定义的变量找不到参数匹配: " + matchResult.variables[i] + ", at: " + i);
						}
					}
				}
			}
			
			String paramExpress = "(";
			for (int i = 0; i < ps.length; i++) {
				if (i > 0) {
					paramExpress += ", ";
				}
				if (ps[i].equals(SENTENCE_POS)) {
					ps[i] = "" + (matchResult.sentencePos + offset.sentenceOffset);
				}
				paramExpress += ps[i];
			}
			paramExpress += ')';
			
			writter.addEvidence(evidenceName + paramExpress);
		}
	}

}
