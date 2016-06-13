package com.yiyihealth.ds.date.timeline;

import java.util.Date;

public class TimeLine {

	private static final long MILLISECONDSOFONEDAY = 86400000;

	@SuppressWarnings("deprecation")
	public static Date convertDate(Date refDate, String strDate) {

		Date newDate = new Date(0);

		if (strDate.contains("今年")) {// 关键字"今年"判断
			if (strDate.contains("月") && strDate.contains("日")) {
				int date = checkNumber(strDate.substring(strDate.indexOf('月'), strDate.indexOf('日')));
				int month = checkNumber(strDate.substring(0, strDate.indexOf('月')));
				newDate.setYear(refDate.getYear());
				newDate.setMonth(month - 1);
				newDate.setDate(date);
				newDate.setHours(0);
				return newDate;
			} else if (strDate.contains("月") && !strDate.contains("日")) {
				int month = chineseNumberToInt(strDate);
				newDate.setYear(refDate.getYear());
				newDate.setMonth(month - 1);
				newDate.setDate(1);
				newDate.setHours(0);
				return newDate;
			} else {
				newDate.setYear(refDate.getYear());
				newDate.setMonth(0);
				newDate.setDate(1);
				newDate.setHours(0);
				return newDate;
			}
		} else if (strDate.contains("本月")) {// 关键字"本月"判断
			newDate.setYear(refDate.getYear());
			newDate.setMonth(refDate.getMonth());
			newDate.setHours(0);
			return newDate;

		} else if (strDate.contains("至今") || strDate.contains("目前") || strDate.contains("当时") || strDate.contains("近日")) {
			newDate.setYear(refDate.getYear());
			newDate.setMonth(refDate.getMonth());
			newDate.setDate(refDate.getDate());
			newDate.setHours(0);
			return newDate;
		}else if(strDate.contains("月") && strDate.contains("日") &&!strDate.contains("年")){
			int date = checkNumber(strDate.substring(strDate.indexOf('月'), strDate.indexOf('日')));
			int month = checkNumber(strDate.substring(0, strDate.indexOf('月')));
			newDate.setYear(refDate.getYear());
			newDate.setMonth(month-1);
			newDate.setDate(date);
			newDate.setHours(0);
			return newDate;
		}else if(cheackFormat(strDate,"dd月")){
			int month = checkNumber(strDate.substring(0, strDate.indexOf('月')));
			newDate.setYear(refDate.getYear());
			newDate.setMonth(month-1);
			newDate.setDate(1);
			newDate.setHours(0);
			return newDate;
		}else if(cheackFormat(strDate,"dd年")){
			int year = checkNumber(strDate.substring(0, strDate.indexOf('年')));
			if (year>20) {
				newDate.setYear(year);
				newDate.setMonth(0);
				newDate.setDate(1);
				newDate.setHours(0);
			}else{
				newDate.setYear(year+100);
				newDate.setMonth(0);
				newDate.setDate(1);
				newDate.setHours(0);
			}
			
			return newDate;
		}
		
		int number = chineseNumberToInt(strDate);
		long refTime = refDate.getTime();
		long newTime = 0;
		String unit = getUnit(strDate);
		long temp = 0;
		switch (unit) {
		case "天":
			temp = MILLISECONDSOFONEDAY;
			break;
		case "日":
			temp = MILLISECONDSOFONEDAY;
			break;
		case "周":
			temp = MILLISECONDSOFONEDAY * 7;
			break;
		case "月":
			temp = MILLISECONDSOFONEDAY * 30;
			break;
		case "年":
			temp = MILLISECONDSOFONEDAY * 365;
			break;
		default:
			break;
		}

		boolean isAfter = checkAfterOrAgo(strDate);
		
		if (isAfter) {
			if(strDate.contains("半")){
				newTime = (long) (refTime + 0.5 * temp);
			}else{
				newTime = refTime + number * temp;
			}
			
		} else {
			if(strDate.contains("半")){
				newTime = (long) (refTime - 0.5 * temp);
			}else{
				newTime = refTime - number * temp;
			}
		}
		newDate.setTime(newTime);
		return newDate;

	}
	

	// 获取单位：年月周日
	private static String getUnit(String strDate) {
		String tmp = "";
		for (int i = 0; i < strDate.length(); i++) {
			char c = strDate.charAt(i);
			char[] chArr = new char[] { '天', '周', '月', '年', '日' };
			for (int j = 0; j < chArr.length; j++) {
				if (c == chArr[j]) {
					tmp += chArr[j];
				}
			}
		}
		return tmp;
	}

