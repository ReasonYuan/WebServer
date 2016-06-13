package com.yiyihealth.ai.dsmain.learning;

import com.yiyihealth.nlp.deepstruct.dict.EWord;

public abstract class EWordStats {
	
	protected String nature;
	
	public EWordStats(String nature) {
		this.nature = nature;
	}

	public abstract void stats(EWord[] words);
}
