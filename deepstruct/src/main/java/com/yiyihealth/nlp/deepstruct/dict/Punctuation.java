package com.yiyihealth.nlp.deepstruct.dict;

/**
 * 常用符号集
 * @author qiangpeng
 *
 */
public class Punctuation {

	/**
	 * 需要成对出现的符号
	 */
	public static final int PAIR_QUOTES = 1, PAIR_SINGLE_QUOTES = 2, PAIR_BRACKET = 3;
	
	private static String punctuation = ",;\"':()?!，；、。“”‘’：？！（） 《》[]";
	
	public static final char COMMA_C = '，';
	
	public static final char COMMA_E = ',';
	
	public static final char SEMI_C = '；';
	
	public static final char SEMI_E = ';';
	
	public static final char FULLSTOP_C = '。';
	
	public static final char QUOTES_E = '"';
	
	public static final char QUOTES_C_L = '“';
	
	public static final char QUOTES_C_R = '”';
	
	public static final char SINGLE_QUOTES_E = '\'';
	
	public static final char SINGLE_QUOTES_C_L = '‘';
	
	public static final char SINGLE_QUOTES_C_R = '’';
	
	public static final char COLON_E = ':';
	
	public static final char COLON_C = '：';
	
	public static final char BRACKET_E_L = '(';
	
	public static final char BRACKET_E_R = ')';
	
	public static final char BRACKET_C_L = '（';
	
	public static final char BRACKET_C_R = '）';
	
	public static final char QMARK_E = '?';
	
	public static final char QMARK_C = '？';
	
	public static final char EMARK_E = '!';
	
	public static final char EMARK_C = '！';
	
	public static final char CAESURA_C = '、';
	
	public static final char SPACE = ' ';
	
	public static final char LEFT_BOOK_BRACKET = '《';
	
	public static final char RIGHT_BOOK_BRACKET = '》';
	
	public static final char LEFT_M_BRACKET = '[';
	
	public static final char RIGHT_M_BRACKET = ']';
	
	
	public static final String STR_FULLSTOP = "" + FULLSTOP_C;
	
	public static String getPunctuation(){
		return punctuation;
	}
	
	/**
	 * 是否是逗号
	 * @param c
	 * @return
	 */
	public static final boolean isComma(char c){
		return COMMA_C == c || COMMA_E == c;
	}
	
	/**
	 * 是否是分号
	 * @param c
	 * @return
	 */
	public static final boolean isSemi(char c){
		return SEMI_C == c || SEMI_E == c;
	}
	
	/**
	 * 是否是句号, TODO 如果是英文的句号，程序需另行做特殊判断
	 * @param c
	 * @return
	 */
	public static final boolean isFullstop(char c){
		return FULLSTOP_C == c;
	}
	
	/**
	 * 是否是双引号, 这里不区分左右，也更能容错
	 * @param c
	 * @return
	 */
	public static final boolean isQuotes(char c){
		return QUOTES_E == c || QUOTES_C_L == c || QUOTES_C_R == c;
	}
	
	/**
	 * 是否是单引号, 这里不区分左右，也更能容错
	 * @param c
	 * @return
	 */
	public static final boolean isSingleQuotes(char c){
		return SINGLE_QUOTES_E == c || SINGLE_QUOTES_C_L == c || SINGLE_QUOTES_C_R == c;
	}
	
	/**
	 * 是否是冒号
	 * @param c
	 * @return
	 */
	public static final boolean isColon(char c){
		return COLON_E == c || COLON_C == c;
	}
	
	/**
	 * 是否是右括号
	 * @param c
	 * @return
	 */
	public static final boolean isRightBracket(char c){
		return BRACKET_E_R == c || BRACKET_C_R == c;
	}
	
	/**
	 * 是否是左括号
	 * @param c
	 * @return
	 */
	public static final boolean isLeftBracket(char c){
		return BRACKET_E_L == c || BRACKET_C_L == c;
	}
	
	/**
	 * 是否是问号
	 * @param c
	 * @return
	 */
	public static final boolean isQmark(char c){
		return QMARK_E == c || QMARK_C == c;
	}
	
	/**
	 * 是否是感叹号
	 * @param c
	 * @return
	 */
	public static final boolean isEmark(char c){
		return EMARK_E == c || EMARK_C == c;
	}
	
	/**
	 * 是否是顿号
	 * @param c
	 * @return
	 */
	public static final boolean isCaesura(char c){
		return CAESURA_C == c;
	}
	
	/**
	 * 是否是空格
	 * @param c
	 * @return
	 */
	public static final boolean isSpace(char c){
		return SPACE == c;
	}
	
	public static boolean isSentenceEnd(EWord eWord){
		return eWord.getWord().equals(""+Punctuation.FULLSTOP_C);
	}
	
}
