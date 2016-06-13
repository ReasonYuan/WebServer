package com.yiyihealth.nlp.deepstruct.csense;

import com.yiyihealth.nlp.deepstruct.dict.EWord;

public class FeatureItem extends CSItem {

	public FeatureItem(String content, ItemType type) {
		super(content, type);
	}

	@Override
	public boolean satisfied(EWord eWord) {
		// TODO Auto-generated method stub
		return false;
	}

}
