package com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher;

import com.yiyihealth.nlp.deepstruct.dict.Term;

public abstract class NodeMatcher {
	
	/**
	 * 中英文字符转换
	 */
	protected static char[][] CE_TRANS = {{'/', '／'}, {'-', '－'}, {'：', ':'}, {'＋', '+'}};
	
	/**
	 * 
	 * @param c
	 * @return 如果有新的词匹配到了，或没有下一个分支节点时返回未定义的字符串
	 */
	public abstract Term nextChar(char c, int pos);
	
	public abstract void reset();
	
}
