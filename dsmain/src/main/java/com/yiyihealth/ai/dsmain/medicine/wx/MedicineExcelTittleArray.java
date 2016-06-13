package com.yiyihealth.ai.dsmain.medicine.wx;

import java.util.ArrayList;
import java.util.Collection;

public class MedicineExcelTittleArray {
	/**药物名**/
	private String medicineName = "";
	
	public String getMedicineName() {
		return medicineName;
	}

	public void setMedicineName(String medicineName) {
		this.medicineName = medicineName;
	}

	public Collection<? extends String> getExcelStringArray(){
		ArrayList<String> list = new ArrayList<String>();
		list.add("用药时间");
		list.add(medicineName);
		list.add("剂量");
		list.add("频次");
		if(this.medicineName.equals("类克")){
			list.add("依据");
		}
		list.add("好转");
		list.add("无好转");
		return list;
	}
}
