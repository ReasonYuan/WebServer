package com.yiyihealth.ds.esearch.syntaxtrie;

public class NatureRuleMatcher extends SynSeqRuleMatcher {
	
	private int offset = 0;
	
	private int matchAnyIdx = -1;
	
	private String nature2Inherence;
	
	public NatureRuleMatcher(SyntaxRule rule, String nature2Inherence) {
		super(rule);
		for (int i = 0; i <  rule.nodes.size(); i++) {
			if (nature2Inherence.equals(rule.nodes.get(i).nature)) {
				matchAnyIdx = i;
				break;
			}
		}
		this.nature2Inherence = nature2Inherence;
	}
	
	public int getBackOffset() {
		return rule.nodes.size() - 1 - matchAnyIdx;
	}
	
	public String getNature2Inherence() {
		return nature2Inherence;
	}
	
	public String getNatureSeqAroundStr(){
		return rule.getNatureSeqAroundStr(matchAnyIdx);
	}
	
	@Override
	public void reset() {
		offset = 0;
	}
	
	@Override
	public boolean fullMatched() {
		return offset == rule.nodes.size();
	}
	
	/**
	 * 这一轮刚做过Any Match
	 * @return
	 */
	public boolean isJustMatchedAny(){
		return offset - 1 == matchAnyIdx;
	}
	

	@Override
	public boolean nextWord(String word, String nature) {
		//只关心nature
		boolean result = false;
		if (offset < rule.nodes.size()) {
			if (offset == matchAnyIdx || nature.equals(rule.nodes.get(offset).nature)) {
				offset++;
				result = true;
			}
		}
		return result;
	}
	
}
