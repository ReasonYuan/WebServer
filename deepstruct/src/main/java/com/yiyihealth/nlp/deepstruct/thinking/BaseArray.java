package com.yiyihealth.nlp.deepstruct.thinking;

import java.util.Hashtable;

public class BaseArray<T> {

	protected Hashtable<Integer, T> items = new Hashtable<>();
	
	public void add(Integer i, T t){
		items.put(i, t);
	}
	
	public T get(Integer id){
		return items.get(id);
	}
	
}
