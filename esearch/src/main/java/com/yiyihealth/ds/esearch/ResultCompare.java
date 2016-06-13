package com.yiyihealth.ds.esearch;

import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager.OnRecordLoadListener;

public class ResultCompare {

	public static void main(String[] args) {
		new ResultCompare().compare();
	}

	public void compare() {
		String resultFile = "/Users/qiangpeng/Documents/workspace/java/tuffy/yongyao.txt";
		try {
			final ArrayList<ArrayList<EWord>> samples = new ArrayList<ArrayList<EWord>>();
			new FileManager().loadWords("../dsdata/records", new OnRecordLoadListener() {
				public void onRecordLoad(String filename, ArrayList<EWord> words) {
					samples.add(words);
				}
			});
			
			ArrayList<EWord> words = new ArrayList<EWord>();
			for (int i = 0; i < samples.size(); i++) {
				words.addAll(samples.get(i));
			}
			int counter = words.size();
			int[] result = new int[counter];
			parseResult(result, resultFile);
			for (int i = 0; i < result.length; i++) {
				if (result[i] == 1) {
					EWord word = words.get(i);
					int extend = 4;
					if (!word.getNature().equals(WordNatures.getNatureFullByName(ESearch.CURRENT_TMP_NATURE)) && word.getNature().equals("Unrec") && i >= extend && i < result.length - extend) {
						StringBuffer sBuffer = new StringBuffer();
						for (int j = i - extend; j <= i + extend; j++) {
							sBuffer.append(words.get(j).getWord());
						}
						System.out.println(i + " => "+ word.getWord() + ": should be drug = " + sBuffer.toString() + ", orgNature: " + word.getNature());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseResult(int[] result, String resultFile) throws Exception {
		String text = FileUtils.fileRead(resultFile);
		char[] chars = text.toCharArray();
		int start = -1;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '(') {
				start = i + 1;
			} else if (chars[i] == ')') {
				String posStr = new String(chars, start, i - start);
				result[Integer.parseInt(posStr)] = 1;
				start = -1;
			}
		}
	}

}
