package com.yiyihealth.ai.dsmain.nlp;

import java.util.ArrayList;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleSyntaxCorrect;
import com.yiyihealth.nlp.deepstruct.dict.Paper;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.utils.JSONUtil;

public class NlpParser {
	
	private SimpleSyntaxCorrect checker;
	
	public NlpParser(String projectDir){
		checker = new SimpleSyntaxCorrect(projectDir + "/../../simple_syntax/simple_syntax_correct.txt");
	}

	/**
	 * 对原始数据分词, 然后存入证据文件里
	 * @param jsonArray
	 * @param documentID
	 * @param dir2save 存放分词结果的目录
	 * @param save2File TODO 这是词典很小的时候的临时方案，根据简单语法规则推测新词并在下轮用新词重新分词
	 * @return
	 */
	public ArrayList<Term> parse(JsonArray jsonArray, String documentID, String dir2save, boolean save2File){
    	JSONObject jsonObject = new JSONObject();
    	Paper paper = new Paper();
    	for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject object = jsonArray.getObject(i);
			Set<String> names = object.getNames();
			for(String name: names){
				if (!name.equals("index")) {
					String content = object.getString(name);
					if (isNeedDoStruct(name)) {
						//System.out.println("==begin==\n" + name + " : " + content);
						//String tString = "患者于2001年9月无明显诱因下出现关节痛,10月出现发热、血白细胞、血小板下降、皮疹,查ANA+, ds-DNA+,诊断为“SLE”,给予强的松片60mg/d治疗,并规律减量,查尿常规提示隐血+,使用CTX,每月一次半年,每三月一次6次,每半年一次2次,其剂量不详,约2年时间。于 2004年停用CTX,激素以2片/d维持病情稳定。04年5月发热,查骨髓培养:金黄色葡萄球菌,给予 泰能+万古抗炎治疗好转,激素以2片/d维持,病情稳定。2006年出现尿蛋白3+,加强的松片4片/d 及纷乐0.1bid、硫唑嘌呤2片qd治疗。2007年出现右侧股骨头坏死,2008.2出现头痛伴恶心、呕 吐、发热,CSF提示隐脑,给予两性霉素25mg/d4月改伊曲康唑液维持治疗3月,并同时口服强的松4片/d维持。2009年1月减强的松片2片/d。4月2日出现发热,并出现排尿不畅、双下肢无力,查血隐球菌乳胶定性试验(+),脑脊液隐球菌乳胶凝集试验(-),查24小时尿蛋白0.925g,予以伊曲康唑治疗后病情稳定,大小便正常,肢体乏力好转后出院。2009年10月出现蛋白尿增加,具体量不 详,并有双下肢浮肿,血肌酐尚正常(具体不详),曾予以“反应停”因出现麻木停药,后改用 “爱若华”半年,因贫血、脱发、蛋白尿未解停用,后改雷公藤3月余,因无效停用。现患者蛋白尿 逐渐增加,最高24小时3.9g,并有肌酐逐渐升高,现血肌酐156umol/L,并有全身浮肿,血压偏高,现为进一步治疗收住入院。患者自起病以来,精神可,胃纳可,大便如常,其他,体重未见明显下降。";
						//List<Term> terms = ToAnalysis.parse(content);
						//System.out.println("words : " + terms);
						ArrayList<Sentence> sentences = new HealthAnalysis().parse(content);
						checker.correctBasicWords(sentences);
						paper.addSection(name, sentences);
					}
				}
			}
		}
    	
    	if (save2File) {
    		JSONArray jsonWords = new JSONArray();
        	jsonObject.put("data", jsonWords);
        	jsonObject.put("documentID", documentID);
        	JSONUtil.toJson(paper, jsonWords);
        	JSONUtil.writeJson4ESearch(jsonObject, documentID, dir2save);
		}
    	
    	return paper.getWords();
    }
	
	//TODO 删除DeepStruct.java里和一下一样的代码
    private static final String[] VALID_TITLES = {"时间", "日期", "主诉", "史", "检查", "诊断", "入院时主要症状和体征", "诊治情况", "主要化验结果及检查", "检查", "出院", "入院", "record"};
    private static boolean isNeedDoStruct(String name){
    	return true;
//    	for (int i = 0; i < VALID_TITLES.length; i++) {
//			if (name.indexOf(VALID_TITLES[i]) >= 0) {
//				return true;
//			}
//		}
//    	return false;
    }
}
