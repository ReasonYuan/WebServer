package com.yiyihealth.ai.dsmain.medicine.wx;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ai.dsmain.db.CouchbaseRecordFetcher;
import com.yiyihealth.ai.dsmain.db.StructRecords;
import com.yiyihealth.ai.dsmain.logic.BasicLogic;
import com.yiyihealth.ai.dsmain.logic.TimeLineLogic;
import com.yiyihealth.ai.timeline.TimeUtil;
import com.yiyihealth.ds.esearch.fol.Atom;
import com.yiyihealth.ds.esearch.fol.FolLoadHelper;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;
import com.yiyihealth.ds.esearch.fol.InherenceEngineFactory;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.EvidenceWritter;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager.OnRecordLoadListener;

public class DrugMain {

	public static void main(String[] args) {

		try {
			// 读取配置文件
			JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
			final String projectDir = config.getString("projectDir");
			String n1sqlFile = config.getString("n1sqlFile");
			String version = config.getString("version");
			String host = config.getString("host");
			String bucket = config.getString("bucket");
			boolean fetchDataFromDB = config.getBooleanValue("fetchDataFromDB");
			boolean storeResultsToDB = config.getBooleanValue("storeResultsToDB");
			boolean doNlp = config.getBooleanValue("doNlp");
			boolean doESearch = config.getBooleanValue("doESearch");

			// 1. 从服务器读取原始数据
			if (fetchDataFromDB) {
				// 从服务器抓取数据并存放到工程目录的original目录
				new CouchbaseRecordFetcher(projectDir, n1sqlFile, host, bucket).fetchRecords();
			}

			// 2. do nlp
			if (doNlp) {
//				new CouchbaseRecordParser().parseWord(projectDir);
				new JsonRecordParser().parseWord(projectDir);
			}

			// 3. 读取分词结果，查询证据
			if (doESearch) {
				final int[] cnt = new int[1];
				new FileManager().loadWords(projectDir + "/nlpwords/", new OnRecordLoadListener() {
					public void onRecordLoad(String filename, ArrayList<EWord> allWords) {
						if (allWords.size() != 0) {
							ArrayList<ArrayList<EWord>> oneRecord = new ArrayList<ArrayList<EWord>>();
							oneRecord.add(allWords);
							EvidenceWritter writter = new EvidenceWritter(
									projectDir + "/evidences/" + filename + ".evi");
							SimpleEvidenceSearcher searcher = new SimpleEvidenceSearcher(
									projectDir + "/../../simple_syntax/esearch.txt", writter);
							ArrayList<WordPos> wordPoses = searcher.searchSimpleEvidence(oneRecord);
							writter.writeToFile();
							writter.appendToFile(wordPoses);
							// 进入推理查询库
							Object object = JSON.toJSON(wordPoses);
							String wordsJson = object.toString();
							FileManager.writeToFile(projectDir + "/evidences/" + filename + ".evi" + ".db", wordsJson);
							cnt[0]++;
						}
					}
				});
				System.out.println("共有病历记录: " + cnt[0]);
			}

			// 4. 逻辑处理
			File dir = new File(projectDir + "/evidences");
			String[] eviFiles = dir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".evi");
				}
			});
			StructRecords structRecords = new StructRecords(host, bucket);
			
			ExcelByUser.initData(projectDir);
			for (int i = 0; i < eviFiles.length; i++) {
				// TODO 为了debug加的判断，正式处理数据时注释掉
//				 if (i > 161 && i <= 162) {
				 if (eviFiles[i].equals("1_CY1.json.evi")) {
				FolLoadHelper.LoadResult loadResult = FolLoadHelper
						.load(projectDir + "/../../common_beliefbase/basic_belief.logic");
				String evidenceFile = projectDir + "/evidences/" + eviFiles[i];
				InherenceEngine engine = InherenceEngineFactory.create(evidenceFile, evidenceFile + ".db",
						loadResult.formulas, loadResult.predicates);
				BasicLogic basicLogic = new BasicLogic(projectDir, engine);
				basicLogic.inherence();
				TimeLineLogic timeLineLogic = new TimeLineLogic(projectDir, engine,
						FolLoadHelper.load(projectDir + "/../../common_beliefbase/timeline_belief.logic").formulas);
				timeLineLogic.inherence();
				
				TimeUtil.timeSpecific(engine, "TagDate", 2 , 0 );
				printAtoms(engine.queryAtom("TestItemAndDes"));
				
				
				//ParseStrutsConfig.search(engine);
				// printAtoms(engine.queryAtom("IsDate"));
//				 printAtoms(engine.queryAtom("TagDate"));
				// printAtoms(engine.queryAtom("ApplyDate"));
				// printAtoms(engine.queryAtom("BackTest"));

				// printAtoms(engine.queryAtom("FindLeftBracketAround"));
				// TODO 把需要结构化的结论保存到Eword的attributes
				// TODO 时间具体化

				

//				printAtoms(engine.queryAtom("ContextDate"));
//				 printAtoms(engine.queryAtom("TagDate"));
//				 printAtoms(engine.queryAtom("IsPatientName"));
//				 printAtoms(engine.queryAtom("AdmissionDate"));
//				 printAtoms(engine.queryAtom("OutAdminDate"));
//				 printAtoms(engine.queryAtom("AdmissionNum"));
//				 printAtoms(engine.queryAtom("RecordNum"));
//				 printAtoms(engine.queryAtom("HaveDrugJiLiangAt"));
//				 printAtoms(engine.queryAtom("HaveDrugPinciAt"));
//				 printAtoms(engine.queryAtom("HaveDrugBadEffects"));
//				 printAtoms(engine.queryAtom("HaveDrugGoodEffects"));
//				 printAtoms(engine.queryAtom("DrugWordWithAdminHeading"));
//				 printAtoms(engine.queryAtom("DrugWordWithOutAdminHeading"));
//				 printAtoms(engine.queryAtom("StopDrug"));
//				printAtoms(engine.queryAtom("AdmissionDate"));
//				printAtoms(engine.queryAtom("OutAdminDate"));
//				printAtoms(engine.queryAtom("RecordType"));
//				printAtoms(engine.queryAtom("ContextDate"));
//				printAtoms(engine.queryAtom("MaxDate"));
//				printAtoms(engine.queryAtom("IsNowContextDate"));
//				printAtoms(engine.queryAtom("HaveInTimeWord"));
//				printAtoms(engine.queryAtom("HaveOutTimeWord"));
//				printAtoms(engine.queryAtom("IsOutAdminDate"));
//				printAtoms(engine.queryAtom("IsAdmissionDate"));
//				printAtoms(engine.queryAtom("TagDate"));
//				printAtoms(engine.queryAtom("IsContextDate"));
//				printAtoms(engine.queryAtom("RealSymptom"));
//				 if(evidenceFile.contains("RY")){
//					 TimeUtil.timeSpecific(engine, "DrugWordWithAdminHeading", 5,2);
//					 Atom[] drugs = ExcelByUser.removeStopDrug(engine.queryAtom("DrugWordWithAdminHeading"), engine.queryAtom("StopDrug"));
//					 Atom[] zhengzhuang = ExcelByUser.getZhengzhuang(engine.queryAtom("RealSymptom"),true);
//					 ExcelByUser.getMedicine(zhengzhuang,engine,evidenceFile,engine.queryAtom("IsPatientName"),engine.queryAtom("AdmissionNum"),engine.queryAtom("RecordNum"),engine.queryAtom("AdmissionDate"), new Atom[]{}, drugs, engine.queryAtom("HaveDrugJiLiangAt"), engine.queryAtom("HaveDrugPinciAt"), engine.queryAtom("Word"), engine.queryAtom("HaveDrugBadEffects"), engine.queryAtom("HaveDrugGoodEffects")); 
//				 }
//				 
//				 if(evidenceFile.contains("CY")){
//					 TimeUtil.timeSpecific(engine, "DrugWordWithOutAdminHeading", 5,2);
//					 Atom[] drugs = ExcelByUser.removeStopDrug(engine.queryAtom("DrugWordWithOutAdminHeading"), engine.queryAtom("StopDrug"));
//					 Atom[] zhengzhuang = ExcelByUser.getZhengzhuang(engine.queryAtom("RealSymptom"), false);
//					 ExcelByUser.getMedicine(zhengzhuang,engine,evidenceFile,engine.queryAtom("IsPatientName"),engine.queryAtom("AdmissionNum"),engine.queryAtom("RecordNum"),engine.queryAtom("AdmissionDate"), engine.queryAtom("OutAdminDate"),drugs , engine.queryAtom("HaveDrugJiLiangAt"), engine.queryAtom("HaveDrugPinciAt"), engine.queryAtom("Word"), engine.queryAtom("HaveDrugBadEffects"), engine.queryAtom("HaveDrugGoodEffects"));
//				 }
//				 
				
				 // TimeLine.convertDate(refDate, strDate);
				// 5. 保存结果
//				if (storeResultsToDB) {
//					structRecords.saveStructDocument(eviFiles[i].substring(0, eviFiles[i].indexOf(".evi")), version,
//							engine.getFolNetwork().getMemDB().toEWords());
//				}
				}
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void printAtoms(Atom[] atoms) {
		for (int i = 0; i < atoms.length; i++) {
			System.out.println(i + ": " + atoms[i].toString());
		}
	}
}
