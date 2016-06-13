package com.miltidim.nlp.deepstruct;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.DateDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.ValueDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.DateNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.ValueNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.DateNodeMatcher;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.ValueNodeMatcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DatesForestTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DatesForestTest( String testName )
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

    public void testDatesForest(){
    	
    	Forest<DateNode> forest = new Forest<DateNode>(new DateNode());
    	DateDictLoader valueDictLoader = new DateDictLoader();
    	ArrayList<Word> words = valueDictLoader.loadDict();
    	for(Word word : words){
    		forest.addWord(word);
    	}
    	//forest.debugDump();
    	
    	DateNodeMatcher matcher = new DateNodeMatcher(forest);
    	//String test1 = "患者于1997年起出现颜面部红斑,伴有手足指(趾)端冻疮样红斑,日晒后加重,伴有 高血压,伴有冬季双手发紫,双下肢浮肿,无发热,无关节肿痛,曾在当地医院就诊,考虑“系统性红斑狼疮可能”,曾给予强的松6#/d+氯喹治疗,患者不规则用药。至2005年1月,患者在北京协 和医院就诊,查ANA1:640,ds-DNA阳性,抗SSA、SSB阳性,尿常规:蛋白2-3+,24小时尿蛋白 15.3g,曾建议肾穿刺检查及强的松8#/d治疗,患者拒绝,并未规律治疗。至2007年,患者在上海龙华医院就诊,当时服用强的松4#/d,予加用CTX0.6/3周,共20余次(2008.10),蛋白尿维持在 3-4g/24h,至2009开始患者出现血肌酐升高,150-174umol/L左右,伴有反复双下肢浮肿,夜尿增 多,1-2次/夜,开始口服爱诺华10mg/d,强的松3#/d治疗,蛋白尿最少控制在0.4g/d,但血压抑 制较高,180/110mmHg左右,半年前,患者双下肢浮肿明显,不能消退,8.15复查血常规“WBC9.15*10~9/L,Hb116g/L,PLT213*10~9/L,Scr266umol/L,UA667umol/L, BUN24.4mmol/L,现患者为进一步诊治,来我院就诊,收治入院。";
    	//String test1 = "2008.10sdf";
    	String test2 = "既往高血压病史10余年。";
    	boolean found = false;
    	for (int i = 0; i < test2.length(); i++) {
    		Term token = matcher.nextChar(test2.charAt(i), i);
			if (token != null) {//2008.10
				if (token.getWord().equals("10余年")) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    }
    
    public void testPeriod(){
    	
    	Forest<DateNode> forest = new Forest<DateNode>(new DateNode());
    	DateDictLoader valueDictLoader = new DateDictLoader();
    	ArrayList<Word> words = valueDictLoader.loadDict();
    	for(Word word : words){
    		forest.addWord(word);
    	}
    	//forest.debugDump();
    	
    	DateNodeMatcher matcher = new DateNodeMatcher(forest);
    	//String test1 = "患者于1997年起出现颜面部红斑,伴有手足指(趾)端冻疮样红斑,日晒后加重,伴有 高血压,伴有冬季双手发紫,双下肢浮肿,无发热,无关节肿痛,曾在当地医院就诊,考虑“系统性红斑狼疮可能”,曾给予强的松6#/d+氯喹治疗,患者不规则用药。至2005年1月,患者在北京协 和医院就诊,查ANA1:640,ds-DNA阳性,抗SSA、SSB阳性,尿常规:蛋白2-3+,24小时尿蛋白 15.3g,曾建议肾穿刺检查及强的松8#/d治疗,患者拒绝,并未规律治疗。至2007年,患者在上海龙华医院就诊,当时服用强的松4#/d,予加用CTX0.6/3周,共20余次(2008.10),蛋白尿维持在 3-4g/24h,至2009开始患者出现血肌酐升高,150-174umol/L左右,伴有反复双下肢浮肿,夜尿增 多,1-2次/夜,开始口服爱诺华10mg/d,强的松3#/d治疗,蛋白尿最少控制在0.4g/d,但血压抑 制较高,180/110mmHg左右,半年前,患者双下肢浮肿明显,不能消退,8.15复查血常规“WBC9.15*10~9/L,Hb116g/L,PLT213*10~9/L,Scr266umol/L,UA667umol/L, BUN24.4mmol/L,现患者为进一步诊治,来我院就诊,收治入院。";
    	//String test1 = "2008.10sdf";
    	String test2 = "10余年既往高血压病史。";
    	boolean found = false;
    	for (int i = 0; i < test2.length(); i++) {
    		Term token = matcher.nextChar(test2.charAt(i), i);
			if (token != null) {//2008.10
				if (token.getWord().equals("10余年") && token.getNature().equals(WordNatures.PERIOD)) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    	
    	test2 = "3月余年既往高血压病史。";
    	found = false;
    	for (int i = 0; i < test2.length(); i++) {
    		Term token = matcher.nextChar(test2.charAt(i), i);
			if (token != null) {//2008.10
				if (token.getWord().equals("3月余") && token.getNature().equals(WordNatures.PERIOD)) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    	
    }
    
    public void testDate(){
    	
    	Forest<DateNode> forest = new Forest<DateNode>(new DateNode());
    	DateDictLoader valueDictLoader = new DateDictLoader();
    	ArrayList<Word> words = valueDictLoader.loadDict();
    	for(Word word : words){
    		DateNode dateNode = (DateNode) forest.addWord(word);
    		if (dateNode.getParseForamt() == null) {
				dateNode.setParseForamt(word.getMeanings().get(0).getParseFormat());
			}
			//System.out.println(String.format("word: %s, format: %s", word.getText(), dateNode.getParseForamt()));
    	}
    	DateNodeMatcher matcher = new DateNodeMatcher(forest);
    	String test2 = "2014-9-9既往高血压病史。";
    	boolean found = false;
    	for (int i = 0; i < test2.length(); i++) {
    		Term token = matcher.nextChar(test2.charAt(i), i);
			if (token != null) {//2014-9-9
				if (token.getWord().equals("2014-9-9") && token.getNature().equals(WordNatures.DATE) && token.getNormalDate() != null) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    }

    public void testDateAndMonth(){
    	ArrayList<Sentence> sentences = new HealthAnalysis().parse("2009-6-1月经");
    	assertTrue(sentences.get(0).getWords().size() > 0);
    }
    
    public void testSequenceDates(){
    	String testStr = "此后定期随访,2011-06-29、2011-09-06、2011-10-12/2011-11-21四次复诊,尿蛋白定量逐渐降至248mg/24h。";
    	
    	Forest<DateNode> forest = new Forest<DateNode>(new DateNode());
    	DateDictLoader dateDictLoader = new DateDictLoader();
    	ArrayList<Word> words = dateDictLoader.loadDict();
    	for(Word word : words){
    		DateNode dateNode = (DateNode) forest.addWord(word);
    		if (dateNode.getParseForamt() == null) {
				dateNode.setParseForamt(word.getMeanings().get(0).getParseFormat());
			}
			//System.out.println(String.format("word: %s, format: %s", word.getText(), dateNode.getParseForamt()));
    	}
    	DateNodeMatcher matcher = new DateNodeMatcher(forest);
    	boolean found = false;
    	for (int i = 0; i < testStr.length(); i++) {
    		Term token = matcher.nextChar(testStr.charAt(i), i);
			if (token != null) {//2014-9-9
				if (token.getWord().equals("2011-11-21") && token.getNature().equals(WordNatures.DATE) && token.getNormalDate() != null) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    	
    	Forest<ValueNode> forest1 = new Forest<ValueNode>(new ValueNode());
    	ValueDictLoader valueDictLoader = new ValueDictLoader();
    	ArrayList<Word> words1 = valueDictLoader.loadDict();
    	for(Word word : words1){
    		forest1.addWord(word);
    	}
    	//CharNode node = (CharNode) forest.getRoot();//.getNode("mg/");
    	//node.debugDump(false);
    	
    	ValueNodeMatcher matcher1 = new ValueNodeMatcher(forest1);
    	found = false;
    	for (int i = 0; i < testStr.length(); i++) {
			Term token = matcher1.nextChar(testStr.charAt(i), i);
			if (token != null) {
				if (token.getWord().equals("/2011")) {
					found = true;
				}
			}
		}
    	assertFalse(found);
    	
    	ArrayList<Sentence> sentences = new HealthAnalysis().parse(testStr);
    	assertTrue(((Sentence)sentences.get(0)).contains("2011-11-21"));		
    }
}
