package com.yiyihealth.nlp.deepstruct.dict.trie.hitales;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Word;

/**
 * 医疗健康词典森林
 * @author qiangpeng
 *
 */
public class Forest<T extends CharNode> {
	
	private T root;
	
	public Forest(T root) {
		this.root = root;
	}

	public T getRoot() {
		return root;
	}

//	public void addWord(String word){
//		root.addNode(word);
//	}
	
	public TrieNode addWord(Word word){
		TrieNode node = root.addNode(word.getText());
		node.setIsWordEnd(true);
		node.setDictWord(word);
		return node;
	}
	
	public void deleteWord(String word){
		root.deleteSubNode(word);
	}
	
	/**
	 * 返回单词在树中的末节点
	 * @param word
	 * @return
	 */
	public TrieNode getWordEndNode(String word){
		return root.getNode(word);
	}

	public boolean searchWord(String word){
		return root.getNode(word) != null;
	}
	
	public ArrayList<String> listWords(){
		ArrayList<String> results = new ArrayList<String>();
		root.getWordsOfChildren(results);
		return results;
	}
	
	public void debugDump(boolean full){
		root.debugDump(full);
	}
}
