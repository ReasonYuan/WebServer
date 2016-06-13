package com.yiyihealth.nlp.deepstruct.analysis.sslogic;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.SyntaxSequence.SyntaxNode;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.WordOrderSearcher.SyntaxSequence.SyntaxNode.SkipCondition;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

public class WordOrderSearcher extends SyntaxSearcher {
	
	private SyntaxSequence syntaxSequence;
	
	private OnResultAction onResultAction;

	public WordOrderSearcher(String name, SyntaxSequence syntaxSequence, OnResultAction onResultAction) {
		super(name);
		this.syntaxSequence = syntaxSequence;
		this.onResultAction = onResultAction;
	}

	public SyntaxSequence getSyntaxSequence() {
		return syntaxSequence;
	}
	
	//TODO 整个sslogic的查询基础数据都需要重构：在合并后的整篇记录里查询
	@Override
	public int doSearch(ArrayList<EWord> words, int[] sentencePoses, SearchOffset offset) {
		SyntaxNode firstNode = syntaxSequence.nodes.get(0);
		int size = words.size();
		int foundCnt = 0;
		if (syntaxSequence.notExists) {
			int start = 0;
			int end = 0;
			while(start < size){
				for (int j = start; j < size; j++) {
					if (Punctuation.isSentenceEnd(words.get(j))) {
						end = j;
						break;
					} else if (j == size - 1) {
						end = size;
					}
				}
				MatchResult result = null;
				for (int j = start; j < end; j++) {
					result = doMatch(words, null, firstNode, j, end);
					if (result != null) {
						foundCnt++;
						break;
					}
				}
				if (result == null) {
					MatchResult r = new MatchResult();
					r.sentencePos = sentencePoses[start];
					r.matchFailed = true;
					onResultAction.doAction(words, r, offset);
				}
				start = end+1;
			}
		} else {
			//TODO 下面代码需要优化，针对合并Action，每次合并后size变短
			for (int j = 0; j < words.size(); j++) {
				if (isInRequiredSections(j, words)) {
					MatchResult result = doMatch(words, null, firstNode, j, words.size());
					if (result != null) {
						result.sentencePos = sentencePoses[j];
						//仅当需要检测成功时才执行
						if (onResultAction != null) {
							// 已全部匹配
							onResultAction.doAction(words, result, offset);
							foundCnt++;
						}
					}
				}
			}
		}
		return foundCnt;
	}
	
	private boolean isLegalSkip(EWord word, SkipCondition skipCondition){
		if (skipCondition == null) {
			return true;
		}
		if (skipCondition.isNature) {
			//是这些词性
			if (skipCondition.isNot) {
				return !skipCondition.conditions.contains(word.getNature());
			} else {
				return skipCondition.conditions.contains(word.getNature());
			}
		} else {
			//是这些词
			if (skipCondition.isNot) {
				return !skipCondition.conditions.contains(word.getWord());
			} else {
				return skipCondition.conditions.contains(word.getWord());
			}
		}
	}
	
