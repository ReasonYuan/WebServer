package com.yiyihealth.ai.dsmain.learning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ai.dsmain.medicine.wx.ExcelByUser;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class FeatureStats extends EWordStats {
	
	private static Hashtable<String, int[]> natureStats;
	
	private static Hashtable<String, int[]> wordStats;
	
	private static Hashtable<Integer, Hashtable<String, int[]>> natures = new Hashtable<>(); 
	
	private static Hashtable<Integer, Hashtable<String, int[]>> words = new Hashtable<>(); 

	private int writeCount = 0;
	
	private String[] fileNameStr = {"feature_eval","feature_train","feature_test"};
	
	private final static int preWindowNumSize = 5;
	private final static int nextWindowNumSize = 5;
	private final static int usrCount = 10;
	
	public FeatureStats(String nature) {
		super(nature);
	}
	
	/**
	 * 不感兴趣的词
	 * @param word
	 * @return
	 */
	private boolean filterWord(EWord word){
		boolean passTest = true;
		if (word.getNature().equals(WordNatures.PUNC)) {
			//不把标点符号计算在内
			passTest = false;
		}
		return passTest;
	}

	@Override
	public void stats(EWord[] words) {
		final int preWindowNum = -preWindowNumSize;
		final int nextWindowNum = nextWindowNumSize;
		for (int i = 0; i < words.length; i++) {
			if (words[i].getNature().equals(nature)) {
				int preWindow = preWindowNum;
				int nextWindow = nextWindowNum;
				int start = i+preWindow;
				int end = i+nextWindow+1;
				final int startTmp  = i+preWindow;
//				final int endTmp  = i+nextWindow+1;
				for (int j = i; j >= start && j >= 0; j--) {
					if (Punctuation.isSentenceEnd(words[j])) {
						start = j + 1;
						break;
					}
				}
				for (int j = i; j < end && j < words.length; j++) {
					if (Punctuation.isSentenceEnd(words[j])) {
						end = j - 1;
						break;
					}
				}
				for(int j=start; j<end; j++){
					if (j >= 0 && j < words.length) {
						if (filterWord(words[j])) {
							int pos = j - startTmp;
							EWord statWord = words[j];
							addSum(true, statWord.getWord(),pos);
							addSum(false, statWord.getNature(),pos);
						}
					}
				}
			}
		}
	}
	
	private void addSum(boolean isWord, String text,int postion){
		if (isWord) {
			wordStats = words.get(postion);
			if(wordStats == null){
				wordStats = new Hashtable<>();
			}
			
			int[] counter = wordStats.get(text);
			if (counter == null) {
				counter = new int[1];
				wordStats.put(text, counter);
			}
			counter[0]++;
			words.put(postion, wordStats);
		} else {
			natureStats =  natures.get(postion);
			if(natureStats == null){
				natureStats = new Hashtable<>();
			}
			
			int[] counter = natureStats.get(text);
			if (counter == null) {
				counter = new int[1];
				natureStats.put(text, counter);
			}
			counter[0]++;
			natures.put(postion, natureStats);
		}
	}
	
	public ArrayList<Element> createElementCollectionWordsAndNature(){
		HashMap<Integer, ArrayList<Element>> wordsMap = createElementCollection(words, "FeatureStatsWords",false);
		HashMap<Integer, ArrayList<Element>> naturesMap = createElementCollection(natures, "FeatureStatsNature",true);
		ArrayList<Element> allElementLists = new ArrayList<>();
		for(int i = 0;i<wordsMap.size();i++){
			if(i != 5){
				ArrayList<Element> wordLists = wordsMap.get(i);
				allElementLists.addAll(wordLists);
				ArrayList<Element> natureLists = naturesMap.get(i);
				allElementLists.addAll(natureLists);
			}
		}
		System.out.println("list size-----" + allElementLists.size());
//		for(int m = 0;m< allElementLists.size();m++){
//			System.out.println(m+"-----name----" + allElementLists.get(m).getName() + "-----isNature---" + allElementLists.get(m).isNature());
//		}
		return allElementLists;
	}
	
	private static HashMap<Integer, ArrayList<Element>> createElementCollection(Hashtable<Integer, Hashtable<String, int[]>> table,String fileName,boolean isNature){
		HashMap<Integer, ArrayList<Element>> map = new HashMap<>();
		Set<Integer> nkeys = table.keySet();
		for (Integer postion : nkeys) {
			Hashtable<String, int[]> hashtable = table.get(postion);
			Set<String> keys = hashtable.keySet();
			ArrayList<Element> list = new ArrayList<>();
			for (String key : keys) {
				int[] counter = hashtable.get(key);
				Element element = new Element();
				element.setCounts(counter[0]);
				element.setName(key);
				element.setPosition(postion);
				if(isNature){
					element.setNature(true);
				}
				list.add(element);
			}
			
			list.sort(new Comparator<Element>() {

				@Override
				public int compare(Element o1, Element o2) {
					return o2.getCounts() - o1.getCounts();
				}
			});
			map.put(postion, list);
		}
		
		for (int i = 0; i < map.size(); i++) {
			ArrayList<Element> tmpList = map.get(i);
			printElement(tmpList);
			if(i != 5){
				tmpList = reCreateList(tmpList,usrCount,i);
				map.put(i, tmpList);
				
			}
			
		}
		return map;
	}
	
	
	private static void printElement(ArrayList<Element> tmpList){
		String str =  "";
		for (int i = 0; i < tmpList.size(); i++) {
			str += "["+ tmpList.get(i).getPosition() + "   "+ tmpList.get(i).getName() + "   " + tmpList.get(i).getCounts() +"]    ";
		}
		System.out.println(str);
		JSONObject config;
		try {
			config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
			final String projectDir = config.getString("projectDir");
			
			ExcelByUser.writeYiJuToFile(projectDir + "/FeartureResults",str,"fearture_information" +".txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static ArrayList<Element> reCreateList(ArrayList<Element> tmpList,int preCouts,int postion){
		ArrayList<Element> list = new ArrayList<>();
		if(tmpList.size() >= preCouts){
			for(int i = 0;i<preCouts;i++){
				list.add(tmpList.get(i));
			}
			return list;
		}else{
			int needAddZeroSize = preCouts - tmpList.size();
			for(int i = 0; i< needAddZeroSize;i++){
				Element element = new Element();
				element.setPosition(postion);
				tmpList.add(element);
			}
			return tmpList;
		}
	}
	
	/**
	 * 找到Feature向量
	 * @param words
	 */
	public void findFeatureVector(EWord[] words,ArrayList<Element> lists,String fileName){
		final int preWindowNum = -preWindowNumSize;
		final int nextWindowNum = nextWindowNumSize;
		for (int i = 0; i < words.length; i++) {
			FeatureVector featureVector = new FeatureVector((nextWindowNumSize+preWindowNumSize)*(usrCount*2));
			if (words[i].getNature().equals(nature)) {
				int wordPos = i;
				String wordsSeq = ""; 
				for (int j = i - 5; j < i + 5; j++) {
					if(j >= 0 && j < words.length){
						if (j == i) {
							wordsSeq += "[" + words[j].getWord() + "]";
						} else {
							wordsSeq += words[j].getWord();
						}
					}
				}
				int preWindow = preWindowNum;
				int nextWindow = nextWindowNum;
				int start = i+preWindow;
				int end = i+nextWindow+1;
				final int startTmp  = i+preWindow;
//				final int endTmp  = i+nextWindow+1;
				for (int j = i; j >= start && j >= 0; j--) {
					if (Punctuation.isSentenceEnd(words[j])) {
						start = j + 1;
						break;
					}
				}
				for (int j = i; j < end && j < words.length; j++) {
					if (Punctuation.isSentenceEnd(words[j])) {
						end = j - 1;
						break;
					}
				}
				for(int j=start; j<end; j++){
					if (j >= 0 && j < words.length) {
						if (filterWord(words[j])) {
							int pos = j - startTmp;
							EWord statWord = words[j];
							findFeartureWithPosInList(lists, pos, statWord,featureVector);
						}
					}
				}
				printFeatureVector(featureVector,fileName,wordPos,wordsSeq);
			}
			
		}
	}
	
	private void printFeatureVector(FeatureVector featureVector,String fileName,int wordPos,String wordsSeq){
		String str = "";
		for(int i = 0; i<featureVector.getInitValues().length;i++){
			if(i != featureVector.getInitValues().length - 1){
				str += featureVector.getValueForIndex(i)+",";
			}else{
				str += featureVector.getValueForIndex(i);
			}
			
		}
		str = "1," +str;
		str = "//"+fileName +"  " + wordPos + wordsSeq +"   "+"\r\n" + str;
		writeCount ++;
		JSONObject config;
		try {
			config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
			final String projectDir = config.getString("projectDir");
			String name  = fileNameStr[0];
			if(writeCount > 300){
				name = fileNameStr[1];
			}
//			
			ExcelByUser.writeYiJuToFile(projectDir + "/FeartureResults",str,name +".txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private FeatureVector findFeartureWithPosInList(ArrayList<Element> list,int pos,EWord word,FeatureVector featureVector){
		for (int i = 0; i < list.size(); i++) {
			Element element = list.get(i);
			int ePos = element.getPosition();
			String name = element.getName();
			if(ePos == pos){
				if(name.equals(word.getWord())){
					featureVector.setValueForIndex(i, 1);
				}
				if(name.equals(word.getNature())){
					featureVector.setValueForIndex(i, 1);
				}
			}
		}
		return featureVector;
	}
}
