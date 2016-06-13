package com.yiyihealth.nlp.deepstruct.csense;

import com.yiyihealth.nlp.deepstruct.dict.EWord;

public abstract class CSItem {
	
	protected String content;
	
	public static enum ItemType {
		NATURE,
		WORD,
		FEATURE
	}
	
	protected ItemType type;
	
	public CSItem(String content, ItemType type) {
		this.content = content;
		this.type = type;
	}
	
	public String getContent() {
		return content;
	}
	
	public ItemType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return content + "-" + type;
	}

	public abstract boolean satisfied(EWord eWord);
	
}
