package com.yiyihealth.nlp.deepstruct.csense;

import java.util.StringTokenizer;

import com.yiyihealth.nlp.deepstruct.csense.CSItem.ItemType;

public class CSParser {

	public CSParser(){
		
	}
	
	public static void main(String[] args) {
		String teString = "{[用药]}={口服,服,静滴,服用,治疗,+,＋,[连接介词]}";
		CSItem[][] testItems = new CSParser().parse(teString);
	}
	
	public CSItem[][] parse(String csExpress){
		StringTokenizer st = new StringTokenizer(csExpress, "=-!");
		if (st.countTokens() != 2) {
			throw new RuntimeException("常识表达式必须在－／＝左右都存在描述: " + csExpress);
		}
		String[] parts = new String[2];
		parts[0] = st.nextToken().trim();
		parts[1] = st.nextToken().trim();
		CSItem[][] resutls = new CSItem[2][];
		for (int i = 0; i < parts.length; i++) {
			StringTokenizer stPart = new StringTokenizer(parts[i], "{},，");
			int cnt = stPart.countTokens();
			CSItem[] items = new CSItem[cnt];
			for (int j = 0; j < cnt; j++) {
				String strItem = stPart.nextToken().trim();
				if (strItem.length() == 0) {
					throw new RuntimeException("常识表达式语法错误: " + csExpress);
				}
				boolean leftWithMB = strItem.startsWith("[");
				boolean rightWithMB = strItem.endsWith("]");
				if (leftWithMB && rightWithMB) {
					NatureItem natureItem = new NatureItem(strItem.substring(1, strItem.length()-1), ItemType.NATURE);
					items[j] = natureItem;
				} else if (leftWithMB || rightWithMB) {
					throw new RuntimeException("常识表达式语法错误: " + csExpress);
				} else {
					WordItem wordItem = new WordItem(strItem, ItemType.WORD);
					items[j] = wordItem;
				}
			}
			resutls[i] = items;
		}
		return resutls;
	}
	
}
