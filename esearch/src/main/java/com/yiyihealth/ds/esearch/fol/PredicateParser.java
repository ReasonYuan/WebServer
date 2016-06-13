package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import com.yiyihealth.ds.esearch.fol.FolParser.ExpArrayList;

public class PredicateParser implements LogicParser<Predicate> {
	// basic tokens
	public static final String NOT = "!";
	public static final String LEFT_BRACKET = "(";
	public static final String RIGHT_BRACKET = ")";
	public static final String COMMA = ",";

	public static final String PARAM_NAME = "param";
	public static final String PREDICATE_NAME = "predicateName";
	
	public static final char QUOTE = '"';

	// 语法节点后续期望的节点
	private static final Hashtable<String, ExpArrayList> expects = new Hashtable<String, ExpArrayList>();

	static {
		expects.put(NOT, new ExpArrayList(PREDICATE_NAME));
		expects.put(LEFT_BRACKET, new ExpArrayList(PARAM_NAME));
		expects.put(PARAM_NAME, new ExpArrayList(COMMA, RIGHT_BRACKET));
		expects.put(COMMA, new ExpArrayList(PARAM_NAME));
		expects.put(PREDICATE_NAME, new ExpArrayList(LEFT_BRACKET));

		// 目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT
		if (expects.get(LEFT_BRACKET).size() != 1 || !expects.get(LEFT_BRACKET).contains(PARAM_NAME)) {
			throw new RuntimeException("Error: 目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT");
		}
		if (expects.get(COMMA).size() != 1 || !expects.get(COMMA).contains(PARAM_NAME)) {
			throw new RuntimeException("Error: 目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT");
		}
	}

	public static void main(String[] args) {
		new PredicateParser().parse("Friend(x, y)");
	}

	public Predicate parse(String text) {
		char[] chars = text.toCharArray();
		StringBuffer sbContext = new StringBuffer();
		Stack<String> bracketPairStack = new Stack<String>();
		ExpArrayList currentExpects = new ExpArrayList(PREDICATE_NAME, NOT);

		ArrayList<String> tokens = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		boolean inQuote = false;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (!inQuote) {
				if (c == RIGHT_BRACKET.charAt(0)) {
					if (bracketPairStack.size() > 0) {
						bracketPairStack.pop();
					} else {
						throw new RuntimeException("Bracket is not in pair: " + text.substring(0, i));
					}
				} else if (c == LEFT_BRACKET.charAt(0)) {
					bracketPairStack.push(LEFT_BRACKET);
				}
			}
			if (c == ' ' || c == '\t' || c == '\n') {
				continue;
			} else {
				if (currentExpects == null) {
					System.out.println("");
					throw new RuntimeException("");
				}
				if (currentExpects.contains(PARAM_NAME)) {
					// 仅这个地方期望内容, 所以用contains
					if(c == QUOTE) {
						if(!inQuote){
							inQuote = true;
						} else {
							//如果本身是引号
							if (i < chars.length - 1 && chars[i+1] == QUOTE) {
								sbContext.append(c);
							} else {
								inQuote = false;
							}
						}
					} else if (!inQuote && (c == COMMA.charAt(0) || c == RIGHT_BRACKET.charAt(0))) {
						String content = sbContext.toString().trim();
						// System.out.println("content: " + content);
						tokens.add(content);
						types.add(PARAM_NAME);
						String keyword = COMMA.charAt(0) == c ? COMMA : RIGHT_BRACKET;
						tokens.add(keyword);
						types.add(keyword);
						// System.out.println("keyword: " + keyword);
						sbContext.setLength(0);
						currentExpects = expects.get(keyword);
					} else {
						sbContext.append(c);
					}
				} else if (currentExpects.contains(PREDICATE_NAME) || currentExpects.contains(NOT)) {
					if (c == NOT.charAt(0)) {
						if (currentExpects.size() == 1) {
							throw new RuntimeException("Only predicate name is expected!");
						}
						if (sbContext.length() > 0) {
							throw new RuntimeException("'NOT' must before predicate name!");
						} else {
							tokens.add(NOT);
							types.add(NOT);
							currentExpects = expects.get(NOT);
							if (currentExpects == null) {
								System.out.println("");
								throw new RuntimeException("");
							}
						}
					} else if (c == LEFT_BRACKET.charAt(0)) {
						tokens.add(sbContext.toString().trim());
						types.add(PREDICATE_NAME);
						sbContext.setLength(0);
						tokens.add(LEFT_BRACKET);
						types.add(LEFT_BRACKET);
						currentExpects = expects.get(LEFT_BRACKET);
						if (currentExpects == null) {
							System.out.println("");
							throw new RuntimeException("");
						}
					} else {
						sbContext.append(c);
					}
				} else {
					int offset = i < 20 ? 0 : i - 20;
					throw new RuntimeException("Unknown syntax around: " + new String(chars, offset, i - offset + 1));
				}
			}
		}
		if (bracketPairStack.size() > 0) {
			throw new RuntimeException("Some part is not close by ')': " + text);
		}

//		 System.out.println("org: " + text);
//		 System.out.println("tokens: " + tokens.toString());
//		 System.out.println("types: " + types.toString());

		Predicate predicate = toPredicate(text, tokens, types);
		System.out.println(predicate.toString());
		
		return predicate;
	}

	private static Predicate toPredicate(String line, ArrayList<String> tokens, ArrayList<String> types) {
		Predicate predicate = new Predicate();
		int size = tokens.size();
		boolean end = false;
		for (int i = 0; i < size; i++) {
			String type = types.get(i);
			if (type.equals(NOT)) {
				predicate.setWithNot(true);
			} else if (type.equals(PARAM_NAME)) {
				if (tokens.get(i).equals("")) {
//					throw new RuntimeException(
//							"Evidence " + line + " has an empty param!");
				}
				predicate.addParam(tokens.get(i));
			} else if (type.equals(PREDICATE_NAME)) {
				predicate.setName(tokens.get(i));
			} else if (type.equals(RIGHT_BRACKET)) {
				end = true;
			}
		}
		if (!end) {
			throw new RuntimeException("Evidence " + line + " is not closed!");
		}
		predicate.onPredicateParseFinish(false);
		return predicate;
	}

}
