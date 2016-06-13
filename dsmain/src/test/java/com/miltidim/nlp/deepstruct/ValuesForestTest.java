package com.miltidim.nlp.deepstruct;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.UnitDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.excelloader.ValueDictLoader;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.UnitNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.ValueNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.UnitMatcher;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.ValueNodeMatcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ValuesForestTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ValuesForestTest( String testName )
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
    
    public void testUnitForest(){
    	Forest<UnitNode> forest = new Forest<UnitNode>(new UnitNode());
    	UnitDictLoader unitDictLoader = new UnitDictLoader();
    	ArrayList<Word> words = unitDictLoader.loadDict();
    	for(Word word : words){
    		forest.addWord(word);
    	}
    	CharNode node = (CharNode) forest.getRoot();//.getNode("mg/");
    	node.debugDump(false);
    	
    	UnitMatcher matcher = new UnitMatcher(forest);
    	String test2 = "体温23℃,30次/分,24小时尿蛋白定量390mg/24h、血沉26mm/h、补体C30.73g/L,考虑为“系统性红斑狼疮狼疮性肾炎”";
    	boolean found = false;
    	for (int i = 0; i < test2.length(); i++) {
			Term token = matcher.nextChar(test2.charAt(i), i);
			if (token != null) {
				if (token.getWord().equals("℃")) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    }

    public void testValuesForest(){
    	
    	Forest<ValueNode> forest = new Forest<ValueNode>(new ValueNode());
    	ValueDictLoader valueDictLoader = new ValueDictLoader();
    	ArrayList<Word> words = valueDictLoader.loadDict();
    	for(Word word : words){
    		forest.addWord(word);
    	}
    	
    	ValueNodeMatcher matcher = new ValueNodeMatcher(forest);
    	String test1 = "患者于1997年起出现颜面部红斑,伴有手足指(趾)端冻疮样红斑,日晒后加重,伴有 高血压,伴有冬季双手发紫,双下肢浮肿,无发热,无关节肿痛,曾在当地医院就诊,考虑“系统性红斑狼疮可能”,曾给予强的松6#/d+氯喹治疗,患者不规则用药。至2005年1月,患者在北京协 和医院就诊,查ANA1:640,ds-DNA阳性,抗SSA、SSB阳性,尿常规:蛋白2-3+,24小时尿蛋白 15.3g,曾建议肾穿刺检查及强的松8#/d治疗,患者拒绝,并未规律治疗。至2007年,患者在上海龙华医院就诊,当时服用强的松4#/d,予加用CTX0.6/3周,共20余次(2008.10),蛋白尿维持在 3-4g/24h,至2009开始患者出现血肌酐升高,150-174umol/L左右,伴有反复双下肢浮肿,夜尿增 多,1-2次/夜,开始口服爱诺华10mg/d,强的松3#/d治疗,蛋白尿最少控制在0.4g/d,但血压抑 制较高,180/110mmHg左右,半年前,患者双下肢浮肿明显,不能消退,8.15复查血常规“WBC9.15*10~9/L,Hb116g/L,PLT213*10~9/L,Scr266umol/L,UA667umol/L, BUN24.4mmol/L,现患者为进一步诊治,来我院就诊,收治入院。";
    	boolean found = false;
    	for (int i = 0; i < test1.length(); i++) {
			Term token = matcher.nextChar(test1.charAt(i), i);
			if (token != null && token.getWord().equals("667")) {
				found = true;
			}
		}
    	assertTrue(found);
    	
    	String test2 = "羟氯喹0.1g2/日治疗原发病,";
    	found = false;
    	for (int i = 0; i < test2.length(); i++) {
			Term token = matcher.nextChar(test2.charAt(i), i);
			if (token != null) {
				if (token.getWord().equals("2")) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    	
//    	matcher.reset();
//    	String test3 = "补体C31.080g/L,";
//    	found = false;
//    	for (int i = 0; i < test3.length(); i++) {
//			Term token = matcher.nextChar(test3.charAt(i), i);
//			if (token != null) {
//				if (token.text.equals("补体C3")) {
//					found = true;
//				}
//			}
//		}
//    	assertTrue(found);
    	
    }
    
    public void testFloatValues(){
    	
    	Forest<ValueNode> forest = new Forest<ValueNode>(new ValueNode());
    	ValueDictLoader valueDictLoader = new ValueDictLoader();
    	ArrayList<Word> words = valueDictLoader.loadDict();
    	for(Word word : words){
    		forest.addWord(word);
    	}
    	
    	ValueNodeMatcher matcher = new ValueNodeMatcher(forest);
    	String test1 = "O0.1g2/日";
    	boolean found = false;
    	for (int i = 0; i < test1.length(); i++) {
			Term token = matcher.nextChar(test1.charAt(i), i);
			if (token != null && token.getWord().equals("0.1")) {
				found = true;
			}
		}
    	assertTrue(found);
    }
}
