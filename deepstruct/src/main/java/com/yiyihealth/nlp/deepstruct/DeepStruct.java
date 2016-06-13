package com.yiyihealth.nlp.deepstruct;

import java.util.ArrayList;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.Sentence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.NewWordDict;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleSyntaxCorrect;
import com.yiyihealth.nlp.deepstruct.db.CoreBucket;
import com.yiyihealth.nlp.deepstruct.db.CoreBucket.OnQueryEachRecordInfo;
import com.yiyihealth.nlp.deepstruct.db.CouchbaseDBManager;
import com.yiyihealth.nlp.deepstruct.dict.Paper;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.JSONUtil;

/**
 * Hello world!
 *
 */
public class DeepStruct 
{
	
	private static final SimpleSyntaxCorrect checker = new SimpleSyntaxCorrect("./sslogics/simple_syntaxc_orrect.txt");
	private static final SimpleSyntaxCorrect sequencePrinter = new SimpleSyntaxCorrect("./sslogics/simple_evidence_dates_around.txt");
	
	/**
	 * 查找特定序列，方便输出到人工观察文本文件
	 */
	private static final ArrayList<String> sequence4Rules = new ArrayList<>();
	
    public static void main( String[] args ) 
    {
    	
    	System.out.println("病历类别: 入院诊断:1, 出院诊断:5, 其他:9, 检查: 4, 治疗方案:10, 手术名称:3");
    	System.out.println("参数：开始位置 病历数 [病历类别] [病历类别] [病历类别] ...(最多6个参数)");
    	
    	//new HealthAnalysis().parse("");
    	
    	System.out.println("正在连接数据库...");
    	
    	String host = "http://localhost:8091";
    	String bucket = "test";
//    	String host = "http://121.199.16.216:8091";
//    	String bucket = "research";
//    	try {
//    		String string = FileUtils.fileRead(new File("./conf/host.conf"));
//    		JSONObject conf = JSONObject.parseObject(string);
//    		host = conf.getString("host");
//    		bucket = conf.getString("bucket");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    	
    	CoreBucket testBucket = new CoreBucket(CouchbaseDBManager.getBucket(host, bucket));
//    	CoreBucket testBucket = new CoreBucket(CouchbaseDBManager.getBucket("http://121.199.16.216:8091", "research"));
    	
    	System.out.println("正在查询数据...");
    	//TODO 临时代码，第一轮增加根据推理增加的词，第二轮才入库
    	searchDB(testBucket, args, false);
    	try {
    		Thread.sleep(2000);
		} catch (Exception e) {
		}
    	System.out.println("second turn...");
    	searchDB(testBucket, args, true);
    	
    	NewWordDict.getInstance().debugPrintAll();
    	//JsonDocument oneDoc = testBucket.getDocument("doctor_13391236437_record_100075");
    	
    	//System.out.println(oneDoc.content().getArray("info").getObject(0).getString("note_info").replace("\\n", ""));
    	StringBuffer sBuffer = new StringBuffer();
    	for (String debugDate : sequence4Rules) {
			sBuffer.append(debugDate).append("\n");
		}
    	FileManager.writeToFile("./checkout_dates.txt", sBuffer.toString());
    }
    
    private static void searchDB(CoreBucket testBucket, String[] args, boolean store2DB){
    	String doctors = "meta().id like \"doctor_13671609763%\" "
    			+ "or meta().id like \"doctor_13916265159%\" "
    			+ "or meta().id like \"doctor_13916927066%\" "
    			+ "or meta().id like \"doctor_13601921066%\"";
    	//默认值
    	final int[] params = {0, 10000, 5, 1, 9, 4, 10, 3};
    	int typeCnt = 0;
    	for (int i = 0; i < params.length && i < args.length; i++) {
			try {
				params[i] = Integer.parseInt(args[i]);
			} catch (Exception e) {
			}
		}
    	for (int i = 0; i < params.length; i++){
    		if (i >= 2 && params[i] > 0) {
    			typeCnt++;
    		}
    	}
    	String type = "info[0].record_type = %d";
		if (typeCnt == 0) {
			throw new RuntimeException("必须提供病历类型, 病历类型为数字!");
		}
    	String types = "";
    	for (int i = 2; i < params.length; i++) {
    		if(params[i] > 0){
    			types += String.format(type, params[i]);
        		typeCnt--;
        		if (typeCnt > 0) {
        			types += " or ";
    			}
    		}
		}
    	
    	final int[] cnt = new int[10000];
    	//EvidenceWritter evidenceWritter = new EvidenceWritter("../dsdata/projects/sentence_tells/v01/evidences/evidences.txt");
    	//SimpleEvidenceSearcher seSearcher = new SimpleEvidenceSearcher("../dsdata/projects/sentence_tells/v01/esearch.txt", evidenceWritter);
    	//ArrayList<EWord> allWords = new ArrayList<>();
    	testBucket.listNoteInfos(doctors, params[0], params[1], types, new OnQueryEachRecordInfo() {
			@Override
			public void onEachRecordInfo(JsonObject object) {
				//System.out.println(object);
				try {
					System.out.println("read one file..........!");
		    		String noteinfo = object.getString("note_info").replace("\\n", "");
		    		JsonArray jsonArray = JsonArray.fromJson(noteinfo);
		    		System.out.println((cnt[0]+++params[0]) + "----------|-----------" + object.getString("id") + "------");
		    		ArrayList<Term> words = doStruct(jsonArray, testBucket, object.getString("id"), store2DB);
		    		//allWords.addAll(words);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    	
//    	if (store2DB) {
//    		//TODO remove following code
//    		//System.out.println("write evidences file...........!");
//    		//seSearcher.searchSimpleEvidence(allWords, seSearcher.getSentencePoses(allWords), new SearchOffset(0));
//    		//seSearcher.writeEvidence2File();
//		}
    }
    
    private static ArrayList<Term> doStruct(JsonArray jsonArray, CoreBucket bucket, String documentID, boolean storeToDB){
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
//						JSONArray section = new JSONArray();
						checker.correctBasicWords(sentences);
						if (storeToDB) {
							//仅为人工查看数据情况
//							ArrayList<String> sequences = sequencePrinter.correctBasicWords((ArrayList<Sentence>) sentences);
//							sequence4Rules.addAll(sequences);
						}
						paper.addSection(name, sentences);
					}
				}
			}
		}
    	
    	if (storeToDB) {
    		JSONArray jsonWords = new JSONArray();
        	jsonObject.put("data", jsonWords);
        	jsonObject.put("documentID", documentID);
        	JSONUtil.toJson(paper, jsonWords);
        	//System.out.println("debug: " + jsonObject.toJSONString());
        	bucket.saveStructDocument(documentID, "v02", jsonObject);
        	try {
        		//Thread.sleep(200);
    		} catch (Exception e) {
    		}
        	JSONUtil.writeJson4ESearch(jsonObject, documentID, "../dsdata/records/");
		}
    	
    	return paper.getWords();
    }
    
    private static final String[] VALID_TITLES = {"时间", "日期", "主诉", "史", "检查", "诊断", "入院时主要症状和体征", "诊治情况", "主要化验结果及检查"};
    private static boolean isNeedDoStruct(String name){
    	//return true;
    	for (int i = 0; i < VALID_TITLES.length; i++) {
			if (name.indexOf(VALID_TITLES[i]) >= 0) {
				return true;
			}
		}
    	return false;
    }
    
    
}
