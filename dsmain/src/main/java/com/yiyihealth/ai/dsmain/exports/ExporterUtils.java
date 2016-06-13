package com.yiyihealth.ai.dsmain.exports;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ai.dsmain.atom.Diag;
import com.yiyihealth.ai.dsmain.atom.Exam;
import com.yiyihealth.ai.dsmain.atom.Symptom;
import com.yiyihealth.ai.dsmain.atom.TestItem;
import com.yiyihealth.ai.dsmain.medicine.wx.Medicine;
import com.yiyihealth.ai.dsmain.medicine.wx.UnDefAndUnRecTittles;
import com.yiyihealth.ai.dsmain.medicine.wx.UnRecObject;
import com.yiyihealth.ds.esearch.fol.Atom;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;

public class ExporterUtils {
	
	public static ArrayList<Object> getDrugExtract(InherenceEngine engine){
		ArrayList<Object> medicines = new ArrayList<>();
		Atom[] mediAtoms = engine.queryAtom("UseFinallDrug");
		Atom[] mediStopAtoms = engine.queryAtom("StopFinallDrug");
		for (int i = 0; i < mediAtoms.length; i++) {
			Atom mAtom = mediAtoms[i];
			Medicine medicine = new Medicine();
			String name = mAtom.getParams().get(0).toString();
			String jiliang = parseNull(mAtom.getParams().get(4).toString()) + parseNull(mAtom.getParams().get(5).toString());
			String pinci = parseNull(mAtom.getParams().get(6).toString()) + parseNull(mAtom.getParams().get(7).toString());
			String date = engine.getDate(Integer.parseInt(mAtom.getParams().get(2).toString().trim()));
			String postion = mAtom.getParams().get(2).toString();
			medicine.setMedicineName(name);
			medicine.setJiLiang(jiliang);
			medicine.setPinCi(pinci);
			medicine.setDate(date);
			medicine.setPosition(postion);
			medicine.setNowUse("使用");
			medicines.add(medicine.toHashMap());
		}
		for (int i = 0; i < mediStopAtoms.length; i++) {
			Atom mAtom = mediStopAtoms[i];
			Medicine medicine = new Medicine();
			String name = mAtom.getParams().get(0).toString();
			String jiliang = parseNull(mAtom.getParams().get(4).toString()) + parseNull(mAtom.getParams().get(5).toString());
			String pinci = parseNull(mAtom.getParams().get(6).toString()) + parseNull(mAtom.getParams().get(7).toString());
			String date = engine.getDate(Integer.parseInt(mAtom.getParams().get(2).toString().trim()));
			String postion = mAtom.getParams().get(2).toString();
			medicine.setMedicineName(name);
			medicine.setJiLiang(jiliang);
			medicine.setPinCi(pinci);
			medicine.setDate(date);
			medicine.setPosition(postion);
			medicine.setNowUse(mAtom.getParams().get(16).toString());
			medicines.add(medicine.toHashMap());
		}
		
		
		return medicines;
	}
	
	public static ArrayList<Object> getSymptomList(InherenceEngine engine) {
		Atom[] atoms = engine.queryAtom("FinalReSymptom");
		ArrayList<Object> result = new ArrayList<>();
		
		ArrayList<Symptom> tmpResult = new ArrayList<>();
		
		for (int i = 0; i < atoms.length; i++) {
			
			Symptom symptom = new Symptom();
			
			Atom atom = atoms[i];
			ArrayList<Object> params = atom.getParams();
			int symptomPos = Integer.parseInt(params.get(4).toString());
			String date = engine.getDate(symptomPos);
			
			String symptomStr = (String) params.get(2);
			
			String buweiNature = (String) params.get(6);
			if (buweiNature.equals("Region")) {
				String buwei = (String) params.get(5);
				String adj = (String) params.get(9);
				if (adj.equals("RegionAdj")||adj.equals("RegionQ")||adj.equals("RegionDes")) {
					buwei = (String)params.get(8) + buwei;
				}
				symptom.setRegion(buwei);
			}
			symptom.setDate(date);
			symptom.setName(symptomStr);
			symptom.setPos(symptomPos+"");
			
			boolean isExits = false;
			for (int j = 0; j < result.size(); j++) {
				Symptom symp = (Symptom)tmpResult.get(j);
				String mTimeStr = symp.getDate();
				String mSymptomStr = symp.getName();
				String mRegionStr = symp.getRegion();
				String regionStr = symptom.getRegion();
				
				if (date.equals(mTimeStr) && symptomStr.equals(mSymptomStr) ) {
					
//					if (regionStr == null && mRegionStr == null) {
//						isExits = true;
//					}
					
					if (regionStr != null && mRegionStr != null && regionStr.equals(mRegionStr)) {
						isExits = true;
					}
				}
			}
			
			if (!isExits) {
				tmpResult.add(symptom);
				result.add(symptom.toHashMap());
			}
		}
		
		
		return result;
	}

