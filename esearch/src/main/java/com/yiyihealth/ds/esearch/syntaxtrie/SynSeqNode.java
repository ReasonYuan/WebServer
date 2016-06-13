package com.yiyihealth.ds.esearch.syntaxtrie;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Set;

import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

/**
 * 语序节点, 以所有的词性都作为根节点，增加查询速度何降低查询难度
 * @author qiangpeng
 *
 */
public class SynSeqNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6954390416373510357L;

	private int depth = 0;
	
	/**
	 * 统计该节点作为叶节点出现的次数
	 */
	private int counter = 0;
	
	private String nature;
	
	private Hashtable<String, SynSeqNode> children = new Hashtable<String, SynSeqNode>();
	
	private WordSubNode wordTrieRoot = new WordSubNode();
	
	/**
	 * 该节点出现数字的统计
	 */
	private int subWordValueCnt = 0;
	
	private SynSeqNode parent;
	
	/**
	 * 在做序列匹配的时候，我自己要标志成true
	 */
	private boolean isMe = false;
	
	private String word = "";
	
	
	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public SynSeqNode(String nature) {
		this.nature = nature;
	}
	
	public SynSeqNode() {
	}
	
	public Hashtable<String, SynSeqNode> getChildren() {
		return children;
	}
	
	public boolean isMe() {
		return isMe;
	}
	
	public void setMe(boolean isMe) {
		this.isMe = isMe;
	}
	
	/**
	 * 只返回depth指定深度的“节点的children”的size, 并且参与计数的counter>minCounter
	 * @param depth
	 * @return
	 */
	public int getChildrenSize(int depth, int minCounter){
		if (this.depth == depth) {
			//return children.size();
			int sum = 0;
			Set<String> keys = children.keySet();
			for(String key : keys){
				SynSeqNode node = children.get(key);
				if (node.counter > minCounter) {
					sum++;
				}
			}
			return sum;
		} else if(this.depth < depth){
			int sum = 0;
			Set<String> keys = children.keySet();
			for(String key : keys){
				SynSeqNode node = children.get(key);
				if (node.counter > minCounter) {
					sum += node.getChildrenSize(depth, minCounter);
				}
			}
			return sum;
		}
		throw new RuntimeException("不应该运行到这里!");
	}
	
	public SynSeqNode getParent() {
		return parent;
	}

	public void setParent(SynSeqNode parent) {
		this.parent = parent;
	}

	public void addSubTrieWord(String word){
		if (nature.equals(WordNatures.getNatureFullByName(WordNatures.VALUE))) {
			subWordValueCnt++;
		} else {
			WordSubNode trieNode = (WordSubNode) wordTrieRoot.addNode(word);
			trieNode.setIsWordEnd(true);
			trieNode.increaseCounter();
		}
	}
	
	public SynSeqNode getNode(String nature){
		return children.get(nature);
	}
	
	public int getDepth() {
		return depth;
	}
	
	public int getCounter() {
		return counter;
	}
	
	public String getNature() {
		return nature;
	}
	
	/**
	 * 增加统计计数
	 */
	private void increaseCounter() {
		counter++;
	}
	
	public WordSubNode getWordTrieRoot() {
		return wordTrieRoot;
	}
	
	/**
	 * 已经有该子节点的话就只增加计数，否则增加子节点
	 * @param nature
	 * @return
	 */
	public SynSeqNode addNode(String nature,String word){
		SynSeqNode node = children.get(nature);
		if (node == null) {
			node = new SynSeqNode(nature);
			children.put(nature, node);
			node.parent = this;
			node.depth = depth + 1;
			node.word = word;
		}
		node.increaseCounter();
		return node;
	}
	
	@Override
	public String toString() {
		return getStrSeq();
	}
	
	/**
	 * 已经有该子节点的话就只增加计数，否则增加子节点
	 * @param nature
	 * @return
	 */
	public SynSeqNode addNode(String nature){
		SynSeqNode node = children.get(nature);
		if (node == null) {
			node = new SynSeqNode(nature);
			children.put(nature, node);
			node.parent = this;
			node.depth = depth + 1;
		}
		node.increaseCounter();
		return node;
	}
	
	public String getStrSeq(){
		if (getParent() == null) {
			return "";
		} else {
			String xing = isMe() ? "*" : "";
			return (getParent() == null ? "" : getParent().getStrSeq()) + "[" + getNature() + " " + getWord() +" " + xing + " "+ getCounter() +"]  ";
		}
	}
}
