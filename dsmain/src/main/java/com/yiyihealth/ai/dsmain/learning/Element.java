package com.yiyihealth.ai.dsmain.learning;

/**
 * 定义的一个元素
 * @author wangxi
 *
 */
public class Element {

	private String name = "";
	private int counts = 0;
	private int position = 0;
	private boolean isNature = false;
	
	public boolean isNature() {
		return isNature;
	}
	public void setNature(boolean isNature) {
		this.isNature = isNature;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	
}
