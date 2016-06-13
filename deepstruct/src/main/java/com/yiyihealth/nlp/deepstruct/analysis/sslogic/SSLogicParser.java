package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;

import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.SyntaxSequence;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.SyntaxSequence.SyntaxNode.SkipCondition;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.SyntaxSequence.VerifyType;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.TrieNode;

public class SSLogicParser {
	
	//basic tokens
	public static final String EXISTS = "exists";//word(w, nature()).nature()..nature().nature() | after ..word(w).nature()
	public static final String NOT = "!";
	public static final String AFTER = "after";
	public static final String BEFORE = "before";
	public static final String AROUND = "around";
	public static final String HEADING = "heading";
	public static final String NATURE = "nature";
	public static final String WORD = "word";
	public static final String TAG = "tag";
	public static final String LEFT_BRACKET = "(";
	public static final String RIGHT_BRACKET = ")";
	public static final String COMMA = ",";
	public static final String DOT = ".";
	public static final String CONDITION_LINE = "|";
	public static final String SKIP = "skip";
	public static final String ACTION_SYMBOL = "=>";
	public static final String COLON = ":";
	public static final String SENTENCE_END = "end";
	public static final String CONTAINS_BY = "containsBy";
	
	//actions
	public static final String ACTION_COMBINE = "combine";
	public static final String ACTION_EVIDENCE = "evidence";
	
	//仅为做判断，方便统一数据
	public static final String CONTENT_TEXT = "content";
	
	//转义字符
	private static final char TRANS_CHAR = '\\';
	
	private static final String[] TOKEN_NAMES = {
			EXISTS, NOT, AFTER, BEFORE, AROUND, COLON, SENTENCE_END, 
			NATURE, WORD, LEFT_BRACKET, RIGHT_BRACKET, CONTAINS_BY, ACTION_EVIDENCE, 
			COMMA, DOT, SKIP, CONDITION_LINE, ACTION_SYMBOL, ACTION_COMBINE, TAG, HEADING
		};
	
	private static final CharNode keywords = new CharNode();
	
	//语法节点后续期望的节点
	private static final Hashtable<String, ExpArrayList> expects = new Hashtable<String, ExpArrayList>();
	
	static {
		for (int i = 0; i < TOKEN_NAMES.length; i++) {
			keywords.addNode(TOKEN_NAMES[i]).setIsWordEnd(true); 
		}
		expects.put(NOT, new ExpArrayList(EXISTS, AROUND, BEFORE, AFTER));
		expects.put(EXISTS, new ExpArrayList(WORD, NATURE, HEADING));
		expects.put(AFTER, new ExpArrayList(WORD, NATURE));
		expects.put(BEFORE, new ExpArrayList(WORD, NATURE));
		expects.put(AROUND, new ExpArrayList(WORD, NATURE));
		expects.put(NATURE, new ExpArrayList(LEFT_BRACKET));
		expects.put(WORD, new ExpArrayList(LEFT_BRACKET));
		expects.put(HEADING, new ExpArrayList(LEFT_BRACKET));
		expects.put(CONTAINS_BY, new ExpArrayList(LEFT_BRACKET));
		expects.put(SKIP, new ExpArrayList(LEFT_BRACKET));
		expects.put(LEFT_BRACKET, new ExpArrayList(CONTENT_TEXT));
		expects.put(CONTENT_TEXT, new ExpArrayList(COMMA, RIGHT_BRACKET, COLON));
		expects.put(COMMA, new ExpArrayList(NATURE));
		expects.put(DOT, new ExpArrayList(NATURE, WORD, SKIP, TAG, SENTENCE_END));
		expects.put(TAG, new ExpArrayList(LEFT_BRACKET));
		expects.put(RIGHT_BRACKET, new ExpArrayList(RIGHT_BRACKET, DOT, CONDITION_LINE, ACTION_SYMBOL, CONTAINS_BY));
		expects.put(CONDITION_LINE, new ExpArrayList(EXISTS, NOT, AFTER, BEFORE, AROUND));
		expects.put(ACTION_SYMBOL, new ExpArrayList(ACTION_COMBINE, ACTION_EVIDENCE));
		expects.put(ACTION_COMBINE, new ExpArrayList(LEFT_BRACKET));
		expects.put(ACTION_EVIDENCE, new ExpArrayList(LEFT_BRACKET));
		expects.put(COLON, new ExpArrayList(CONTENT_TEXT));
		expects.put(SENTENCE_END, new ExpArrayList(CONDITION_LINE, ACTION_SYMBOL, CONTAINS_BY));
		
		//目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT
		if (expects.get(LEFT_BRACKET).size() != 1 || !expects.get(LEFT_BRACKET).contains(CONTENT_TEXT) 
				|| expects.get(COLON).size() != 1 || !expects.get(COLON).contains(CONTENT_TEXT)) {
			throw new RuntimeException("Error: 目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT");
		}
	}
	
