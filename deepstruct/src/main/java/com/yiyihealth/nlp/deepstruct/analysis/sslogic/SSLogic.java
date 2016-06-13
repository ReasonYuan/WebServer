package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class SSLogic {
	
	public SSLogic() {
	}
	
	public int[] getSentencePoses(ArrayList<EWord> record){
		int sentencePos = 0;
		int[] sentencePoses = new int[record.size()];
		for (int m = 0; m < record.size(); m++) {
			sentencePoses[m] = sentencePos;
			EWord word = record.get(m);
			if (Punctuation.isSentenceEnd(word) && m != record.size() - 1) {
				sentencePos++;
			}
		}
		return sentencePoses;
	}
	
	public int[] getBlockPoses(ArrayList<EWord> record){
		int blockPos = -1;
		int[] blockPoses = new int[record.size()];
		for (int m = 0; m < record.size(); m++) {
			EWord word = record.get(m);
			if(word.getNature().equals(WordNatures.HEADING)){
				blockPos++;
			}
			blockPoses[m] = blockPos;
		}
		return blockPoses;
	}
	
	protected ArrayList<WordOrderSearcher> loadLogics(String logicFile){
		ArrayList<WordOrderSearcher> searchers = new ArrayList<>();
		ArrayList<String> logicsText = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(logicFile))));
			String line = br.readLine();
			while (line != null) {
				line = line.trim();
				if (line.startsWith("＃") || line.startsWith("#") || line.startsWith("//") || line.equals("")) {
					line = br.readLine();
					continue;
				}
				logicsText.add(line);
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String str: logicsText){
			WordOrderSearcher searcher = SSLogicParser.parse(str);
			searchers.add(searcher);
		}
		return searchers;
	}
	
	protected ArrayList<WordOrderSearcher> loadLogics(String[] strs){
		ArrayList<WordOrderSearcher> searchers = new ArrayList<>();
		ArrayList<String> logicsText = new ArrayList<>();
		
		for (int i = 0; i < strs.length; i++) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(strs[i]))));
				String line = br.readLine();
				while (line != null) {
					line = line.trim();
					if (line.startsWith("＃") || line.startsWith("#") || line.startsWith("//") || line.equals("")) {
						line = br.readLine();
						continue;
					}
					logicsText.add(line);
					line = br.readLine();
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (String str: logicsText){
			WordOrderSearcher searcher = SSLogicParser.parse(str);
			searchers.add(searcher);
		}
		return searchers;
	}
}
