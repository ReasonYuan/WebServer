package com.yiyihealth.ai.dsmain.medicine.wx;

import java.util.ArrayList;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class User extends Object implements Cloneable{
		/**病人姓名**/
		private String patientName = "";
		/**住院号**/
		private String adminNum = "";
		/**病历号**/
		private String recordNum = "";
		/**入院日期**/
		private String admisionDate = "";
		/**出院日期**/
		private String outAdmisionDate = "";
		/**使用药物列表**/
		private ArrayList<Medicine> medicineList = null;
		/**诊断**/
		private String zhenduan = "";
		/**症状**/
		private String zhengzhuang = "";
		
		public String getZhenduan() {
			return zhenduan;
		}

		public void setZhenduan(String zhenduan) {
			this.zhenduan = zhenduan;
		}

		public String getZhengzhuang() {
			return zhengzhuang;
		}

		public void setZhengzhuang(String zhengzhuang) {
			this.zhengzhuang = zhengzhuang;
		}

		public String getAdminNum() {
			return adminNum;
		}

		public void setAdminNum(String adminNum) {
			this.adminNum = adminNum;
		}

		public String getPatientName() {
			return patientName;
		}

		public void setPatientName(String patientName) {
			this.patientName = patientName;
		}

		public String getRecordNum() {
			return recordNum;
		}

		public void setRecordNum(String recordNum) {
			this.recordNum = recordNum;
		}

		public String getAdmisionDate() {
			return admisionDate;
		}

		public void setAdmisionDate(String admisionDate) {
			this.admisionDate = admisionDate;
		}

		public String getOutAdmisionDate() {
			return outAdmisionDate;
		}

		public void setOutAdmisionDate(String outAdmisionDate) {
			this.outAdmisionDate = outAdmisionDate;
		}

		public ArrayList<Medicine> getMedicineList() {
			return medicineList;
		}

		public void setMedicineList(ArrayList<Medicine> medicineList) {
			this.medicineList = medicineList;
		}
		
		public String toJson(){
			JSONObject object = new JSONObject();
			try {
				object.put("patientName", patientName);
				object.put("recordNum", recordNum);
				object.put("adminNum", adminNum);
				object.put("admisionDate", admisionDate);
				object.put("outAdmisionDate", outAdmisionDate);
				object.put("zhenduan", zhenduan);
				object.put("zhengzhuang", zhengzhuang);
				object.put("medicineList", getMedicine());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return object.toString();
		}
		
		public ArrayList<String> getMedicine(){
			ArrayList<String> medicineListStr = new ArrayList<String>();
			for(int i = 0;i<medicineList.size();i++){
				Medicine medicine = medicineList.get(i);
				medicineListStr.add(medicine.toJson());
			}
			return medicineListStr;
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
}
