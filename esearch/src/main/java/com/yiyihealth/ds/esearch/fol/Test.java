package com.yiyihealth.ds.esearch.fol;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ds.esearch.fol.FolParser.FormulaOrPredicate;
import com.yiyihealth.nlp.deepstruct.db.CoreBucket;
import com.yiyihealth.nlp.deepstruct.db.CouchbaseDBManager;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class Test {
	
	private static CoreBucket testBucket;
	
	private static String host = "http://121.199.16.216:8091";
	private static String bucket = "research";

	public static void main(String[] args) {
		
		System.out.println("正在连接数据库...");
//		String host = "http://localhost:8091";
//		String bucket = "test";

		LogicLoader<FolParser, FormulaOrPredicate> formulaLoader = new LogicLoader<FolParser, FormulaOrPredicate>(
				new FolParser());
		ArrayList<FormulaOrPredicate> folOrPres = formulaLoader.loadLogics("./beliefbase/yiyibelief.logic");
		Object[] fs = (Object[]) folOrPres.toArray();
		ArrayList<Formula> formulas = new ArrayList<Formula>();
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		for (int i = 0; i < fs.length; i++) {
			FormulaOrPredicate formulaOrPredicate = (FormulaOrPredicate) fs[i];
			if (formulaOrPredicate.formula != null) {
				formulas.add(formulaOrPredicate.formula);
			} else if (formulaOrPredicate.predicate != null) {
				predicates.add(formulaOrPredicate.predicate);
			}
		}

		File dir = new File("./evidences/");
		String[] files = dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".evi");
			}
		});

		for (int i = 0; i < files.length; i++) {
			// if (files[i].equals("doctor_13601921066_record_2222.json.evi")) {
			timeline(formulas, predicates, "./evidences/" + files[i], "./evidences/" + files[i] + ".db",
					files[i].substring(0, files[i].indexOf(".")));
			// }
			System.out.println(i + " done!");
		}
	}

	private static void timeline(ArrayList<Formula> formulas, ArrayList<Predicate> predicates, String evidenceFile,
			String dbFile, String doucmentID) {
		LogicLoader<PredicateParser, Predicate> evidencesLoader = new LogicLoader<PredicateParser, Predicate>(
				new PredicateParser());
		ArrayList<Predicate> evidences = evidencesLoader.loadLogics(evidenceFile);

		// System.out.println("evidences: " + evidences);
		// System.out.println("formulas: " + formulas);
		// System.out.println("predicates: " + predicates);

		ArrayList<Predicate> illegalEvidences = FolLoadHelper.findIllegalEvidences(evidences, predicates);
		if (illegalEvidences.size() > 0) {
			System.out.println("illegal evidences: " + illegalEvidences);
			throw new RuntimeException("证据谓词不合法!");
		}

		// TODO 必须验证证据符合谓词定义

		InherenceEngine engine = new InherenceEngine(formulas, evidences, dbFile);
		engine.doInherence();

		Atom[] atoms = engine.queryAtom("TagDate");
		for (int i = 0; i < atoms.length; i++) {
			System.out.println(i + ": " + atoms[i].toString());
			MemDB memDB = engine.getFolNetwork().getMemDB();
			memDB.tagDate(atoms[i],"");
		}
		ArrayList<EWord> finalWords = engine.getFolNetwork().getMemDB().toEWords();
		saveStructDocument(doucmentID, "v03", finalWords);

		// System.out.println("1: " + engine.query("Smoke", "Anne"));
		// System.out.println("2: " + engine.query("Smoke", "BOB"));
		// System.out.println("3: " + engine.query("Cancer", "BOB"));
		// System.out.println("4: " + engine.query("Friend", "BOB", "Anne"));
		// System.out.println("5: " + engine.query("Cancer", "BB"));
		//
		// System.out.println("6: " + engine.query("DateRefToSentence", "17",
		// "4"));
		//
		// System.out.println("7: " + engine.query("ZhangXing", "张三"));
		// System.out.println("8: " + engine.query("ZhangXing", "张伟"));
		// System.out.println("9: " + engine.query("ZhangXing", "李四"));
		// System.out.println("7: " + engine.queryAtom("ZhangXing", "张三"));
		// System.out.println("8: " + engine.queryAtom("ZhangXing", "张伟"));
		// System.out.println("9: " + engine.queryAtom("ZhangXing", "李四"));
	}
	
	   //TODO 需要重构
    public static void saveStructDocument(String documentID, String version, ArrayList<EWord> words){
    	JSONObject jsonObject = new JSONObject();
    	JSONArray jsonWords = new JSONArray();
    	jsonObject.put("data", jsonWords);
    	jsonObject.put("documentID", documentID);
    	
		for (int k = 0; k < words.size(); k++) {
			JSONObject item = new JSONObject();
			EWord word = words.get(k);
			item.put("word", word.getWord());
			item.put("nature", word.getNature());
			item.put("natureChinese", WordNatures.getNatureReadableName(word.getNature()));
			if (word.getAttributes().size() > 0) {
				item.put("attributes", word.getAttributes());
			}
			if (word.getTaggedDates().size() > 0) {
				item.put("refDates", JSONArray.toJSON(word.getTaggedDates()));
			}
			jsonWords.add(item);
		}
    	
		try {
			if (testBucket == null) {
				testBucket = new CoreBucket(CouchbaseDBManager.getBucket(host, bucket));
			}
			testBucket.saveStructDocument(documentID, version, jsonObject);
		} catch (Exception e) {
			testBucket = new CoreBucket(CouchbaseDBManager.getBucket(host, bucket));
			try {
				testBucket.saveStructDocument(documentID, version, jsonObject);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
    }

}
