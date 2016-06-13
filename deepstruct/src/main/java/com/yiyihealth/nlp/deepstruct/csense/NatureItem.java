package com.yiyihealth.nlp.deepstruct.csense;

import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class NatureItem extends CSItem {
	
	public NatureItem(String content, ItemType type) {
		super(getNatureNameByReadableName(content), type);
	}
	
	private static String getNatureNameByReadableName(String content){
		try {
			return WordNatures.getNatureFullByReadableName(content);
		} catch (Exception e) {
			return content;
		}
	}

	@Override
	public boolean satisfied(EWord eWord) {
		return false;
	}

}
