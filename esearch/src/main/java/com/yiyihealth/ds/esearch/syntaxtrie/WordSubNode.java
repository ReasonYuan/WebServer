package com.yiyihealth.ds.esearch.syntaxtrie;

import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;

public class WordSubNode extends CharNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2907086587461214895L;
	/**
	 * 统计该节点作为叶节点出现的次数
	 */
	private int counter = 0;
	
	public int getCounter() {
		return counter;
	}

	public void increaseCounter() {
		this.counter++;
	}
	
	@Override
	protected CharNode createNewNode() {
		return new WordSubNode();
	}
	
}
