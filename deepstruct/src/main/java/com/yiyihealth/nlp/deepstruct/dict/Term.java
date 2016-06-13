package com.yiyihealth.nlp.deepstruct.dict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class Term extends EWord implements Cloneable {
		
		/**
		 * 几类日期需要结合上下文判断
		 */
		private static final String[] DATE_IN_CONTEXT_2_CHECKS = { 
				"DATE_OR_VALUE", "MONTH_OR_PERIOD", "MONTH_OR_PERIOD", 
				"WHICH_YEAR", "WHICH_YEAR", "WHICH_YEAR", "WHICH_YEAR", 
				"YEAR_OR_PERIOD", "WHAT_IS_THAT_DATE", "WHAT_IS_NOW", 
				"MIDDLE_OF_MONTH", "BEGIN_OF_MONTH", "END_OF_MONTH","SOME_TIME_AGO","SOME_TIME_LATER"
			};
		
		
		private static Hashtable<String, SimpleDateFormat> formatter = new Hashtable<String, SimpleDateFormat>();
		
		private static final SimpleDateFormat normalFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		/**
		 * 在文本中开始的位置
		 */
		public int startPos;
		
		//TODO 下面需要重构
		// 是否是时间轴上的时间点
		public boolean isTimeline = false;
		
		//private String timeformat;
		
		private String normalDate = null;
		
		/**
		 * 是否是定义了的词的规则，如果不是，输出到人工干预
		 */
		public boolean isDefined = false;
		
		private ArrayList<String> candidateNatures = new ArrayList<>();
		
		public Term(String text, String nature, boolean isDefined, int startPos) {
			super(text, nature);
			this.isDefined = isDefined;
			this.startPos = startPos;
		}
		
		@Override
		public String toString() {
			return String.format("text: %s, nature: %s, isDefined: %b, attr: %s, canNatures: %s", word, nature, isDefined, attributes.toString(), candidateNatures.toString());
		}
		
		public String getNormalDate() {
			return normalDate;
		}
		
		public void addCandidateNatures(ArrayList<String> natures){
			for(String nature : natures){
				if (!candidateNatures.contains(nature)) {
					candidateNatures.add(nature);
				}
			}
		}
		
		public void addCandidateNature(String nature){
			if (!candidateNatures.contains(nature)) {
				candidateNatures.add(nature);
			}
		}
		
		public void removeCandidateNature(String nature){
			candidateNatures.remove(nature);
		}
		
		public void clearCandidateNatures(){
			candidateNatures.clear();
		}
		
		public ArrayList<String> getCandidateNatures() {
			return candidateNatures;
		}
		
		public boolean shouldCheckTimeContext(){
			String timeformat = (String) getAttribute(ATTR_TIMEFORMAT);
			for (int i = 0; i < DATE_IN_CONTEXT_2_CHECKS.length; i++) {
				if (DATE_IN_CONTEXT_2_CHECKS[i].equals(timeformat)) {
					return true;
				}
			}
			return false;
		}
		
		public void setTimeformat(String timeformat) {
			if (timeformat == null || timeformat.equals("非标准时间")) {
				return;
			}
			putAttribute(ATTR_TIMEFORMAT, timeformat);
			if (shouldCheckTimeContext()) {
				putAttribute(ATTR_CONTEXT_DATE, "true");
				//不能直接格式化，需结合上下文
				return;
			}
			
			SimpleDateFormat sdf = formatter.get(timeformat);
			if(sdf == null){
				sdf = new SimpleDateFormat(timeformat);
				formatter.put(timeformat, sdf);
			}
			try {
				String wordToParse = word;
				if (wordToParse.contains("－") && !timeformat.contains("－")) {
					wordToParse = wordToParse.replaceAll("－", "-");
				}
				Date date = sdf.parse(wordToParse);
				normalDate = normalFormatter.format(date);
			} catch (Exception e) {
				//TODO handle exceptions
				e.printStackTrace();
			}
		}
		
		@Override
		public void setNature(String nature) {
			if (this.nature != null && this.nature.equals(WordNatures.DATE) && !this.nature.equals(nature)) {
				removeTimeFormat();
			}
			super.setNature(nature);
		}
		
		private void removeTimeFormat(){
			attributes.remove(ATTR_TIMEFORMAT);
			attributes.remove(ATTR_CONTEXT_DATE);
			normalDate = null;
		}
		
		public String getTimeformat() {
			return (String) getAttribute(ATTR_TIMEFORMAT);
		}
		
		/**
		 * 是否是未识别的
		 * @return
		 */
		public boolean isNatureUnrecongnized(){
			return nature.equals(WordNatures.UNREC) || nature.equals("");
		}
		
//		public void setGuessNature(String nature){
//			this.nature = nature;
//		}
		
		@Override
		public Object clone() {
			try {
				return super.clone();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}