	private MatchResult doMatch(ArrayList<EWord> words, MatchResult previousResult, SyntaxNode firstNode, int start, int end){
		StringBuffer debugBuffer = new StringBuffer();
		SyntaxNode nodeToMatch = firstNode;
		int skipCounter = 0;
		MatchResult result = new MatchResult();
		result.variables = new String[words.size()];
		int wordsSize = words.size();
		for (int j = start; j < end; j++) {
			EWord term = words.get(j);
			boolean isToEndOfThisSentence = Punctuation.isSentenceEnd(term);
			if (nodeToMatch != null) {
				if (nodeToMatch.isEndNode) {
					if (isToEndOfThisSentence) {
						//匹配到了句末, end只能出现在匹配末尾
						if (nodeToMatch.next != null) {
							throw new RuntimeException("语法错误: end只能出现在匹配末尾");
						}
						nodeToMatch = null;
					}
					break;
				} else if (nodeToMatch.isSkip()) {
					if (nodeToMatch.next != null) {
						if (nodeToMatch.skipLessOrMore == 1 && skipCounter < nodeToMatch.skipNumber) {
							//强制跳过，force to skip, n+表示必须跳过多少个词而不必匹配
							if(!isLegalSkip(term, nodeToMatch.skipCondition)){
								break;
							}
							skipCounter++;
							debugBuffer.append(term.getWord());
							result.variables[j] = nodeToMatch.variableName;
						} else if (j == wordsSize - 1 && nodeToMatch.next != null && nodeToMatch.next.isEndNode) {
							//最后一个词匹配
							if(!isLegalSkip(term, nodeToMatch.skipCondition)){
								break;
							}
							//没有更多词需要匹配，但到句末，并且是句末匹配节点
							result.variables[j] = nodeToMatch.variableName;
							debugBuffer.append(term.getWord());
							nodeToMatch = null;
						} else {
							if(nodeToMatch.next.matchTerm(term)){
								//下一个词满足条件
								result.variables[j] = nodeToMatch.next.variableName;
								nodeToMatch = nodeToMatch.next.next;
								skipCounter = 0;
								debugBuffer.append(term.getWord());
							} else {
								boolean skipable = true;
								//TODO 限制2+的match
								if (nodeToMatch.skipLessOrMore == -1) {
									if(skipCounter >= nodeToMatch.skipNumber){
										skipable = false;
									}
								}
								if (skipable) {
									if(!isLegalSkip(term, nodeToMatch.skipCondition)){
										break;
									}
									result.variables[j] = nodeToMatch.variableName;
									//skipped
									debugBuffer.append(term.getWord());
									skipCounter++;
								} else {
									//结束匹配, 超过skip次数
									break;
								}
							}
						}
					} else {
						if (nodeToMatch.skipLessOrMore == 1) {
							throw new RuntimeException("末尾任意匹配不支持跳过型匹配!");
						}
						boolean skipable = true;
						if (nodeToMatch.skipLessOrMore == -1) {
							if(skipCounter >= nodeToMatch.skipNumber){
								skipable = false;
							}
						}
						if(!isLegalSkip(term, nodeToMatch.skipCondition)){
							break;
						}
						if (skipable && !(isToEndOfThisSentence || j == wordsSize - 1)) {
							result.variables[j] = nodeToMatch.variableName;
							//skipped
							debugBuffer.append(term.getWord());
							skipCounter++;
						} else {
							if (j == wordsSize - 1) {
								result.variables[j] = nodeToMatch.variableName;
								//skipped
								debugBuffer.append(term.getWord());
							}
							//结束匹配, 超过skip次数
							nodeToMatch = nodeToMatch.next;
						}
					}
				} else {
					skipCounter = 0;
					if(nodeToMatch.matchTerm(term)){
						result.variables[j] = nodeToMatch.variableName;
						debugBuffer.append(term.getWord());
						nodeToMatch = nodeToMatch.next;
						skipCounter = 0;
						//刚好到句末
						if (j == wordsSize - 1 && nodeToMatch != null){
							//还有需要匹配的，如果匹配句末或任意匹配，则匹配成功
							if(nodeToMatch.isEndNode || nodeToMatch.isSkip() && nodeToMatch.skipLessOrMore == -1
									&& (nodeToMatch.next == null || nodeToMatch.next.isEndNode)) {
								nodeToMatch = null;
							}
						}
					} else {
						//没有得到匹配
						break;
					}
				}
			}
			if (nodeToMatch == null) {
				result.debugMatchText =  debugBuffer.toString();
				return result;
			}
			if(isToEndOfThisSentence){
				break;
			}
		}
		return null;
	}
	
	private boolean isInRequiredSections(int position, ArrayList<EWord> words){
		if (syntaxSequence.belong2Heading != null) {
			//查看是否在需要的title(段落里)
			//找title
			EWord headingWord = null;
			for (int i = position - 1; i >= 0; i--) {
				EWord word = words.get(i);
				if (word.getNature().equals(WordNatures.HEADING)) {
					headingWord = word;
					break;
				}
			}
			if (headingWord != null) {
				for (int i = 0; i < syntaxSequence.belong2Heading.length; i++) {
					if (syntaxSequence.belong2Heading[i].startsWith("%")) {
						if(headingWord.getWord().endsWith(syntaxSequence.belong2Heading[i].replace("%", ""))){
							return true;
						}
					} else if (syntaxSequence.belong2Heading[i].endsWith("%")) {
						if(headingWord.getWord().startsWith(syntaxSequence.belong2Heading[i].replace("%", ""))){
							return true;
						}
					} else if(syntaxSequence.belong2Heading[i].equals(headingWord.getWord())){
						return true;
					}
				}
			}
		} else {
			//如果没有heading限制则默认合法
			return true;
		}
		return false;
	}
	
	public OnResultAction getOnResultAction() {
		return onResultAction;
	}
	
	/**
	 * 匹配结果
	 *
	 */
	public static class MatchResult {
		public String[] variables;
		public int sentencePos = -1;
		boolean matchFailed = false;
		public String debugMatchText = "";
	}
	
	public static class SyntaxSequence {
		
		private int depth = 0;
		
		public static enum VerifyType {
			EXISTS, AROUND, BEFORE, AFTER
		};
		
		protected VerifyType verifyType;
		
		protected boolean notExists = false;

		protected ArrayList<SyntaxSequence> conditions = new ArrayList<SyntaxSequence>();
		
		protected SyntaxSequence parent = null;
		
		/**
		 * 对应变量名词, 一般指组合后的某个变量是否包含原来的某个词性
		 */
		protected String containsBy = null;
		
