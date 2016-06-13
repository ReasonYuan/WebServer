package com.yiyihealth.nlp.deepstruct.csense;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.csense.CSItem.ItemType;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;

public class CSItemParser {
	
	public static void main(String[] args) {
		CSItem[] results = new CSItemParser().parseAuxiliaries("../dsknowledgebase/low_level/辅助性质.txt");
		System.out.println("items length: " + results.length);
	}

	public CSItem[] parseAuxiliaries(String file){
		try {
			ArrayList<String> lines = FileManager.readLargeFileToLines(file);
			CSItem[] results = new CSItem[lines.size()];
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				boolean startWithMB = line.charAt(0) == '[';
				boolean endsWithMB = line.charAt(line.length()-1) == ']';
				if (endsWithMB && startWithMB) {
					NatureItem natureItem = new NatureItem(line.substring(1, line.length()-1), ItemType.NATURE);
					results[i] = natureItem;
					continue;
				} else if(endsWithMB || startWithMB){
					throw new RuntimeException("辅助词语法错误: "+ line);
				}
				startWithMB = line.charAt(0) == '(';
				endsWithMB = line.charAt(line.length()-1) == ')';
				if (endsWithMB && startWithMB) {
					FeatureItem featureItem = new FeatureItem(line.substring(1, line.length()-1), ItemType.FEATURE);
					results[i] = featureItem;
					continue;
				} else if(endsWithMB || startWithMB){
					throw new RuntimeException("辅助词语法错误: "+ line);
				}
				WordItem wordItem = new WordItem(line, ItemType.WORD);
				results[i] = wordItem;
			}
			return results;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
