package com.miltidim.nlp.deepstruct;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yiyihealth.nlp.deepstruct.dict.HitalesDict;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.NormalForest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.NormalNodeMatcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HitalesForestTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HitalesForestTest( String testName )
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
    
    public void testDate() throws ParseException{
    	{
    		//yy年mm月dd日	04年05月02日,12年11月14日
        	SimpleDateFormat sdf = new SimpleDateFormat("yy年mm月dd日");
        	SimpleDateFormat sdffull = new SimpleDateFormat("yyyy年mm月dd日");
        	String f1 = "57年05月02日";
        	String f2 = "12年11月14日";
        	String f3 = "12年2月7日";
        	Date date1 = (Date) sdf.parse(f1);
        	Date date2 = (Date) sdf.parse(f2);
        	Date date3 = (Date) sdf.parse(f3);
        	assertEquals(sdffull.format(date1), "1957年05月02日");
        	assertEquals(sdffull.format(date2), "2012年11月14日");
        	assertEquals(sdffull.format(date3), "2012年02月07日");
    	}
    	{
    		//yyyy-mm-dd	2014-3-12,2015-1-04
    		SimpleDateFormat sdf = new SimpleDateFormat("yy-mm-dd");
        	SimpleDateFormat sdffull = new SimpleDateFormat("yyyy年mm月dd日");
        	String f1 = "14-3-12";
        	String f2 = "57-1-04";
        	Date date1 = (Date) sdf.parse(f1);
        	Date date2 = (Date) sdf.parse(f2);
        	assertEquals(sdffull.format(date1), "2014年03月12日");
        	assertEquals(sdffull.format(date2), "1957年01月04日");
    	}
    }
    
    public void testForest() throws JsonProcessingException{
    	NormalForest forest = new NormalForest();
    	String[] tw = {"你", "我", "我们", "我们不是", "我们不是超人", "我们是勤劳的小蜜蜂", "中国", "美国", "日本"};
    	for (int i = 0; i < tw.length; i++) {
			forest.addWord(WordFactory.createWord(tw[i]));
		}
    	ArrayList<String> words = forest.listWords();
    	
//    	forest.debugDump();
//    	for (Iterator<String> iterator = words.iterator(); iterator.hasNext();) {
//			String string = (String) iterator.next();
//			System.out.println(string);
//		}
    	
    	assertEquals(tw.length, words.size());
    	assertEquals(forest.getWordEndNode(tw[3]).getWord(), tw[3]);
    	assertEquals(forest.getWordEndNode(tw[0]).getWord(), tw[0]);
    	assertEquals(forest.getWordEndNode(tw[4]).getWord(), tw[4]);
    	for (int i = 0; i < tw.length; i++) {
    		assertTrue(forest.searchWord(tw[i]));
		}
    }
    
    public void testRecognization(){
    	Forest<CharNode> forest = new Forest<CharNode>(new CharNode());
    	ArrayList<Word> words = HitalesDict.loadNormalDict();
    	for(Word word : words){
    		forest.addWord(word);
    	}
    	//forest.debugDump();
    	
    	NormalNodeMatcher<CharNode> matcher = new NormalNodeMatcher<CharNode>(forest);
    	//String test1 = "患者于1997年起出现颜面部红斑,伴有手足指(趾)端冻疮样红斑,日晒后加重,伴有 高血压,伴有冬季双手发紫,双下肢浮肿,无发热,无关节肿痛,曾在当地医院就诊,考虑“系统性红斑狼疮可能”,曾给予强的松6#/d+氯喹治疗,患者不规则用药。至2005年1月,患者在北京协 和医院就诊,查ANA1:640,ds-DNA阳性,抗SSA、SSB阳性,尿常规:蛋白2-3+,24小时尿蛋白 15.3g,曾建议肾穿刺检查及强的松8#/d治疗,患者拒绝,并未规律治疗。至2007年,患者在上海龙华医院就诊,当时服用强的松4#/d,予加用CTX0.6/3周,共20余次(2008.10),蛋白尿维持在 3-4g/24h,至2009开始患者出现血肌酐升高,150-174umol/L左右,伴有反复双下肢浮肿,夜尿增 多,1-2次/夜,开始口服爱诺华10mg/d,强的松3#/d治疗,蛋白尿最少控制在0.4g/d,但血压抑 制较高,180/110mmHg左右,半年前,患者双下肢浮肿明显,不能消退,8.15复查血常规“WBC9.15*10~9/L,Hb116g/L,PLT213*10~9/L,Scr266umol/L,UA667umol/L, BUN24.4mmol/L,现患者为进一步诊治,来我院就诊,收治入院。";
    	//String test1 = "2008.10sdf";
    	String test2 = "曾建议肾穿刺检查及强的松8#/d治疗。";
    	boolean found = false;
    	for (int i = 0; i < test2.length(); i++) {
    		Term token = matcher.nextChar(test2.charAt(i), i);
			if (token != null) {//2008.10
				if (token.getWord().equals("肾穿刺检查")) {
					found = true;
				}
			}
		}
    	assertTrue(found);
    	
    	matcher = new NormalNodeMatcher<CharNode>(forest);
    	String test3 = "无水肿。";//全身关节无畸形,无压痛,双下肢
    	boolean found3 = false;
    	for (int i = 0; i < test3.length(); i++) {
    		Term token = matcher.nextChar(test3.charAt(i), i);
			if (token != null) {
				if (token.getWord().equals("水肿")) {
					found3 = true;
				}
			}
		}
    	assertTrue(found3);
    	
    }
}
