package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


/**
 * 
 * @author qiangpeng
 *
 */
public class InherenceEngine {
	
	/**
	 * 推理记录
	 */
	Hashtable<String, Boolean> inHistories = new Hashtable<String, Boolean>();
	
	/**
	 * 还未得到证实的假说
	 */
	Hashtable<String, Boolean> propositions = new Hashtable<String, Boolean>();

	ArrayList<Predicate> evidences;
	
	ArrayList<Predicate> predicates;
	
	ArrayList<Formula> formulas;
	
	private FolNetwork folNetwork;
	
	protected InherenceEngine(ArrayList<Formula> formulas, ArrayList<Predicate> evidences, String dbFile) {
		this.formulas = formulas;
		this.evidences = evidences;
		folNetwork = new FolNetwork(formulas, dbFile);
		//先把证据加入
		for (int i = 0; i < evidences.size(); i++) {
			//有Not的表示值为false
			Atom atom = new Atom(folNetwork.getAtomManager(), PredicateManager.getInstance().getPredicateByName(evidences.get(i).getName()), evidences.get(i).getParams(), !evidences.get(i).isWithNot(), null, null);
			folNetwork.addAtom(atom);
		}
	}
	
	public void addFormula(Formula formula){
		folNetwork.addFormula(formula);
	}
	
	public FolNetwork getFolNetwork() {
		return folNetwork;
	}
	
	public void doInherence(){
//		Formula[] fs = new Formula[formulas.size()];
//		formulas.toArray(fs);
//		for (int i = 0; i < fs.length; i++) {
//			Formula formula = fs[i];
//			ArrayList<Predicate> fsPredicates = formula.getPredicates();
//			boolean[] satisified = new boolean[fsPredicates.size()];
//			for (int j = 0; j < fsPredicates.size(); j++) {
//				Predicate predicate = fsPredicates.get(j);
//				//寻求每一个谓词的满足情况
//				ArrayList<Atom> atoms = allAtoms.get(predicate.getName());
//				
//			}
//		}
		int turnCnt = 0;
		while (folNetwork.isHaveNewAtom()) {
			System.out.println("turn: " + turnCnt++);
			folNetwork.inherence();
		}
	}
	
	public boolean query(Predicate predicate, ArrayList<String> params){
		String key = predicate.getName() + "(" + params.toString() + ")";
		Atom atom = folNetwork.getAllAtoms().get(key);
		return atom == null ? false : atom.isTrue;
	}
	
	public boolean query(String predicateName, String... params){
		Hashtable<Object, Atom> atoms = folNetwork.getAllCategories().get(predicateName);
		String key = "[";
		for (int i = 0; i < params.length; i++) {
			key += params[i];
			if (i < params.length - 1) {
				key += ", ";
			}
		}
		key += "]";
		Atom atom = atoms.get(key);
		return atom == null ? false : atom.isTrue;
	}
	

	public String queryAtom(String predicateName, String... params){
		Hashtable<Object, Atom> atoms = folNetwork.getAllCategories().get(predicateName);
		String key = "[";
		for (int i = 0; i < params.length; i++) {
			key += params[i];
			if (i < params.length - 1) {
				key += ", ";
			}
		}
		key += "]";
		Atom atom = atoms.get(key);
		return atom == null ? "" : atom.toString();
	}
	
	/**
	 * 过滤查询Atoms
	 * @param predicateName
	 * @param params - 表示要查询的内容
	 * @param anyMatchs - true: 该位置参数任意匹配, false：需要params里的参数判断, 可为空(null)
	 * @return
	 */
	public Atom[] queryAtom(String predicateName, String[] params, boolean[] anyMatchs){
		if (params.length != anyMatchs.length) {
			throw new RuntimeException("参数长度必须和AnyMatchs长度一致!");
		}
		Hashtable<Object, Atom> atoms = folNetwork.getAllCategories().get(predicateName);
		Atom[] atomsArr = new Atom[atoms.size()];
		atoms.values().toArray(atomsArr);
		ArrayList<Atom> filteredAtoms = new ArrayList<>();
		for (int i = 0; i < atomsArr.length; i++) {
			Atom atom = atomsArr[i];
			boolean passed = true;
			for (int j = 0; j < params.length; j++) {
				if (anyMatchs != null && !anyMatchs[j]) {
					if(!atom.getParams().get(j).equals(params[j])){
						passed = false;
						break;
					}
				}
			}
			if (passed) {
				filteredAtoms.add(atom);
			}
		}
		Atom[] results = new Atom[filteredAtoms.size()];
		filteredAtoms.toArray(results);
		return results;
	}
	
