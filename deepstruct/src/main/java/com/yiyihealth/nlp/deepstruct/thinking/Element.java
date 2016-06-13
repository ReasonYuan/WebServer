package com.yiyihealth.nlp.deepstruct.thinking;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * 兴奋元素、抑制元素、镜像元素
 * @author qiangpeng
 *
 */
public class Element implements BaseElement {
	
	protected double elecPool = 0; 
	
	protected double bias = 1;
	
	private final Integer id = EIDGenerator.generateID();
	
	/**
	 * 向上所连接的元素
	 */
	private ArrayList<Element> parents = new ArrayList<>();
	
	public final Object eData;
	
	/**
	 * 序列元素
	 */
	private Hashtable<Integer, Element> children = new Hashtable<>();
	
	/**
	 * 序列元素线性存储
	 */
	private ArrayList<Element> childrenInArray = new ArrayList<>();
	
	/**
	 * 刺激源权重
	 */
	private Hashtable<Integer, Double> childrenWeights = new Hashtable<>();
	
	public Element(Object eData) {
		this.eData = eData;
	}
	
	public Element() {
		eData = null;
	}
	
	public void addChild(Element element){
		children.put(element.getID(), element);
		childrenInArray.add(element);
		//TODO ....weight
		childrenWeights.put(element.getID(), 1d);
		
		//TODO 目前均分
		Set<Integer> keys = childrenWeights.keySet();
		int size = keys.size();
		for(Integer key : keys){
			childrenWeights.put(key, 1d/size);
		}
	}
	
	public void connectOutTo(Element element){
		parents.add(element);
		element.addChild(this);
	}

	@Override
	public Integer getID() {
		return id;
	}

	@Override
	public void stimulate(Integer sourceId, double value) {
		
	}

	@Override
	public void feedback(Integer sourceId, double value) {
		
	}
	
}

