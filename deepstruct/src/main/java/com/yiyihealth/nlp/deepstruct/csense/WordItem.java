package com.yiyihealth.nlp.deepstruct.csense;

import com.yiyihealth.nlp.deepstruct.dict.EWord;

public class WordItem extends CSItem {


	public WordItem(String content, ItemType type) {
		super(content, type);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean satisfied(EWord eWord) {
		return false;
	}
	
}
