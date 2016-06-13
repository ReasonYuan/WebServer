package com.yiyihealth.nlp.deepstruct.dict;

import java.util.ArrayList;

public class Path {
	
	public ArrayList<PathNode> wordClasses;
	
	public PathNode getNode(int index){
		return wordClasses.get(index);
	}
	
	public PathNode first(){
		return getNode(0);
	}
	
	public PathNode last(){
		return getNode(wordClasses.size()-1);
	}
		
	public void addNode(WordNature wordClass, Word word){
		PathNode node = new PathNode(wordClass, word);
		if (wordClasses.size() > 0) {
			PathNode pre = wordClasses.get(wordClasses.size() - 1);
			pre.next = node;
			node.previous = pre;
		}
		wordClasses.add(node);
	}
	
}

class PathNode {
	
	PathNode(WordNature wordClass, Word word){
		this.word = word;
		this.wordClass = wordClass;
	}
	
	WordNature wordClass;
	
	Word word;
	
	PathNode previous;
	
	PathNode next;
	
}