		/**
		 * 仅在某个或某几个title范围内查找
		 */
		protected String[] belong2Heading = null;
		
		protected ArrayList<SyntaxNode> nodes = new ArrayList<SyntaxNode>();
		
		public SyntaxSequence(SyntaxSequence parent) {
			this.parent = parent;
			if (parent != null) {
				parent.conditions.add(this);
				depth = parent.depth + 1;
			}
		}
		
		@Override
		public String toString() {
			return "{depth: " + depth + ", verifyType:" + verifyType + ", negative: " + notExists + ",\nnodes:" + nodes + ",\nconditions: " + conditions + "}\n";
		}
		
		public ArrayList<SyntaxSequence> getConditions() {
			return conditions;
		}

		public SyntaxSequence getParent() {
			return parent;
		}

		public boolean isNegative() {
			return notExists;
		}
		
		protected void addSkipNode(int number, int lessOrMore, String variableName, SkipCondition skipCondition){
			SyntaxNode node = new SyntaxNode(number, lessOrMore);
			node.setVariableName(variableName);
			node.skipCondition = skipCondition;
			addNode(node);
		}
		
		protected void addEndNode(){
			SyntaxNode node = new SyntaxNode();
			node.isEndNode = true;
			addNode(node);
		}
		
		protected void setVerifyType(VerifyType verifyType) {
			this.verifyType = verifyType;
		}

		protected void addNode(String word, String nature, String variableName) {
			String enNature = nature == null ? null : WordNatures.getNatureByReadableName(nature);
			SyntaxNode node = new SyntaxNode(enNature != null, word != null, enNature, word);
			node.setVariableName(variableName);
			addNode(node);
		}
		
		protected void setContainsBy(String containsBy) {
			this.containsBy = containsBy;
		}
		
		public void setBelong2Heading(String[] belong2Heading) {
			this.belong2Heading = belong2Heading;
		}
		
		public String getContainsBy() {
			return containsBy;
		}
		
		private void addNode(SyntaxNode node){
			if (nodes.size() > 0) {
				SyntaxNode nodePre = nodes.get(nodes.size() - 1);
				nodePre.next = node;
				node.previous = nodePre; 
			}
			nodes.add(node);
		}
		
		public static class SyntaxNode {
			public static class SkipCondition {
				ArrayList<String> conditions = new ArrayList<>();
				boolean isNot;
				boolean isNature;
			}
			protected boolean verifyNature = false;
			protected boolean verifyWord = false;
			protected String nature;
			protected String word;
			protected String debugNature;
			protected String variableName;
			
			/**
			 * 是否是可以跳过任意单词
			 */
			protected int skipNumber = 0;
			
			protected int skipLessOrMore = -1;
			
			protected SyntaxNode previous = null;
			protected SyntaxNode next = null;
			
			protected boolean isEndNode = false;
			
			protected SkipCondition skipCondition;
			
			protected SyntaxNode(){
			}
			
			protected SyntaxNode(int skipNumber, int skipLessOrMore){
				this.skipNumber = skipNumber;
				this.skipLessOrMore = skipLessOrMore;
			}
			
			protected SyntaxNode(boolean verifyNature, boolean verifyWord, String nature, String word){
				this.verifyNature = verifyNature;
				this.verifyWord = verifyWord;
				this.nature = nature;
				this.word = word;
				debugNature = nature == null ? null : WordNatures.getNatureReadableName(nature);
			}
			
			public boolean isEndNode() {
				return isEndNode;
			}
			
			public void setVariableName(String variableName) {
				this.variableName = variableName;
			}
			
			public boolean matchTerm(EWord term) {
				if (skipNumber > 0) {
					throw new RuntimeException("Do not call skip node's matchTerm()!");
				} else if (isEndNode) {
					return term.getWord().equals(""+Punctuation.FULLSTOP_C);
				} else {
					boolean pass = false;
					if (verifyNature && nature.equals(term.getNature())){
						pass = true;
					}
					if (verifyWord && word.equals(term.getWord())) {
						pass = true;
					}
					return pass;
				}
			}
			
			@Override
			public String toString() {
				return "\nverifyNature:" + verifyNature + ", verifyWord: " + verifyWord + ", word:" + word + ", nature: " + nature + ", skipNumber:" + skipNumber + ", skipLessOrMore: " + skipLessOrMore + ", variableName: " + variableName;
			}
			
			public boolean isVerifyNature() {
				return verifyNature;
			}
			public boolean isVerifyWord() {
				return verifyWord;
			}
			public String getNature() {
				return nature;
			}
			public String getWord() {
				return word;
			}

			public boolean isSkip() {
				return skipNumber > 0;
			}

			public SyntaxNode getPrevious() {
				return previous;
			}

			public SyntaxNode getNext() {
				return next;
			}
			
		}
	}

	
}
