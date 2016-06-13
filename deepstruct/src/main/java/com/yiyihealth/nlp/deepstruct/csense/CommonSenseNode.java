package com.yiyihealth.nlp.deepstruct.csense;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.csense.CSItem.ItemType;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Term;

public class CommonSenseNode {
	
	/**
	 * 是否双向有效
	 */
	private boolean doubleDir = false;
	
	private CSItem[] leftNodes;
	
	private CSItem[] rightNodes;
	
	private CSItem[] auxilaries;
	
	private String csExpress;
	
	//TODO 以后用语义解析的方式
	private static final String[] doubleDirSignal = {"}={", "}!={", "} = {", "} != {"};
	
	/**
	 * 反向验证
	 */
	private CommonSenseNode reverseDirNode;
	
	/**
	 * @param csExpress - 常识表达式
	 */
	public CommonSenseNode(String csExpress, CSItem[] auxilaries) {
		CSItem[][] bothSides = new CSParser().parse(csExpress);
		this.leftNodes = bothSides[0];
		this.rightNodes = bothSides[1];
		this.auxilaries = auxilaries;
		this.csExpress = csExpress;
		for (int i = 0; i < doubleDirSignal.length; i++) {
			if (!doubleDir) {
				doubleDir = csExpress.contains(doubleDirSignal[i]);
			}
		}
		if (doubleDir) {
			reverseDirNode = new CommonSenseNode(auxilaries, rightNodes, leftNodes, csExpress);
		}
	}
	
	private CommonSenseNode(CSItem[] auxilaries, CSItem[] leftNodes, CSItem[] rightNodes, String csExpress){
		this.auxilaries = auxilaries;
		this.leftNodes = leftNodes;
		this.rightNodes = rightNodes;
		this.csExpress = csExpress;
	}
	
	public boolean isLegal(String nature, int pos, ArrayList<EWord> words){
		boolean legal = false;
		legal |= checkLeft(nature, pos, words);
		if (!legal) {
			legal |= checkRight(nature, pos, words);
		}
		if (!legal && reverseDirNode != null) {
			legal = reverseDirNode.isLegal(nature, pos, words);
		}
		return legal;
	}
	
	/**
	 * 
	 * @param nature
	 * @param pos	- 要检测常识的词的位置
	 * @param words
	 * @return
	 */
	private boolean checkLeft(String nature, int pos, ArrayList<EWord> words){
		boolean describeOnLeft = false;
		for (int i = 0; i < leftNodes.length; i++) {
			//TODO 处理特征
			if (leftNodes[i].getType() == ItemType.NATURE && leftNodes[i].getContent().equals(nature)) {
				describeOnLeft = true;
				break;
			}
		}
		if (describeOnLeft) {
			if (pos < words.size() - 1) {
				for (int i = pos+1; i < words.size(); i++) {
					Term term = (Term) words.get(i);
					for (int j = 0; j < rightNodes.length; j++) {
						boolean passed = isTermMatchCSSide(term, rightNodes[j]);
						if (passed) {
							return true;
						}
					}
					boolean isAuxilary = isAuxilary(term, false);
					if (!isAuxilary) {
						break;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isAuxilary(Term term, boolean isJustVerifyNature){
		boolean isAuxilary = false;
		for (int j = 0; j < auxilaries.length; j++) {
			CSItem auxiItem = auxilaries[j];
			boolean passed = false;
			switch (auxiItem.type) {
			case NATURE:
				//TODO 需要优化
				passed = isJustVerifyNature ? term.getNature().equals(auxiItem.content) : term.getCandidateNatures().contains(auxiItem.content);
				break;
			case WORD:
				passed = term.getWord().equals(auxiItem.content);
				break;
			case FEATURE:
				throw new RuntimeException("Feature is unimplemented!");
			default:
				break;
			}
			if (passed) {
				isAuxilary = true;
				break;
			}
		}
		return isAuxilary;
	}
	
	/**
	 * 某词是否满足常识的一边
	 * @param term
	 * @param item
	 * @return
	 */
	private boolean isTermMatchCSSide(Term term, CSItem item){
		boolean passed = false;
		switch (item.type) {
		case NATURE:
			passed = term.getCandidateNatures().contains(item.content);
			break;
		case WORD:
			passed = term.getWord().equals(item.content);
			break;
		case FEATURE:
			throw new RuntimeException("Feature is unimplemented!");
		default:
			break;
		}
		return passed;
	}
	
	private boolean checkRight(String nature, int pos, ArrayList<EWord> words){
		boolean describeOnRight = false;
		for (int i = 0; i < rightNodes.length; i++) {
			//TODO 处理特征
			if (rightNodes[i].getType() == ItemType.NATURE && rightNodes[i].getContent().equals(nature)) {
				describeOnRight = true;
				break;
			}
		}
		if (describeOnRight) {
			if (pos > 0) {
				for (int i = pos-1; i >= 0; i--) {
					Term term = (Term) words.get(i);
					for (int j = 0; j < leftNodes.length; j++) {
						boolean passed = isTermMatchCSSide(term, leftNodes[j]);
						if (passed) {
							return true;
						}
					}
					boolean isAuxilary = isAuxilary(term, true);
					if (!isAuxilary) {
						break;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return csExpress;
	}
}
