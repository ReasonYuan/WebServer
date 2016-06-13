package com.yiyihealth.nlp.deepstruct.csense;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Term;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;

public class NBRec {
	
	public static class NBRecResult {
		NBRecResult(String reason, String naturePicked){
			this.reason = reason;
			this.naturePicked = naturePicked;
		}
		public String reason;
		public String naturePicked;
	}
	
	public static interface IllegalCSListener {
		public void onIllegal(String context);
	}
	
	private IllegalCSListener illegalCSListener;
	
	private ArrayList<CommonSenseNode> csNodes = new ArrayList<>();
	
	private CSItem[] auxilaries;

	public NBRec(String auxilaryFile) {
		auxilaries = new CSItemParser().parseAuxiliaries(auxilaryFile);
	}
	
	public void setIllegalCSListener(IllegalCSListener illegalCSListener) {
		this.illegalCSListener = illegalCSListener;
	}
	
	/**
	 * 装载常识文件
	 * @param files
	 */
	public void loadCommonSense(String...files){
		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			try {
				ArrayList<String> lines = FileManager.readLargeFileToLines(file);
				for(String line : lines){
					if (line.trim().length() > 0) {
						CommonSenseNode node = new CommonSenseNode(line, auxilaries);
						csNodes.add(node);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void loadRepeatSense(String...files){
		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			try {
				ArrayList<String> lines = FileManager.readLargeFileToLines(file);
				for(String line : lines){
					if (line.trim().length() > 0) {
						String newLine = "{" + line + "}={" + line + "}";
						CommonSenseNode node = new CommonSenseNode(newLine, auxilaries);
						csNodes.add(node);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 识别正确的词性
	 * @param wordToRecongnize
	 * @param pos
	 * @param words
	 * @return - null: 无法抉择，非空：选择出的唯一合理词性
	 */
	public NBRecResult recNature(Term wordToRecongnize, int pos, ArrayList<EWord> words, String logTag){
		ArrayList<String> natures = wordToRecongnize.getCandidateNatures();
		int legalCounter = 0;
		String naturePick = null;
		ArrayList<CommonSenseNode> multiCS = new ArrayList<>();
		for(String nature : natures){
			for (int i = 0; i < csNodes.size(); i++) {
				CommonSenseNode node = csNodes.get(i);
				if(node.isLegal(nature, pos, words)){
					legalCounter++;
					multiCS.add(node);
					naturePick = nature;
					break;
				}
			}
		}
		if (legalCounter == 1) {
			return new NBRecResult(null, naturePick);
		} else if(legalCounter == 0){
			String reason = "没有任何常识区分这些词性, 第" + pos + "个词:" + wordToRecongnize.getWord() + wordToRecongnize.getCandidateNatures() + ", " + "\n在句子: " + toOneSentence(words);
			recordIllegalInfo(reason, logTag);
			return new NBRecResult(reason, null);
		} else {
			String reason = "有多个常识区分这些词性, 第" + pos + "个词:" + wordToRecongnize.getWord() + wordToRecongnize.getCandidateNatures() + ", 多个常识为: " + multiCS.toString() +  ", \n在句子: " + toOneSentence(words);
			recordIllegalInfo(reason, logTag);
			return new NBRecResult(reason, null);
		}
	}
	
	public void recordIllegalInfo(String line, String logTag){
		//System.out.println("line: " + line);
		if (illegalCSListener != null) {
			illegalCSListener.onIllegal(logTag + ": " + line);
		}
	}
	
	private String toOneSentence(ArrayList<EWord> words){
		StringBuffer stringBuffer = new StringBuffer();
		for (EWord word : words) {
			stringBuffer.append(word.getWord());
		}
		return stringBuffer.toString() + "\n" + toSimpleWordNatures(words) + "\n" + words.toString();
	}
	
	private String toSimpleWordNatures(ArrayList<EWord> words){
		StringBuffer buffer = new StringBuffer();
		for(EWord word : words){
			buffer.append(word.getWord()).append("[").append(word.getNature()).append("] ");
		}
		return buffer.toString();
	}
}
