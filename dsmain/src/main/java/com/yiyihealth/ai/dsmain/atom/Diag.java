package com.yiyihealth.ai.dsmain.atom;

import java.util.HashMap;

public class Diag {
	
	private String name;
	private String date;
	private String pos;
	
	
	public Diag(){
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	
	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public HashMap<String, String> toHashMap() {
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("诊断", name);
		hashMap.put("时间", date);
		hashMap.put("位置", pos);
		return hashMap;

	}
	

}
