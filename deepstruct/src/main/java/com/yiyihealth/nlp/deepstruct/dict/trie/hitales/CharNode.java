package com.yiyihealth.nlp.deepstruct.dict.trie.hitales;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import com.yiyihealth.nlp.deepstruct.dict.Word;

public class CharNode implements TrieNode, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6050779841885810591L;

	/**
	 * 该节点所代表的字符
	 */
	protected char c;
	
	protected boolean isWordEnd = false;
	
	private CharNode parent;
	
	protected int depth = 0;
	
	protected Word dictWord;
	
	/**
	 * 子节点，用hash表保存, 以后优化算法
	 */
	Hashtable<Character, CharNode> children = new Hashtable<>();
	
	/**
	 * 无意义的char
	 */
	public static final char MEANINGLESSCHAR = 4;
	/**
	 * 往前多看一个字符, 
	 */
	protected char forewardOneChar = MEANINGLESSCHAR;
	
	/**
	 * 递归查找isWordEnd＝true的节点
	 * @param results
	 */
	public void getWordsOfChildren(ArrayList<String> results){
		Set<Character> keys = children.keySet();
		for(Character c : keys){
			TrieNode node = children.get(c);
			if (node.isWordEnd()) {
				results.add(node.getWord());
			}
			node.getWordsOfChildren(results);
		}
	}
	
	@Override
	public TrieNode getParent() {
		return parent;
	}
	
	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public boolean isWordEnd() {
		return isWordEnd;
	}

	@Override
	public void setIsWordEnd(boolean is) {
		this.isWordEnd = is;
	}

	@Override
	public TrieNode getNode(String key) {
		char[] cs = key.toCharArray();
		TrieNode node = null;
		for (int i = 0; i < cs.length; i++) {
			if (i == 0) {
				node = getNode(cs[i]);
			} else if(node != null){
				node = node.getNode(cs[i]);
			}
			if (node == null) {
				break;
			}
		}
		return node;
	}

	@Override
	public String getString() {
		if (depth == 0) {
			throw new RuntimeException("Illigal call, root node does not support getString()!");
		}
		char[] chars = new char[depth];
		TrieNode node = this;
		for (int i = depth; i > 0; i--) {
			chars[i-1] = node.getCharOfNode();
			node = node.getParent();
		}
		return new String(chars, 0, chars.length);
	}

	@Override
	public String getWord() {
		if (depth == 0) {
			throw new RuntimeException("Illigal call, root node does not support getWord()!");
		}
		if (isWordEnd) {
			return getString();
		} else {
			return null;
		}
	}

	@Override
	public TrieNode addChar(char c) {
		TrieNode node = getNode(c);
		if(node == null){
			CharNode cNode = createNewNode();
			cNode.c = c;
			cNode.parent = this;
			cNode.depth = depth + 1;
			node = cNode;
			children.put(c, cNode);
		}
		return node;
	}
	
	protected CharNode createNewNode(){
		return new CharNode();
	}

	@Override
	public TrieNode addNode(String key) {
		char[] chars = key.toCharArray();
		if (chars.length == 0) {
			throw new RuntimeException("key's length must > 0!");
		}
		TrieNode node = this;
		for (int i = 0; i < chars.length; i++) {
			node = node.addChar(chars[i]);
		}
		return node;
	}

	@Override
	public TrieNode getNode(char c) {
		return children.get(c);
	}

	@Override
	public boolean deleteChar(char c) {
		return children.remove(c) != null;
	}

	@Override
	public boolean deleteSubNode(String key) {
		char[] chars = key.toCharArray();
		TrieNode[] nodes = new TrieNode[chars.length];
		TrieNode node = this;
		boolean result = false;
		for (int i = 0; i < nodes.length; i++) {
			node = node.getNode(chars[0]);
			if (node != null) {
				nodes[i] = node;
			} else {
				break;
			}
		}
		for (int i = nodes.length - 1; i >= 0; i--) {
			if (nodes[i] != null) {
				if(nodes[i].getChildrenSize() == 0){
					nodes[i].getParent().deleteChar(chars[i]);
				} else {
					break;
				}
			}
		}
		return result;
	}

	@Override
	public char getCharOfNode() {
		if (depth == 0) {
			throw new RuntimeException("Illigal call, root node does not have char!");
		}
		return c;
	}

	@Override
	public int getChildrenSize() {
		return children.size();
	}

	public char getChar() {
		return c;
	}
	
	/**
	 * for debug
	 * @return
	 */
	public String getTransChar(){
		return c + "";
	}
	
	@Override
	public String toString() {
		return String.format("c: %s, hasParent: %b, isWordEnd: %b, children.size: %d, depth: %d", getTransChar(), parent != null, isWordEnd, children.size(), depth);
	}
	
	public void debugDump(boolean full){
		if (full && depth != 0 || !full && (children.size() == 0)) {
			System.out.println(toString() + ", " + getString());
		}
		for (int i = 0; i < children.size(); i++) {
			Enumeration<CharNode> e = children.elements();
			while (e.hasMoreElements()) {
				CharNode charNode = (CharNode) e.nextElement();
				charNode.debugDump(full);
			}
		}
	}

	@Override
	public void setDictWord(Word dictWord) {
		this.dictWord = dictWord;
	}

	@Override
	public Word getDictWord() {
		return this.dictWord;
	}

	@Override
	public void forewardOneChar(char c) {
		forewardOneChar = c;
	}

}
