package com.yiyihealth.ai.dsmain.medicine.wx;

public class TestItemObject {
	private String name = "";
	private String value = "";
	private String exception = "";
	private String date = "";
	private String belongSentence = "";
	
	public String getBelongSentence() {
		return belongSentence;
	}
	public void setBelongSentence(String belongSentence) {
		this.belongSentence = belongSentence;
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
	public String getException() {
		return exception;
	}
	public void setException(String exception) {
		if(exception.equals("null")){
			exception = " ";
		}
		this.exception = exception;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	@Override
	public String toString() {
		return (getName() +"  "+ getValue()+"  " + getException()+ "  " + getDate());
	}
}
