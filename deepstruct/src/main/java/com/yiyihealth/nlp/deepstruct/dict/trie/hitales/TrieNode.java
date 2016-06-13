package com.yiyihealth.nlp.deepstruct.dict.trie.hitales;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Word;

public interface TrieNode {
	
	public TrieNode getParent();
	
	public boolean isWordEnd();
	
	public void setIsWordEnd(boolean is);

	public TrieNode getNode(String key);
	
	/**
	 * @return 当前节点代表的单词, 不论{@link #isWordEnd()}是否为true
	 */
	public String getString();
	
	/**
	 * @return 当且仅当 {@link #isWordEnd()}为true是才返回字符串，否则返回Null
	 */
	public String getWord();
	
	public TrieNode addChar(char c); 
	
	public TrieNode addNode(String key); 
	
	public TrieNode getNode(char c); 
	
	public char getCharOfNode();
	
	public boolean deleteChar(char c);
	
	public boolean deleteSubNode(String key);
	
	public int getChildrenSize();
	
	public void getWordsOfChildren(ArrayList<String> results);
	
	/**
	 * 设置这个节点所对应的字典里的词
	 * @param dictWord
	 */
	public void setDictWord(Word dictWord);
	
	/**
	 * 返回这个节点对应的词典里的词
	 * @return
	 */
	public Word getDictWord();
	
	public char getChar();
	
	public int getDepth();
	
	/**
	 * 往前多看一个字符
	 * @param c
	 */
	public void forewardOneChar(char c);
	
}
