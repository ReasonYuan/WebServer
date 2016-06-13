package com.yiyihealth.nlp.deepstruct.dict.trie.hitales;

public class NumberMatcher {

	private char[] buf = new char[20];
	
	private int size = 0;
	
	private int dotNum = 0;
	
	private void putChar(char c){
		if (size == buf.length) {
			char[] nbuf = new char[buf.length + 8];
			System.arraycopy(buf, 0, nbuf, 0, size);
			buf = nbuf;
		}
		buf[size++] = c;
	}
	
	private void clear(){
		size = 0;
		dotNum = 0;
	}
	
	/**
	 * @param c
	 * @return 是否
	 */
	public String matchNext(char c){
		if(c >= 0 && c <= 9){
			putChar(c);
			return null;
		} else if(c == '.'){
			if(dotNum == 0){
				putChar(c);
				dotNum++;
			} else {
				clear();
			}
			return null;
		} else {
			if (size > 0) {
				String result = new String(buf, 0, size);
				clear();
				return result;
			} else {
				return null;
			}
		}
	}
	
}
