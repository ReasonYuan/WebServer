package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

public class SearchOffset {

	public int wordOffset = 0;
	public int sentenceOffset = 0;
	public int recordOffset = 0;
	public int blockOffset = 0;
	public String currentTitle = null;
	
	public SearchOffset(int wordOffset) {
		this.wordOffset = wordOffset;
	}
	
	public SearchOffset(int wordOffset, int sentenceOffset) {
		this.sentenceOffset = sentenceOffset;
		this.wordOffset = wordOffset;
	}
	
	public SearchOffset(int wordOffset, int sentenceOffset, int recordOffset) {
		this.sentenceOffset = sentenceOffset;
		this.wordOffset = wordOffset;
		this.recordOffset = recordOffset;
	}
	
	public SearchOffset(int wordOffset, int sentenceOffset, int recordOffset, int blockOffset) {
		this.recordOffset = recordOffset;
		this.blockOffset = blockOffset;
		this.wordOffset = wordOffset;
		this.blockOffset = blockOffset;
	}
}
