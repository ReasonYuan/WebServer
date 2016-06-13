package com.yiyihealth.nlp.deepstruct.dict;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * 脱离了分词的单词，主要给逻辑层使用
 * @author qiangpeng
 *
 */
public class EWord implements Cloneable {
	
	public static final String ATTR_TIMEFORMAT = "timeformat";
	public static final String ATTR_CONTEXT_DATE = "isContextDate";
	public static final String ATTR_TIMESTAMP = "timestamp";
	
	public static final SimpleDateFormat normalFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@SuppressWarnings({ "unused", "serial" })
	private static final Hashtable<String, SimpleDateFormat> sdfs = new Hashtable<String, SimpleDateFormat>(){
		public synchronized SimpleDateFormat get(Object key) {
			SimpleDateFormat res = super.get(key);
			if (res == null) {
				try {
					res = new SimpleDateFormat((String)key);
					super.put((String)key, res);
				} catch (Exception e) {
				}
			}
			return res;
		};
	};

	protected String word;
	protected String nature;
	
	protected boolean isSentenceEnd = false;
	
	/**
	 * 确定词性后，再确定其特征
	 */
	protected ArrayList<String> features = new ArrayList<>();
	
	protected ArrayList<String> taggedDates = new ArrayList<String>();
	
	/**
	 * 指向的具体日期, 对应 {@link #taggedDates}
	 */
	protected ArrayList<String> taggedActualDates = new ArrayList<String>();
	
	/**
	 * 特别的属性，供程序计算使用，定义待议论, 目前仅给时间使用
	 */
	protected Hashtable<String, Object> attributes = new Hashtable<String, Object>();
	
	public EWord(String word, String nature) {
		this.nature = nature;
		this.word = word;
		isSentenceEnd = Punctuation.isSentenceEnd(this);
	}
	
	public boolean isDate(){
		return nature.equals(WordNatures.DATE);
	}
	
	public void tagDate(String date){
		taggedDates.add(date);
	}
	
	public void tagActualDate(String date){
		taggedActualDates.add(date);
	}
	
	public ArrayList<String> getTaggedDates() {
		return taggedDates;
	}
	
	public ArrayList<String> getTaggedActualDates() {
		return taggedActualDates;
	}
	
	public boolean hasFeature(String feature) {
		return features.contains(feature);
	}
	
	public void addFeature(String feature){
		if(!hasFeature(feature)){
			features.add(feature);
		}
	}
	
	public void putAttribute(String key, String value){
		attributes.put(key, value);
	}
	
	/**
	 * 通过日期获取具体日期
	 * @param date 
	 * @return
	 */
	public String getTaggedActualDate(String date) {
		int pos = taggedDates.indexOf(date);
		if(pos != -1){
			return taggedActualDates.get(pos);
		}else{
			return date;
		}
	}
	
	public void parseDateOnInit(){
		if (attributes.get(ATTR_TIMEFORMAT) != null && attributes.get(ATTR_CONTEXT_DATE) == null) {
			try {
				if (getWord().contains("－")) {
					setWord(getWord().replaceAll("－", "-"));
				}
				setTimestamp(sdfs.get(attributes.get(ATTR_TIMEFORMAT)).parse(getWord()).getTime());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public Object getAttribute(String key){
		return attributes.get(key);
	}
	
	public Hashtable<String, Object> getAttributes() {
		return attributes;
	}
	
	public long getTimestamp() {
		Long ts = (Long) attributes.get(ATTR_TIMESTAMP);
		return ts == null ? 0 : ts;
	}
	
	public void setTimestamp(long timestamp) {
		attributes.put(ATTR_TIMESTAMP, timestamp);
	}
	
	public String getWord() {
		return word;
	}
	
	public String getNature() {
		return nature;
	}
	
	public void setWord(String word) {
		this.word = word;
		isSentenceEnd = Punctuation.isSentenceEnd(this);
	}
	
	public boolean isSentenceEnd() {
		return isSentenceEnd;
	}
	
	public void setNature(String nature) {
		this.nature = nature;
	}
	
	@Override
	public String toString() {
		return "word: " + word + ", nature: " + nature + ", " + attributes.toString();
	}
}
