package com.yiyihealth.ds.esearch.fol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

public class Atom implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9218111076493895862L;
	
	String predicateName;
	ArrayList<Object> params;
	/**
	 * 为了加快运行速度, 把 {@link #params} 对应到varName2Params
	 */
	private Hashtable<Object, Object> varName2Params = new Hashtable<Object, Object>();
	/**
	 * 为了加快计算速度，默认解析原子中整数参数到下面对应的数组里
	 */
	int[] intParams;
	/**
	 * 为了加快计算速度，默认解析原子中null参数到下面对应的数组里
	 */
	boolean[] nullParams;
	boolean isTrue;
	int nullParamsCounter = 0;
	
	private String keyOfParams;
	
	private static final AtomicInteger idGenerator = new AtomicInteger(1);
	
	private final AtomID ID;
	
	/**
	 * 
	 * @param predicate
	 * @param params
	 * @param isTrue
	 * @param formuaId - 推导公式所对应的事实，如果是原始事实则为null
	 * @param atomIds - 推导公式所对应的事实，如果是原始事实则为null
	 */
	public Atom(AtomManager atomManager, Predicate predicate, ArrayList<Object> params, boolean isTrue, String formulaId, String[] atomIds) {
		ID = new AtomID("" + idGenerator.getAndIncrement(), formulaId, atomIds);
		this.predicateName = predicate.getName();
		this.params = params;
//		if (predicate.getParams().get(0).toString().startsWith(Predicate.STR_ID)) {
//			//默认需要填充一个id
//			this.params.add(0, "" + ID);
//		} else {
//			throw new RuntimeException("所有的谓词都需要一个_ID");
//		}
		this.isTrue = isTrue;
		this.keyOfParams = params.toString();
		parseIntAndNullParams();
		atomManager.addAtom(this);
	}
	
	public ArrayList<Object> getParams() {
		return params;
	}
	
	public ArrayList<Object> cloneParams(){
		ArrayList<Object> params = new ArrayList<>();
		params.addAll(this.params);
		return params;
	}
	
	/**
	 * 谨慎调用！！！！
	 * @param index
	 * @param param
	 */
	public void updateParam(int index, Object param){
		params.set(index, param);
		//TODO 判断是是boolean或int
		this.keyOfParams = params.toString();
	}
	
	/**
	 * 是否是整数参数
	 * @param paramIndex
	 * @return
	 */
	public boolean isIntegerParam(int paramIndex){
		return intParams[paramIndex] != Integer.MAX_VALUE;
	}
	
	/**
	 * 返回原子的id
	 * @return
	 */
	public AtomID getID() {
		return ID;
	}
	
	public String getPredicateName() {
		return predicateName;
	}
	
	private void parseIntAndNullParams(){
		nullParamsCounter = 0;
		intParams = new int[params.size()];
		nullParams = new boolean[params.size()];
		for (int i = 0; i < intParams.length; i++) {
			try {
				intParams[i] = Integer.parseInt(params.get(i).toString());
			} catch (Exception e) {
				intParams[i] = Integer.MIN_VALUE;
			}
			if (params.get(i).equals("null")) {
				nullParams[i] = true;
				nullParamsCounter++;
			}
		}
	}
	
	public String getParamsKey() {
		return keyOfParams;
	}
	
	public String getGlobalKey(){
		return predicateName + "(" + keyOfParams + ")";
	}
	
	@Override
	public String toString() {
		return (isTrue ? "" : "!") + predicateName + "(" + keyOfParams + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString());
	}
	
}