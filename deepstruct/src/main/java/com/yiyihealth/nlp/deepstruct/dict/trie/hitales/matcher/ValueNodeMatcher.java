package com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher;

import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.TrieNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.ValueNode;

public class ValueNodeMatcher extends NodeMatcher {
	
	//private Forest<ValueNode> valueForest;
	
	private ValueNode rootNode;
	
	public ValueNodeMatcher(Forest<ValueNode> valueForest) {
		//this.valueForest = valueForest;
		rootNode = valueForest.getRoot();
	}

	private StringBuffer buffer = new StringBuffer();
	
	private ValueNode currentNode = null;
	
	private int currentRepeatCnt = 0;
	
	/**
	 * @param c
	 * @return 直到解析出值，返回
	 */
	public Term nextChar(char c, int pos){
		char orgChar = c;
		String text = null;
		boolean isDefined = false;
		int backoffset = 0;
		if (currentNode == null) {
			queryRootNode(c, orgChar);
		} else {
			if (currentNode.isEqual(c) && currentRepeatCnt < currentNode.getMatchTimes()) {
				//是否需要重复?
				currentRepeatCnt++;
				buffer.append(orgChar);
			} else {
				//还有没有分支
				TrieNode trieNode = currentNode.getNode(c);
				if (trieNode == null) {
					for (int i = 0; i < CE_TRANS.length; i++) {
						if (CE_TRANS[i][0] == c) {
							trieNode = currentNode.getNode(c);
						} else if (CE_TRANS[i][1] == c) {
							trieNode = currentNode.getNode(c);
						}
					}
				}
				if (trieNode == null) {
					//没有分支
					if (currentNode.isWordEnd()) {
						//就是单词
						text = buffer.toString();
						isDefined = true;
					} else {
						//如果匹配到前面的有效内容，往回一个节点查询（TODO 往回多个节点由于数字的循环匹配，比较复杂，目前不考虑）
						TrieNode backNode = currentNode.getParent();
						if (backNode != null && backNode.getDepth() != 0 && backNode.isWordEnd()) {
							text = buffer.substring(0, buffer.length() - 1);
							backoffset = 1;
							isDefined = true;
						} else {
							//部分匹配，不是完整单词
							text = buffer.toString();
							isDefined = false;
						}
					}
					buffer.setLength(0);
					currentNode = null;
					//重新从根节点找 
					queryRootNode(c, orgChar);
				} else {
					currentNode = (ValueNode) trieNode;
					buffer.append(orgChar);
				}
			}
		}
		if (text == null) {
			return null;
		} else {
			Term term = new Term(text, WordNatures.VALUE, isDefined, pos - backoffset - text.length());
			term.addCandidateNature(WordNatures.VALUE);
			return term;
		}
	}
	
	private void queryRootNode(char c, char orgChar){
		TrieNode trieNode = rootNode.getNode(c);
		if (trieNode != null) {
			currentNode = (ValueNode) trieNode;
			currentRepeatCnt = 0;
			buffer.append(orgChar);
		}
	}
	
	@Override
	public void reset() {
		currentNode = null;
		buffer.setLength(0);
	}
}
