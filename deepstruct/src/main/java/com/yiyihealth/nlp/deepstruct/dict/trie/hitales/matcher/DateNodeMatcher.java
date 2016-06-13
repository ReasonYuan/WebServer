package com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher;

import java.util.ArrayList;
import java.util.Calendar;

import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.DateNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.TrieNode;

public class DateNodeMatcher extends NodeMatcher {
	
	private DateNode rootNode;
	
	private StringBuffer buffer = new StringBuffer();
	
	private DateNode currentNode = null;
	
	public DateNodeMatcher(Forest<DateNode> forest) {
		rootNode = forest.getRoot();
	}
	
	/**
	 * @param c
	 * @return 直到解析出值，返回
	 */
	public Term nextChar(char c, int pos){
		if (c == 'd') {
			return null;
		}
		String text = null;
		String timeformat = null;
		boolean isDefined = false;
		ArrayList<String> candidateNatures = new ArrayList<>();
		String possibleNature = WordNatures.UNREC;
		if (currentNode == null) {
			queryRootNode(c);
		} else {
			TrieNode dNode = currentNode.getNode(c);
			if (dNode == null) {
				for (int i = 0; i < CE_TRANS.length; i++) {
					if (CE_TRANS[i][0] == c) {
						dNode = currentNode.getNode(c);
					} else if (CE_TRANS[i][1] == c) {
						dNode = currentNode.getNode(c);
					}
				}
			}
			if (dNode == null) {
				char lastChar = buffer.charAt(buffer.length()-1);
				//TODO 时间规则需要细化
				if (!(c >= 0 && c <= 9 && lastChar >= 0 && lastChar <= 9)) {
					text = buffer.toString();
					isDefined = currentNode.isWordEnd();
					if (isDefined) {
						currentNode.getDictWord().copyNatures(candidateNatures);
						timeformat = currentNode.getParseForamt();
						//TODO 默认取的第一个词性，以后重构
						possibleNature = currentNode.getDictWord().getMeanings().get(0).getWordNature().getName();
					}
					buffer.setLength(0);
				}
				if (text.length() == 4) {
					//TODO 对yyyy的经常和数字混淆，这里做一个限制, 每个词都要在excel加检验规则
					//目前先用下面的临时方案
					try {
						int year = Integer.parseInt(text);
						if (year > Calendar.getInstance().get(Calendar.YEAR) + 1 || year < 1930) {
							//认为不是日期，临时方案
							text = null;
						} else {
							candidateNatures.add(WordNatures.VALUE);
						}
					} catch (Exception e) {
					}
				}
				currentNode = null;
				queryRootNode(c);
			} else {
				currentNode = (DateNode) dNode;
				buffer.append(c);
			}
		}
		Term result = null;
		if (text != null) {
			result = new Term(text, possibleNature, isDefined, pos - text.length());
			result.setTimeformat(timeformat);
			result.addCandidateNatures(candidateNatures);
		}
		return result;
	}
	
	private void queryRootNode(char c) {
		TrieNode node = rootNode.getNode(c);
		if (node != null) {
			currentNode = (DateNode) node;
			buffer.setLength(0);
			buffer.append(c);
		}
	}

	@Override
	public void reset() {
		currentNode = null;
		buffer.setLength(0);
	}
}
