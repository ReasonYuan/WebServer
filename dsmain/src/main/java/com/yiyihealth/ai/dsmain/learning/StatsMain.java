package com.yiyihealth.ai.dsmain.learning;

import java.io.File;
import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager.OnRecordLoadListener;

public class StatsMain {

	private static SequanceStats mSequanceStats = null;
	private static SequanceStats mSequanceStatsDaoxu = null;
	private static FeatureStats mFeatureStats = null;
	private static final String SequanceStatsNature = "SequanceStatsNature.txt";
	private static final String SequanceStatsNatureDaoxu = "SequanceStatsNatureDaoXu.txt";
	
	public static void main(String[] args) throws Exception {
		JSONObject config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
		final String projectDir = config.getString("projectDir");
		
		String statsNature = "Drug";
		
		mSequanceStats	= new SequanceStats(statsNature);
		mSequanceStatsDaoxu	= new SequanceStats(statsNature);
		mFeatureStats = new FeatureStats(statsNature);
		final int[] cnt = new int[1];
		
		
		doSeqStats(mSequanceStats, projectDir, false);
		doSeqStats(mSequanceStatsDaoxu, projectDir, true);
		mSequanceStats.test(mSequanceStats.getRoot(),SequanceStatsNature);
		mSequanceStatsDaoxu.test(mSequanceStatsDaoxu.getRoot(),SequanceStatsNatureDaoxu);
		
		FileManager.writeObject(projectDir + "/SequanceStatsResults/StatsTree.obj", mSequanceStats.getRoot());
		FileManager.writeObject(projectDir + "/SequanceStatsResults/StatsTreeDaoxu.obj", mSequanceStatsDaoxu.getRoot());
		Object object = FileManager.readObject(projectDir + "/SequanceStatsResults/StatsTree.obj");
		
		new FileManager().loadWords(projectDir + "/nlpwords/", new OnRecordLoadListener() {
			public void onRecordLoad(String filename, ArrayList<EWord> allWords) {
//				if(filename.equals("doctor_13671609763_record_2237.json")){
					if (allWords.size() != 0) {
						EWord[] eWords = new EWord[allWords.size()];
						allWords.toArray(eWords);
						mFeatureStats.stats(eWords);
					}
//				}
				cnt[0]++;
			}
		});
		final ArrayList<Element> lists = mFeatureStats.createElementCollectionWordsAndNature();
		new FileManager().loadWords(projectDir + "/nlpwords/", new OnRecordLoadListener() {
			public void onRecordLoad(String filename, ArrayList<EWord> allWords) {
					if (allWords.size() != 0) {
						EWord[] eWords = new EWord[allWords.size()];
						allWords.toArray(eWords);
						mFeatureStats.findFeatureVector(eWords,lists,filename);
					}
				cnt[0]++;
			}
		});
		
	}
	
	private static void doSeqStats(final SequanceStats stats, String projectDir, final boolean isDaoxu) throws Exception{
		new FileManager().loadWords(projectDir + "/nlpwords/", new OnRecordLoadListener() {
			public void onRecordLoad(String filename, ArrayList<EWord> allWords) {
//				if(filename.equals("doctor_13671609763_record_2237.json")){
					if (allWords.size() != 0) {
						EWord[] eWords = new EWord[allWords.size()];
						allWords.toArray(eWords);
						
						if (isDaoxu) {
							EWord[] eWordsDaoxu = new EWord[eWords.length];
							for (int i = 0; i < eWordsDaoxu.length; i++) {
								eWordsDaoxu[i] = eWords[eWordsDaoxu.length - i - 1];
							}
							System.arraycopy(eWordsDaoxu, 0, eWords, 0, eWords.length);
						}
						
						stats.stats(eWords);
					}
//				}
			}
		});
	}
	
}
