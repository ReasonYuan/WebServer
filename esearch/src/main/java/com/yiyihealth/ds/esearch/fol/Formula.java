package com.yiyihealth.ds.esearch.fol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

public class Formula implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7944554970423495191L;

	private String debugLabel = "";
	
	private ArrayList<Predicate> predicates = new ArrayList<Predicate>();
	
	private String[] paramNames;
	
	private int[][] sameNameParamIdx;
	
	private Hashtable<String, Object[]> crossProduct;
	
	private int[] lastCrossMax;
	
	private static final AtomicInteger idGenerator = new AtomicInteger(1);
	
	private final String ID = "" + idGenerator.getAndIncrement();
	
	public Formula(String debugLabel) {
		this.debugLabel = debugLabel;
		FormulaManager.getInstance().addFormula(this);
	}
	
	public int[] getLastCrossMax() {
		return lastCrossMax;
	}
	
	public String getID() {
		return ID;
	}
	
	/**
	 * 生成参数名表
	 */
	private void generateParamNames(){
		int paramCnt = 0;
		for (int i = 0; i < predicates.size(); i++) {
			paramCnt += predicates.get(i).getParams().size();
		}
		paramNames = new String[paramCnt];
		int offset = 0;
		for (int i = 0; i < predicates.size(); i++) {
			for(int j=0; j<predicates.get(i).getParams().size(); j++){
				paramNames[offset++] = (String)predicates.get(i).getParams().get(j);
			}
		}
		sameNameParamIdx = new int[paramNames.length][];
		for (int i = 0; i < paramNames.length; i++) {
			ArrayList<Integer> sameIdx = new ArrayList<Integer>();
			for (int j = 0; j < paramNames.length; j++) {
				if (i != j && paramNames[i].equals(paramNames[j])) {
					sameIdx.add(j);
				}
			}
			if (sameIdx.size() > 0) {
				int[] sames = new int[sameIdx.size()];
				for (int j = 0; j < sames.length; j++) {
					sames[j] = sameIdx.get(j);
				}
				sameNameParamIdx[i] = sames;
			}
		}
		crossProduct = new Hashtable<String, Object[]>();
		lastCrossMax = new int[predicates.size()];
	}
	
	public void addPredicate(Predicate predicate){
		this.predicates.add(predicate);
		generateParamNames();
	}
	
	public int[][] getSameNameParamIdx() {
		return sameNameParamIdx;
	}
	
	public String[] getParamNames() {
		return paramNames;
	}
	
	public String getDebugLabel() {
		return debugLabel;
	}
	
	public Hashtable<String, Object[]> getCrossProduct() {
		return crossProduct;
	}
	
	public ArrayList<Predicate> getPredicates() {
		return predicates;
	}
	
	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer(debugLabel + ": ");
		for (int i = 0; i < predicates.size(); i++) {
			sBuffer.append(predicates.get(i).toString() + (i == predicates.size() - 1 ? "" : " v "));
		}
		return sBuffer.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(toString());
	}
	
}
