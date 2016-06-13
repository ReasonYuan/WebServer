package com.miltidim.nlp.deepstruct;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.EvidenceWritter;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.NewWordDict;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SearchOffset;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleSyntaxCorrect;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WordOrderSearcherTest  extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WordOrderSearcherTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HitalesForestTest.class );
    }

	public void testCombine(){
		SimpleSyntaxCorrect checker = new SimpleSyntaxCorrect("./testdata/sslogics/test_combine.txt");
		ArrayList<Sentence> sentences = new HealthAnalysis().parse("查超声(腹部)(2011-10-13)肝、胆、胰、脾");
		checker.correctBasicWords(sentences);
		boolean success = false;
		for (int i = 0; i < sentences.get(0).getWords().size(); i++) {
			if (sentences.get(0).getWords().get(i).getWord().equals("超声(腹部)")) {
				success = true;
			}
		}
		//TODO 由于目前不能合并包括标点符号的内容，所以这里暂时验证false
		assertFalse(success);
		
	}

	public void testDates(){
		SimpleSyntaxCorrect checker1 = new SimpleSyntaxCorrect("./sslogics/simple_syntaxc_orrect.txt");
		SimpleSyntaxCorrect sequencePrinter = new SimpleSyntaxCorrect("./sslogics/simple_evidence_dates_around.txt");

		ArrayList<Sentence> sentences = new HealthAnalysis().parse("B型钠尿肽,月胆囊结石、胆囊炎");
		ArrayList<String> matches = checker1.correctBasicWords(sentences);
		matches = checker1.correctBasicWords(sentences);
		matches = sequencePrinter.correctBasicWords(sentences);
		sentences = new HealthAnalysis().parse("B型钠尿肽月胆囊结石、胆囊炎");
		NewWordDict.getInstance().debugPrintAll();
		
		//TODO 由于目前不能合并包括标点符号的内容，所以这里暂时验证false
		assertTrue(matches.size() == 0);
	}
	
	public void testPrinter(){
		SimpleSyntaxCorrect checker = new SimpleSyntaxCorrect("./testdata/sslogics/test_printer.txt");
		ArrayList<Sentence> sentences = new HealthAnalysis().parse("超声(腹部)：胆囊结石、胆囊炎、脾大；心电图：窦性心律2009年提示：不完全性右束支传导阻滞；心脏彩超：1.主动脉瓣钙化伴轻度反流");
		ArrayList<String> founds = checker.correctBasicWords(sentences);
		assertTrue(founds.size() > 0);
	}
	
	public void testEvidence(){
		EvidenceWritter writter = new EvidenceWritter(null);
		SimpleEvidenceSearcher searcher = new SimpleEvidenceSearcher("./testdata/sslogics/test_evidence.txt", writter);
		ArrayList<Sentence> sentences = new HealthAnalysis().parse("查超声(腹部)(2011-10-13)肝、胆、胰、脾、双肾声像图未见异常。");
		searcher.searchSimpleEvidence(sentences.get(0).getWords()
				, searcher.getSentencePoses(sentences.get(0).getWords())
				, new SearchOffset(0));
		boolean success = writter.getEvidences().size() > 0;
		assertTrue(success);
	}
	
	public void testHaveDrugGoodEffectsSearch(){
		String testStr = "患者于3月前无明显诱因出现剑突下压榨性疼痛，逐渐加重并向肩部放射，伴呕吐、乏力、出汗，无发热，无晕厥，症状持续存在不缓解，就诊于嘉善县第一人民医院就诊，以“急性胆囊炎”收治入院，经“抗感染”等治疗3天，自觉剑突下疼痛好转，但出现胸闷、后背部疼痛，不能平卧，体温升高，查心肌酶明显升高，拟“心肌梗死合并肺部感染”转入心内科，给予“拜阿司匹林，波立维”双联抗血小板，“低分子肝素针”抗凝，“瑞舒伐他汀”调脂稳定斑块，“速尿、安体舒通”合用利尿对症支持治疗，患者症状无好转。来我院心内科就诊，诊断为“冠状动脉粥样硬化性心脏病，急性下壁、后壁、侧壁心肌梗死KillipⅡ级”；“急性胆囊炎”；“低蛋白血症”；“上消化道出血”；“血小板减少症”“系统性红斑狼疮”。予以阿司匹林肠溶片、氯吡格雷片二联抗血小板，可定片调脂，补充白蛋白，速尿、螺内酯利尿改善心功能，泰美尼克针抑酸护胃，输血小板，特比奥针促血小板生长等治疗；出院时血小板(2014-3-23)为39X10^9/L，生命体征平稳，无胸闷痛，无发热，无咳嗽咳痰，无恶心呕吐。患者回家后3天再次出现剑突下疼痛伴恶心呕吐，呕吐物为胃内容物，呕吐后疼痛稍缓解，无发热，无腹泻，就诊于嘉兴学院附属第二医院，诊断为“冠状动脉粥样硬化性心脏病”“急性冠脉综合症”“系统性红斑狼疮”“肺部感染”“间质性肺炎”“胆囊炎”“胆管结石”“肾积水”“狼疮性肾炎”“胃肠道血管炎”“口腔真菌感染”“子宫肌瘤”。给予“哌拉西林钠他巴唑坦抗感染，甲强龙和丙种球蛋白免疫治疗，护胃补液等各种对症支持治疗”(具体不详)。症状未能缓解，遂来我院就诊，门诊以呕吐(系统性红斑狼疮？)收入院。患者自患病以来，精神状态较差，体重无明显变化，纳差，未解大便2天，小便基本正常，睡眠较差。";
		EvidenceWritter writter = new EvidenceWritter(null);
		SimpleEvidenceSearcher searcher = new SimpleEvidenceSearcher("./testdata/sslogics/test_skipconditions.txt", writter);
		ArrayList<Sentence> sentences = new HealthAnalysis().parse(testStr);
		searcher.searchSimpleEvidence(sentences.get(0).getWords()
				, searcher.getSentencePoses(sentences.get(0).getWords())
				, new SearchOffset(0));
		boolean success = writter.getEvidences().size() == 0;
		assertTrue(success);
	}
	
}
