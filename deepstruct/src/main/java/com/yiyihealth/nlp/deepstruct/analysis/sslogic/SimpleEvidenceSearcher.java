package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.nlp.deepstruct.dict.EWord;

public class SimpleEvidenceSearcher extends SSLogic {
	
	private ArrayList<WordOrderSearcher> searchers = null;
	
	private EvidenceWritter evidenceWritter;
	
	public SimpleEvidenceSearcher(String logicFile, EvidenceWritter evidenceWritter) {
		JSONArray config = null;
		try {
			config = JSONObject.parseArray(FileUtils.fileRead(new File(logicFile+"/"+"../../simple_syntax/esearch_config.json")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] logicfiles=new String[config.size()];
		for (int i = 0; i < config.size(); i++) {
			logicfiles[i] = logicFile+"/"+config.getString(i);
		}
		
		searchers = loadLogics(logicfiles);
		for(WordOrderSearcher searcher : searchers){
			OnEvidenceAction onEvidenceAction = (OnEvidenceAction) searcher.getOnResultAction();
			onEvidenceAction.setWritter(evidenceWritter);
		}
		this.evidenceWritter = evidenceWritter;
	}
	
	public void searchSimpleEvidence(ArrayList<EWord> words, int[] sentencePoses, SearchOffset offset){
		for(WordOrderSearcher wordOrderSearcher : searchers){
			wordOrderSearcher.doSearch(words, sentencePoses, offset);
		}
	}
	
	/**
	 * @param records
	 * @return
	 */
	public ArrayList<WordPos> searchSimpleEvidence(ArrayList<ArrayList<EWord>> records){
		ArrayList<WordPos> results = new ArrayList<WordPos>();
		SearchOffset offset = new SearchOffset(0, 0, 0);
		for(ArrayList<EWord> record : records){
			int[] sentencePoses = getSentencePoses(record);
			int[] blockPoses = getBlockPoses(record);
			for(WordOrderSearcher wordOrderSearcher : searchers){
				wordOrderSearcher.doSearch(record, sentencePoses, offset);
			}
			for(int i=0; i<record.size(); i++){
				results.add(new WordPos(record.get(i), i+offset.wordOffset, sentencePoses[i] + offset.sentenceOffset, offset.recordOffset, blockPoses[i] + offset.blockOffset));
			}
			//计算偏移
			offset.wordOffset += record.size();
			offset.recordOffset += 1;
			offset.blockOffset += blockPoses[blockPoses.length - 1] + 1;
			offset.sentenceOffset += sentencePoses[sentencePoses.length-1] + 1;
		}
		return results;
	}
	
//	/**
//	 * TODO 下面这个方法和上面方法一样，临时没有更好方案，借用一下, 需要重构
//	 * @param records
//	 * @param doNotSearch
//	 * @return
//	 */
//	public ArrayList<WordPos> searchSimpleEvidence(ArrayList<ArrayList<EWord>> records, boolean doNotSearch){
//		ArrayList<WordPos> results = new ArrayList<WordPos>();
//		SearchOffset offset = new SearchOffset(0, 0, 0);
//		for(ArrayList<EWord> record : records){
//			int[] sentencePoses = getSentencePoses(record);
//			if (!doNotSearch) {
//				for(WordOrderSearcher wordOrderSearcher : searchers){
//					wordOrderSearcher.doSearch(record, sentencePoses, offset);
//				}
//			}
//			for(int i=0; i<record.size(); i++){
//				results.add(new WordPos(record.get(i), i+offset.wordOffset, sentencePoses[i] + offset.sentenceOffset, offset.recordOffset));
//			}
//			//计算偏移
//			offset.wordOffset += record.size();
//			offset.recordOffset += 1;
//			offset.sentenceOffset += sentencePoses[sentencePoses.length-1] + 1;
//		}
//		return results;
//	}
	
	public EvidenceWritter getEvidenceWritter() {
		return evidenceWritter;
	}
	
	public void writeEvidence2File(){
		evidenceWritter.writeToFile();
		evidenceWritter.clear();
	}
	
	public static class WordPos {
		public EWord eWord;
		public int pos;
		public int sentencePos;
		public int recordPos;
		
		//TODO 重构, 加入构造函数, 按理应该保留recordPos(可能需要改成recordid，方便以后扩展为多agent)
		public int blockPos = -1;
		
		public WordPos(EWord eWord, int pos, int sentencePos, int recordPos, int blockPos) {
			this.eWord = eWord;
			this.pos = pos;
			this.sentencePos = sentencePos;
			this.recordPos = recordPos;
			this.blockPos = blockPos;
		}
		
	}
}
