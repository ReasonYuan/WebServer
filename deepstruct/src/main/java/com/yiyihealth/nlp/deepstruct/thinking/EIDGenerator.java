package com.yiyihealth.nlp.deepstruct.thinking;

import java.util.concurrent.atomic.AtomicInteger;

public class EIDGenerator {

	private static final AtomicInteger atomicInteger = new AtomicInteger(0);
	
	public static Integer generateID(){
		return atomicInteger.getAndIncrement();
	}
	
}
