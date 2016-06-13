package com.yiyihealth.ds.esearch.utils;

import java.util.ArrayList;

public class ArrayUtil {

	public static String Arr2String(Object[] array){
		ArrayList<Object> objects = new ArrayList<Object>();
		for (int i = 0; i < array.length; i++) {
			objects.add(array[i]);
		}
		return objects.toString();
	}
	
}
