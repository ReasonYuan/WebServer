package com.yiyihealth.ai.dsmain;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;

import junit.framework.TestCase;

public class CSenseMultiNatureTest extends TestCase {

	public void testBuWeiZhengzhuang(){
		String test = "颈部疼痛6年，缓解疼痛，颈椎活动受限制2年";
		HealthAnalysis analysis = new HealthAnalysis();
		ArrayList<Sentence> sentences = analysis.parse(test);
		Sentence sentence = sentences.get(0);
		assertTrue(sentence.getWords().get(2).getNature().equals("Symptom"));
		assertTrue(sentence.getWords().get(6).getNature().equals("Symptom"));
	}
	
}
