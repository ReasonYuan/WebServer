package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;

public class Predicate {
	
	public static final String STR_ID = "_ID";
	
	//下面是内置谓词方法
	/**
	 * Follow(pos1, pos2) 后则pos2 follow pos1
	 */
	public static final String BUILDIN_FOLLOW = "Follow";
	/**
	 * After(pos1, pos2) 后则pos2 after pos1
	 */
	public static final String BUILDIN_AFTER = "After";
	/**
	 * DateAfter(pos1, pos2) 日期pos2位置的日期 after pos1位置的日期,
	 */
	public static final String BUILDIN_DATEAFTER = "DateAfter";
	/**
	 * AfterWithin(pos1, pos2, num) 后则pos2 after pos1, 在num这个数字内, 这个相当于一个窗口限制
	 */
	public static final String BUILDIN_AFTERWITHIN = "AfterWithin";
	/**
	 * Contains(property, constantString)
	 */
	public static final String BUILDIN_CONTAINS = "Contains";
	/**
	 * StartsWith(property, constantString)
	 */
	public static final String BUILDIN_STARTSWITH = "StartsWith";
	/**
	 * EndsWith(property, constantString)
	 */
	public static final String BUILDIN_ENDSWITH = "EndsWith";
	/**
	 * Equals(property, constantString)
	 */
	public static final String BUILDIN_EQUALS = "Equals";
	/**
	 * LargeThan(property, constantString)
	 */
	public static final String BUILDIN_LARGETHAN = "LargeThan";
	/**
	 * LessThan(property, constantString)
	 */
	public static final String BUILDIN_LESSTHAN = "LessThan";
	/**
	 * 单词是否有某个属性, HasAttr(pos, attrKey)
	 */
	public static final String BUILDIN_HASATTR = "HasAttr";
	/**
	 * 单词是否有某个属性, EqualsAttr(pos, attrKey, attrValue)
	 */
	public static final String BUILDIN_EQUALSATTR = "EqualsAttr";
	/**
	 * 某位置间没有某词, NoWordBetween(word, nature, startPos, endPos)
	 */
	public static final String BUILDIN_NOWORDBETWEEN = "NoWordBetween";
	/**
	 * 某句话没有某词, NoWordBetween(word, nature, sentencePos)
	 */
	public static final String BUILDIN_NOWORDINSENTENCE = "NoWordInSentence";
	/**
	 * 某位置间只有某种词, OnlyWordBetween(word, nature, startPos, endPos)
	 */
	public static final String BUILDIN_ONLYWORDBETWEEN = "OnlyWordBetween";
	/**
	 * 参照posRef的第一个前面, IN_SENTENCE: 表示在该句里查找, IN_SECTION: 表示整段落查找, ~n|~-n表示在同一句里前后n个词范围内查找
	 * FindFirstBefore(posRef, IN_SENTENCE|IN_SECTION|~n|~-n, compareWord, compareNature, compareAttrKey, compareAttrValue, foundPos, foundWord, foundNature)
	 * , 其中compareWord, compareNature, compareAttrKey必有一项非空, 如果compareAttrValue不为空，则compareAttrKey必须不为空
	 */
	public static final String BUILDIN_FINDFIRSTBEFORE = "FindFirstBefore";
	/**
	 * 参照posRef的第一个后面, IN_SENTENCE: 表示在该句里查找, IN_SECTION: 表示整段落查找, ~n|~-n表示在同一句里前后n个词范围内查找
	 * FindFirstAfter(posRef, IN_SENTENCE|IN_SECTION|~n|~-n, compareWord, compareNature, compareAttrKey, compareAttrValue, foundPos, foundWord, foundNature)
	 * , 其中compareWord, compareNature, compareAttrKey必有一项非空, 如果compareAttrValue不为空，则compareAttrKey必须不为空
	 */
	public static final String BUILDIN_FINDFIRSTAFTER = "FindFirstAfter";
	/**
	 * FindMaxDate(pos, word, nature), 用法: !FindMaxDate(pos, word, nature) v MaxDate(pos, word, nature), 得到一个结论MaxDate(pos, word, nature)
	 */
	public static final String BUILDIN_FINDMAXDATE = "FindMaxDate";
	/**
	 * 参照posRef的第一个前面, IN_SENTENCE: 表示在该句里查找, IN_SECTION: 表示整段落查找
	 * FindSequence(posRef, IN_SENTENCE|IN_SECTION, xxx.txt, pos1, pos2, pos3, pos4)
	 */
	public static final String BUILDIN_FINDSEQUENCE = "FindSequence";
	
