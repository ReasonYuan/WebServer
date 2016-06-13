package com.yiyihealth.ds.esearch.syntaxtrie;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.EWord;

/**
 * 把寻找的规律再返回数据集里进行统计验证
 * @author qiangpeng
 *
 */
public class NatureRuleVerifer {
	
	private NatureRuleMatcher matcher;
	
	private int succussMatchCnt = 0;
	
	private int failMatchCnt = 0;
	
	private float sucPercent = 0;
	
	public NatureRuleVerifer(NatureRuleMatcher matcher) {
		this.matcher = matcher;
	}
	
	public NatureRuleMatcher getMatcher() {
		return matcher;
	}

	public int getFailMatchCnt() {
		return failMatchCnt;
	}
	
	public int getSuccussMatchCnt() {
		return succussMatchCnt;
	}
	
	public float getSucPercent() {
		return sucPercent;
	}
	
	/**
	 * 计算匹配到并且匹配合格率<br/>
	 * 验证通过率, 如果通过率过低则放弃该规则
	 * @param rule
	 * @param words
	 */
	public void test(SyntaxRule rule, ArrayList<EWord> words){
		int size = words.size();
		for (int i = 0; i < size; i++) {
			boolean correct = false;
			for (int j = i; j < size; j++) {
				EWord word = words.get(j);
				if(matcher.nextWord(word.getWord(), word.getNature())){
					if (matcher.isJustMatchedAny()) {
						correct = matcher.getNature2Inherence().equals(word.getNature());
					}
					if (matcher.fullMatched()) {
						if (correct) {
							succussMatchCnt++;
						} else {
							failMatchCnt++;
						}
						matcher.reset();
						break;
					}
				} else {
					matcher.reset();
					break;
				}
			}
		}
		if(succussMatchCnt + failMatchCnt == 0){
			sucPercent = 0;
		} else {
			sucPercent = ((float)succussMatchCnt) / (failMatchCnt + succussMatchCnt);
		}
	}
	
}
