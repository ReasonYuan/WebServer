package com.yiyihealth.ai.dsmain.learning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

import com.alibaba.fastjson.JSONObject;
import com.yiyihealth.ai.dsmain.medicine.wx.ExcelByUser;
import com.yiyihealth.ds.esearch.syntaxtrie.SynSeqNode;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;

/**
 * 对某个词性或词的IsSequence出现频率统计
 * @author qiangpeng
 *
 */
public class SequanceStats extends EWordStats {
	
	private SynSeqNode root;
	
	public SequanceStats(String nature) {
		super(nature);
		root = new SynSeqNode(nature);
	}
	
	public SynSeqNode getRoot() {
		return root;
	}
	
	/**
	 * 过滤掉影响预测的词
	 * @param word
	 * @return
	 */
	public static boolean filterWord(EWord word){
		boolean passTest = true;
		if (word.getNature().equals(WordNatures.PUNC)) {
			//不把标点符号计算在内
			passTest = false;
		}
		return passTest;
	}
	
	public void stats(EWord[] words){
		final int preWindowNum = -6;
		final int nextWindowNum = 6;
		for (int i = 0; i < words.length; i++) {
			if (words[i].getNature().equals(nature)) {
				int preWindow = preWindowNum;
				int nextWindow = nextWindowNum;
				boolean decreaseNext = true;
				while (preWindow < 0 || nextWindow > 0) {
					SynSeqNode synSeqNode = null;
					int start = i+preWindow;
					int end = i+nextWindow+1;
					for (int j = i; j >= start && j >= 0; j--) {
						if (Punctuation.isSentenceEnd(words[j])) {
							start = j + 1;
							break;
						}
					}
					for (int j = i; j < end && j < words.length; j++) {
						if (Punctuation.isSentenceEnd(words[j])) {
							end = j;
//							System.out.println("--end1--"+end);
							break;
						}
					}
//					System.out.println("--end2--"+end);
					for(int j=start; j<end; j++){
						if (j >= 0 && j < words.length) {
							EWord statWord = words[j];
							if (filterWord(statWord)) {
								if (synSeqNode == null) {
									synSeqNode = root.addNode(statWord.getNature(),statWord.getWord());
//									System.out.println("---"+j+"---"+statWord.getWord());
								} else {
									synSeqNode = synSeqNode.addNode(statWord.getNature(),statWord.getWord());
//									System.out.println("----"+j+"---"+statWord.getWord());
								}
								synSeqNode.addSubTrieWord(statWord.getWord());
								if (synSeqNode.getStrSeq().contains("Value 0.8 4")) {
									System.out.println("dddd");
								}
							}
							if(synSeqNode != null){
								synSeqNode.setMe(j == i);
							}
							
						}
					}
					//逐个缩减窗口大小
					if (decreaseNext) {
						nextWindow--;
					} else {
						preWindow++;
					}
					decreaseNext = !decreaseNext;
					
				}
				
			}
		}
	}
	
	public void test(SynSeqNode synSeqNode,String fileName){
		if(synSeqNode != null){
			findAllNode(synSeqNode);
			
			zinodeList.sort(new Comparator<SynSeqNode>() {
				@Override
				public int compare(SynSeqNode o1, SynSeqNode o2) {
					return o2.getCounter() - o1.getCounter();
				}
				
			});
			JSONObject config;
			String projectDir = "";
			try {
				config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
				projectDir = config.getString("projectDir");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			for (SynSeqNode node : zinodeList) {
				String tmpNature = node.getStrSeq();
				try {
					ExcelByUser.writeYiJuToFile(projectDir + "/SequanceStatsResults", tmpNature,
							fileName + "S.txt");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

//			findOneTreeNodeList(zinodeList);
		}
	}
	
	private void findAllNode(SynSeqNode synSeqNode){
		zinodeList.clear();
		findLastOneList(synSeqNode);
//		System.out.println("-----zinodeList----"+zinodeList.size());	
	}
	ArrayList<SynSeqNode> zinodeList = new ArrayList<>();

	/**
	 * 找到所有子节点
	 * @param synSeqNode
	 */
	public void findLastOneList(SynSeqNode synSeqNode){
		Set<String> keys = synSeqNode.getChildren().keySet();
		Hashtable<String, SynSeqNode> children = synSeqNode.getChildren();
		if (keys.size() == 0) {
			zinodeList.add(synSeqNode);
		} else {
			for (String key : keys) {
				SynSeqNode node = children.get(key);
				System.out.println("------key------nature----"+ key + "-----" +node.getNature());
				findLastOneList(node);
			}
		}
	}
	
	/**
	 * 找到所有的子节点组成的每一个list列表
	 * @param allnodeList
	 * @throws IOException 
	 */
	public static void findOneTreeNodeList(ArrayList<SynSeqNode> allnodeList) {
		ArrayList<ArrayList<SynSeqNode>> allList = new ArrayList<>();
		for (int i = 0; i < allnodeList.size(); i++) {
			SynSeqNode node = allnodeList.get(i);
			ArrayList<SynSeqNode> list = new ArrayList<>();
			list.add(node);
			findParentNode(node,list,allList);
		}
		printList(allList);
		
	}
	
	public static void findParentNode(SynSeqNode node,ArrayList<SynSeqNode> oneList,ArrayList<ArrayList<SynSeqNode>> allList){
		SynSeqNode  parent = node.getParent();
		if(parent != null){
			oneList.add(0, parent);
			findParentNode(parent,oneList,allList);
		}else{
			allList.add(oneList);
		}
	}
	
	/**
	 * 打印遍历出来的每一个list
	 * @param lists
	 * @throws IOException 
	 */
	public static void printList(ArrayList<ArrayList<SynSeqNode>> lists){
		for (int i = 0; i < lists.size(); i++) {
			ArrayList<SynSeqNode> oneList = lists.get(i);
			oneList.sort(new Comparator<SynSeqNode>() {

				@Override
				public int compare(SynSeqNode o1, SynSeqNode o2) {
					return o2.getCounter() - o1.getCounter();
				}
			});
			oneList.remove(0);
			String tmp = "";
			String tmpNature = "";
			for (int j = 0; j < oneList.size(); j++) {
				tmp +="["+ oneList.get(j).getNature() +"   "+oneList.get(j).getWord() +"]  ";
				tmpNature +="["+ oneList.get(j).getNature() + "   "+oneList.get(j).getCounter()+"]  ";
			}
//			System.out.println(tmp);
			JSONObject config;
			try {
				config = JSONObject.parseObject(FileUtils.fileRead(new File("./conf/config.json")));
				final String projectDir = config.getString("projectDir");
				ExcelByUser.writeYiJuToFile(projectDir + "/SequanceStatsResults",tmpNature,"SequanceStatsResultsNature.txt");
				ExcelByUser.writeYiJuToFile(projectDir + "/SequanceStatsResults",tmp,"SequanceStatsResults.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
