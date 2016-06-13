package com.yiyihealth.nlp.deepstruct.utils;

import java.util.HashMap;
import java.util.Map;

public class DateUtil {

	private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<String, String>() {{
		put("^\\d{4}\\.\\d{2}$", "yyyy.MM");
		put("^\\d{4}\\.\\d{1}$", "yyyy.M");
		put("^\\d{4}年\\d{2}月$", "yyyy年MM月");
		put("^\\d{2}年\\d{2}月$", "yy年MM月");
		put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
		put("^\\d{4}-\\d{1,2}$", "yyyy-MM");
		put("^\\d{4}年$", "yyyy年");
		put("^\\d{2}年$", "yy年");
	}};

	
}
