package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.MatchResult;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class OnCombineAction extends OnResultAction {
	
	private String[] express = null;
	
	public OnCombineAction(String actionName, String actionCmd, String tag) {
		super(actionName, actionCmd, tag);
		char[] chars = actionCmd.toCharArray();
		StringBuffer sBuffer = new StringBuffer();
		ArrayList<String> tokens = new ArrayList<>();
		for (int i = 0; i < chars.length; i++) {
			switch (chars[i]) {
			case '+':
				tokens.add(sBuffer.toString());
				tokens.add("+");
				sBuffer.setLength(0);
				break;
			case '=':
				tokens.add(sBuffer.toString());
				tokens.add("=");
				sBuffer.setLength(0);
				break;
			default:
				sBuffer.append(chars[i]);
				break;
			}
		}
		if (sBuffer.length() > 0) {
			tokens.add(sBuffer.toString());
		}
		express = new String[tokens.size()];
		tokens.toArray(express);
	}

	@Override
	public void doAction(ArrayList<EWord> words, MatchResult matchResult, SearchOffset offset) {
		if (matchResult.matchFailed) {
			//do nothing
			return;
		}
		String[] values = new String[express.length];
		int startPos = Integer.MAX_VALUE;
		ArrayList<Term> removable = new ArrayList<Term>();
		int replaceAt = -1;
		for (int i = 0; i < express.length; i++) {
			if (express[i].equals("=")) {
				break;
			}
			if (!express[i].equals("+")) {
				//variables
				for (int j = 0; j < matchResult.variables.length; j++) {
					if (express[i].equals(matchResult.variables[j])) {
						if (replaceAt == -1) {
							replaceAt = j;
						}
						Term term = (Term)words.get(j);
						if (term.getNature().equals(WordNatures.PUNC)) {
							//匹配不健全，把标点符号也匹配进来了
							//TODO 这里一定要处理，目前不能合并包括标点符号的内容
							return;
						}
						if(values[i] == null){
							removable.add(term);
							values[i] = term.getWord();
							startPos = Math.min(startPos, term.startPos);
						} else {
							removable.add(term);
							values[i] += term.getWord();
							startPos = Math.min(startPos, term.startPos);
						}
					}
				}
			}
		}
		for (int i = 0; i < values.length; i++) {
			if (express[i].equals("+")) {
				values[i+1] = (values[i-1] == null ? "" : values[i-1]) + (values[i+1] == null ? "" : values[i+1]);
			} else if (express[i].equals("=")) {
				values[i+1] = values[i-1];
			}
		}
		Term newTerm = new Term(values[values.length-1], WordNatures.getNatureByReadableName(express[express.length-1]), true, startPos);
		words.removeAll(removable);
		words.add(replaceAt, newTerm);
		NewWordDict.getInstance().addNewWord(newTerm.getWord(), newTerm.getNature(), tag);
		//System.out.println("new term: " + newTerm);
		
		//TODO 实现验证逻辑，对新词的可能性进行推理
		HealthAnalysis.addNormalWord(newTerm.getWord(), newTerm.getNature());
	}

}