	public static ArrayList<Object> getDiagList(InherenceEngine engine) {
		Atom[] atoms = engine.queryAtom("ReDiag");
		ArrayList<Object> result = new ArrayList<>();
		
		for (int i = 0; i < atoms.length; i++) {
			Diag diag = new Diag();
			
			Atom atom = atoms[i];
			ArrayList<Object> params = atom.getParams();
			int diagPos = Integer.parseInt(params.get(2).toString());
			String date = engine.getDate(diagPos);
			
			String diagStr = (String) params.get(0);
			
			diag.setDate(date);
			diag.setName(diagStr);
			diag.setPos(diagPos+"");
			
			result.add(diag.toHashMap());
		}
		
		
		return result;
	}

	public static ArrayList<Object> getTestItemList(InherenceEngine engine) {
		Atom[] atoms = engine.queryAtom("TestItemAndDes");
		ArrayList<Object> result = new ArrayList<>();
		
		for (int i = 0; i < atoms.length; i++) {
			TestItem testItem = new TestItem();
			
			Atom atom = atoms[i];
			ArrayList<Object> params = atom.getParams();
			int testItemPos = Integer.parseInt(params.get(2).toString());
			String date = engine.getDate(testItemPos);
			
			String testItemStr = (String) params.get(0);
			
			String secendNature = (String) params.get(4);
			String thirdNature = (String) params.get(7);
			String fourthNature = (String) params.get(10);
			
			if (secendNature.equals("Value")) {
				testItem.setValue((String) params.get(3));
			}
			if (thirdNature.equals("ValueUnit")) {
				testItem.setValueUnit((String) params.get(6));
			}
			if (fourthNature.equals("DirVerb")) {
				testItem.setAbnormal((String) params.get(9));
			}
			if (secendNature.equals("DirVerb")) {
				testItem.setAbnormal((String) params.get(3));
			}
			
			
			testItem.setDate(date);
			testItem.setName(testItemStr);
			testItem.setPos(testItemPos+"");
			
			result.add(testItem.toHashMap());
			
		}
		return result;
	}
	
	
	public static ArrayList<Object> getExamList(InherenceEngine engine) {
		Atom[] atoms = engine.queryAtom("ReExamIndicatewxx");
		ArrayList<Object> result = new ArrayList<>();
		
		for (int i = 0; i < atoms.length; i++) {
			Exam testItem = new Exam();
			
			Atom atom = atoms[i];
			ArrayList<Object> params = atom.getParams();
			int testItemPos = Integer.parseInt(params.get(1).toString());
			String date = engine.getDate(testItemPos);
			String indicate = params.get(4).toString();
			String name = params.get(0).toString();
			testItem.setIndicate(indicate);
			testItem.setName(name);
			testItem.setDate(date);
			testItem.setPos(testItemPos+"");
			result.add(testItem.toHashMap());
		}
		return result;
	}
	
	
	public static String parseNull(String string){
		if (string.equals("null") || string.equals("")) {
			return "";
		}
		return string;
	}
	
