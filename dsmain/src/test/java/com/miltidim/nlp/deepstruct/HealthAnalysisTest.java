package com.miltidim.nlp.deepstruct;

import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;

import junit.framework.TestCase;

public class HealthAnalysisTest extends TestCase {
	
	public void testDictKeNai(){
		String test = "可耐受，休息后无明显好转";//伴腰骶部疼痛，
		HealthAnalysis analysis = new HealthAnalysis();
		Sentence sentence = analysis.parse(test).get(0);
		assertTrue(sentence.getWords().contains("可耐"));
		
	}
	
	public void testWordSplit(){
		String test = "两侧上颌窦炎症";
		HealthAnalysis analysis = new HealthAnalysis();
		Sentence sentence = analysis.parse(test).get(0);
		assertTrue(sentence.getWords().contains("上颌窦"));
		assertTrue(sentence.getWords().contains("炎症"));
	}

}
