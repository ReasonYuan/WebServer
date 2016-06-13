package com.yiyihealth.nlp.deepstruct.thinking;

import java.util.Hashtable;

/**
 * 简单mini大脑
 * @author qiangpeng
 *
 */
public class MiniBrain {
	
	/**
	 * 最基本的字概念
	 */
	private Hashtable<Character, Element> baseElements = new Hashtable<>();
	
	/**
	 * 思考时产生疑问
	 * @author qiangpeng
	 *
	 */
	public static interface DoubtListener {
		public void onDoubt(Question question);
	}
	
	/**
	 * 看见了什么字
	 * @param c
	 */
	public void saw(char c){
		
	}
	
	/**
	 * @param elements
	 */
	public void learnOneSeq(Element... elements){
		
	}
	
	public void thinkingWhatNow(){
		
	}
	
}