	/**
	 * 将list按照位置排序
	 * @param lists
	 * @return
	 */
	public static ArrayList<Object> sortList(ArrayList<Object> lists){
		lists.sort(new Comparator<Object>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(Object o1, Object o2) {
				return Integer.parseInt(((HashMap<String, String>)o1).get("位置").trim()) - Integer.parseInt(((HashMap<String, String>)o2).get("位置").trim());
			}
		});
		return lists;
	}
	
	private static ArrayList<String> queryUndefAndUnRecWord(InherenceEngine engine) {
		Atom[] atoms = engine.queryAtom("Word");
		ArrayList<String> undefLists = new ArrayList<>();
		for (int i = 0; i < atoms.length; i++) {
			Atom atom = atoms[i];
			String nature = atom.getParams().get(1).toString();
			String word = atom.getParams().get(0).toString();
			String tittle = "";
			boolean find = false;
			switch (nature) {
			case WordNatures.UNDEF:
				tittle = "未定义：" + word + "   建议将其定义为：";
				find = true;
				break;
			case WordNatures.UNREC:
				tittle = "未识别：" + word + "   建议将其定义为：";
				find = true;
				break;
			default:
				break;
			}
			if (find) {
				String pos = atom.getParams().get(2).toString();
				int cPos = Integer.parseInt(pos);
				String paragraph = findParagraph(cPos, engine) + "\n";
				undefLists.add(tittle);
				undefLists.add(paragraph);
			}
		}
		return undefLists;
	}
	
	private static ArrayList<UnRecObject> queryUndefAndUnRecWord2(InherenceEngine engine) {
		Atom[] atoms = engine.queryAtom("Word");
		ArrayList<UnRecObject> undefLists = new ArrayList<>();
		for (int i = 0; i < atoms.length; i++) {
			Atom atom = atoms[i];
			String nature = atom.getParams().get(1).toString();
			String word = atom.getParams().get(0).toString();
			boolean find = false;
			switch (nature) {
			case WordNatures.UNDEF:
				find = true;
				break;
			case WordNatures.UNREC:
				find = true;
				break;
			default:
				break;
			}
			if (find) {
				UnRecObject mObject = new UnRecObject();
				String pos = atom.getParams().get(2).toString();
				int cPos = Integer.parseInt(pos);
				mObject.setWordPos(pos);
				mObject.setNature(nature);
				mObject.setWord(word);
				String paragraph = findParagraph(cPos, engine,mObject) ;
				mObject.setLongContent(paragraph);
				undefLists.add(mObject);
			}
		}
		return undefLists;
	}
	
	public static String findParagraph(int pos,InherenceEngine engine){
		int lastIndex = -10;
		int nextIndex = 10;
		int startIndex = pos + lastIndex;
		int endIndex = pos + nextIndex;
		
		if (startIndex < 0) {
			startIndex = 0;
		}
		
		if (endIndex > engine.getFolNetwork().getMemDB().getWords().length) {
			endIndex = engine.getFolNetwork().getMemDB().getWords().length;
		}
		
		String str = "";
		for (int i = startIndex; i < endIndex; i++) {
			if(pos == i){
				str += " [" + engine.getFolNetwork().getMemDB().getEWord(i).getWord().trim().toString() +"] ";
			}else {
				str += engine.getFolNetwork().getMemDB().getEWord(i).getWord().trim().toString();
			}
		}
		return str;
	}
	
	public static String findParagraph(int pos,InherenceEngine engine,UnRecObject unRecObject){
		int lastIndex = -10;
		int nextIndex = 10;
		int startIndex = pos + lastIndex;
		int endIndex = pos + nextIndex;
		
		if (startIndex < 0) {
			startIndex = 0;
		}
		
		if (endIndex > engine.getFolNetwork().getMemDB().getWords().length) {
			endIndex = engine.getFolNetwork().getMemDB().getWords().length;
		}
		
		String str = "";
		for (int i = startIndex; i < endIndex; i++) {
//			if(pos == i){
//				str += " [" + engine.getFolNetwork().getMemDB().getEWord(i).getWord().trim().toString() +"] ";
//			}else {
				str += engine.getFolNetwork().getMemDB().getEWord(i).getWord().trim().toString();
//			}
		}
		return str;
	}
	
	
	public static String findHistoryIllString(InherenceEngine engine,String historyIllKeyName){
		WordPos[] wordPos = engine.getFolNetwork().getMemDB().getWords();
		String src = "";
		int hisPos = 0;
		for (int i = 0; i < wordPos.length; i++) {
			String word = wordPos[i].eWord.getWord();
			if(word.equals(historyIllKeyName)){
				hisPos = i;
				break;
			}
		}
		
		for (int i = hisPos+1; i < wordPos.length; i++) {
			String nature =wordPos[i].eWord.getNature();
			String word = wordPos[i].eWord.getWord();
			if(!nature.equals("Heading")){
				src += word;
			}else{
				break;
			}
		}
		return src;
	}
	
	public static String findAllIllString(InherenceEngine engine){
		WordPos[] wordPos = engine.getFolNetwork().getMemDB().getWords();
		String src = "";
		
		for (int i = 0; i < wordPos.length; i++) {
			String word = wordPos[i].eWord.getWord();
			src += word;
		}
		return src;
	}
	
	public static void writeUndefAndUnRecTofile(String path,InherenceEngine engine,String fileName){
		String src = findHistoryIllString(engine, "history_of_present_illness");
		ArrayList<String> lists = queryUndefAndUnRecWord(engine);
		String ss = "文件：" + fileName + "\n";
		ss =  ss + src + "\n\n";
		String tittle = "==============以下词为：未定义与未识别============== \n\n";
		ss += tittle;
		for (int i = 0; i < lists.size(); i++) {
			ss += lists.get(i) + "\n";
		}
		FileManager.writeToFile(path, ss);
	}
	
	public static JSONObject getUndefAndUnRecToJson(InherenceEngine engine){
		String src = "";
		JSONObject mJsonObject = null;
			mJsonObject =  new JSONObject();
			src = findAllIllString(engine);
//			mJsonObject.put(UnDefAndUnRecTittles.fileName, fileName);
			mJsonObject.put(UnDefAndUnRecTittles.allContext, src);
			ArrayList<UnRecObject> lists = queryUndefAndUnRecWord2(engine);
			lists.sort(new Comparator<UnRecObject>() {
				@Override
				public int compare(UnRecObject o1, UnRecObject o2) {
					return Integer.parseInt(o1.getWordPos()) - Integer.parseInt(o2.getWordPos());
				}
			});
			JSONArray mArray = new JSONArray();
			for (UnRecObject unRecObject : lists) {
				mArray.add(unRecObject.toJsonObject());
			}
			mJsonObject.put(UnDefAndUnRecTittles.unDefAndUnRecWords, mArray);
			
			return mJsonObject;
//			FileManager.writeToFile(path, format(mJsonObject.toJSONString()));
	}
	
	
	/**
	   * 得到格式化json数据  退格用\t 换行用\r
	   */
	  public static String format(String jsonStr) {
	    int level = 0;
	    StringBuffer jsonForMatStr = new StringBuffer();
	    for(int i=0;i<jsonStr.length();i++){
	      char c = jsonStr.charAt(i);
	      if(level>0&&'\n'==jsonForMatStr.charAt(jsonForMatStr.length()-1)){
	        jsonForMatStr.append(getLevelStr(level));
	      }
	      switch (c) {
	      case '{': 
	      case '[':
	        jsonForMatStr.append(c+"\n");
	        level++;
	        break;
	      case ',': 
	        jsonForMatStr.append(c+"\n");
	        break;
	      case '}':
	      case ']':
	        jsonForMatStr.append("\n");
	        level--;
	        jsonForMatStr.append(getLevelStr(level));
	        jsonForMatStr.append(c);
	        break;
	      default:
	        jsonForMatStr.append(c);
	        break;
	      }
	    }
	    
	    return jsonForMatStr.toString();

	  }
	  
	  private static String getLevelStr(int level){
	    StringBuffer levelStr = new StringBuffer();
	    for(int levelI = 0;levelI<level ; levelI++){
	      levelStr.append("\t");
	    }
	    return levelStr.toString();
	  }
}
