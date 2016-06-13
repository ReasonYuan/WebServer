package com.yiyihealth.ai.dsmain;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.dict.HitalesDict;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.RecordsDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.UnitNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.UnitMatcher;

import junit.framework.Assert;
import junit.framework.TestCase;

public class RecordsDictLoaderTest extends TestCase {

//	public void testRecordsDictLoader(){
//		RecordsDictLoader recordsDictLoader = new RecordsDictLoader();
//		ArrayList<Word> words = recordsDictLoader.loadDict();
//		assertTrue(words.size() > 0);
//	}
	
	public void testNlplValueUnit(){
		ArrayList<Sentence> sentences = new HealthAnalysis().parse("益赛普25mg2次/周皮下注射");
		assertTrue(sentences.get(0).contains("次/周"));
		
		Forest<UnitNode>	unitForest = new Forest<UnitNode>(new UnitNode());
		HitalesDict dict = HitalesDict.getHealthCareDict();
		ArrayList<Word> unitWords = dict.getUnitWords();
		for (Word word : unitWords) {
			unitForest.addWord(word);
		}
		UnitMatcher matcher = new UnitMatcher(unitForest);
		sentences = new HealthAnalysis().parse("mgqd");
		for (int i = 0; i < "mgqd".length(); i++) {
			char c = "mgqd".charAt(i);
			Term token = matcher.nextChar(c, i);
			if (token != null) {
				assertTrue(token.getWord().equals("mg"));
				break;
			}
		}
		assertTrue(sentences.get(0).contains("mg"));
		
		sentences = new HealthAnalysis().parse("第2次");
		for (int i = 0; i < "第2次".length(); i++) {
			char c = "第2次".charAt(i);
			Term token = matcher.nextChar(c, i);
			if (token != null) {
				assertTrue(token.getWord().equals("第2次"));
				break;
			}
		}
		assertTrue(sentences.get(0).contains("第2次"));
	}
	
//	public void testCombinePian(){
//		ArrayList<Sentence> sentences = new HealthAnalysis().parse("予“柳氮磺胺吡啶片0.5g口服2/日、双氯芬酸");
//		assertTrue(sentences.get(0).contains("柳氮磺胺吡啶片"));
//	}
	
	public void testValueUnitFollows(){
		String test = "3-甲强龙160mgbid*4-甲强龙";
		ArrayList<Sentence> sentences = new HealthAnalysis().parse(test);
		assertTrue(sentences.get(0).contains("mg"));
	}
	
//	public void testNlpSym(){
//		String test = "治疗后腹痛腹胀好转。";
//		ArrayList<Sentence> sentences = new HealthAnalysis().parse(test);
//		assertTrue(HealthAnalysis.getNormalForest().getWordEndNode("胀") != null);
//		assertTrue(HealthAnalysis.getNormalForest().searchWord("腹胀"));
//		assertTrue(HealthAnalysis.getNormalForest().searchWord("腹胀感"));
//		assertTrue(sentences.get(0).contains("胀"));
//	}
	
//	public void testNlpSym2(){
//		String test = "有盗汗症状";
//		ArrayList<Sentence> sentences = new HealthAnalysis().parse(test);
//		assertTrue(HealthAnalysis.getNormalForest().getWordEndNode("胀") != null);
//		assertTrue(HealthAnalysis.getNormalForest().searchWord("腹胀"));
//		assertTrue(HealthAnalysis.getNormalForest().searchWord("腹胀感"));
//		assertTrue(sentences.get(0).contains("胀"));
//	}
}
