package com.yiyihealth.ds.esearch.syntaxtrie;

import java.util.ArrayList;

public class SyntaxRule {
	
	private int counter = 0;
	
	private ArrayList<String> examples = new ArrayList<String>();
	
	ArrayList<SyntaxRuleNode> nodes = new ArrayList<SyntaxRuleNode>();

	/**
	 * 加入末尾
	 * @param nature
	 */
	public void addNode(int index, String nature) {
		nodes.add(index, new SyntaxRuleNode(nature, null));
	}

	/**
	 * 加入队首
	 * @param nature
	 */
	public void pushNode(String nature) {
		nodes.add(0, new SyntaxRuleNode(nature, null));
	}
	
	public ArrayList<String> getExamples() {
		return examples;
	}
	
	public void setExamples(ArrayList<String> examples) {
		this.examples = examples;
	}
	
	public boolean startsWithNature(String nature){
		return nodes.get(0).nature.equals(nature);
	}
	
	public boolean containsNature(String nature){
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).nature.equals(nature)) {
				return true;
			}
		}
		return false;
	}
	
	public int getCounter() {
		return counter;
	}
	
	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < nodes.size(); i++) {
			result += nodes.get(i).toString();
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SyntaxRule)) {
			throw new IllegalArgumentException("Can only campare with SyntaxRule!");
		}
		return toString().equals(obj.toString());
	}
	
	public String getReadableRule() {
		String result = "";
		for (int i = 0; i < nodes.size(); i++) {
			result += nodes.get(i).toReadableString();
		}
		return result;
	}
	
	public String getNatureRule() {
		String result = "";
		for (int i = 0; i < nodes.size(); i++) {
			result += nodes.get(i).toNatureString();
		}
		return result;
	}
	
	/**
	 * 返回推理逻辑表达式，nature2Inherence表示要推理的词性<br/>
	 * 前提条件是这个规则是正确的规则
	 * @param nature2Inherence
	 * @return
	 */
	public String getNatureLogic(String nature2Inherence){
		if (nodes.size() <= 1) {
			throw new RuntimeException("Rule nodes size must > 1!");
		}
		//final String express = "0.8 Nature(\"Drug\", pos1), Nature(\"Value\", pos2), !Nature(\"ValueUnit\", pos3), [pos2=pos1 + 1 AND pos3=pos2 + 1] => ShouldBeNature(\"ValueUnit\", pos3)";
		int index = -1;
		for (int i = 0; i < nodes.size(); i++) {
			if(nodes.get(i).nature.equals(nature2Inherence)){
				index = i;
				break;
			}
		}
		if (index == -1) {
			return null;
		} else {
//			String result = "0.8 ";//任意权重，因为还需要学习，只要不为0即可
//			String shouldBeNature = "";
//			for (int i = 0; i < nodes.size(); i++) {
//				SyntaxRuleNode node = nodes.get(i);
//				String naturePre = WordNatures.getPredicateByName(node.nature);
//				if (i == index) {
//					result += "!"+naturePre+"(pos" + i + ")";
//					shouldBeNature = naturePre;
//				} else {
//					result += naturePre + "(pos" + i + ")";
//				}
//				result += ", ";
//			}
//			result += "[";
//			for (int i = 0; i < nodes.size() - 1; i++) {
//				result += "pos" + (i + 1) + "=pos" + i + " + 1";
//				if (i < nodes.size() - 2) {
//					result += " AND ";
//				}
//			}
//			result += "] => ShouldBe" + shouldBeNature + "(pos" + index + ")";
//			return result;
			
//			String result = "0.8 ";//任意权重，因为还需要学习，只要不为0即可
//			String shouldBeNature = "";
//			for (int i = 0; i < nodes.size(); i++) {
//				SyntaxRuleNode node = nodes.get(i);
//				String naturePre = WordNatures.getPredicateByName(node.nature);
//				if (i == index) {
//					result += naturePre+"(pos" + i + ")";//"!"+"!"=nothing
//					shouldBeNature = naturePre;
//				} else {
//					result += "!"+naturePre + "(pos" + i + ")";
//				}
//				result += " v ";
//			}
//			for (int i = 0; i < nodes.size() - 1; i++) {
//				result += "!After(" + i + ", " + (i+1) + ")";
//				if (i < nodes.size() - 2) {
//					result += " v ";
//				}
//			}
//			result += " v ShouldBe" + shouldBeNature + "(pos" + index + ")";
//			return result;
			
//			if (nodes.size() > 5) {
//				throw new RuntimeException("nodes.size > 5 is not supported yet!");
//			}
//			final String [][] evidences = {
//					{
//						"Nature1_0(pos, natureEvi, posEvi)", 
//						"Nature1_1(natureEvi, posEvi, pos)"
//					}, {
//						"Nature2_0(pos, natureEvi1, posEvi1, natureEvi2, posEvi2)",
//						"Nature2_1(natureEvi1, posEvi1, pos, natureEvi2, posEvi2)",
//						"Nature2_2(natureEvi1, posEvi1, natureEvi2, posEvi2, pos)"
//					}, {
//						"Nature3_0(pos, natureEvi1, posEvi1, natureEvi2, posEvi2, natureEvi3, posEvi3)",
//						"Nature3_1(natureEvi1, posEvi1, pos, natureEvi2, posEvi2, natureEvi3, posEvi3)",
//						"Nature3_2(natureEvi1, posEvi1, natureEvi2, posEvi2, pos, natureEvi3, posEvi3)",
//						"Nature3_3(natureEvi1, posEvi1, natureEvi2, posEvi2, natureEvi3, posEvi3, pos)"
//					},  {
//						"Nature4_0(pos, natureEvi1, posEvi1, natureEvi2, posEvi2, natureEvi3, posEvi3, natureEvi4, posEvi4)",
//						"Nature4_1(natureEvi1, posEvi1, pos, natureEvi2, posEvi2, natureEvi3, posEvi3, natureEvi4, posEvi4)",
//						"Nature4_2(natureEvi1, posEvi1, natureEvi2, posEvi2, pos, natureEvi3, posEvi3, natureEvi4, posEvi4)",
//						"Nature4_3(natureEvi1, posEvi1, natureEvi2, posEvi2, natureEvi3, posEvi3, pos, natureEvi4, posEvi4)",
//						"Nature4_4(natureEvi1, posEvi1, natureEvi2, posEvi2, natureEvi3, posEvi3, natureEvi4, posEvi4, pos)"
//					}
//
//			};
//			String evidence = evidences[nodes.size()-2][index];
//			String result = "0.8 ";//任意权重，因为还需要学习，只要不为0即可
//			int replaceCnt = 1;
//			for (int i = 0; i < nodes.size(); i++) {
//				SyntaxRuleNode node = nodes.get(i);
//				String naturePre = WordNatures.getPredicateByName(node.nature);
//				if (i != index) {
//					evidence = evidence.replace("natureEvi" + replaceCnt, naturePre);
//					replaceCnt++;
//				}
//			}
//			result = result + "!" + evidence + " v ShouldBeNature(pos, " + WordNatures.getPredicateByName(nodes.get(index).nature) + ")";
//			return result;
			
			String result = "0.1 ";//任意权重，因为还需要学习，只要不为0即可
			String natureSeqAround = getNatureSeqAroundStr(index);
			result = result + "!HaveNatureSeqAround(pos, " + natureSeqAround + ")";
			return result;
		}
		
	}
	
	protected String getNatureSeqAroundStr(int matchAnyIndex){
		String natureSeqAround = "";
		for (int i = 0; i < nodes.size(); i++) {
			SyntaxRuleNode node = nodes.get(i);
			String naturePre = node.nature;
			if (i != matchAnyIndex) {
				natureSeqAround += naturePre;
			} else {
				natureSeqAround += "pos";
			}
			if (i < nodes.size() - 1) {
				natureSeqAround += "_";
			}
		}
		return natureSeqAround;
	}
	
	public static class SyntaxRuleNode {
		
		public SyntaxRuleNode(String nature, String word) {
			this.word = word;
			this.nature = nature;
			if (word == null && nature == null) {
				throw new RuntimeException("nature or word must not be null both!");
			}
		}
		
		public boolean equals(Object obj) {
			SyntaxRuleNode node = (SyntaxRuleNode) obj;
			return word.equals(node.word) && nature.equals(node.nature);
		};
		
		String word = "";
		String nature = "";
		
		@Override
		public String toString() {
			return String.format("{nature=%s, word=%s}", nature, word);
		}
		
		public String toReadableString() {
			return String.format("{nature=%s, word=%s}", nature, word);
		}
		
		public String toNatureString() {
			return String.format("{nature=%s}", nature);
		}
	}
	
}
