package com.yiyihealth.ai.dsmain;

import com.yiyihealth.ds.esearch.fol.Express;

import junit.framework.TestCase;

public class ExpressTest extends TestCase {

	public void testParseExpress(){
		String[][] ts = {
				{"pos1+100 + 200 <=pos2 < 5000", "true"},
				{"pos1+pos2=30", "false"},
				{"pos2>=pos1+10>30", "false"},
				{"pos1>=pos2", "true"},
				{"pos3-100<pos1>=pos2", "false"}
		};
		for (int i = 0; i < ts.length; i++) {
			Express express = new Express(ts[i][0]);
			System.out.println("\r\n" + express.getTokens().toString());

			int[][] vars = {
					{11,0,0,0,0,0,1500,0,0},
					{100,0,200,0,0},
					{100,0,1,0,0,0,0},
					{100,0,90},
					{150,0,0,0,100,0,190}
					};
			int[] tmp = (int[]) vars[i];
			
			boolean isOk = express.caculateExpress(tmp);
			assertEquals(isOk, ts[i][1].equals("true"));
		}
	}
	
	
	
}
