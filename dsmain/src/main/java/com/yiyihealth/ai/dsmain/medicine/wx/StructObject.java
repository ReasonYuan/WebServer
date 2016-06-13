package com.yiyihealth.ai.dsmain.medicine.wx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class StructObject {
	
	/**
	 * 谓词名字
	 */
	private String predicateName = "";
	/**
	 * 谓词参数
	 */
	private ArrayList<String> predicateParams = new ArrayList<>();
	/**
	 * 谓词方法
	 */
	private String pridicateFuc = "";
	
	/**
	 * 结果
	 */
	private String result = "";
	
	private HashMap<String, Integer> pamrasMap = new HashMap<>();
	
	public String getPredicateName() {
		return predicateName;
	}
	public void setPredicateName(String predicateName) {
		this.predicateName = predicateName;
	}
	public ArrayList<String> getPredicateParams() {
		return predicateParams;
	}
	public void setPredicateParams(ArrayList<String> predicateParams) {
		this.predicateParams = predicateParams;
		setParamsMap();
	}
	public String getPridicateFuc() {
		return pridicateFuc;
	}
	public void setPridicateFuc(String pridicateFuc) {
		this.pridicateFuc = pridicateFuc;
	}
	
	public String getParamsStr(){
		String ss = "";
		for (int i = 0; i < predicateParams.size(); i++) {
			ss += predicateParams.get(i);
		}
		return ss;
	}
	
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	@Override
	public String toString() {
		return predicateName +"   " + getParamsStr() + "   " + getPridicateFuc() + "   " + result;
	}
	
	private void setParamsMap(){
		pamrasMap.clear();
		for (int i = 0; i < predicateParams.size(); i++) {
			pamrasMap.put(predicateParams.get(i), i);
		}
	}
	
	public HashMap<String, Integer> getPamrasMap() {
		return pamrasMap;
	}
	
	/**
	 * 得到查询参数list
	 * @return
	 */
	public ArrayList<SearchWord> getSearchPamras() {
		ArrayList<SearchWord> searchList = new ArrayList<>();
		for (String string : pamrasMap.keySet()) {
			if (!string.equals("_") && !isLetter(string)) {
				SearchWord searchWord = new SearchWord();
				searchWord.setSearchWord(string);
				searchWord.setWordPos(pamrasMap.get(string));
				searchList.add(searchWord);
			}
		}
		searchList.sort(new Comparator<SearchWord>() {

			@Override
			public int compare(SearchWord o1, SearchWord o2) {
				return o1.getWordPos() - o2.getWordPos();
			}
		});
		return searchList;
	}
	
	/**
	 * 得到结果list
	 * @return
	 */
	public ArrayList<SearchWord> getResultPamras() {
		ArrayList<SearchWord> resultList = new ArrayList<>();
		for (String string : pamrasMap.keySet()) {
			if (!string.equals("_") && isLetter(string)) {
				SearchWord searchWord = new SearchWord();
				searchWord.setSearchWord(string);
				searchWord.setWordPos(pamrasMap.get(string));
				resultList.add(searchWord);
			}
		}
		resultList.sort(new Comparator<SearchWord>() {

			@Override
			public int compare(SearchWord o1, SearchWord o2) {
				return o1.getWordPos() - o2.getWordPos();
			}
		});
		return resultList;
	}

	/**
	 * 判断字符是否为字母
	 */
	public static boolean isLetter(String str){
		String reg = "[a-zA-Z]";
		return  str.matches(reg);
	}
	
}
