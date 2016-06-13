package com.yiyihealth.ai.dsmain.medicine.wx;

import java.util.HashMap;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class Medicine{
	/**用药时间**/
	private String userMedicineTime = "";
	/**药物名**/
	private String medicineName = "";
	/**剂量**/
	private String jiLiang = "";
	/**频次**/
	private String pinCi = "";
	/**药物所属的锚点**/
	private String anchor = "";
	/**药物所在短句，专为药物"类克"使用**/
	private String specialYiJu = ""; 
	/**好效果**/
	private String godResult = "";
	/**无效果**/
	private String badResult = "";
	
	private String position = "";
	
	private String date = "";
	
	/**
	 * 使用药 或者 暂停使用药
	 */
	private String nowUse = "";
	
	public String getNowUse() {
		return nowUse;
	}

	public void setNowUse(String nowUse) {
		this.nowUse = nowUse;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getGodResult() {
		return godResult;
	}

	public void setGodResult(String godResult) {
		this.godResult = godResult;
	}

	public String getBadResult() {
		return badResult;
	}

	public void setBadResult(String badResult) {
		this.badResult = badResult;
	}
	
	public String getUserMedicineTime() {
		return userMedicineTime;
	}
	public void setUserMedicineTime(String userMedicineTime) {
		this.userMedicineTime = userMedicineTime;
	}
	public String getMedicineName() {
		return medicineName;
	}
	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}
	public String getJiLiang() {
		return jiLiang;
	}
	public void setJiLiang(String jiLiang) {
		this.jiLiang = jiLiang;
	}
	public String getPinCi() {
		return pinCi;
	}
	public void setPinCi(String pinCi) {
		this.pinCi = pinCi;
	}
	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
	public String getSpecialYiJu() {
		return specialYiJu;
	}
	public void setSpecialYiJu(String specialYiJu) {
		this.specialYiJu = specialYiJu;
	}
	public String toJson(){
		JSONObject object = new JSONObject();
		try {
			object.put("时间", userMedicineTime);
			object.put("用药", medicineName);
			object.put("剂量", jiLiang);
			object.put("频次", pinCi);
//			object.put("godResult", godResult);
//			object.put("badResult", badResult);
//			object.put("anchor", anchor);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object.toString();
	}
	
	public  HashMap<String, String> toHashMap(){
		HashMap<String, String> map = new HashMap<>();
		map.put("时间", date);
		map.put("用药", medicineName);
		map.put("剂量", jiLiang);
		map.put("频次", pinCi);
		map.put("位置", position);
		map.put("是否使用", nowUse);
		return map;
	}
}
