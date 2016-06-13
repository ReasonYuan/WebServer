package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.CharNode;
import com.yiyihealth.nlp.deepstruct.dict.trie.hitales.TrieNode;

public class Express {

	public static final String ADD = "+";
	public static final String SUB = "-";
	public static final String EQUALS = "=";
	public static final String GT = ">";
	public static final String LT = "<";
	public static final String GET = ">=";
	public static final String LET = "<=";
	
	public static final int S_ADD = 1;
	public static final int S_SUB = 2;
	public static final int S_EQUALS = 3;
	public static final int S_GT = 4;
	public static final int S_LT = 5;
	public static final int S_GET = 6;
	public static final int S_LET = 7;
	
	private static CharNode root = new CharNode();
	
	private ArrayList<String> tokens = new ArrayList<>();
	
	private static ArrayList<String> ResWordsList = null;
	
	private static ArrayList<String> SplitSymbolList = null;
	
	private final int[] symbols;
	
	private final int[] initData;
	
	private final String[] paramVars;
	
	private final boolean[] isInited;
	
	private static String[] RESWORDS = {
		ADD, SUB, EQUALS, GT, LT, GET, LET
	};
	
	private static String[] SPLIT_SYMBOL = {
			EQUALS, GT, LT, GET, LET
	};
	
	static {
		SplitSymbolList = new ArrayList<>(Arrays.asList(SPLIT_SYMBOL)); 
		ResWordsList = new ArrayList<>(Arrays.asList(RESWORDS)); 
		for (int i = 0; i < ResWordsList.size(); i++) {
			TrieNode node = root.addNode(ResWordsList.get(i));
			node.setIsWordEnd(true);
		}
	}
	
