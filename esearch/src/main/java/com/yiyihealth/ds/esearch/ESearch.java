package com.yiyihealth.ds.esearch;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;

import org.codehaus.plexus.util.FileUtils;

import com.yiyihealth.ds.esearch.syntaxtrie.NatureRuleMatcher;
import com.yiyihealth.ds.esearch.syntaxtrie.NatureRuleVerifer;
import com.yiyihealth.ds.esearch.syntaxtrie.SynSeqNode;
import com.yiyihealth.ds.esearch.syntaxtrie.SyntaxRule;
import com.yiyihealth.nlp.deepstruct.dict.EWord;
import com.yiyihealth.nlp.deepstruct.dict.Punctuation;
import com.yiyihealth.nlp.deepstruct.dict.WordNatures;
import com.yiyihealth.nlp.deepstruct.utils.FileManager;
import com.yiyihealth.nlp.deepstruct.utils.FileManager.OnRecordLoadListener;

public class ESearch {
	
	public static final String FULLSTOP = ""+Punctuation.FULLSTOP_C;
	
	public static final String CURRENT_TMP_NATURE = WordNatures.DATE;

	public static void main(String[] args) {
		ESearch eSearch = new ESearch();
		eSearch.loadWords4Search();
	}
	
	public void loadWords4Search(){
		
		try {
			new FileManager().loadWords("../dsdata/records", new OnRecordLoadListener(){

				public void onRecordLoad(String filename, ArrayList<EWord> recordWords) {
					ArrayList<ArrayList<EWord>> samples = new ArrayList<ArrayList<EWord>>();
					samples.add(recordWords);
					
					SynSeqNode rootNext = new SynSeqNode();
					SynSeqNode rootPre = new SynSeqNode();
					//构建词性树加字典子树
					for (int i = 0; i < samples.size(); i++) {
						ArrayList<EWord> words = samples.get(i);
						int wordSize = words.size();
						for (int j = 0; j < wordSize; j++) {
							EWord eWord = words.get(j);
							SynSeqNode subNextRoot = rootNext.addNode(eWord.getNature());
							subNextRoot.addSubTrieWord(eWord.getWord());
							SynSeqNode subPreRoot = rootPre.addNode(eWord.getNature());
							subPreRoot.addSubTrieWord(eWord.getWord());
							//前后整句遍历，构建subRoot
							SynSeqNode preCurrent = subPreRoot;
							for (int k = j-1; k >= 0; k--) {
								String wpre = words.get(k).getWord();
								String npre = words.get(k).getNature();
								if (wpre.equals(FULLSTOP) || npre.equals(WordNatures.getNatureFullByName(WordNatures.HEADING))) {
									//句末或者段落时结束循环
									break;
								}
								if (wpre == null || npre == null) {
									throw new RuntimeException("Must have nature and word!");
								}
								preCurrent = preCurrent.addNode(npre);
								preCurrent.addSubTrieWord(wpre);
							}
							SynSeqNode nextCurrent = subNextRoot;
							for (int k = j+1; k < wordSize; k++) {
								String wnext = words.get(k).getWord();
								String nnext = words.get(k).getNature();
								if (wnext.equals(FULLSTOP) || nnext.equals(WordNatures.getNatureFullByName(WordNatures.HEADING))) {
									//句末或者段落时结束循环
									break;
								}
								if (wnext == null || nnext == null) {
									throw new RuntimeException("Must have nature and word!");
								}
								nextCurrent = nextCurrent.addNode(nnext);
								nextCurrent.addSubTrieWord(wnext);
							}
						}
					}
					ArrayList<SyntaxRule> rules = searchRules(rootPre, rootNext, samples, 1);
					try {
						searchEvidences(samples, rules);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void writeQuery(ArrayList<ArrayList<EWord>> samples){
		String filename = "./mln/drug/query.db";
		BufferedOutputStream outputStream = null;
		int counter = 0;
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(new File(filename)));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
			for (int i = 0; i < samples.size(); i++) {
				ArrayList<EWord> jsonWords = samples.get(i);
				int wordSize = jsonWords.size();
				for (int j = 0; j < wordSize; j++) {
					bw.write("ShouldBeDrug(" + counter + ")");
					bw.newLine();
					counter++;
				}
			}
			bw.flush();
			bw.close();
			outputStream = null; 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e2) {
				}
			}
		}
	}
	
	private void searchEvidences(ArrayList<ArrayList<EWord>> samples, ArrayList<SyntaxRule> rules) throws Exception {
		String filename = "../dsdata/mln/drug/evidence.db";
		BufferedOutputStream outputStream = null;
		NatureRuleMatcher[] matchers = new NatureRuleMatcher[rules.size()];
		int counter = 0;
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(new File(filename)));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
			for (int i = 0; i < samples.size(); i++) {
				ArrayList<EWord> jsonWords = samples.get(i);
				int wordSize = jsonWords.size();
				for (int j = 0; j < wordSize; j++) {
					EWord wordJson = jsonWords.get(j);
					String nature = wordJson.getNature();
					String word = wordJson.getWord();
					for (int k = 0; k < matchers.length; k++) {
						if (matchers[k] == null) {
							matchers[k] = new NatureRuleMatcher(rules.get(k), WordNatures.getNatureFullByName(CURRENT_TMP_NATURE));
						}
						if(matchers[k].nextWord(word, nature)){//
							if (matchers[k].fullMatched()) {
								String seq = matchers[k].getNatureSeqAroundStr();
								bw.write("HaveNatureSeqAround(" + (counter - matchers[k].getBackOffset()) + ", " + seq + ")");
								bw.newLine();
								matchers[k].reset();
							}
						} else {
							matchers[k].reset();
						}
					}
					counter++;
				}
			}
			bw.flush();
			bw.close();
			outputStream = null; 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e2) {
				}
			}
		}
	}
	
	private ArrayList<SyntaxRule> searchRules(SynSeqNode rootPre, SynSeqNode rootNext, ArrayList<ArrayList<EWord>> samples, int loopCnt){
		
		int minCnt = 0;
		System.out.println("3: " + rootPre.getChildrenSize(3, minCnt));
		System.out.println("2: " + rootPre.getChildrenSize(2, minCnt));
		System.out.println("1: " + rootPre.getChildrenSize(1, minCnt));
		System.out.println("0: " + rootPre.getChildrenSize(0, minCnt));
		System.out.println("0: " + rootNext.getChildrenSize(0, minCnt));
		System.out.println("1: " + rootNext.getChildrenSize(1, minCnt));
		System.out.println("2: " + rootNext.getChildrenSize(2, minCnt));
		System.out.println("3: " + rootNext.getChildrenSize(3, minCnt));
		
		Hashtable<SyntaxRule, Integer> stats = new Hashtable<SyntaxRule, Integer>();
		
		Hashtable<SyntaxRule, ArrayList<String>> examples = new Hashtable<SyntaxRule, ArrayList<String>>();
		
		//查询规律出现次数
		for (int i = 0; i < samples.size(); i++) {
			ArrayList<EWord> words = samples.get(i);
			int wordSize = words.size();
			for (int j = 0; j < wordSize; j++) {
				EWord word = words.get(j);
				SynSeqNode subNextRoot = rootNext.getNode(word.getNature());
				//subNextRoot.addSubTrieWord(word.getString("word"));
				SynSeqNode subPreRoot = rootPre.getNode(word.getNature());
				//subPreRoot.addSubTrieWord(word.getString("word"));
				//前后整句遍历，构建subRoot
				SynSeqNode preCurrent = subPreRoot;
				String example = word.getWord();//sub root
				int loop = 0;
				for (int k = j-1; k >= 0; k--) {
					String wpre = words.get(k).getWord();
					String npre = words.get(k).getNature();
					if (wpre.equals(FULLSTOP) || npre.equals(WordNatures.getNatureFullByName(WordNatures.HEADING))) {
						//句末或者段落时结束循环
						break;
					}
					SynSeqNode pc = preCurrent.getNode(npre);
					if (pc == null) {
						break;
					} else {
						preCurrent = pc;
					}
					example = wpre + example;
					if (++loop >= loopCnt) {
						break;
					}
				}
				loop = 0;
				SynSeqNode nextCurrent = subNextRoot;
				for (int k = j+1; k < wordSize; k++) {
					String wnext = words.get(k).getWord();
					String nnext = words.get(k).getNature();
					if (wnext.equals(FULLSTOP) || nnext.equals(WordNatures.getNatureFullByName(WordNatures.HEADING))) {
						//句末或者段落时结束循环
						break;
					}
					SynSeqNode nc = nextCurrent.getNode(nnext);
					if (nc == null) {
						break;
					} else {
						nextCurrent = nc;
					}
					example = example + wnext;
					if (++loop >= loopCnt) {
						break;
					}
				}
				if (preCurrent.getDepth() > 1 && nextCurrent.getDepth() > 1) {
					//仅寻找两边都能找到的
					SyntaxRule rule = new SyntaxRule();
					SynSeqNode pNode = preCurrent;
					int insertIndex = 0;
					while (pNode != null && pNode.getDepth() > 0) {
						rule.addNode(insertIndex++, pNode.getNature());
						pNode = pNode.getParent();
					}
					SynSeqNode nNode = nextCurrent;
					while (nNode != null && nNode.getDepth() > 1) {
						rule.addNode(insertIndex, nNode.getNature());
						nNode = nNode.getParent();
					}
					if (stats.get(rule) == null) {
						stats.put(rule, new Integer(1));
					} else {
						stats.put(rule, stats.get(rule)+1); 
					}
					
					//for debug
					ArrayList<String> exs = examples.get(rule);
					if (exs == null) {
						exs = new ArrayList<String>();
						examples.put(rule, exs);
					}
					example = "\n" + example;
					if (!exs.contains(example)) {
						exs.add(example);
					}
					rule.setExamples(exs);
				}
			}
		}
		
		System.out.println("stats size: " + stats.size());
		Set<SyntaxRule> keys = stats.keySet();
		ArrayList<SyntaxRule> rules = new ArrayList<SyntaxRule>();
		for(SyntaxRule rule : keys){
			if (rule.containsNature(WordNatures.getNatureFullByName(ESearch.CURRENT_TMP_NATURE)) 
					&& !rule.containsNature(WordNatures.getNatureFullByName(WordNatures.UNDEF))
					&& !rule.containsNature(WordNatures.getNatureFullByName(WordNatures.UNREC))) {
				rule.setCounter(stats.get(rule));
				rules.add(rule);
			}
		}
		
		rules.sort(new Comparator<SyntaxRule>() {

			public int compare(SyntaxRule o1, SyntaxRule o2) {
				return o2.getCounter() - o1.getCounter();
			}
		});
		
		ArrayList<String> logicExpresses = new ArrayList<String>();
		int minCounter = 1;
		int minSum = 0;
		for (int i = 0; i < rules.size(); i++) {
			if(rules.get(i).getCounter() > minCounter){
				minSum++;
				SyntaxRule rule = rules.get(i);
//				System.out.println(i + ": readable rule = " + rule.getReadableRule());
//				System.out.println(i + ": cnt: " + rule.getCounter() + ", rule: " + rule.getNatureRule());
//				System.out.println(i + ": example = \n" + examples.get(rule));
				String logicExpress = rule.getNatureLogic(WordNatures.getNatureFullByName(ESearch.CURRENT_TMP_NATURE));
				logicExpress += " v ShouldBeDrug(pos)";
				if (logicExpress != null && !rule.startsWithNature(WordNatures.getNatureFullByName(WordNatures.PUNC))
						&& !logicExpresses.contains(logicExpress)) {
					logicExpresses.add(logicExpress);
				}
			}
		}
		
		ArrayList<EWord> allWords = new ArrayList<EWord>();
		for (ArrayList<EWord> words : samples) {
			allWords.addAll(words);
		}
		
		ArrayList<NatureRuleVerifer> verifers = new ArrayList<NatureRuleVerifer>();
		for (int i = 0; i < rules.size(); i++) {
			SyntaxRule rule = rules.get(i);
			NatureRuleMatcher matcher = new NatureRuleMatcher(rule, WordNatures.getNatureFullByName(ESearch.CURRENT_TMP_NATURE));
			NatureRuleVerifer verifer = new NatureRuleVerifer(matcher);
			verifer.test(rule, allWords);
			verifers.add(verifer);
		}
		
		verifers.sort(new Comparator<NatureRuleVerifer>() {
			public int compare(NatureRuleVerifer o1, NatureRuleVerifer o2) {
				int r = (int)((o2.getSucPercent() - o1.getSucPercent()) * 1000);
				if (r == 0) {
					r = o2.getSuccussMatchCnt() - o1.getSuccussMatchCnt();
				}
				return r;
			}
		});
		
		for(int i=0; i<verifers.size(); i++){
			NatureRuleVerifer verifer = verifers.get(i);
			System.out.println(String.format("%d: %3f, sucCnt: %d, failCnt: %d, logic: %s", i, verifer.getSucPercent(), verifer.getSuccussMatchCnt(), verifer.getFailMatchCnt(), verifer.getMatcher().getNatureSeqAroundStr()));
			if (verifer.getSucPercent() < 0.6) {
				//成功率太低，不是适合作为规则
				rules.remove(verifer.getMatcher().getRule());
			}
		}
		
//		for (String logic : logicExpresses) {
//			//System.out.println("logic: " + logic);
//		}
		
		System.out.println("min sum: " + minSum + ", logic size: " + logicExpresses.size());
		
		writeLogicExpress("../dsdata/mln/drug/prog.mln", logicExpresses);
		
		return rules;
	}
	
	private void writeLogicExpress(String file, ArrayList<String> logicExpresses){
		StringBuffer sBuffer = new StringBuffer();
		for(String string : logicExpresses){
			sBuffer.append(string).append("\n");
		}
		try {
			FileUtils.fileWrite(file, "utf-8", sBuffer.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
