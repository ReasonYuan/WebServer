package com.yiyihealth.nlp.deepstruct.thinking;

/**
 * 一切皆原子、一切皆序列
 * @author qiangpeng
 *
 */
public interface BaseElement {
	
	/**
	 * 接受刺激
	 */
	public void stimulate(Integer sourceId, double value);
	
	public void feedback(Integer sourceId, double value);
	
	public Integer getID();
	
}