	public Express(String express) {
		char[] chars = express.toCharArray();
		TrieNode currentNode = null;
		StringBuffer var = new StringBuffer();
		boolean findNode = false;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			currentNode = root.getNode(c);

			if (currentNode == null) {
				if (findNode) {
					addStr(var);
				}
				var.append(c);
				findNode = false;
				if (i == chars.length - 1) {
					addStr(var);
				}
			} else {
				if (!findNode) {
					addStr(var);
				}
				var.append(c);
				findNode = true;
			}
		}
		symbols = new int[tokens.size()];
		for (int j = 0; j < symbols.length; j++) {
			String token = tokens.get(j).trim();
			switch (token) {
			case ADD:
				symbols[j] = S_ADD;
				break;
			case SUB:
				symbols[j] = S_SUB;
				break;
			case EQUALS:
				symbols[j] = S_EQUALS;
				break;
			case GET:
				symbols[j] = S_GET;
				break;
			case LET:
				symbols[j] = S_LET;
				break;
			case LT:
				symbols[j] = S_LT;
				break;
			case GT:
				symbols[j] = S_GT;
				break;
			default:
				break;
			}
		}
		isInited = new boolean[symbols.length];
		initData = new int[symbols.length];
		for (int j = 0; j < symbols.length; j++) {
			if (symbols[j] == 0) {
				try {
					initData[j] = Integer.parseInt(tokens.get(j).trim());
					isInited[j] = true;
				} catch (Exception e) {
				}
			}
		}
		paramVars = generateParamVarNames();
	}
	
	private void addStr(StringBuffer var){
		String varN = var.toString();
		var.setLength(0);
		tokens.add(varN);
	}
	
	public ArrayList<String> getTokens() {
		return tokens;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getParamVarNames(){
		return paramVars;
	}
	
	/**
	 * 
	 * @return
	 */
	private String[] generateParamVarNames(){
		String[] paramVarNames = new String[tokens.size()];
		for (int i = 0; i < tokens.size(); i++) {
			String value = tokens.get(i);
			if(ResWordsList.contains(value) || isNumeric(value)){
				value = null;
			}
			paramVarNames[i] = value;
		}
		return paramVarNames;
	}
	
	/**
	 * 判断字符串是否为数字
	 * @param str
	 * @return
	 */
	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str.trim());
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
	
	public boolean caculateExpress(int[] vars){
		int[] cacMem = new int[symbols.length];
		System.arraycopy(initData, 0, cacMem, 0, initData.length);
		for (int i = 0; i < vars.length; i++) {
			if (symbols[i] == 0 && !isInited[i]) {
				cacMem[i] = vars[i];
			}
		}
		int[] sums = new int[3];
		int sumIdx = 0;
		int[] compareSymbol = new int[2];
		int addOrSub = 1;
		for (int i = 0; i < symbols.length; i++) {
			switch (symbols[i]) {
			case 0:
				sums[sumIdx] += cacMem[i]*addOrSub;
				break;
			case S_ADD:
				addOrSub = 1;
				break;
			case S_SUB:
				addOrSub = -1;
				break;
			default:
				compareSymbol[sumIdx] = symbols[i];
				sumIdx++;
				addOrSub = 1;
				break;
			}
		}
		
		boolean finalTest = true;
		for (int i = 0; i < sumIdx; i++) {
			int a = sums[i];
			int b = sums[i+1];
			boolean test = false;
			switch (compareSymbol[i]) {
			case S_LT:
				test = a < b;
				break;
			case S_LET:
				test = a <= b;
				break;
			case S_GT:
				test = a > b;
				break;
			case S_GET:
				test = a >= b;
				break;
			case S_EQUALS:
				test = a == b;
				break;
			default:
				break;
			}
			if (!test) {
				finalTest = false;
				break;
			}
		}
		
		
//		String[] src = insertData(vars);
//		splitData(src);
//		calculateEveryData();
//		boolean isRight =  calculateEveryDataIsRight();
//		return isRight;
		
//		
		return finalTest;
	}
	
	/**
	 * 将数据组装为可以计算的数组
	 * @param vars
	 */
	public String[] insertData(int[] vars){
		String[] src = new String[tokens.size()];
		tokens.toArray(src);
		String[] params = getParamVarNames();
		int length = params.length;
		for (int i = 0; i < length; i++) {
			if(params[i] != null && !isNumeric(params[i])){
				src[i] = "" +vars[i];
			}
		}
		
		return src;
	}
	
	/**
	 * 拆分可进行计算的数据
	 * @param src
	 */
	private ArrayList<String> splitSymbolList = new ArrayList<>();
	private HashMap<Integer, ArrayList<String>> calculateMap = new HashMap<>();
	public void splitData(String[] src){
		ArrayList<String> calculate =  new ArrayList<>();;
		for (int i = 0; i < src.length; i++) {
			String text = src[i];
			if(!SplitSymbolList.contains(text)){
				calculate.add(text);
				if(i == src.length - 1){
					calculateMap.put(calculateMap.size(), calculate);
				}
			}else{
				splitSymbolList.add(text);
				calculateMap.put(calculateMap.size(), calculate);
				calculate  = new ArrayList<>();
			}
		}
	}
	
	/**
	 * 计算每组数据的结果
	 */
	private ArrayList<Integer> results = new ArrayList<>();
	
	public void calculateEveryData(){
		Set<Integer> keys = calculateMap.keySet();
		for (Integer key : keys) {
			ArrayList<String> list = calculateMap.get(key);
			int result = 0;
			int tmp = 0;
			String lastFuc = "+";
			for (int i = 0; i < list.size(); i++) {
				String text = list.get(i).trim();
				if(i%2 == 0){
					tmp = Integer.parseInt(text);
				}else {
					lastFuc = text;
					continue;
				}
				
				if(lastFuc.equals("+")){
					result +=tmp;
				}else{
					result -=tmp;
				}
			}
			results.add(result);
		}
	}
	
	public boolean calculateEveryDataIsRight(){
		boolean isRight = false;
		ArrayList<Boolean> list = new ArrayList<>();
		for (int j = 0; j < splitSymbolList.size(); j++) {
			String ss = splitSymbolList.get(j);
			switch (ss) {
			case "=":
				if(results.size() > 0){
					isRight = (results.get(0) == results.get(1)); 
					results.remove(0);
				}
				break;
			case ">=":
				if(results.size() > 0){
					isRight = (results.get(0) >= results.get(1)); 
					results.remove(0);
				}
				break;
			case "<=":
				if(results.size() > 0){
					isRight = (results.get(0) <= results.get(1)); 
					results.remove(0);
				}
				break;
			case "<":
				if(results.size() > 0){
					isRight = (results.get(0) < results.get(1)); 
					results.remove(0);
				}
				break;
			case ">":
				if(results.size() > 0){
					isRight = (results.get(0) > results.get(1)); 
					results.remove(0);
				}
				break;
			default:
				break;
			}
			list.add(isRight);
		}
		return isAllRight(list);
	}
	
	public boolean isAllRight(ArrayList<Boolean> allRihts){
		int findRightCount = 0;
		for (int i = 0; i < allRihts.size(); i++) {
			boolean isOk = allRihts.get(i);
			if(isOk){
				findRightCount++;
			}
		}
		if(findRightCount != allRihts.size()){
			return false;
		}else {
			return true;
		}
	}
	
}
