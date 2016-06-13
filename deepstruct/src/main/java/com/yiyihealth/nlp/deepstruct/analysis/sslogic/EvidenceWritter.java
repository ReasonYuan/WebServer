package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;

public class EvidenceWritter {
	
	private String outputFilename;

	public EvidenceWritter(String outputFilename) {
		this.outputFilename = outputFilename;
	}
	
	private ArrayList<String> evidences = new ArrayList<>();
	
	public void writeToFile(){
		if (outputFilename == null) {
			return;
		}
		try {
			BufferedWriter bosw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename)));
			//BufferedOutputStreamWriter bosw = new BufferedOutputStreamWriter();
			for(String str : evidences){
				bosw.write(str);
				bosw.newLine();
			}
			bosw.flush();
			bosw.close();
		} catch (Exception e) {
			//TODO handle exceptions
			e.printStackTrace();
		}
	}
	
	public void appendToFile(ArrayList<WordPos> wordPoses){
		ArrayList<String> basicEvidences = new ArrayList<String>();
		for(WordPos wordPos : wordPoses){
			String basicEvidence = "Word(\"" + wordPos.eWord.getWord() + "\", " + wordPos.eWord.getNature() + ", " + wordPos.pos + ", " + wordPos.sentencePos + ", " + wordPos.blockPos + ", " + wordPos.recordPos + ")";
			basicEvidences.add(basicEvidence);
		}
		if (outputFilename == null) {
			return;
		}
		try {
			BufferedWriter bosw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename, true)));
			//BufferedOutputStreamWriter bosw = new BufferedOutputStreamWriter(new FileOutputStream(outputFilename, true));
			for(String str : basicEvidences){
				bosw.write(str);
				bosw.newLine();
			}
			bosw.flush();
			bosw.close();
		} catch (Exception e) {
			//TODO handle exceptions
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getEvidences() {
		return evidences;
	}
	
	public void addEvidence(String evidence){
		evidences.add(evidence);
	}
	
	public void clear(){
		evidences.clear();
	}
}