	// 判断是之前还是之后，时间戳是加是减
	private static boolean checkAfterOrAgo(String strDate) {
		boolean isAfter = false;
		for (int i = 0; i < strDate.length(); i++) {
			char c = strDate.charAt(i);
			if (c == '前' || c == '上' || c == '昨') {
				isAfter = false;
			} else if (c == '后' || c == '下'|| c == '明') {
				isAfter = true;
			}
		}
		return isAfter;
	}

	/**
	 * 中文數字转阿拉伯数组【十万九千零六十 --> 109060】存在阿拉伯数字之间返回阿拉伯数字
	 *
	 * @param strDate
	 * @return
	 */
	private static int chineseNumberToInt(String strDate) {
		int result = 0;

		// 检查是否为阿拉伯数字 如果是阿拉伯数字result不为0
		result = checkNumber(strDate);
		if (result != 0) {
			return result;
		}

		int temp = 1;// 存放一个单位的数字如：十万
		int count = 0;// 判断是否有chArr
		char[] cnArr = new char[] { '一', '二', '三', '四', '五', '六', '七', '八', '九' };
		char[] chArr = new char[] { '十', '百', '千', '万', '亿' };
		for (int i = 0; i < strDate.length(); i++) {
			boolean b = true;// 判断是否是chArr
			char c = strDate.charAt(i);
			for (int j = 0; j < cnArr.length; j++) {// 非单位，即数字
				if (c == cnArr[j]) {
					if (0 != count) {// 添加下一个单位之前，先把上一个单位值添加到结果中
						result += temp;
						temp = 1;
						count = 0;
					}
					// 下标+1，就是对应的值
					temp = j + 1;
					b = false;
					break;
				}
			}
			if (b) {// 单位{'十','百','千','万','亿'}
				for (int j = 0; j < chArr.length; j++) {
					if (c == chArr[j]) {
						switch (j) {
						case 0:
							temp *= 10;
							break;
						case 1:
							temp *= 100;
							break;
						case 2:
							temp *= 1000;
							break;
						case 3:
							temp *= 10000;
							break;
						case 4:
							temp *= 100000000;
							break;
						default:
							break;
						}
						count++;
					}
				}
			}
			if (i == strDate.length() - 1) {// 遍历到最后一个字符
				result += temp;
			}
		}
		return result;
	}

	// 截取文本中阿拉伯数字和一些特殊处理
	public static int checkNumber(String chineseNumber) {
		int result = 0;
		chineseNumber = chineseNumber.trim();
		String str2 = "";
		if (chineseNumber != null && !"".equals(chineseNumber)) {
			for (int i = 0; i < chineseNumber.length(); i++) {
				if (chineseNumber.charAt(i) >= 48 && chineseNumber.charAt(i) <= 57) {
					str2 += chineseNumber.charAt(i);
				}
				if (chineseNumber.charAt(i) == '两') {
					str2 += '2';
				}
				if (chineseNumber.charAt(i) == '下' && chineseNumber.charAt(i) == '上') {
					str2 += '1';
				}
				
			}
			if (chineseNumber.charAt(0) == '昨' || chineseNumber.charAt(0) == '明') {
				str2 += '1';
			}
			
			if("".equals(str2)){
				if(chineseNumber.charAt(0) == '前' || chineseNumber.charAt(0) == '后'){
					str2 += '2';
				}
			}
			
			
			
			if (!"".equals(str2)) {
				result = Integer.parseInt(str2);
			}
		}

		return result;
	}
	
	public static boolean cheackFormat(String str,String formatStr) {
		boolean isSimilar = true;
		str = str.trim();
		formatStr = formatStr.trim();
		if (str == null && "".equals(str) || formatStr == null && "".equals(formatStr)) {
			isSimilar = false;
		}else{
			if (str.length() != formatStr.length()) {
				isSimilar = false;
			}else{
				for (int i = 0; i < formatStr.length(); i++) {
					if (formatStr.charAt(i) == 'd') {
						if (str.charAt(i) >= 48 && str.charAt(i)<= 57) {
//							isSimilar = true;
						}else{
							isSimilar = false;
						}
					}else{
						if (formatStr.charAt(i) == str.charAt(i)) {
//							isSimilar = true;
						}else{
							isSimilar = false;
						}
					}
				}
			}
		}
		
		return isSimilar;
	}

}
