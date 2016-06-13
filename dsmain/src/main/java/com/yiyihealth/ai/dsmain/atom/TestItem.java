package com.yiyihealth.ai.dsmain.atom;

import java.util.HashMap;

public class TestItem {
	private String date;
	private String name;
	private String value;
	private String valueUnit;
	//异常
	private String abnormal;
	
	private String pos;
	
	
	
	public TestItem(){
		
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValueUnit() {
		return valueUnit;
	}

	public void setValueUnit(String valueUnit) {
		this.valueUnit = valueUnit;
	}

	public String getAbnormal() {
		return abnormal;
	}

	public void setAbnormal(String abnormal) {
		this.abnormal = abnormal;
	}
	
	
	
	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public HashMap<String, String> toHashMap() {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("化验", name);
		hashMap.put("时间", date);
		hashMap.put("数值", value);
		hashMap.put("数值单位", valueUnit);
		hashMap.put("异常", abnormal);
		hashMap.put("位置", pos);
		return hashMap;
	}
	
	

}
