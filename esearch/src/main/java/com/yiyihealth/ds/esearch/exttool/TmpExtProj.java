package com.yiyihealth.ds.esearch.exttool;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ds.esearch.fol.Atom;
import com.yiyihealth.ds.esearch.fol.FolParser;
import com.yiyihealth.ds.esearch.fol.FolParser.FormulaOrPredicate;
import com.yiyihealth.ds.esearch.fol.Formula;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;
import com.yiyihealth.ds.esearch.fol.InherenceEngineFactory;
import com.yiyihealth.ds.esearch.fol.LogicLoader;
import com.yiyihealth.ds.esearch.fol.MemDB;
import com.yiyihealth.ds.esearch.fol.Predicate;
import com.yiyihealth.ds.esearch.fol.PredicateParser;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class TmpExtProj {
	
	public static void main(String[] args) {
		
//		String host = "http://localhost:8091";
//		String bucket = "test";
		 
		// try {
		// String string = FileUtils.fileRead(new File("./conf/host.conf"));
		// JSONObject conf = JSONObject.parseObject(string);
		// host = conf.getString("host");
		// bucket = conf.getString("bucket");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		
		TempDrugExt.preProcess();

		LogicLoader<FolParser, FormulaOrPredicate> formulaLoader = new LogicLoader<FolParser, FormulaOrPredicate>(
				new FolParser());
		ArrayList<FormulaOrPredicate> folOrPres = formulaLoader.loadLogics("./tmp_drug_proj/beliefbase/yiyibelief.logic");//
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

		File dir = new File("./tmp_drug_proj/evidences/");
		String[] files = dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".evi");
			}
		});
		
		ArrayList<ArrayList<Formula>> seqFormulas = loadSequenceFormulas();

		for (int i = 0; i < files.length; i++) {
			// if (files[i].equals("doctor_13601921066_record_2222.json.evi")) {
			System.out.println("evi file: " + "./tmp_drug_proj/evidences/" + files[i]);
			timeline(formulas, predicates, "./tmp_drug_proj/evidences/" + files[i], "./tmp_drug_proj/evidences/" + files[i] + ".db",
					files[i].substring(0, files[i].indexOf(".")), seqFormulas);
			// }
			System.out.println(i + " done!");
		}
	}
	
	private static ArrayList<ArrayList<Formula>> loadSequenceFormulas(){
		ArrayList<ArrayList<Formula>> formulasList = new ArrayList<ArrayList<Formula>>();
		//TODO 不要写死代码
		final String[] otherLogics = {
				"./tmp_drug_proj/beliefbase/tag_admission_date.logic"
			};
		LogicLoader<FolParser, FormulaOrPredicate> formulaLoader = new LogicLoader<FolParser, FormulaOrPredicate>(
				new FolParser());
		for (int j = 0; j < otherLogics.length; j++) {
			ArrayList<FormulaOrPredicate> folOrPres = formulaLoader.loadLogics(otherLogics[j]);
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
			formulasList.add(formulas);
		}
		return formulasList;
	}

	private static void timeline(ArrayList<Formula> formulas, ArrayList<Predicate> predicates, String evidenceFile,
			String dbFile, String doucmentID, ArrayList<ArrayList<Formula>> sequenceFormulas) {
		LogicLoader<PredicateParser, Predicate> evidencesLoader = new LogicLoader<PredicateParser, Predicate>(
				new PredicateParser());
		ArrayList<Predicate> evidences = evidencesLoader.loadLogics(evidenceFile);

		// System.out.println("evidences: " + evidences);
		// System.out.println("formulas: " + formulas);
		// System.out.println("predicates: " + predicates);

		ArrayList<Predicate> illegalEvidences = findIllegalEvidences(evidences, predicates);
		if (illegalEvidences.size() > 0) {
			System.out.println("illegal evidences: " + illegalEvidences);
			throw new RuntimeException("证据谓词不合法!");
		}

		// TODO 必须验证证据符合谓词定义

		InherenceEngine engine = InherenceEngineFactory.create(evidenceFile, dbFile, formulas, predicates) ;//new InherenceEngine(formulas, evidences, dbFile);
		System.out.println(0 + "->基本逻辑...");
		engine.doInherence();
		for (int i = 0; i < sequenceFormulas.size(); i++) {
			for(Formula formula : sequenceFormulas.get(i)){
				engine.addFormula(formula);
			}
			System.out.println((i+1) + "->后续逻辑...");
			engine.doInherence();
		}

		Atom[] atoms = engine.queryAtom("TagDate");
		printAtoms(atoms);
		for (int i = 0; i < atoms.length; i++) {
			MemDB memDB = engine.getFolNetwork().getMemDB();
			memDB.tagDate(atoms[i],"");
		}
		Atom[] haveAdmission = engine.queryAtom("IsAdmissionDate");
		Atom[] jiliang = engine.queryAtom("JiLiang");
		printAtoms(haveAdmission);
		printAtoms(jiliang);
		try {
			engine.getFolNetwork().getMemDB().writeDrugExcel(evidenceFile, jiliang, haveAdmission, "./tmp_drug_proj/output_json/drugs.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		ArrayList<EWord> finalWords = engine.getFolNetwork().getMemDB().toEWords();
//		saveStructDocument(doucmentID, "v03", finalWords);

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
	
	private static void printAtoms(Atom[] atoms){
		for (int i = 0; i < atoms.length; i++) {
			System.out.println(i + ": " + atoms[i].toString());
		}
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
		
		
    	
    }

	private static ArrayList<Predicate> findIllegalEvidences(ArrayList<Predicate> evidences,
			ArrayList<Predicate> predicates) {
		ArrayList<Predicate> illegalEvidences = new ArrayList<Predicate>();
		for (int j = 0; j < evidences.size(); j++) {
			Predicate evidence = evidences.get(j);
			boolean legal = false;
			for (int i = 0; i < predicates.size(); i++) {
				if (predicates.get(i).getName().equals(evidence.getName())) {
					if (predicates.get(i).getParams().size() == evidence.getParams().size()) {
						legal = true;
						break;
					}
				}
			}
			if (!legal) {
				illegalEvidences.add(evidence);
			}
		}
		return illegalEvidences;
	}

}