	/**
	 * 查找前后面第n个词是否是某种词
	 * FindAround(posRef, number, compareWord, compareNature, compareAttrKey, compareAttrValue, foundPos, foundWord, foundNature)
	 */
	public static final String BUILDIN_FINDAROUND = "FindAround";
	/**
	 * ExistWord(compareWord, compareNature, compareAttrKey, compareAttrValue, foundPos, foundWord, foundNature), 用法: 
	 * 1: !ExistWord(compareWord, compareNature, compareAttrKey, compareAttrValue, foundPos, foundWord, foundNature) v SomeWord(foundPos, foundWord, foundNature), 得到一个结论SomeWord(foundPos, foundWord, foundNature)
	 * 2: ExistWord(compareWord, compareNature, compareAttrKey, compareAttrValue, foundPos, foundWord, foundNature) v !SomeWord(compareWord, compareNature), 得到一个结论!SomeWord(compareWord, compareNature)
	 */
	public static final String BUILDIN_EXISTWORD = "ExistWord";
	
	/**
	 * 查找文本内容, FindText(posStart, intcludeStart, posEnd, includeEnd, foundText), posStart：开始位置，posEnd：结束位置, 可以为null(表示到居末), intcludeStart：是否包含开始位置，默认为不包含，includeEnd:是否包含结束位置，默认为不包含
	 * 例子: !YouYinSeq(startPos, endPos) v !FindText(startPos, endPos, false, false, foundText) v FoundYYText(startPos, endPos, foundText)
	 */
	public static final String BUILDIN_FINDTEXT = "FindText";
	
	/**
	 * 表达式, 比如Express(pos1+pos2=30), Express(pos1>=pos2), Express(pos1+10<=pos2), Express(pos2>=pos1+10>30)
	 * 仅支持: +-><=, 同时支持三连判断，比如：pos2>pos1+10>30
	 */
	public static final String BUILDIN_EXPRESS = "Express";
	
	/**
	 * 是否有某个特征, HasFeature(featureName, pos1, pos2, pos3, pos4, ...)参数变长，可以传任意多个pos
	 */
	public static final String BUILDIN_HASFEATURE = "HasFeature";
	
	/**
	 * 是否否定的内容, !A(pos, x, y) v !IsDenied(pos) v Something(x, y)
	 */
	public static final String BUILDIN_ISDENIED = "IsDenied";
	
	private static final String[] buildins = {
			BUILDIN_FOLLOW, BUILDIN_AFTER, BUILDIN_CONTAINS
			, BUILDIN_STARTSWITH, BUILDIN_ENDSWITH, BUILDIN_EQUALS
			, BUILDIN_HASATTR, BUILDIN_EQUALSATTR, BUILDIN_NOWORDBETWEEN
			, BUILDIN_NOWORDINSENTENCE, BUILDIN_ONLYWORDBETWEEN
			, BUILDIN_AFTERWITHIN, BUILDIN_DATEAFTER, BUILDIN_LARGETHAN
			, BUILDIN_LESSTHAN, BUILDIN_FINDFIRSTBEFORE, BUILDIN_FINDFIRSTAFTER
			, BUILDIN_FINDAROUND, BUILDIN_FINDMAXDATE, BUILDIN_EXISTWORD,BUILDIN_FINDSEQUENCE
			, BUILDIN_FINDTEXT, BUILDIN_HASFEATURE, BUILDIN_ISDENIED, BUILDIN_EXPRESS
		};
	
	/**
	 * 只有部分内置谓词是全局谓词
	 */
	private static final String[] globalBuildins = {
			BUILDIN_FINDMAXDATE, BUILDIN_EXISTWORD
		};
	
