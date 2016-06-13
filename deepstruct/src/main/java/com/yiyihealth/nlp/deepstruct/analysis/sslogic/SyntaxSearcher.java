package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.EWord;

/**
 * 
 * Fake predicate
 * @author qiangpeng
 *
 */
public abstract class SyntaxSearcher {
	
	/**
	 * 名称
	 */
	private String name;
	
	public SyntaxSearcher(String name) {
		this.name = name;
	}
	
	/**
	 * 查找指定规则的出现情况
	 * @param words
	 * @param onlyMatchOnce 如果句子里出现多次，找到第一次就不再寻找
	 * @return
	 * 	－ 出现了多少次
	 */
	public abstract int doSearch(ArrayList<EWord> words, int[] sentencePoses, SearchOffset offset);
	
}
