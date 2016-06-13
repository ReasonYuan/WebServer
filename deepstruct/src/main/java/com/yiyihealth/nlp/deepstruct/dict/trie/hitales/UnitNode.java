package com.yiyihealth.nlp.deepstruct.dict.trie.hitales;

public class UnitNode extends CharNode {
	
	@Override
	public TrieNode getNode(char c) {
		TrieNode node = super.getNode(c);
		if (node == null) {
			//转换成大小写再试试，一般人会把数值单位大小写不管的输入
			if (c >= 'a' && c <= 'z') {
				node = super.getNode((char)('A' + (c - 'a')));
			} else if (c >= 'A' && c <= 'Z') {
				node = super.getNode((char)('a' + (c - 'A')));
			} else if (c == '／') {
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
