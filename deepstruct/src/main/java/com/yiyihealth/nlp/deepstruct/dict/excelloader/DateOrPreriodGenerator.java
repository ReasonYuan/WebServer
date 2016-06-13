package com.yiyihealth.nlp.deepstruct.dict.excelloader;

import java.util.ArrayList;

public class DateOrPreriodGenerator {
	private static final String SOME_TIME_AGO = "SOME_TIME_AGO";
	private static final String SOME_TIME_LATER = "SOME_TIME_LATER";

	public static class DPItem {

		/**
		 * 日期或时间长度文本
		 */
		String text;
		/**
		 * 是否是日期
		 */
		boolean isDate;

		/**
		 * 
		 * 属性参数
		 */
		String attr;

		public DPItem(String text, boolean isDate, String attr) {
			this.text = text;
			this.isDate = isDate;
			this.attr = attr;
		}

		public DPItem(String text, boolean isDate) {
			this.text = text;
			this.isDate = isDate;
			attr = "";
		}

	}

	public static ArrayList<DPItem> generateDatesAndPeriods() {
		ArrayList<DPItem> results = new ArrayList<DPItem>();
		// TODO 生成时间、时间长度列表

		String[] numberDate = { "d", "dd" };

		String[] specialNumber = { "半", "两" };

		String[] unitStrs = { "个小时","小时","天", "日", "周", "月", "个月", "个周", "年" };

		String[] dateStrs = { "前", "以前", "之前", "后", "以后", "之后","余前","余后" };

		String[] notDateStrs = { "余", "左右", "半" };

//		String[] unitStrs2 = { "天", "日", "周", "月", "年" };

		String[] strings = { "近" };
		
		String[] strs = { "余", "多" };

		// 特殊时间
		String[] specialTimeAgo = { "上个月", "上个周", "昨天", "前天", "去年", "前年" };

		String[] specialTimeLater = { "下个月", "下个周", "明天", "后天", "明年" };

		// number(d dd) + unit + 描述 如d个月后、dd个月左右、d个月前等等
		for (int i = 0; i < numberDate.length; i++) {
			String tmp = numberDate[i];

			for (int j = 0; j < unitStrs.length; j++) {
				String tmpStr = tmp;
				tmpStr += unitStrs[j];

				DPItem mdpItem = new DPItem(tmpStr, false);
				results.add(mdpItem);

				for (int k = 0; k < notDateStrs.length; k++) {
					String finalTmpStr = tmpStr;
					finalTmpStr += notDateStrs[k];
					DPItem pItem = new DPItem(finalTmpStr, false);
					results.add(pItem);
				}

				for (int k = 0; k < dateStrs.length; k++) {
					if (k < 3) {
						String finalTmpStr = tmpStr;
						finalTmpStr += dateStrs[k];
						DPItem pItem = new DPItem(finalTmpStr, true, SOME_TIME_AGO);
						results.add(pItem);
					} else {
						String finalTmpStr = tmpStr;
						finalTmpStr += dateStrs[k];
						DPItem pItem = new DPItem(finalTmpStr, true, SOME_TIME_LATER);
						results.add(pItem);
					}

				}
			}

			String tmp1 = numberDate[i] + "余";
			for (int j = 0; j < unitStrs.length; j++) {
				String tmpStr1 = tmp1;
				tmpStr1 += unitStrs[j];
				DPItem pItem1 = new DPItem(tmpStr1, false);
				results.add(pItem1);

			}

		}

		// number(中文1-100) + unit + 描述 如一个月后、二十二个月左右、一个月前等等
		for (int i = 1; i < 100; i++) {
			String tmp = foematInteger(i);
			for (int j = 0; j < unitStrs.length; j++) {
				String tmpStr = tmp;
				tmpStr += unitStrs[j];

				DPItem mdpItem = new DPItem(tmpStr, false);
				results.add(mdpItem);

				for (int k = 0; k < notDateStrs.length; k++) {
					String finalTmpStr = tmpStr;
					finalTmpStr += notDateStrs[k];
					DPItem pItem = new DPItem(finalTmpStr, false);
					results.add(pItem);

				}

				for (int k = 0; k < dateStrs.length; k++) {

					if (k < 3) {
						String finalTmpStr = tmpStr;
						finalTmpStr += dateStrs[k];
						DPItem pItem = new DPItem(finalTmpStr, true, SOME_TIME_AGO);
						results.add(pItem);
					} else {
						String finalTmpStr = tmpStr;
						finalTmpStr += dateStrs[k];
						DPItem pItem = new DPItem(finalTmpStr, true, SOME_TIME_LATER);
						results.add(pItem);
					}
				}
			}

			String tmp1 = foematInteger(i) + "余";
			for (int j = 0; j < unitStrs.length; j++) {
				String tmpStr1 = tmp1;
				tmpStr1 += unitStrs[j];
				DPItem pItem1 = new DPItem(tmpStr1, false);
				results.add(pItem1);

			}

		}

		// "半""两" + unit + 描述 如:半个月之前 半个月后 两年前等
		for (int i = 0; i < specialNumber.length; i++) {
			String tmp = specialNumber[i];
			for (int j = 0; j < unitStrs.length; j++) {
				String tmpStr = tmp;
				tmpStr += unitStrs[j];

				DPItem mdpItem = new DPItem(tmpStr, false);
				results.add(mdpItem);

				for (int k = 0; k < notDateStrs.length; k++) {
					String finalTmpStr = tmpStr;
					finalTmpStr += notDateStrs[k];
					DPItem pItem = new DPItem(finalTmpStr, false);
					results.add(pItem);
				}

				for (int k = 0; k < dateStrs.length; k++) {
					if (k < 3) {
						String finalTmpStr = tmpStr;
						finalTmpStr += dateStrs[k];
						DPItem pItem = new DPItem(finalTmpStr, true, "SOME_TIME_AGO");
						results.add(pItem);
					} else {
						String finalTmpStr = tmpStr;
						finalTmpStr += dateStrs[k];
						DPItem pItem = new DPItem(finalTmpStr, true, "SOME_TIME_LATER");
						results.add(pItem);
					}
					// String finalTmpStr = tmpStr;
					// finalTmpStr += dateStrs[k];
					// DPItem pItem = new DPItem(finalTmpStr,
					// true,"SOME_TIME_AGO_OR_LATER");
					// results.add(pItem);
				}

			}
			String tmp1 = specialNumber[i] + "余";
			for (int j = 0; j < unitStrs.length; j++) {
				String tmpStr = tmp1;
				tmpStr += unitStrs[j];
				DPItem pItem = new DPItem(tmpStr, false);
				results.add(pItem);
			}

		}

		// 近 + number + unit 如:近一个月 近三个月 近半个月
		for (int i = 0; i < strings.length; i++) {
			String tmp = strings[i];
			for (int j = 1; j < 100; j++) {
				String tmpStr = tmp;
				tmpStr += foematInteger(j);
				for (int l = 0; l < unitStrs.length; l++) {
					String finalTmpStr = tmpStr;
					finalTmpStr += unitStrs[l];
					DPItem pItem = new DPItem(finalTmpStr, true, "SOME_TIME_AGO");
					results.add(pItem);
				}

			}

			for (int j = 0; j < specialNumber.length; j++) {
				String tmpStr = tmp;
				tmpStr += specialNumber[j];
				for (int l = 0; l < unitStrs.length; l++) {
					String finalTmpStr = tmpStr;
					finalTmpStr += unitStrs[l];
					DPItem pItem = new DPItem(finalTmpStr, true, "SOME_TIME_AGO");
					results.add(pItem);
				}
			}
		}

		// 特殊时间
		for (int i = 0; i < specialTimeAgo.length; i++) {
			String tmp = specialTimeAgo[i];
			DPItem pItem = new DPItem(tmp, true, "SOME_TIME_AGO");
			results.add(pItem);

		}

		// 特殊时间
		for (int i = 0; i < specialTimeLater.length; i++) {
			String tmp = specialTimeLater[i];
			DPItem pItem = new DPItem(tmp, true, "SOME_TIME_LATER");
			results.add(pItem);

		}

		return results;
	}

	static String[] units = { "", "十", "百", "千", "万", "十万", "百万", "千万", "亿", "十亿", "百亿", "千亿", "万亿" };
	static char[] numArray = { '零', '一', '二', '三', '四', '五', '六', '七', '八', '九' };

	public static String foematInteger(int num) {
		char[] val = String.valueOf(num).toCharArray();

		int len = val.length;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			String m = val[i] + "";
			int n = Integer.valueOf(m);
			boolean isZero = n == 0;
			if (len == 1 && n == 0) {
				sb.append("零");
				break;
			}
			String unit = units[(len - 1) - i];
			if (isZero) {
				if (i >= 1 && '0' == val[i - 1]) {
					// not need process if the last digital bits is 0
					continue;
				} else {
					boolean isMyZero = false;
					for (int j = i; j < len; j++) {
						String mm = val[j] + "";
						int nn = Integer.valueOf(mm);
						if (nn != 0) {
							// no unit for 0
							isMyZero = true;

						}

					}
					if (isMyZero) {
						sb.append(numArray[n]);
					}

				}

			} else {
				sb.append(numArray[n]);
				sb.append(unit);
			}
		}
		return sb.toString();
	}

}
