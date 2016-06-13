package com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher;

import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.UnitNode;

public class UnitMatcher extends NormalNodeMatcher<UnitNode> {

	public UnitMatcher(Forest<UnitNode> forest) {
		super(forest);
	}
	
	@Override
	public Term nextChar(char c, int pos) {
		return super.nextChar(c, pos);
	}
	
}