	public Atom[] queryAtom(String predicateName){
		Hashtable<Object, Atom> atoms = folNetwork.getAllCategories().get(predicateName);
		Atom[] atomsArr = new Atom[atoms.size()];
		atoms.values().toArray(atomsArr);
		return atomsArr;
	}
	
	public ArrayList<ArrayList<String>> queryAtom(String predicateName,int timePos,HashMap<String, Integer> searchWordMap,HashMap<String, Integer> resultsWordMap){
		Hashtable<Object, Atom> atoms = folNetwork.getAllCategories().get(predicateName);
		Atom[] atomsArr = new Atom[atoms.size()];
		atoms.values().toArray(atomsArr);
		
		ArrayList<Atom> findAtoms = new ArrayList<>(); 
		for (int i = 0; i < atomsArr.length; i++) {
			int findCount = 0;
			for (String key : searchWordMap.keySet()) {
				int wordPos = searchWordMap.get(key);
				String word = atomsArr[i].getParams().get(wordPos).toString();
				if (key.equals(word)) {
					findCount++;
				}
			}
			
			if (findCount == searchWordMap.size()) {
				//说明找到了相应的Atom
				findAtoms.add(atomsArr[i]);
			}
		}
		ArrayList<ArrayList<String>> results = new ArrayList<>();
		
		//输出结果
		if(findAtoms.size() != 0){
			for (int i = 0; i < findAtoms.size(); i++) {
				Atom mAtom = findAtoms.get(i);
				String result = "";
				ArrayList<String> resultsList = new ArrayList<>();
				for (String key : resultsWordMap.keySet()) {
					int wordPos = resultsWordMap.get(key);
					
					String date = "";
					if(wordPos == timePos){
						date = getDate(Integer.parseInt(mAtom.getParams().get(wordPos).toString()));
						result  +=  "   "+date;
						resultsList.add(date);
					}else{
						result  += "   "+ mAtom.getParams().get(wordPos).toString();
						resultsList.add(mAtom.getParams().get(wordPos).toString());
					}
					if(!result.equals("null") && !result.equals("")){
						
					}
					
				}
				results.add(resultsList);
			}
		}
		return results;
	}
	
	public String getDate(int pos){
		ArrayList<String> timeStrs = folNetwork.getMemDB().getEWord(pos).getTaggedActualDates();
		String string = "";
		for (int i = 0; i < timeStrs.size(); i++) {
			string += timeStrs.get(i);
		}
		return string;
	}
	
	/**
	 * 合并重复的事实, 比如 @A(1, 2, null, wang, xi, null) 和 @A(1, 2, 3, wang, xi, da) 可以合并成 @A(1, 2, 3, wang, xi, da)
	 */
	public boolean removeRepeatAtoms(){
		return folNetwork.removeRepeatAtoms();
	}
	
	/**
	 * 去掉只有同一个位置参数不同的较短的谓词
	 * @return
	 */
	public boolean removeLongerAtoms(){
		return folNetwork.removeLongerAtoms();
	}
	
	/**
	 * 合并排比项
	 * @return
	 */
	public boolean combinePaitis(){
		return folNetwork.combinePaitis();
	}
	
	/**
	 * 去掉只有同一个位置参数不同的较短的谓词
	 * @return
	 */
	boolean removeShortAtoms(){
		return folNetwork.removeShortAtoms();
	}
}


