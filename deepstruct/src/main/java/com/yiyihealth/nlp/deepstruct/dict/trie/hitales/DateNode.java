package com.yiyihealth.nlp.deepstruct.dict.trie.hitales;

public class DateNode extends CharNode {

	protected boolean normalDate = true;

	protected String parseForamt;

	public String getParseForamt() {
		return parseForamt;
	}

	public void setParseForamt(String parseForamt) {
		this.parseForamt = parseForamt;
	}

	@Override
	public TrieNode getNode(char c) {
		if (c >= '0' && c <= '9') {
			return super.getNode('d');
		} else {
			TrieNode node = super.getNode(c);
			if (node == null) {
				if (c == '／') {
					//中文到英文
					node = super.getNode('/');
				} else if (c == '－') {
					//中文到英文
					node = super.getNode('-');
				}
			}
			return node;
		}
	}

	@Override
	public TrieNode addNode(String key) {
		//System.out.println("key: " + key);
		char[] chars = key.toCharArray();
		if (chars.length == 0) {
			throw new RuntimeException("key's length must > 0!");
		}
		TrieNode node = this;
		for (int i = 0; i < chars.length; i++) {
			//TODO 如果以后各式中有‘d’的真字符，需要增加转义功能
			node = node.addChar(chars[i]);
		}
		return node;
	}

	public boolean isNormalDate() {
		return normalDate;
	}

	public void setNormalDate(boolean normalDate) {
		this.normalDate = normalDate;
	}

	@Override
	protected CharNode createNewNode() {
		return new DateNode();
	}

}
