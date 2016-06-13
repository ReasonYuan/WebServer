package com.yiyihealth.ai.dsmain.atom;

import java.util.HashMap;

public class Symptom {
	
	private String name;
	private String date;
	private String region;
	private String pos;
	public Symptom(){
		
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

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}
	
	
	
	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public HashMap<String, String> toHashMap() {
		
		HashMap<String, String> hashMap = new HashMap<>();
		hashMap.put("症状", name);
		hashMap.put("部位", region);
		hashMap.put("时间", date);
		hashMap.put("位置", pos);
		return hashMap;
		
	}
	
	

}
