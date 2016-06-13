package com.yiyihealth.ai.dsmain.learning;

import java.util.Random;

public class RandomGen {
	
	static Random random = new Random(System.currentTimeMillis());

	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			String string = "1";
			for (int j = 0; j < 100; j++) {
				string += ","+random.nextInt(2);
			}
			System.out.println(string);
		}
	}

}
