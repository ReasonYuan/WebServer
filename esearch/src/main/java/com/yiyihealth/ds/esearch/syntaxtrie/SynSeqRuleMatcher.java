package com.yiyihealth.ds.esearch.syntaxtrie;

public abstract class SynSeqRuleMatcher {

	protected SyntaxRule rule;

	public SynSeqRuleMatcher(SyntaxRule rule) {
		this.rule = rule;
	}
	
	public SyntaxRule getRule() {
		return rule;
	}
	
	public abstract void reset();
	
	/**
	 * 是否已完整匹配这个规则
	 * @return
	 */
	public abstract boolean fullMatched();
	
	/**
	 * 匹配下一个词
	 * @param word
	 * @param nature
	 * @return
	 */
	public abstract boolean nextWord(String word, String nature);

}
