package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import com.yiyihealth.ds.esearch.fol.FolParser.FormulaOrPredicate;

public class FolParser implements LogicParser<FormulaOrPredicate>{
	
	private PredicateParser predicateParser = new PredicateParser();
	
	//basic tokens
	public static final String NOT = "!";
	public static final String LEFT_BRACKET = "(";
	public static final String RIGHT_BRACKET = ")";
	public static final String COMMA = ",";
	public static final String OR = "v";
	public static final String COLON = ":";
	
	public static final String PARAM_NAME = "param";
	public static final String PREDICATE_NAME = "predicateName";
	public static final String DEBUG_LABEL = "debugLabel";
	
	public static final char QUOTE = '"';
	
	//语法节点后续期望的节点
	private static final Hashtable<String, ExpArrayList> expects = new Hashtable<String, ExpArrayList>();
	
	static {
		expects.put(NOT, new ExpArrayList(PREDICATE_NAME));
		expects.put(LEFT_BRACKET, new ExpArrayList(PARAM_NAME));
		expects.put(PARAM_NAME, new ExpArrayList(COMMA, RIGHT_BRACKET));
		expects.put(COMMA, new ExpArrayList(PARAM_NAME));
		expects.put(RIGHT_BRACKET, new ExpArrayList(OR));
		expects.put(OR, new ExpArrayList(NOT, PREDICATE_NAME));
		expects.put(COLON, new ExpArrayList(NOT, PREDICATE_NAME));
		expects.put(DEBUG_LABEL, new ExpArrayList(COLON));
		expects.put(PREDICATE_NAME, new ExpArrayList(LEFT_BRACKET));
		
		//目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT
		if (expects.get(LEFT_BRACKET).size() != 1 || !expects.get(LEFT_BRACKET).contains(PARAM_NAME)) {
			throw new RuntimeException("Error: 目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT");
		}
		if (expects.get(COMMA).size() != 1 || !expects.get(COMMA).contains(PARAM_NAME)) {
			throw new RuntimeException("Error: 目前语法限制这里只能一条内容，且必须是：CONTENT_TEXT");
		}
	}
	
	public static void main(String[] args) {
		new FolParser().parse("Logic1: !a(pos1) v b(pos2) v !c(pos1, pos2) v sdfDf(sd)");
	}
	
	public FormulaOrPredicate parse(String text){
		//如果是谓词，则调用谓词解析
		if (text.indexOf(':') == -1) {
			return new FormulaOrPredicate(null, predicateParser.parse(text));
		}
		//否则解析公式
		char[] chars = text.toCharArray();
		StringBuffer sbContext = new StringBuffer();
		Stack<String> bracketPairStack = new Stack<String>();
		ExpArrayList currentExpects = new ExpArrayList(DEBUG_LABEL);
		
		ArrayList<String> tokens = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();
		String logicName = "";
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
				} else if (c == LEFT_BRACKET.charAt(0)){
					bracketPairStack.push(LEFT_BRACKET);
				}
			}
			if (c == ' ' || c == '\t' || c == '\n') {
				continue;
			} else {
				if (currentExpects.contains(DEBUG_LABEL)) {
					if (c == COLON.charAt(0)) {
						if (sbContext.length() == 0) {
							throw new RuntimeException("Logic name must be defined!");
						} else {
							logicName = sbContext.toString().trim();
							sbContext.setLength(0);
							currentExpects = expects.get(COLON);
						}
					} else {
						sbContext.append(c);
					}
				} else if (currentExpects.contains(PARAM_NAME)) {
					// 仅这个地方期望内容, 所以用contains
					if(c == QUOTE) {
						if(!inQuote){
							inQuote = true;
						} else {
							inQuote = false;
						}
					} else if (!inQuote && (c == COMMA.charAt(0) || c == RIGHT_BRACKET.charAt(0))) {
						String content = sbContext.toString().trim();
						//System.out.println("content: " + content);
						tokens.add(content);
						types.add(PARAM_NAME);
						String keyword = COMMA.charAt(0) == c ? COMMA : RIGHT_BRACKET;
						tokens.add(keyword);
						types.add(keyword);
						//System.out.println("keyword: " + keyword);
						sbContext.setLength(0);
						currentExpects = expects.get(keyword);
					} else {
						sbContext.append(c);
					}
				} else if(currentExpects.contains(PREDICATE_NAME) || currentExpects.contains(NOT)){
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
						}
					} else if (c == LEFT_BRACKET.charAt(0)) {
						tokens.add(sbContext.toString().trim());
						types.add(PREDICATE_NAME);
						sbContext.setLength(0);
						tokens.add(LEFT_BRACKET);
						types.add(LEFT_BRACKET);
						currentExpects = expects.get(LEFT_BRACKET);
					} else {
						sbContext.append(c);
					}
				} else if(currentExpects.contains(OR)){
					if (c == OR.charAt(0)) {
						tokens.add(OR);
						types.add(OR);
						sbContext.setLength(0);
						currentExpects = expects.get(OR);
					} else {
						int offset = i < 20 ? 0 : i - 20;
						throw new RuntimeException("v is expected at: " + new String(chars, offset, i - offset + 1));
					}
				} else {
					int offset = i < 20 ? 0 : i - 20;
					throw new RuntimeException("Unknown syntax around: " + new String(chars, offset, i - offset + 1));
				}
			}
		}
		if (bracketPairStack.size() > 0) {
			throw new RuntimeException("Some part is not close by ')'");
		}
		
//		System.out.println("org: " + text);
//		System.out.println("tokens: " + tokens.toString());
//		System.out.println("types: " + types.toString());
		
		Formula formula = toFormula(logicName, tokens, types);
		System.out.println(formula.toString());
		
		return new FormulaOrPredicate(formula, null);
	}
	
	private static Formula toFormula(String logicName, ArrayList<String> tokens, ArrayList<String> types){
		Formula formula = new Formula(logicName);
		int size = tokens.size();
		Predicate predi = new Predicate();
		for (int i = 0; i < size; i++) {
			String type = types.get(i);
			if (type.equals(NOT)) {
				predi.setWithNot(true);
			} else if (type.equals(PARAM_NAME)) {
				if (tokens.get(i).equals("")) {
					throw new RuntimeException("Logic " + logicName + " - " + predi.getName() + " has an empty param!");
				}
				predi.addParam(tokens.get(i));
			} else if (type.equals(PREDICATE_NAME)) {
				predi.setName(tokens.get(i));
			} else if (type.equals(RIGHT_BRACKET)) {
				if (predi.getParams().size() == 0) {
					throw new RuntimeException("Logic " + logicName + " - " + predi.getName() + " has no params!");
				}
				predi.onPredicateParseFinish(i == size - 1);
				formula.addPredicate(predi);
				predi = new Predicate();
			}
		}
		return formula;
	}
	
	public static class FormulaOrPredicate {
		
		public Formula formula;
		
		public Predicate predicate;
		
		public FormulaOrPredicate(Formula formula, Predicate predicate) {
			this.formula = formula;
			this.predicate = predicate;
		}
		
	}

	public static class ExpArrayList extends ArrayList<String> {
		
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
}