	public static void main(String[] args) {
		SSLogicParser.parse("exists nature(行为动词).skip(X:3-).nature(标点符号).skip(*).nature(治疗行为) | !exists nature(数值) containsBy(X) ");
	}
	
	public static WordOrderSearcher parse(String text){
		char[] chars = text.toCharArray();
		boolean waitNextToken = true;
		TrieNode previousNode = null;
		StringBuffer sbContext = new StringBuffer();
		Stack<String> bracketPairStack = new Stack<String>();
		ExpArrayList currentExpects = (ExpArrayList) expects.get(EXISTS);
		
		ArrayList<String> tokens = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		boolean shouldTransChar = false;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == RIGHT_BRACKET.charAt(0)) {
				if (bracketPairStack.size() > 0) {
					bracketPairStack.pop();
				} else {
					throw new RuntimeException("Bracket is not in pair: " + text.substring(0, i));
				}
			} else if (c == LEFT_BRACKET.charAt(0)){
				bracketPairStack.push(LEFT_BRACKET);
			}
			if (waitNextToken && (c == ' ' || c == '\t')) {
				continue;
			} else {
				waitNextToken = false;
				//仅这个地方期望内容, 所以用contains
				if (currentExpects.contains(CONTENT_TEXT)) {
					if (!shouldTransChar && c == TRANS_CHAR) {
						shouldTransChar = true;
					} else {
						if (!shouldTransChar && (c == COMMA.charAt(0) || c == RIGHT_BRACKET.charAt(0))) {
							String content = sbContext.toString();
							//System.out.println("content: " + content);
							tokens.add(content);
							types.add(CONTENT_TEXT);
							String keyword = COMMA.charAt(0) == c ? COMMA : RIGHT_BRACKET;
							tokens.add(keyword);
							types.add(keyword);
							//System.out.println("keyword: " + keyword);
							sbContext.setLength(0);
							currentExpects = expects.get(keyword);
							previousNode = null;
							waitNextToken = true;
						} else if (!shouldTransChar && c == COLON.charAt(0)) {
							String content = sbContext.toString();
							//System.out.println("content: " + content);
							tokens.add(content);
							types.add(CONTENT_TEXT);
							String keyword = COLON;
							tokens.add(keyword);
							types.add(keyword);
							//System.out.println("keyword: " + keyword);
							sbContext.setLength(0);
							currentExpects = expects.get(keyword);
							previousNode = null;
							waitNextToken = true;
						} else {
							sbContext.append(c);
						}
						if (shouldTransChar) {
							shouldTransChar = false;
						}
					}
				} else {
					TrieNode currentNode = null;
					if (previousNode == null) {
						currentNode = keywords.getNode(c);
					} else {
						currentNode = previousNode.getNode(c);
					}
					TrieNode nodeToCheck = null;
					if (currentNode != null) {
						if(i == chars.length - 1){
							nodeToCheck = currentNode;
						}
					} else {
						nodeToCheck = previousNode;
					}
					if (nodeToCheck != null) {
						//达到结尾或无匹配
						String keyword = nodeToCheck.getWord();
						if (keyword != null) {
							if (keyword.equals(LEFT_BRACKET)) {
								if (!shouldTransChar && c == TRANS_CHAR) {
									shouldTransChar = true;
								} else {
									sbContext.append(c);
								}
							}
							currentExpects = expects.get(keyword);
							if (currentExpects == null) {
								throw new RuntimeException("keyword " + keyword +  "does not have expects!");
							}
							tokens.add(keyword);
							types.add(keyword);
							//System.out.println("keyword: " + keyword);
							previousNode = keywords.getNode(c);
						} else {
							throw new RuntimeException("Invalide token at: " + i + ", "+ text.substring(0, i) + ", not keyword: " + previousNode.getString());
						}
						waitNextToken = true;
					} else {
						previousNode = currentNode;
					}
				}
				
			}
		}
		if (bracketPairStack.size() > 0) {
			throw new RuntimeException("Some part is not close by ')'");
		}
		
		System.out.println("org: " + text);
		//System.out.println("tokens: " + tokens.toString());
		//System.out.println("types: " + types.toString());
		
		ToSyntaxResult result = toSyntaxSentance(tokens, types);
		
		WordOrderSearcher checker = new WordOrderSearcher("", result.syntaxSequence, result.onResultAction);
		
		return checker;
	}
	
	private static ToSyntaxResult toSyntaxSentance(ArrayList<String> tokens, ArrayList<String> types){
		SyntaxSequence syntaxSequence = new SyntaxSequence(null);
		SyntaxSequence root = syntaxSequence;
		OnResultAction onResultAction = null;
		int size = tokens.size();
		for (int i = 0; i < size; i++) {
			String type = types.get(i);
			if (type.equals(SSLogicParser.NOT)) {
				syntaxSequence.notExists = true;
			} else if (type.equals(SSLogicParser.EXISTS)) {
				syntaxSequence.setVerifyType(VerifyType.EXISTS);
			} else if (type.equals(SSLogicParser.AROUND)) {
				syntaxSequence.setVerifyType(VerifyType.AROUND);
			} else if (type.equals(SSLogicParser.BEFORE)) {
				syntaxSequence.setVerifyType(VerifyType.BEFORE);
			} else if (type.equals(SSLogicParser.AFTER)) {
				syntaxSequence.setVerifyType(VerifyType.AFTER);
			} else if (type.equals(SSLogicParser.NATURE)) {
				if (tokens.get(i-1).equals(COMMA)) {
					//内嵌nature, ignore
				} else {
					if (tokens.get(i+3).equals(COLON)) {
						//有定义变量
						syntaxSequence.addNode(null, tokens.get(i+4), tokens.get(i+2));
					} else {
						syntaxSequence.addNode(null, tokens.get(i+2), null);
					}
				}
			} else if (type.equals(SSLogicParser.HEADING)) {
				String titles = tokens.get(i+2);
				ArrayList<String> titlesArr = new ArrayList<>();
				if (titles.startsWith("[")) {
					StringTokenizer tokenizer = new StringTokenizer(titles, "[| \t]");
					int cnt = tokenizer.countTokens();
					for (int j = 0; j < cnt; j++) {
						titlesArr.add(tokenizer.nextToken());
					}
				} else {
					titlesArr.add(titles);
				}
				String[] strArrs = new String[titlesArr.size()];
				syntaxSequence.setBelong2Heading(titlesArr.toArray(strArrs));
			} else if (type.equals(SSLogicParser.WORD)) {
				boolean withVar = false;
				int offset = 0;
				if (tokens.get(i+3).equals(COLON)){
					withVar = true;
					offset = 2;
				}
				String nature = null;
				if (tokens.get(i+3+offset).equals(COMMA)) {
					//内嵌nature, ignore
					if (!types.get(i+4+offset).equals(NATURE)) {
						throw new RuntimeException("Word: " + tokens.get(i+1) + " should follow by nature if ',' exists!");
					}
					nature = tokens.get(i+6+offset);
				}
				syntaxSequence.addNode(tokens.get(i+2+offset), nature, withVar ? tokens.get(i+2) : null);
			} else if (type.equals(SSLogicParser.CONDITION_LINE)) {
				SyntaxSequence condition = new SyntaxSequence(syntaxSequence);
				syntaxSequence = condition;
			} else if (type.equals(SSLogicParser.SKIP)) {
				boolean withVar = false;
				int offset = 0;
				if (tokens.get(i+3).equals(COLON)){
					withVar = true;
					offset = 2;
				}
				String skipNum = tokens.get(i+2+offset);
				String conditionStr = null;
				ArrayList<String> conditions = new ArrayList<>();
				SkipCondition skipCondition = null;
				boolean isNot = false;
				boolean isNature = false;
				if (skipNum.indexOf(CONDITION_LINE) > 0) {
					conditionStr = skipNum.substring(skipNum.indexOf(CONDITION_LINE) + 1);
					skipNum = skipNum.substring(0, skipNum.indexOf(CONDITION_LINE));
				}
				if (conditionStr != null) {
					isNot = conditionStr.charAt(0) == '!';
					if (conditionStr.substring(isNot ? 1 : 0).startsWith("nature")) {
						isNature = true;
					} else if (conditionStr.substring(isNot ? 1 : 0).startsWith("word")) {
						isNature = false;
					} else {
						throw new RuntimeException("skip的条件必须是nature或word!");
					}
					if (conditionStr.indexOf('=') == -1) {
						throw new RuntimeException("skip的条件必须有=!");
					}
					String eachcs = conditionStr.substring(conditionStr.indexOf('=')+1);
					if (eachcs.startsWith("[")) {
						StringTokenizer tokenizer = new StringTokenizer(eachcs, "[, \t]");
						int cnt = tokenizer.countTokens();
						for (int j = 0; j < cnt; j++) {
							if (isNature) {
								conditions.add(WordNatures.getNatureByReadableName(tokenizer.nextToken()));
							} else {
								conditions.add(tokenizer.nextToken());
							}
						}
					} else {
						if (isNature) {
							conditions.add(WordNatures.getNatureByReadableName(eachcs));
						} else {
							conditions.add(eachcs);
						}
					}
					skipCondition = new SkipCondition();
					skipCondition.conditions = conditions;
					skipCondition.isNot = isNot;
					skipCondition.isNature = isNature;
				}
				int skip = 0;
				int dir = -1;
				if (skipNum.charAt(skipNum.length()-1) == '-') {
					skip = Integer.parseInt(skipNum.substring(0, skipNum.length()-1));
					dir = -1;
				} else if (skipNum.charAt(skipNum.length()-1) == '+') {
					skip = Integer.parseInt(skipNum.substring(0, skipNum.length()-1));
					dir = 1;
				} else if(skipNum.equals("*")){
					skip = 9999;//Large number, instead of Integer.MAX_VALUE
				} else {
					skip = Integer.parseInt(skipNum);
				}
				syntaxSequence.addSkipNode(skip, dir, withVar ? tokens.get(i+2) : null, skipCondition);
			} else if (type.equals(SSLogicParser.ACTION_SYMBOL)) {
				String tag = "";
				if (tokens.size() > i+8 && tokens.get(i+6).equals("tag")) {
					tag = tokens.get(i+8);
				}
				onResultAction = OnResultActionFactory.createAction(tokens.get(i+1), tokens.get(i+3), tag);
			} else if (type.equals(CONTAINS_BY)) {
				syntaxSequence.setContainsBy(tokens.get(i+2));
			} else if(type.equals(SENTENCE_END)){
				syntaxSequence.addEndNode();
			}
		}
		System.out.println(root);
		return new ToSyntaxResult(root, onResultAction);
	}
	
}

class ToSyntaxResult {
	SyntaxSequence syntaxSequence;
	OnResultAction onResultAction;
	public ToSyntaxResult(SyntaxSequence syntaxSequence, OnResultAction onResultAction) {
		this.syntaxSequence = syntaxSequence;
		this.onResultAction = onResultAction;
	}
}

class ExpArrayList extends ArrayList<String> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ExpArrayList(String...exps) {
		for (int i = 0; i < exps.length; i++) {
			add(exps[i]);
		}
	}
	
}
