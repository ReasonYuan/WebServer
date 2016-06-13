package com.yiyihealth.ai.dsmain.st;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ai.dsmain.db.CouchbaseRecordFetcher;
import com.yiyihealth.ai.dsmain.db.StructRecords;
import com.yiyihealth.ai.dsmain.exports.Exporter;
import com.yiyihealth.ai.dsmain.exports.ExporterFactory;
import com.yiyihealth.ai.dsmain.exports.ExporterUtils;
import com.yiyihealth.ai.dsmain.logic.BasicLogic;
import com.yiyihealth.ai.dsmain.logic.TimeLineLogic;
import com.yiyihealth.ai.dsmain.medicine.wx.MongoDBUtils;
import com.yiyihealth.ai.dsmain.nlp.RecordParser;
import com.yiyihealth.ai.timeline.TimeUtil;
import com.yiyihealth.ds.esearch.fol.Atom;
import com.yiyihealth.ds.esearch.fol.FolLoadHelper;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;
import com.yiyihealth.ds.esearch.fol.InherenceEngineFactory;
import com.yiyihealth.nlp.deepstruct.analysis.HealthAnalysis;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.EvidenceWritter;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager.OnRecordLoadListener;

public class DSMain {

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
			boolean dumpMems = config.getBooleanValue("dumpMems");
			String recordsParserClass = config.getString("recordsParser");
			boolean export = config.getJSONObject("export") == null ? false : config.getJSONObject("export").getBooleanValue("doExport");
			JSONObject mObject = config.getJSONObject("dicInfomation");
			boolean updateDicFromDB = mObject.getBooleanValue("updateDicFromDB"); 
			boolean writeUndefAndUnRec = config.getBooleanValue("writeUndefAndUnRec");
			
			//0.是否从服务器下载分词字典
			if(updateDicFromDB){
				MongoDBUtils.downLoadDic();
			}
			// 1. 从服务器读取原始数据
			if (fetchDataFromDB) {
				// 从服务器抓取数据并存放到工程目录的original目录
				new CouchbaseRecordFetcher(projectDir, n1sqlFile, host, bucket).fetchRecords();
			}

			// 2. do nlp
			if (doNlp) {
				RecordParser recordParser = (RecordParser) Class.forName(recordsParserClass).newInstance();
				recordParser.parseWord(projectDir);
				//new CouchbaseRecordParser().parseWord(projectDir);
				//new LocalJsonFileParser().parseWord(projectDir);
				
				//把常识错误信息写入文件
				File file = new File("./illegalcs");
				if (!file.exists()) {
					file.mkdir();
				}
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < HealthAnalysis.getIllegalCSInfos().size(); i++) {
					buffer.append(HealthAnalysis.getIllegalCSInfos().get(i) + "\n\n");
				}
				FileManager.writeToFile("./illegalcs/unknowncs.txt", buffer.toString());
			}
			
			// 3. 读取分词结果，查询证据
			if (doESearch) {
				final int[] cnt = new int[1];
				new FileManager().loadWords(projectDir + "/nlpwords/", new OnRecordLoadListener() {
					public void onRecordLoad(String filename, ArrayList<EWord> allWords) {
						if (allWords.size() != 0) {
							//为人阅读
							StructRecords.writeNlpWords4Human(filename, projectDir + "/output", allWords);
							
							ArrayList<ArrayList<EWord>> oneRecord = new ArrayList<ArrayList<EWord>>();
							oneRecord.add(allWords);
							EvidenceWritter writter = new EvidenceWritter(
									projectDir + "/evidences/" + filename + ".evi");
							SimpleEvidenceSearcher searcher = new SimpleEvidenceSearcher(
									projectDir, writter);
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
			for (int i = 0; i < eviFiles.length; i++) {
				// TODO 为了debug加的判断，正式处理数据时注释掉
//				if (i > 444 && i <= 445) {
//				if(eviFiles[i].equals("2854_蔡建波-449062_RY5.json.evi")){
					FolLoadHelper.LoadResult loadResult = FolLoadHelper
							.load(projectDir + "/../../common_beliefbase/basic_belief.logic");
					String evidenceFile = projectDir + "/evidences/" + eviFiles[i];
					InherenceEngine engine = InherenceEngineFactory.create(evidenceFile, evidenceFile + ".db",
							loadResult.formulas, loadResult.predicates);
					BasicLogic basicLogic = new BasicLogic(projectDir, engine);
					basicLogic.inherence();

					while (engine.removeRepeatAtoms() || engine.combinePaitis() || engine.removeLongerAtoms()) {
						engine.doInherence();
					};
					
					TimeLineLogic timeLineLogic = new TimeLineLogic(projectDir, engine,
							FolLoadHelper.load(projectDir + "/../../common_beliefbase/timeline_belief.logic").formulas);
					timeLineLogic.inherence();
					
					// printAtoms(engine.queryAtom("IsDate"));
					

					// printAtoms(engine.queryAtom("FindLeftBracketAround"));
					// TODO 把需要结构化的结论保存到Eword的attributes
					// TODO 时间具体化

					TimeUtil.timeSpecific(engine, "TagDate", 2 , 0 );
					
					

					
					// printAtoms(engine.queryAtom("TagDate"));
					String fileNamePrefix = eviFiles[i].substring(0, eviFiles[i].indexOf(".evi"));
					// TimeLine.convertDate(refDate, strDate);
					// 5. 保存结果
					if (storeResultsToDB) {
						structRecords.saveStructDocument(eviFiles[i].substring(0, eviFiles[i].indexOf(".evi")), version,
								engine.getFolNetwork().getMemDB().toEWords(), projectDir + "/output", false);
					}
					
					//导出引擎事实atoms的需要导出的数据
					if (export) {
						Exporter exporter = ExporterFactory.createExporter(config.getJSONObject("export"));
						exporter.export(engine, config.getJSONObject("export").getString("outputDirectory").replace("$projectDir", projectDir) + "/" + fileNamePrefix);
					}
					
					//调试打印事实
					new AtomPrinter(engine).printAtoms();
//				}
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
