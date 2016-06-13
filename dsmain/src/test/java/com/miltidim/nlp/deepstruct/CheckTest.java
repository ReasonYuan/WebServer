package com.miltidim.nlp.deepstruct;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleSyntaxCorrect;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CheckTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CheckTest( String testName )
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
    
    public void testChecker(){
    	//String text = "尿蛋白定量/24H(291,mg/24H,H);血清免疫球蛋白:血清IgG(11.00,g/L),血清IgA(2.70,g/L),血清IgM(0.40,g/L,L),补体C3(1.100,g/L,M),补体C4(0.227,g/L,M)。";
    	//String text = "予骁悉胶囊0.5g口服2/日,强的松片10mg口服1/日控制病情,并予钙尔奇D、罗钙全胶囊补钙,奥克护胃支持治疗,加予迈普新提高免疫力。";
    	//String text = "(2012-1-20)血:C-反应蛋白(1.10,mg/L,M);";
    	//String text = "2.高血压病3级很高危3.肾及输尿管结石";
    	//String text = "2.狼疮性肾炎3.冠状动脉粥样硬化性心脏病?";
    	String text = "我科《一项为期52周的Belimumob与安慰剂相比治疗东北亚地区系统性红斑狼疮SLE受试者的研究》的临床试验";//
    	SimpleSyntaxCorrect checker = new SimpleSyntaxCorrect("./sslogics/simple_syntaxc_orrect.txt");
    	ArrayList<Sentence> sentences = new HealthAnalysis().parse(text);
		checker.correctBasicWords(sentences);
    }

}
