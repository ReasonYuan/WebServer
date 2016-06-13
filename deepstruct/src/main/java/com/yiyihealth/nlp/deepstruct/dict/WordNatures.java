package com.yiyihealth.nlp.deepstruct.dict;

public class WordNatures {
	
	/**
	 * 未定义
	 */
	public static final String UNDEF = "Undef";
	/**
	 * 患者
	 */
	public static final String PATIENT = "Patient";
	/**
	 * 时间
	 */
	public static final String DATE = "Date";
	/**
	 * 用药
	 */
	public static final String DRUG = "Drug";
	/**
	 * 化验项
	 */
	public static final String TESTITEM = "TestItem";
	/**
	 * 数值单位
	 */
	public static final String VALUEUNIT = "ValueUnit";
	/**
	 * 数值
	 */
	public static final String VALUE = "Value";
	/**
	 * 部位
	 */
	public static final String REGION = "Region";
	/**
	 * 存在动词
	 */
	public static final String EXISTS = "Exists";
	/**
	 * 症状
	 */
	public static final String SYMPTOM = "Symptom";
	/**
	 * 检查项
	 */
	public static final String EXAM = "Exam";
	/**
	 * 检查结果
	 */
	public static final String RESULT = "Result";
	/**
	 * 时间副词
	 */
	public static final String TIMEADV = "TimeAdv";
	/**
	 * 趋向动词
	 */
	public static final String DIRVERB = "DirVerb";
	/**
	 * 连接介词
	 */
	public static final String LINKCONJ = "LinkConj";
	/**
	 * 行为动词
	 */
	public static final String ACTADV = "ActAdv";
	/**
	 * 肯定词
	 */
	public static final String WORDYES = "WordYes";
	/**
	 * 否定词
	 */
	public static final String WORDNO = "WordNo";
	/**
	 * 不否定词
	 */
	public static final String WORDUN = "WordUn";//wordUnsure
	/**
	 * 诊断
	 */
	public static final String DIAG = "Diag";//diagnosis
	/**
	 * 频度副词
	 */
	public static final String FREQADV = "FreqAdv";
	/**
	 * 部位量词
	 */
	public static final String REGIONQ = "RegionQ";
	/**
	 * 峰值副词
	 */
	public static final String PVADV = "PvAdv";//peakvalueAdv
	/**
	 * 病状描绘词
	 */
	public static final String DISEASEDES = "DiseaseDes";
	/**
	 * 医院
	 */
	public static final String HOSPITAL = "Hospital";
	/**
	 * 过滤词
	 */
	public static final String FILTER = "Filter";
	/**
	 * 时间长度
	 */
	public static final String PERIOD = "Period";
	/**
	 * 功能
	 */
	public static final String CS = "Cs";//common situation
	/**
	 * 一般情况问诊
	 */
	public static final String CSQ = "Csq";//common situation question
	/**
	 * 标点符号
	 */
	public static final String PUNC = "Punc";//punctuation
	/**
	 * 未识别
	 */
	public static final String UNREC = "Unrec";//unrecongnition
	/**
	 * 治疗行为
	 */
	public static final String TREATMENT = "Treatment";
	/**
	 * 可忽略
	 */
	public static final String IGNORE = "Ignore";
	/**
	 * 部位描绘词
	 */
	public static final String REGIONDES = "RegionDes";
	/**
	 * 段落标题
	 */
	public static final String HEADING = "Heading";
	/**
	 * 病症证物
	 */
	public static final String DISEASEEVI = "DiseaseEvi";
	/**
	 * 模糊范围
	 */
	public static final String ALMOST = "Almost";
	/**
	 * 用药方式
	 */
	public static final String TAKENMETHOD = "TMethod";
	/**
	 * 药品量词
	 */
	public static final String DRUGQUAN = "DrugQuan";
	/**
	 * 药品副词
	 */
	public static final String DRUGADVERB = "DrugAdverb";
	
	/**
	 * 药厂
	 */
	public static final String DRUGFAC = "DrugFac";
	/**
	 * 检查行为动词
	 */
	public static final String ACTCHECK = "ActCheck";
	/**
	 * 部位修正词
	 */
	public static final String REGIONADJ = "RegionAdj";
	/**
	 * 功能描述
	 */
	public static final String CSRESULT = "CSResult";
	/**
	 * 手术
	 */
	public static final String OPERATION = "Operation";
	/**
	 * 人体系统
	 */
	public static final String BODYSYS = "BodySys";
	/**
	 * 检查项内容
	 */
	public static final String EXAMCONTENT = "ExamContent";
	/**
	 * 科室
	 */
	public static final String DEPARTMENT = "Department";
	
	/**
	 * 组织
	 */
	public static final String ORGANIZATION  = "Organization";
	
