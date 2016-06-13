package com.yiyihealth.nlp.deepstruct.analysis;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.csense.NBRec.IllegalCSListener;
import com.yiyihealth.nlp.deepstruct.dict.HitalesDict;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.dict.Word;
import com.yiyihealth.nlp.deepstruct.dict.WordFactory;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.DateForest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.DateNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.Forest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.NormalForest;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.UnitNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.ValueNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.DateNodeMatcher;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.NormalNodeMatcher;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.UnitMatcher;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.matcher.ValueNodeMatcher;

public class HealthAnalysis {

	private static HitalesDict dict = HitalesDict.getHealthCareDict();
	// private List<Term> recongnition = dict.retreiveWordsNatures();
	private static NormalForest normalForest = new NormalForest();
	private static DateForest dateForest = new DateForest();
	private static Forest<ValueNode> valueForest = new Forest<ValueNode>(new ValueNode());
	private static Forest<UnitNode>	unitForest = new Forest<UnitNode>(new UnitNode());
	
	private static ArrayList<String> illegalInfos = new ArrayList<>();
	private static IllegalCSListener listener = new IllegalCSListener() {
		@Override
		public void onIllegal(String context) {
			illegalInfos.add(context);
		}
	};
	
	static {
		ArrayList<Word> words = dict.getNormalWords();
		for (Word word : words) {
			normalForest.addWord(word);
		}

		ArrayList<Word> dateWords = dict.getDateWords();
		for (Word word : dateWords) {
			DateNode dateNode = (DateNode) dateForest.addWord(word);
			if (dateNode.getParseForamt() == null) {
				dateNode.setParseForamt(word.getMeanings().get(0).getParseFormat());
			}
		}

		ArrayList<Word> valueWords = dict.getValueWords();
		for (Word word : valueWords) {
			valueForest.addWord(word);
		}
		
		ArrayList<Word> unitWords = dict.getUnitWords();
		for (Word word : unitWords) {
			unitForest.addWord(word);
		}
	}
	
	/**
	 * 获得不符合常识的内容
	 * @return
	 */
	public static ArrayList<String> getIllegalCSInfos(){
		return illegalInfos;
	}
	
	/**
	 * 部分切割后的字符重新解析
	 */
	private static HealthAnalysis partAnalysis = new HealthAnalysis();

	public HealthAnalysis() {

	}
	
	public static void addNormalWord(String text, String nature){
		Word word = WordFactory.createWordWithNatures(text, nature);
		normalForest.addWord(word);
	}
	
	public static NormalForest getNormalForest() {
		return normalForest;
	}

	public ArrayList<Sentence> parse(String logTag, String text) {
		NormalNodeMatcher<CharNode> normalMatcher = new NormalNodeMatcher<CharNode>(normalForest);
		DateNodeMatcher dateMatcher = new DateNodeMatcher(dateForest);
		UnitMatcher unitMatcher = new UnitMatcher(unitForest);
		ValueNodeMatcher valueMatcher = new ValueNodeMatcher(valueForest);
		//String tString = "患者于2001年9月无明显诱因下出现关节痛,10月出现发热、血白细胞、血小板下降、皮疹,查ANA+, ds-DNA+,诊断为“SLE”,给予强的松片60mg/d治疗,并规律减量,查尿常规提示隐血+,使用CTX,每月一次半年,每三月一次6次,每半年一次2次,其剂量不详,约2年时间。于 2004年停用CTX,激素以2片/d维持病情稳定。04年5月发热,查骨髓培养:金黄色葡萄球菌,给予 泰能+万古抗炎治疗好转,激素以2片/d维持,病情稳定。2006年出现尿蛋白3+,加强的松片4片/d 及纷乐0.1bid、硫唑嘌呤2片qd治疗。2007年出现右侧股骨头坏死,2008.2出现头痛伴恶心、呕 吐、发热,CSF提示隐脑,给予两性霉素25mg/d4月改伊曲康唑液维持治疗3月,并同时口服强的松4片/d维持。2009年1月减强的松片2片/d。4月2日出现发热,并出现排尿不畅、双下肢无力,查血隐球菌乳胶定性试验(+),脑脊液隐球菌乳胶凝集试验(-),查24小时尿蛋白0.925g,予以伊曲康唑治疗后病情稳定,大小便正常,肢体乏力好转后出院。2009年10月出现蛋白尿增加,具体量不 详,并有双下肢浮肿,血肌酐尚正常(具体不详),曾予以“反应停”因出现麻木停药,后改用 “爱若华”半年,因贫血、脱发、蛋白尿未解停用,后改雷公藤3月余,因无效停用。现患者蛋白尿 逐渐增加,最高24小时3.9g,并有肌酐逐渐升高,现血肌酐156umol/L,并有全身浮肿,血压偏高,现为进一步治疗收住入院。患者自起病以来,精神可,胃纳可,大便如常,其他,体重未见明显下降。哈哈哈的，下降使得肌肤";
    	//text = "患者于1997年起出现颜面部红斑,伴有手足指(趾)端冻疮样红斑,日晒后加重,伴有 高血压,伴有冬季双手发紫,双下肢浮肿,无发热,无关节肿痛,曾在当地医院就诊,考虑“系统性红斑狼疮可能”,曾给予强的松6#/d+氯喹治疗,患者不规则用药。至2005年1月,患者在北京协 和医院就诊,查ANA1:640,ds-DNA阳性,抗SSA、SSB阳性,尿常规:蛋白2-3+,24小时尿蛋白 15.3g,曾建议肾穿刺检查及强的松8#/d治疗,患者拒绝,并未规律治疗。至2007年,患者在上海龙华医院就诊,当时服用强的松4#/d,予加用CTX0.6/3周,共20余次(2008.10),蛋白尿维持在 3-4g/24h,至2009开始患者出现血肌酐升高,150-174umol/L左右,伴有反复双下肢浮肿,夜尿增 多,1-2次/夜,开始口服爱诺华10mg/d,强的松3#/d治疗,蛋白尿最少控制在0.4g/d,但血压抑 制较高,180/110mmHg左右,半年前,患者双下肢浮肿明显,不能消退,8.15复查血常规“WBC9.15*10~9/L,Hb116g/L,PLT213*10~9/L,Scr266umol/L,UA667umol/L, BUN24.4mmol/L,现患者为进一步诊治,来我院就诊,收治入院。患者自起病以来,精神略差,胃纳可,大便如常,小便如常,体重未见明显下降。 既往高血压病史10余年,最高210/110mmHg,长期口服可乐定、洛丁新等降压药物,血压控制欠佳。";
    	//未在词典中查到的单词
    	text = text.replace(" ", "");
    	//unitmatcher一定要在valuematcher后
    	
    	String[][] transWords = {{"—", "-"}};
    	for (int i = 0; i < transWords.length; i++) {
    		text = text.replace(transWords[i][0], transWords[i][1]);
		}
    	
    	WordSplit wordSplit = new WordSplit(text, listener, normalMatcher, dateMatcher, valueMatcher, unitMatcher);
    	wordSplit.split();
    	wordSplit.predicateNatures(logTag);
    	
    	return wordSplit.getSentences();
	}
	
	public ArrayList<Sentence> parse(String text) {
		return parse("", text);
	}
	
	public static Term parseCutStr(String cutStr){
		ArrayList<Sentence> sentences = partAnalysis.parse(cutStr);
		if (sentences.size() == 1 && sentences.get(0).getWords().size() == 1) {
			Term term = (Term) sentences.get(0).getWords().get(0);
			return term;
		}
		return null;
	}

}

