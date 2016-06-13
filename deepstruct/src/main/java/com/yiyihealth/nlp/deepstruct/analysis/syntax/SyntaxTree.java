package com.yiyihealth.nlp.deepstruct.analysis.syntax;

import java.util.ArrayList;

public class SyntaxTree {

	private SyntaxTrieNode root;
	
	public SyntaxTree(SyntaxTrieNode root) {
		this.root = root;
	}
	
	public void addPredicate(ArrayList<String> natureNames, String functionName) {
		root.addSubNodes(natureNames, functionName);
	}
	
	public SyntaxTrieNode getRoot() {
		return root;
	}
	
}