	/**
	 * 病症描绘词
	 */
	public static final String DISEASEDESCRIPTIONW = "DiseaseDescriptionW";
	
	/**
	 * 既往史
	 */
	public static final String PASTHISTORY = "Pasthistory";
	
	/**
	 *诱因 
	 */
	public static final String INCENTIVE  = "incentive";

	/**
	 * 用0-9和a-zA-Z一个字母表示，后面再加字母表示词性子集<br/>
	 * 第三个元素是谓词名
	 */
	public static String[][] natureNames = {
			{"未定义", UNDEF, "Undef"}, //undefined
			{"患者", PATIENT, "Patient"},
			{"时间", DATE, "Date"},
			{"用药", DRUG, "Drug"},
			{"化验项", TESTITEM, "TestItem"},
			{"数值单位", VALUEUNIT, "ValueUnit"},
			{"数值", VALUE, "Value"},
			{"部位", REGION, "Region"},
			{"存在动词", EXISTS, "Exists"},//不能用exist
			{"症状", SYMPTOM, "Symptom"},
			{"检查项", EXAM, "Exam"},//examination
			{"检查结果", RESULT, "Result"},
			{"时间副词", TIMEADV, "TimeAdv"},
			{"趋向动词", DIRVERB, "DirVerb"},//directionVerb
			{"连接介词", LINKCONJ, "LinkConj"},//linkConjunction
			{"行为动词", ACTADV, "ActAdv"},
			{"肯定词", WORDYES, "WordYes"},
			{"否定词", WORDNO, "WordNo"},
			{"不否定词", WORDUN, "WordUn"},//wordUnsure
			{"诊断", DIAG, "Diag"},//diagnosis
			{"频度副词", FREQADV, "FreqAdv"},
			{"部位量词", REGIONQ, "RegionQ"},
			{"峰值副词", PVADV, "PvAdv"},//peakvalueAdv
			{"病状描绘词", DISEASEDES, "DiseaseDes"},
			{"医院", HOSPITAL, "Hospital"},
			{"过滤词", FILTER, "Filter"},
			{"时间长度", PERIOD, "Period"},
			{"功能", CS, "Cs"},//common situation
			{"一般情况问诊", CSQ, "Csq"},//common situation question
			{"标点符号", PUNC, "Punc"},//punctuation
			{"未识别", UNREC, "Unrec"},//unrecongnition
			{"治疗行为", TREATMENT, "Treatment"},
			{"可忽略", IGNORE, "Ignore"},
			{"部位描绘词", REGIONDES, "RegionDes"},
			{"段落标题", HEADING, "Heading"},
			{"病症证物", DISEASEEVI, "DiseaseEvi"},
			{"模糊范围", ALMOST, "Almost"},
			{"用药方式", TAKENMETHOD, "TMethod"},
			{"药品量词", DRUGQUAN, "DrugQuan"},
			{"药厂", DRUGFAC, "DrugFac"},
			{"检查行为动词", ACTCHECK, "ActCheck"},
			{"部位修正词", REGIONADJ, "RegionAdj"},
			{"功能描述", CSRESULT, "CSResult"},
			{"手术", OPERATION, "Operation"},
			{"人体系统", BODYSYS, "BodySys"},
			{"检查项内容", EXAMCONTENT, "ExamContent"},
			{"科室", DEPARTMENT, "Department"},
			{"组织",ORGANIZATION, "Organization"},
			{"病症描绘词",DISEASEDESCRIPTIONW,"DiseaseDescriptionW"},
			{"既往史",PASTHISTORY,"Pasthistory"},
			{"药品副词",DRUGADVERB,"DrugAdverb"},
			{"诱因",INCENTIVE,"incentive"}
		};

	public static String getNatureReadableName(String name){
		for (int i = 0; i < natureNames.length; i++) {
			if (natureNames[i][1].equals(name)) {
				return natureNames[i][0];
			}
		}
		throw new RuntimeException("不存在根词性： " + name);
	}
	
	public static String getNatureByReadableName(String name){
		for (int i = 0; i < natureNames.length; i++) {
			if (natureNames[i][0].equals(name)) {
				return natureNames[i][1];
			}
		}
		throw new RuntimeException("不存在根词性： " + name);
	}
	
	public static String getNatureFullByReadableName(String name){
		for (int i = 0; i < natureNames.length; i++) {
			if (natureNames[i][0].equals(name)) {
				return natureNames[i][2];
			}
		}
		throw new RuntimeException("不存在根词性： " + name);
	}
	
	public static String getNatureFullByName(String name){
		for (int i = 0; i < natureNames.length; i++) {
			if (natureNames[i][1].equals(name)) {
				return natureNames[i][2];
			}
		}
		throw new RuntimeException("不存在根词性： " + name);
	}
	
}
