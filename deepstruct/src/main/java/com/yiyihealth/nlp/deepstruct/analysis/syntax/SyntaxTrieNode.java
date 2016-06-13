package com.yiyihealth.nlp.deepstruct.analysis.syntax;

import java.util.ArrayList;
import java.util.Hashtable;

public class SyntaxTrieNode {
	
	public static final String ANY = "*";
	
	private String natureName;
	
	protected SyntaxTrieNode parent;
	
	private int depth = 0;
	
	protected boolean isPredicateLeaf = false;
	
	private String functionName;

	protected Hashtable<String, SyntaxTrieNode> children = new Hashtable<String, SyntaxTrieNode>();
	
	protected SyntaxTrieNode(String natureName){
		this.natureName = natureName;
	}
	
	public void addSubNodes(ArrayList<String> nodeNatureNames, String functionName){
		String nextName = nodeNatureNames.remove(0);
		if (nextName.equals("＊")) {//中文换成英文
			nextName = ANY;
		}
		SyntaxTrieNode nextNode = children.get(nextName);
		if (nextNode == null) {
			nextNode = new SyntaxTrieNode(nextName);
			nextNode.parent = this;
			nextNode.depth = depth + 1;
			children.put(nextName, nextNode);
		}
		if (nodeNatureNames.size() > 0) {
			nextNode.addSubNodes(nodeNatureNames, functionName);
		} else {
			nextNode.functionName = functionName;
			nextNode.isPredicateLeaf = true;
		}
	}
	
	/**
	 * 
	 * @param word 暂未实现
	 * @param nature
	 * @return
	 */
	public SyntaxTrieNode nextWord(String word, String nature){
		SyntaxTrieNode next = children.get(natureName);
		if (children.get(natureName) != null) {
			return next;
		} else if (children.get(ANY) != null) {
			return children.get(ANY);
		} else if(natureName.equals(ANY)){
			return this;
		} else {
			return null;
		}
	}
	
	public String getNatureName() {
		return natureName;
	}
	
	public String getFunctionName() {
		return functionName;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public Hashtable<String, SyntaxTrieNode> getChildren() {
		return children;
	}
	
	@Override
	public String toString() {
		SyntaxTrieNode p = parent;
		String names = natureName;
		while (p != null && p.depth != 0) {
			names = p.natureName + " > " + names;
			p = p.parent;
		}
		return names;
	}
}
