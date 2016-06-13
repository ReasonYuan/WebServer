package com.yiyihealth.ai.dsmain.learning;

public class FeatureVector {

//	private static final int DimensionSize  = 200;//向量维度大小
	
	private int[] initValues;

	public FeatureVector(int size) {
		initValues = new int[size];
	}
	
	public int[] getInitValues() {
		return initValues;
	}

	public void setValueForIndex(int i,int value){
		initValues[i] = value;
	}
	
	public int getValueForIndex(int i){
		return initValues[i];
	}
}
