package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.Sentence;

/**
 * 进行最基本的可信的语法矫正， TODO 这种做法值得商榷
 * @author qiangpeng
 *
 */
public class SimpleSyntaxCorrect extends SSLogic {
	
	private ArrayList<WordOrderSearcher> logics = null;
	
	public SimpleSyntaxCorrect(String logicFile) {
		if (logics == null) {
			logics = loadLogics(logicFile);
		}
	}
	
	public ArrayList<String> correctBasicWords(ArrayList<Sentence> sentences){
		ArrayList<String> results = new ArrayList<String>();
		for (int i = 0; i < sentences.size(); i++) {
			Sentence sentence = sentences.get(i);
			int[] sentencePoses = getSentencePoses(sentence.getWords());
			for(int j=0; j<logics.size(); j++){
				//这里startIndexOfAllWords没有意义，因为下面会做combine而影响index, 所以传0
				try {
					int foundcnt = logics.get(j).doSearch(sentence.getWords(), sentencePoses, new SearchOffset(0, 0, 0));
					if (foundcnt > 0) {
						results.add(sentence.debugRestoreToString());
						//System.out.println("通过匹配org: " + sentence.debugRestoreToString());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return results;
	}
	
}