	/**
	 * 是否是内置谓词
	 */
	private boolean isBuildin= false;
	
	/**
	 * 是否是global内置谓词
	 */
	private boolean isBlobalBuildin= false;

	private String name;

	private ArrayList<Object> params = new ArrayList<Object>();
	
	private boolean withNot = false;
	
	/**
	 * 参数_ID的名称, _ID不会参与运算
	 */
	private String idName = STR_ID;
	
	private boolean definedIdName = false;
	
	private Express express = null;
	
	private boolean isExpressBuildin = false;
	
	public Predicate() {
	}
	
	private void setIdName(String idName) {
		this.idName = idName;
		definedIdName = true;
	}
	
	public boolean isExpressBuildin() {
		return isExpressBuildin;
	}
	
	public Express getExpress() {
		if (express == null) {
			throw new RuntimeException("只有内置谓词Express才能调用这个方法!");
		}
		return express;
	}
	
	public boolean isDefinedIdName() {
		return definedIdName;
	}
	
	public String getIdName() {
		return idName;
	}
	
	public boolean isBuildin() {
		return isBuildin;
	}
	
	public boolean isBlobalBuildin() {
		return isBlobalBuildin;
	}
	
	/**
	 * 是否是公式中的最后一个谓词
	 * @param isLastInForuma
	 */
	public void onPredicateParseFinish(boolean isLastInForuma) {
		if (!isLastInForuma) {
			boolean with_ID = false;
			for(int i=0; i<params.size(); i++){
				if (i == 0) {
					if (params.get(i).toString().startsWith(STR_ID)) {
						setIdName(params.get(i).toString());
						with_ID = true;
					}
				} else {
					if (params.get(i).toString().startsWith(STR_ID)) {
						throw new RuntimeException("_ID开头的变量是保留字段，只能出现在谓词的第一个参数!");
					}
				}
			}
			//谓词中有ID的，第一个参数ID被记录成参数IDName
			if (with_ID) {
				params.remove(0);
			}
		} else {
			//do nothing，公式的最后一个谓词的_ID*是取前面的参数
		}
	}
	
	public void addParam(Object param){
		params.add(param);
		if (isExpressBuildin) {
			if (params.size() > 1) {
				throw new RuntimeException("表达式内置函数只能有一个参数: 表达式!");
			}
			express = new Express(param.toString());
		}
	}
	
	public void addParams(ArrayList<Object> params){
		params.addAll(params);
//		if (params.get(0).toString().startsWith(STR_ID)) {
//			ArrayList<Object> paramsNew = new ArrayList<Object>();
//			paramsNew.addAll(params);
//			String idParamName = paramsNew.remove(0).toString();
//			setIdName(idParamName);
//			params.addAll(paramsNew);
//		} else {
//			
//		}
	}
	
	public ArrayList<Object> getParams() {
		return params;
	}
	
	public void setName(String name) {
		this.name = name;
		if (name.equals(Predicate.BUILDIN_EXPRESS)) {
			this.isExpressBuildin = true;
		}
		for (int i = 0; i < buildins.length; i++) {
			if (buildins[i].equals(name)) {
				isBuildin = true;
				break;
			}
		}
		for (int i = 0; i < globalBuildins.length; i++) {
			if (globalBuildins[i].equals(name)) {
				isBlobalBuildin = true;
				break;
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setWithNot(boolean withNot) {
		this.withNot = withNot;
	}
	
	public boolean isWithNot() {
		return withNot;
	}
	
	@Override
	public String toString() {
		return (withNot ? "!" : "") + name + "(" + params.toString() + ")";
	}
	
//	/**
//	 * 每一个谓词都有一个_ID参数, 相似于一般函数的this
//	 */
//	public void generatePredicateIDParam(){
////		if (!isBuildin()) {
////			//默认都有参数ID
////			if (!getParams().get(0).toString().startsWith(Predicate.STR_ID)) {
////				getParams().add(0, Predicate.STR_ID);
////			}
////		}
//	}
	
}
