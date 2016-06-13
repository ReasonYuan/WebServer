package com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.TrieNode;

public class NormalNodeMatcher<T extends CharNode> extends NodeMatcher {
	
	private T rootNode;
	
	private TrieNode currentNode = null;
	
	/**
	 * 仅做临时变量缓存使用
	 */
	private char[] tmpBuffer = new char[16];
	
	public NormalNodeMatcher(Forest<T> forest) {
		this.rootNode = forest.getRoot();
	}
	
	public T getRootNode() {
		return rootNode;
	}
	
	public Term nextChar(char c, int pos){
		String text = null;
		boolean isDefined = false;
		String nature = "";
		ArrayList<String> natures = new ArrayList<>();
		int backoffset = 0;
		if (currentNode != null) {
			TrieNode nextNode = currentNode.getNode(c);
			if (nextNode == null) {
				for (int i = 0; i < CE_TRANS.length; i++) {
					if (CE_TRANS[i][0] == c) {
						nextNode = currentNode.getNode(CE_TRANS[i][1]);
					} else if (CE_TRANS[i][1] == c) {
						nextNode = currentNode.getNode(CE_TRANS[i][0]);
					}
				}
			}
			if (nextNode == null) {
				//连续的英文字母默认为英文单词, 并且起始字符是中文
				char currentChar = currentNode.getChar();
				char topChar = currentNode.getString().charAt(0);
				if ((c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') && (currentChar >= 'a' && currentChar <= 'z' || currentChar >= 'A' && currentChar <= 'Z') 
						&& (topChar >= 'a' && topChar <= 'z' || topChar >= 'A' && topChar <= 'Z')) {
					//开始字符也必须是英文, 才认为是英文字符
					if (currentNode.isWordEnd()) {
						text = currentNode.getWord();
						isDefined = true;
						currentNode.getDictWord().copyNatures(natures);
						nature = currentNode.getDictWord().getMeanings().get(0).getWordNature().getName();
						currentNode = rootNode.getNode(c);
					} else {
						currentNode = null;
					}
				} else {
					if (currentNode.isWordEnd()) {
						text = currentNode.getWord();
						isDefined = true;
						currentNode.getDictWord().copyNatures(natures);
						nature = currentNode.getDictWord().getMeanings().get(0).getWordNature().getName();
						currentNode = rootNode.getNode(c);
					} else {
						//回看，是否有单词
						TrieNode parent = currentNode.getParent();
						tmpBuffer[0] = c;
						tmpBuffer[1] = currentNode.getChar();
						while(parent != null && parent.getDepth() > 0){
							backoffset++;
							if (parent.isWordEnd()) {
								text = parent.getWord();
								isDefined = true;
								parent.getDictWord().copyNatures(natures);
								nature = parent.getDictWord().getMeanings().get(0).getWordNature().getName();
								break;
							}
							if(tmpBuffer.length <= backoffset+1){
								char[] buf = new char[tmpBuffer.length + 8];
								System.arraycopy(tmpBuffer, 0, buf, 0, tmpBuffer.length);
								tmpBuffer = buf;
							}
							tmpBuffer[backoffset+1] = parent.getChar();
							parent = parent.getParent();
						}
						if (text != null) {
							//避免跳过足够长的单词，如果太短则在最长适配原则中本身就会被忽略，所以这里不考虑
							TrieNode backNode = null;
							for (int i = backoffset; i >= 0; i--) {
								if (backNode == null) {
									backNode = rootNode.getNode(tmpBuffer[i]);
								} else {
									backNode = backNode.getNode(tmpBuffer[i]);
								}
							}
							currentNode = backNode;
						} else {
							currentNode = rootNode.getNode(c);
						}
					}
				}
			} else {
				//找最长适配串
				currentNode = nextNode;
			}
		} else {
			currentNode = rootNode.getNode(c);
		}
		if (text != null) {
			Term term = new Term(text, nature, isDefined, pos - backoffset - text.length());
			term.addCandidateNatures(natures);
			return term;
		} else {
			return null;
		}
	}
	

	@Override
	public void reset() {
		currentNode = null;
	}
}
