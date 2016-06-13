package com.yiyihealth.ds.esearch.fol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Set;

import com.yiyihealth.ds.esearch.utils.ArrayUtil;
import com.yiyihealth.nlp.deepstruct.analysis.sslogic.SimpleEvidenceSearcher.WordPos;


public class FolNetwork {
	
	private boolean haveNewAtom = false;
	
	private Hashtable<String, Atom> allAtoms = new Hashtable<String, Atom>();
	
	private MemDB memDB;
	
	private AtomManager atomManager = new AtomManager();
	
	private ArrayList<Formula> globalBuildinFormulas = new ArrayList<Formula>();
	
	@SuppressWarnings("serial")
	private Hashtable<String, Hashtable<Object, Atom>> allCategories = new Hashtable<String, Hashtable<Object, Atom>>(){
		public synchronized Hashtable<Object,Atom> get(Object key) {
			Hashtable<Object, Atom> result = super.get(key);
			if (result == null) {
				result = new Hashtable<Object, Atom>();
				super.put((String)key, result);
			}
			return result;
		};
	};
	
	/**
	 * 不确定的原子，每一轮结束后进行仲裁，筛选除最合理的, 仅限于对为止的比较
	 */
	private ArrayList<Atom> unsureAtoms = new ArrayList<Atom>();
	
	/**
	 * 需要去重的事实, 所有伦次结束后去重，再重启推理
	 */
	private ArrayList<Atom> repeatAtoms = new ArrayList<Atom>();
	
	/**
	 * 取最短的事实, 所有伦次结束后去长，再重启推理
	 */
	private ArrayList<Atom> compareLongAtoms = new ArrayList<Atom>();
	
	/**
	 * 取最长的事实, 所有伦次结束后去短，再重启推理
	 */
	private ArrayList<Atom> compareShortAtoms = new ArrayList<Atom>();
	
	/**
	 * 排比事实, 所有伦次结束后去重，再重启推理
	 */
	private ArrayList<Atom> paibiAtoms = new ArrayList<Atom>();
	
	/**
	 * 公式和原子的对应关系 = 公式 : 谓词 : 参数 : 原子
	 */
	@SuppressWarnings("serial")
	private Hashtable<Formula, Hashtable<String, Hashtable<String, Atom>>> netData = new Hashtable<Formula, Hashtable<String, Hashtable<String, Atom>>>(){
		public synchronized Hashtable<String, Hashtable<String, Atom>> get(Object key) {
			Hashtable<String, Hashtable<String, Atom>> result = super.get(key);
			if (result == null) {
				result = new Hashtable<String, Hashtable<String, Atom>>(){
					public synchronized Hashtable<String,Atom> get(Object key) {
						Hashtable<String,Atom> res = super.get(key);
						if (res == null) {
							res = new Hashtable<String,Atom>();
							super.put((String) key, res);
						}
						return res;
					};
				};
				super.put((Formula)key, result);
			}
			return result;
		};
	};
	
	public Hashtable<String, Atom> getAllAtoms() {
		return allAtoms;
	}
	
	/**
	 * 建立谓词和公式的对应关系
	 */
	@SuppressWarnings("serial")
	private Hashtable<String, ArrayList<Formula>> formulasMap = new Hashtable<String, ArrayList<Formula>>(){
		public synchronized ArrayList<Formula> get(Object key) {
			ArrayList<Formula> result = super.get(key);
			if (result == null) {
				result = new ArrayList<Formula>();
				super.put((String)key, result);
			}
			return result;
		};
	}; 
	
	public MemDB getMemDB() {
		return memDB;
	}
	
	public FolNetwork(ArrayList<Formula> formulas, String dbFile) {
		memDB = MemDB.load(dbFile);
		for (int i = 0; i < formulas.size(); i++) {
			Formula formula = formulas.get(i);
			for (int j = 0; j < formula.getPredicates().size(); j++) {
				ArrayList<Formula> fs = formulasMap.get(formula.getPredicates().get(j).getName());
				if (!fs.contains(formula)) {
					fs.add(formula);
				}
			}
			addIfGlobalFormula(formula);
		}
	}
	
	public AtomManager getAtomManager() {
		return atomManager;
	}
	
	/**
	 * 对于全局的，只有内置谓词的公式需要单独调用，因为没有任何事实而导致推理不会进行
	 * @param formula
	 */
	private void addIfGlobalFormula(Formula formula){
		if (formula.getPredicates().size() == 2 && formula.getPredicates().get(0).isBuildin()) {
			if (!formula.getPredicates().get(0).isBlobalBuildin()) {
				throw new RuntimeException(formula.getPredicates().get(0) + "不是全局内置谓词!");
			}
			if (!globalBuildinFormulas.contains(formula)) {
				globalBuildinFormulas.add(formula);
			}
		}
	}
	
	public void addFormula(Formula formula){
		boolean added = false;
		for (int j = 0; j < formula.getPredicates().size(); j++) {
			ArrayList<Formula> fs = formulasMap.get(formula.getPredicates().get(j).getName());
			if (!fs.contains(formula)) {
				fs.add(formula);
				added = true;
			}
		}
		if (added) {
			//netData重载了get，会新建一个数据
			netData.get(formula);
			Collection<Atom> vs = allAtoms.values();
			ArrayList<String> predicates = new ArrayList<String>();
			for (int j = 0; j < formula.getPredicates().size(); j++) {
				predicates.add(formula.getPredicates().get(j).getName());
			}
			for(Atom atom : vs){
				if (predicates.contains(atom.predicateName)) {
					Hashtable<String, Hashtable<String, Atom>> atoms = netData.get(formula);
					Hashtable<String, Atom> atomMap = atoms.get(atom.predicateName);
					atomMap.put(atom.getParamsKey(), atom);
				}
			}
			haveNewAtom = true;
		}
		addIfGlobalFormula(formula);
	}
	
	/**
	 * 查找排比句式
	 * @return
	 */
	boolean combinePaitis(){
		if (paibiAtoms.size() == 0) {
			return false;
		}
		Hashtable<String, ArrayList<Atom>> paibiMaps = new Hashtable<>();
		for(Atom atom : paibiAtoms){
			ArrayList<Atom> atoms = paibiMaps.get(atom.getPredicateName());
			if (atoms == null) {
				atoms = new ArrayList<>();
				paibiMaps.put(atom.getPredicateName(), atoms);
			}
			atoms.add(atom);
		}
		ArrayList<Atom> newPaibiAtoms = new ArrayList<>();
		Set<String> keys = paibiMaps.keySet();
		for(String key : keys){
			ArrayList<Atom> atoms = paibiMaps.get(key);
			atoms.sort(new Comparator<Atom>() {
				@Override
				public int compare(Atom o1, Atom o2) {
					return o1.intParams[0] - o2.intParams[0];
				}
			});
			while(atoms.size() > 0){
				Atom atom = atoms.remove(0);
				//构建一个新的Atom, 来囊括所有相连的排比
				//TODO 应该有一个全局的地方管理predicate，并验证predicate语法, 目前暂时在这里生成一个新的Predicate
				Predicate predicate = new Predicate();
				Predicate predicateWithStar = PredicateManager.getInstance().getPredicateByName(atom.predicateName);
				String newPredicateNameWithoutStar = predicateWithStar.getName().substring(1);
				predicate.setName(newPredicateNameWithoutStar);
				predicate.addParam(predicateWithStar.getParams());
				
				//TODO 验证前是否都是合法的排比候选, 另外，排比的调试树没有建立
				Atom paibiAtom = new Atom(atomManager, predicate, atom.cloneParams(), atom.isTrue, null, null);
				newPaibiAtoms.add(paibiAtom);
				ArrayList<Atom> needRemoved = new ArrayList<>();
				//在剩下的所有Atom中寻找有交集的排比候选
				for (int i = 0; i < atoms.size(); i++) {
					Atom atomTest = atoms.get(i);
					if (paibiAtom.intParams[0] > paibiAtom.intParams[1] || atomTest.intParams[0] > atomTest.intParams[1]) {
						throw new RuntimeException("排比句式必须开始位置放在参数0的位置，结束位置放在参数1的位置！" + paibiAtom + ", " + atomTest);
					}
					if (atomTest.intParams[0] >= paibiAtom.intParams[0] && atomTest.intParams[0] <= paibiAtom.intParams[1] 
						|| atomTest.intParams[1] >= paibiAtom.intParams[0] && atomTest.intParams[1] <= paibiAtom.intParams[1]) {
						//有交集，是排比
						//其他参数一致
						for (int j = 2; j < paibiAtom.getParams().size(); j++) {
							if (!paibiAtom.getParams().get(j).equals(atomTest.getParams().get(j))) {
								throw new RuntimeException("排比谓词的除位置参数外的其他参数必须一致!" + paibiAtom + ", " + atomTest);
							}
						}
						int left = Math.min(paibiAtom.intParams[0], atomTest.intParams[0]);
						int right = Math.max(paibiAtom.intParams[1], atomTest.intParams[1]);
						paibiAtom.intParams[0] = left;
						paibiAtom.intParams[1] = right;
						paibiAtom.updateParam(0, ""+left);
						paibiAtom.updateParam(1, ""+right);
						needRemoved.add(atomTest);
					}
				}
				atoms.removeAll(needRemoved);
			}
		}
		for(Atom atom : newPaibiAtoms){
			addAtom(atom);
		}
		boolean exists = paibiAtoms.size() > 0;
		paibiAtoms.clear();
		return exists;
	}
	
	/**
	 * 检测atomOrg是否被container包含
	 * @param atomOrg
	 * @param container
	 * @return
	 */
	private boolean isContainsRepeat(Atom atomOrg, Atom container){
		boolean res = true;
		for (int i = 0; i < container.params.size(); i++) {
			if (!container.params.get(i).equals(atomOrg.getParams().get(i))) {
				if (!atomOrg.getParams().get(i).equals("null")) {
					res = false;
					break;
				}
			}
		}
		return res;
	}
	
	/**
	 * 是否只有一个参数不同
	 * @param atom
	 * @param rlAtom
	 * @return - -1: 没有参数不同，>=0否则表示有该唯一位置不同
	 */
	private boolean compareOthersDif(final int compareIndex, Atom atom, Atom rlAtom){
		if (atom.getParams().size() < 2) {
			throw new RuntimeException("去长事实的Atom参数个数必须>=2! " + atom);
		}
//		if (atom.intParams[atom.intParams.length-1] != compareIndex || atom.intParams[atom.intParams.length-1]) {
//			
//		}
		Object[] aParams = new Object[atom.getParams().size()];
		Object[] rlparams = new Object[rlAtom.getParams().size()];
		atom.getParams().toArray(aParams);
		rlAtom.getParams().toArray(rlparams);
		boolean result = true;
		//最后一位是位置
		for(int i=0; i<aParams.length - 1; i++){
			if (i != compareIndex) {
				String a = aParams[i].toString();
				String b = rlparams[i].toString();
				if (!a.equals(b)) {
					result = false;
					break;
				}
			}
		}
		return result;
	}
	
	private String getKeyOfAtomWithOneDif(Atom atom, int difIndex){
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < atom.getParams().size(); i++) {
			if (i == difIndex) {
				buffer.append("_");
			} else {
				buffer.append(atom.getParams().get(i));
			}
		}
		return buffer.toString();
	}
	
	/**
	 * 去掉只有同一个位置参数不同的较短的谓词
	 * @return
	 */
	boolean removeShortAtoms(){
		return removeLongerOrShortAtoms(compareShortAtoms, false);
	}
	
	/**
	 * 去掉只有同一个位置参数不同的较短的谓词
	 * @return
	 */
	boolean removeLongerAtoms(){
		return removeLongerOrShortAtoms(compareLongAtoms, true);
	}
	
	/**
	 * 去掉只有同一个位置参数不同的较短的谓词
	 * @return
	 */
	boolean removeLongerOrShortAtoms(ArrayList<Atom> comareAtoms, boolean longer){
		if (comareAtoms.size() == 0) {
			return false;
		}
		ArrayList<Atom> finalAtoms = new ArrayList<>();
		ArrayList<Atom> rlAtoms = new ArrayList<>(comareAtoms);
		Hashtable<String, ArrayList<Atom>> groupAtoms = new Hashtable<>();
		boolean[] groupeds = new boolean[rlAtoms.size()];
		Hashtable<String, Integer> indexs = new Hashtable<>();
		final int compareIndex = comareAtoms.get(0).intParams[comareAtoms.get(0).intParams.length-1];
		if (compareIndex == Integer.MAX_VALUE) {
			throw new RuntimeException("去长的最后一个参数必须是整数：参数位置信息, " + comareAtoms.get(0));
		}
		//分组
		for(int i=0; i<comareAtoms.size(); i++){
			Atom atom = comareAtoms.get(i);
			for(int j=0; j<rlAtoms.size(); j++){
				if (i != j) {
					Atom rlAtom = rlAtoms.get(j);
					boolean whichOneDif = compareOthersDif(compareIndex, atom, rlAtom);
					if (whichOneDif) {
						//只有一个不同
						String key = getKeyOfAtomWithOneDif(atom, compareIndex);
						ArrayList<Atom> group = groupAtoms.get(key);
						if (group == null) {
							group = new ArrayList<Atom>();
							groupAtoms.put(key, group);
						}
						group.add(atom);
						groupeds[i] = true;
						indexs.put(key, compareIndex);
						break;
					}
				}
			}
		}
		Set<String> keys = groupAtoms.keySet();
		for(String key : keys){
			ArrayList<Atom> atoms = groupAtoms.get(key);
			int whichOneDif = indexs.get(key);
			int lenIdx = -1;
			int minmaxLen = longer ? Integer.MAX_VALUE : -1;
			for (int i = 0; i < atoms.size(); i++) {
				int lastMinMax = minmaxLen;
				if (longer) {
					minmaxLen = Math.min(minmaxLen, atoms.get(i).getParams().get(whichOneDif).toString().length());
				} else {
					minmaxLen = Math.max(minmaxLen, atoms.get(i).getParams().get(whichOneDif).toString().length());
				}
				if (longer) {
					if (minmaxLen < lastMinMax) {
						lenIdx = i;
					}
				} else {
					if (minmaxLen > lastMinMax) {
						lenIdx = i;
					}
				}
			}
			if (lenIdx != -1) {
				Atom maxLenAtom = atoms.get(lenIdx);
				finalAtoms.add(maxLenAtom);
			} else {
				throw new RuntimeException("这一组事实去长错误: " + atoms);
			}
		}
		
		for (int i = 0; i < groupeds.length; i++) {
			if (!groupeds[i]) {
				finalAtoms.add(comareAtoms.get(i));
			}
		}
		
		for(Atom atom : finalAtoms){
			//TODO 应该有一个全局的地方管理predicate，并验证predicate语法, 目前暂时在这里生成一个新的Predicate
			Predicate predicate = new Predicate();
			Predicate predicateWithLong = PredicateManager.getInstance().getPredicateByName(atom.predicateName);
			if (predicateWithLong == null) {
				throw new RuntimeException("去长谓词必须预定义: " + atom);
			}
			String newPredicateNameWithoutAt = predicateWithLong.getName().substring(1);
			predicate.setName(newPredicateNameWithoutAt);
			predicate.addParam(predicateWithLong.getParams());
			//去掉@生成新的Atom
			ArrayList<Object> newParams = new ArrayList<>();
			for(int i=0; i<atom.params.size() - 1; i++){
				newParams.add(atom.params.get(i));
			}
			Atom winnerAtom = new Atom(atomManager, predicate, newParams, atom.isTrue, atom.getID().getForumaId(), atom.getID().getIndexInForuma());
			addAtom(winnerAtom);
		}
		
		comareAtoms.clear();
		return finalAtoms.size() > 0;
	}
	
	boolean removeRepeatAtoms(){
		if (repeatAtoms.size() == 0) {
			return false;
		}
		Hashtable<String, ArrayList<Atom>> repeatMaps = new Hashtable<>();
		for(Atom atom : repeatAtoms){
			ArrayList<Atom> atoms = repeatMaps.get(atom.getPredicateName());
			if (atoms == null) {
				atoms = new ArrayList<>();
				repeatMaps.put(atom.getPredicateName(), atoms);
			}
			atoms.add(atom);
		}
		Set<String> keys = repeatMaps.keySet();
		for(String key : keys){
			ArrayList<Atom> atoms = repeatMaps.get(key);
			ArrayList<Atom> atomsAttackers = new ArrayList<>();
			atomsAttackers.addAll(atoms);
			Atom[] failures = new Atom[atoms.size()];
			//TODO 矛盾检测未做
			//atomsAttackers去攻击atoms里的所有元素，只要有更少的Null的，即可把更多null的去除掉
			for (int i = 0; i < atomsAttackers.size(); i++) {
				Atom attacker = atomsAttackers.get(i);
				for (int j = 0; j < atoms.size(); j++) {
					Atom atom = atoms.get(j);
					if (i == j) {
						continue;
					}
//					try {
//						int pos1 = Integer.parseInt(atom.getParams().get(0).toString());
//						int pos2 = Integer.parseInt(attacker.getParams().get(0).toString());
//						if (pos1 != pos2) {
//							break;
//						}
//					} catch (Exception e) {
//						throw new RuntimeException("位置去重的第一个位置必需是位置(数字): " + atom);
//					}
					if (isContainsRepeat(atom, attacker)) {
						failures[j] = atom;
					}
//					boolean attackOk = false;
//					boolean attackFailed = false;
//					boolean samePos = false;
//					for (int k = 0; k < atom.getParams().size(); k++) {
//						Object at = attacker.getParams().get(k);
//						Object unAt = atom.getParams().get(k);
//						if (k == 0) {
//							try {
//								int pos1 = Integer.parseInt(at.toString());
//								int pos2 = Integer.parseInt(unAt.toString());
//								samePos = pos1 == pos2;
//								if (pos1 != pos2) {
//									break;
//								}
//							} catch (Exception e) {
//								throw new RuntimeException("位置去重的第一个位置必需是位置: " + atom);
//							}
//						}
//						if (!at.equals(unAt)) {
//							if (unAt.equals("null")) {
//								attackOk = true;
//							}
//							if (at.equals(null)) {
//								attackFailed = true;
//								break;
//							}
//						}
//					}
//					if (attackOk && !attackFailed && samePos) {
//						//成功击败对手
//						failures[j] = atom;
//					}
				}
			}
			for (int i = 0; i < failures.length; i++) {
				if (failures[i] != null) {
					repeatAtoms.remove(failures[i]);
				}
			}
		}
//		repeatAtoms.sort(new Comparator<Atom>() {
//			@Override
//			public int compare(Atom o1, Atom o2) {
//				try {
//					int i1 = Integer.parseInt(o1.getParams().get(0).toString());
//					int i2 = Integer.parseInt(o2.getParams().get(0).toString());
//					return i2 > i1 ? -1 : (i2 < i1 ? 1 : 0);
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
//			}
//		});
		for(Atom atom : repeatAtoms){
			//TODO 应该有一个全局的地方管理predicate，并验证predicate语法, 目前暂时在这里生成一个新的Predicate
			Predicate predicate = new Predicate();
			Predicate predicateWithAt = PredicateManager.getInstance().getPredicateByName(atom.predicateName);
			if (predicateWithAt == null) {
				throw new RuntimeException("去重谓词必须预定义: " + atom);
			}
			String newPredicateNameWithoutAt = predicateWithAt.getName().substring(1);
			predicate.setName(newPredicateNameWithoutAt);
			predicate.addParam(predicateWithAt.getParams());
			//去掉@生成新的Atom
			Atom winnerAtom = new Atom(atomManager, predicate, atom.params, atom.isTrue, atom.getID().getForumaId(), atom.getID().getIndexInForuma());
			addAtom(winnerAtom);
		}
		boolean exists = repeatAtoms.size() > 0;
		repeatAtoms.clear();
		return exists;
	}
	
	public void addAtom(Atom atom){
		if (atom.predicateName.charAt(atom.predicateName.length()-1) == '?') {
			if (!unsureAtoms.contains(atom)) {
				unsureAtoms.add(atom);
			}
		} else if (atom.predicateName.charAt(0) == '@') {
			if (!repeatAtoms.contains(atom)) {
				repeatAtoms.add(atom);
			}
		} else if (atom.predicateName.charAt(0) == '*') {
			if (!paibiAtoms.contains(atom)) {
				paibiAtoms.add(atom);
			}
		} else if (atom.predicateName.charAt(0) == '$') {
			if (!compareLongAtoms.contains(atom)) {
				compareLongAtoms.add(atom);
			}
		} else if (atom.predicateName.charAt(0) == '&') {
			if (!compareShortAtoms.contains(atom)) {
				compareShortAtoms.add(atom);
			}
		} else {
			//TODO 处理和已知矛盾的问题
			Atom preAtom = allAtoms.get(atom.getGlobalKey());
			if (preAtom != null && preAtom.isTrue != atom.isTrue) {
				//TODO 相互矛盾，需要处理
				System.out.println("放弃处理, 和已知矛盾.....，新推断结果为: " + atom.toString());
				//临时throw
				throw new RuntimeException("临时throw");
				//return;
			}
			if (preAtom == null) {
				allAtoms.put(atom.getGlobalKey(), atom);
				allCategories.get(atom.predicateName).put(atom.getParamsKey(), atom);
				//构建map
				ArrayList<Formula> formulas = formulasMap.get(atom.predicateName);
				for (int i = 0; i < formulas.size(); i++) {
					Formula formula = formulas.get(i);
					Hashtable<String, Hashtable<String, Atom>> atoms = netData.get(formula);
					Hashtable<String, Atom> atomMap = atoms.get(atom.predicateName);
					atomMap.put(atom.getParamsKey(), atom);
				}
				haveNewAtom = true;
			}
		}
	}
	
	public Hashtable<String, Hashtable<Object, Atom>> getAllCategories() {
		return allCategories;
	}
	
	public boolean isHaveNewAtom() {
		return haveNewAtom;
	}
	
	public ArrayList<Formula> getFormulasByPredicateName(String predicateName){
		return formulasMap.get(predicateName);
	}
	
	public void inherence(){
		haveNewAtom = false;
		ArrayList<Atom> provedAtoms = new ArrayList<Atom>();
		ArrayList<Formula> formulas = new ArrayList<Formula>();
		if (globalBuildinFormulas.size() > 0) {
			formulas.addAll(globalBuildinFormulas);
			//全局的运行一次就可以了
			globalBuildinFormulas.clear();
		}
		for (Formula formula : netData.keySet()) {
			formulas.add(formula);
		}
		for (Formula formula : formulas) {
			long startTime = System.currentTimeMillis();
			//System.out.println("formula: " + formula);
			int[][] samePramas = formula.getSameNameParamIdx();
			Hashtable<String, Hashtable<String, Atom>> categories = netData.get(formula);
			ArrayList<Predicate> predicates = formula.getPredicates();
			Atom[][] inAtoms = new Atom[predicates.size()][];
			//最后一个是待求证谓词, 不加入cross product
			for(int i=0; i<predicates.size() - 1; i++){
				Predicate predicate = predicates.get(i);
				Hashtable<String, Atom> atoms = categories.get(predicate.getName());
				Atom[] ats = new Atom[atoms.size()];
				atoms.values().toArray(ats);
				inAtoms[i] = ats;
			}
			inAtoms[inAtoms.length - 1] = new Atom[0];
			int[] sizes = new int[inAtoms.length];
			for (int i = 0; i < sizes.length; i++) {
				sizes[i] = inAtoms[i].length;
			}
			int[] lastCrossMax = new int[sizes.length];
			System.arraycopy(formula.getLastCrossMax(), 0, lastCrossMax, 0, sizes.length);
			CrossProduct cp = new CrossProduct(lastCrossMax, sizes);
			System.arraycopy(sizes, 0, formula.getLastCrossMax(), 0, sizes.length);
			int[] seq = cp.nextSequence();
			while (seq != null) {
				//System.out.println("cp: " + cp.toString());
				//获取参数
				Object[] params = new Object[samePramas.length];
				int paramIndex = 0;
				//TODO 临时为了逻辑简单，最后一项默认为推理项, 所以不加参数
				for (int i = 0; i < seq.length - 1; i++) {
					if (seq[i] > 0) {
						ArrayList<Object> ps = inAtoms[i][seq[i]-1].getParams();
						for (int j = 0; j < ps.size(); j++) {
							params[paramIndex++] = ps.get(j);
						}
					} else {
						paramIndex += predicates.get(i).getParams().size();
					}
				}
				//最后一个是需要证明的谓词，所以不计算其参数
				if (paramIndex != params.length - predicates.get(predicates.size()-1).getParams().size()) {
					throw new RuntimeException("参数数目错误: " + formula + ", " + ArrayUtil.Arr2String(params));
				}
				//首先验证参数是否合法
				boolean isParamLegal = true;
				OUT:
				for (int i = 0; i < samePramas.length; i++) {
					if (samePramas[i] != null) {
						Object param = params[i];
						if (param != null) {
							for (int j = 0; j < samePramas[i].length; j++) {
								Object paramCompare = params[samePramas[i][j]];
								if (paramCompare != null) {
									if (!paramCompare.equals(param)) {
										//这个不合法，参数不可比较, 不进行推理
										isParamLegal = false;
										//System.out.println("参数不合法，不进行推理: paramCompare: " + samePramas[i][j] + " = " + paramCompare + ", param: " + i + " = "+ param + ", " + formula + ", " + ArrayUtil.Arr2String(params));
										break OUT;
									}
								} 
								//TODO 下面验证有逻辑错误，sizes[samePramas[i][j]] 中 samePramas[i][j] 不是 index
//								else if (sizes[samePramas[i][j]] > 0) {
//									throw new RuntimeException("必须提供参数，位于: " + samePramas[i][j] + ", " + formula + ", " + ArrayUtil.Arr2String(params));
//								}
							}
						}
						//TODO 下面验证有逻辑错误，sizes[samePramas[i][j]] 中 samePramas[i][j] 不是 index
//						else if(sizes[i] > 0){
//							throw new RuntimeException("必须提供参数，位于: " + i + ", " + formula + ", " + ArrayUtil.Arr2String(params));
//						}
					}
				}
				if (isParamLegal) {
					//System.out.println("参数合法，进行推理: paramCompare: " + formula + ", " + ArrayUtil.Arr2String(params));
					//可以进行推理了
					Atom provedAtom = caculateFormula(formula, seq, inAtoms, samePramas, params);
					if (provedAtom != null && allAtoms.get(provedAtom.getGlobalKey()) == null) {
						//System.out.println("新得证: " + provedAtom + ", cp: " + cp.toString());
						provedAtoms.add(provedAtom);
					}
				}
				seq = cp.nextSequence();
			}
			System.out.println("spent time: " + (System.currentTimeMillis() - startTime) + ", " + formula);
		}
		if (provedAtoms.size() > 0) {
			for(Atom atom : provedAtoms){
				addAtom(atom);
			}
		}
		if (unsureAtoms.size() > 0) {
			//TODO 目前仅针对肯定谓词，否定谓词待议
			//筛选最合理里，目前按最近原则
			//如果其中某一个参数竞争失败，则整条规则竞争失败
			Atom[] uatoms = new Atom[unsureAtoms.size()];
			unsureAtoms.toArray(uatoms);
			//初始化一个16宽度的计算数组，如果不够用需动态增加
			int[][] peakPoses = new int[memDB.getWordsSize()][16];
			//每个位置参与竞争的原子个数
			int[] peakPoseLens = new int[memDB.getWordsSize()];
			for (int j = 0; j < uatoms.length; j++) {
				//TODO 默认带？的谓词只有整数参数, 如果以后有其他情况需要修改代码
				for (int i = 1; i < uatoms[j].intParams.length; i++) {
					if (uatoms[j].nullParams[i]) {
						//null参数不参与竞争
						continue;
					}
					int peakPos = uatoms[j].intParams[i];
					if (peakPoseLens[peakPos] >= peakPoses[peakPos].length) {
						int[] tmp = new int[peakPoses[peakPos].length + 16];
						System.arraycopy(peakPoses[peakPos], 0, tmp, 0, peakPoses[peakPos].length);
						peakPoses[peakPos] = tmp;
					}
					//j参与这个竞争
					peakPoses[peakPos][peakPoseLens[peakPos]++] = j;
				}
			}
			boolean[] wins = new boolean[uatoms.length];
			//默认都是胜利者，但只要有一个宣称其失败，其被判定为失败
			for (int i = 0; i < wins.length; i++) {
				wins[i] = true;
			}
			Atom[] defeatsBy = new Atom[unsureAtoms.size()];
			//开始竞争
			for (int i = 0; i < peakPoseLens.length; i++) {
				//是否有人竞争
				if (peakPoseLens[i] > 0) {
					int peakPos = i;
					int minDis = Integer.MAX_VALUE;
					int winner = -1;
					int winnerIndex = -1;
					for (int j = 0; j < peakPoseLens[i]; j++) {
						int last = minDis;
						minDis = Math.min(minDis, Math.abs(uatoms[peakPoses[peakPos][j]].intParams[0] - peakPos));
						if (last != minDis) {
							winner = peakPoses[peakPos][j];
							winnerIndex = j;
						} else if (minDis != Integer.MAX_VALUE){
							//如果相同，判定有null多者失败
							int peakWinner = peakPoses[peakPos][j];
							if (uatoms[peakWinner].nullParamsCounter < uatoms[winner].nullParamsCounter) {
								winner = peakWinner;
								winnerIndex = j;
							}
						}
					}
					for (int j = 0; j < peakPoseLens[i]; j++) {
						if (j != winnerIndex) {
							//被判定失败
							wins[peakPoses[peakPos][j]] = false;
							defeatsBy[peakPoses[peakPos][j]] = uatoms[winner];
						}
					}
					if (winner == -1) {
						throw new RuntimeException("必定有winner，竞争计算有逻辑问题！");
					}
				}
			}
			ArrayList<Atom> winnerAtoms = new ArrayList<Atom>();
			for (int i = 0; i < wins.length; i++) {
				if (wins[i]) {
					//TODO 应该有一个全局的地方管理predicate，并验证predicate语法, 目前暂时在这里生成一个新的Predicate
					Predicate predicate = new Predicate();
					Predicate predicateWithQ = PredicateManager.getInstance().getPredicateByName(uatoms[i].predicateName);
					String newPredicateNameWithoutQ = predicateWithQ.getName().substring(0, uatoms[i].predicateName.length() - 1);
					predicate.setName(newPredicateNameWithoutQ);
					predicate.addParam(predicateWithQ.getParams());
					
					//去掉问号生成新的Atom
					Atom winnerAtom = new Atom(atomManager, predicate, uatoms[i].params, uatoms[i].isTrue, null, null);
					winnerAtoms.add(winnerAtom);
				} else {
					System.out.println("被判定竞争失败者: " + uatoms[i] + ", 被： " + defeatsBy[i] + "击败!");
				}
			}
			//判断同一个位置发出的竞争是否得到多个事实，如果得到多个，取最近一个
			Hashtable<String, ArrayList<Atom>> winMultiple = new Hashtable<String, ArrayList<Atom>>();
			for(Atom atom : winnerAtoms){
				String key = atom.predicateName + "_" + atom.getParams().get(0);//第一个位置是发起的位置
				ArrayList<Atom> muList = winMultiple.get(key);
				if (muList == null) {
					muList = new ArrayList<Atom>();
					winMultiple.put(key, muList);
				}
				muList.add(atom);
			}
			Set<String> keys = winMultiple.keySet();
			for (String key : keys) {
				ArrayList<Atom> muList = winMultiple.get(key);
				if (muList.size() > 0) {
					int minDis = Integer.MAX_VALUE;
					int finalWinnerIndex = 0;
					for(int fidx = 0; fidx < muList.size(); fidx++){
						Atom atom = muList.get(fidx);
						//第一位是自己的位置，所以不计算
						for (int i = 1; i < atom.nullParams.length; i++) {
							if (!atom.nullParams[i]) {
								int min = Math.min(minDis, atom.intParams[i]);
								if (min != minDis) {
									//更近的距离
									finalWinnerIndex = fidx;
								}
							}
						}
					}
					addAtom(muList.get(finalWinnerIndex));
				} else {
					addAtom(muList.get(0));
				}
			}
			//清空竞争表
			unsureAtoms.clear();
		}
	}
	
	private Atom caculateFormula(Formula formula, int[] seq, Atom[][] inAtoms, int[][] samePramas, Object[] atomParams){
		ArrayList<Predicate> predicates = formula.getPredicates();
		boolean hasTrue = false;
		boolean broke = false;
		int paramOffset = 0;
		//支持从数据里查询得到新的数据
		Hashtable<Object, Object> searchedParams = null;
		Atom[] cpAtoms = new Atom[seq.length - 1];//参与运算的Atoms
		String[] reasonAtomIds = new String[seq.length];//推论塑因原子的id
		for (int i = 0; i < seq.length - 1; i++) {
			Predicate predicate = predicates.get(i);
			//seq[i] > 0表示有原子
			if (seq[i] > 0) {
				Atom atom = inAtoms[i][seq[i]-1];
				cpAtoms[i] = atom;
				reasonAtomIds[i] = atom.getID().getId();
				hasTrue |= predicate.isWithNot() ? (!atom.isTrue) : atom.isTrue;
			} else if(predicate.isBuildin()) { 
				if (predicate.getName().equals(Predicate.BUILDIN_DATEAFTER) || predicate.getName().equals(Predicate.BUILDIN_FOLLOW) || predicate.getName().equals(Predicate.BUILDIN_AFTER) || predicate.getName().equals(Predicate.BUILDIN_AFTERWITHIN)) {
					int varSize = 2;
					Object[] vars = new Object[varSize];
					for(int m=0; m<varSize; m++){
						int poffset = paramOffset + m;
						if(samePramas[poffset] == null){
							throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_FOLLOW + ", " + Predicate.BUILDIN_AFTER + ", " + Predicate.BUILDIN_DATEAFTER) + " 需要的参数必须在其他谓词中出现, " + formula);
						} else {
							for (int j = 0; j < samePramas[poffset].length; j++) {
								if (poffset != samePramas[poffset][j] && (poffset + 1) != samePramas[poffset][j]) {
									vars[m] =  atomParams[samePramas[poffset][j]];
									break;
								}
							}
						}
						if (vars[m] == null) {
							throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_FOLLOW + ", " + Predicate.BUILDIN_AFTER + ", " + Predicate.BUILDIN_DATEAFTER) + " 需要的参数必须在其他谓词中出现, " + formula);
						}
					}
					try {
						int i1 = Integer.parseInt((String)vars[0]);
						int i2 = Integer.parseInt((String)vars[1]);
						if (predicate.getName().equals(Predicate.BUILDIN_AFTERWITHIN)) {
							//BUILDIN_AFTERWITHIN
							int i3 = Integer.parseInt((String)predicate.getParams().get(2));
							if (i3 <= 0) {
								throw new RuntimeException("AfterWithin must >= 1");
							}
							int afterNum = i3;
							boolean cres = (i2 - i1 <= afterNum && i2 - i1 > 0);
							hasTrue |= (predicate.isWithNot() ? !cres : cres); 
						} else if (predicate.getName().equals(Predicate.BUILDIN_AFTER)) {
							boolean cres = (i1 < i2);
							hasTrue |= (predicate.isWithNot() ? !cres : cres);
						} else if (predicate.getName().equals(Predicate.BUILDIN_DATEAFTER)) {
							boolean cres = memDB.dateAfter(i1, i2);
							hasTrue |= (predicate.isWithNot() ? !cres : cres);
						} else {
							boolean cres = (i1 + 1 == i2);
							hasTrue |= (predicate.isWithNot() ? !cres : cres); 
						}
					} catch (Exception e) {
						throw new RuntimeException(formula.toString(), e);
					}
				} else if(predicate.getName().equals(Predicate.BUILDIN_CONTAINS) 
						|| predicate.getName().equals(Predicate.BUILDIN_STARTSWITH)
						|| predicate.getName().equals(Predicate.BUILDIN_ENDSWITH)
						|| predicate.getName().equals(Predicate.BUILDIN_EQUALS)
						|| predicate.getName().equals(Predicate.BUILDIN_LARGETHAN)
						|| predicate.getName().equals(Predicate.BUILDIN_LESSTHAN)){	
					String var = null;
					if(samePramas[paramOffset] == null){
						throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_CONTAINS + "等字符串内置函数需要的参数必须在其他谓词中出现, " + formula);
					} else {
						for (int j = 0; j < samePramas[paramOffset].length; j++) {
							if (paramOffset != samePramas[paramOffset][j] && (paramOffset + 1) != samePramas[paramOffset][j]) {
								var = (String) atomParams[samePramas[paramOffset][j]];
								break;
							}
						}
					}
					if (var == null) {
						throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_CONTAINS + "等字符串内置函数需要的参数必须在其他谓词中出现, " + formula);
					}
					String text = (String) predicate.getParams().get(1);//第二个参数用来比较
					if (predicate.getName().equals(Predicate.BUILDIN_CONTAINS)) {
						hasTrue |= (predicate.isWithNot() ? !var.contains(text) : var.contains(text));
					} else if (predicate.getName().equals(Predicate.BUILDIN_STARTSWITH)) {
						hasTrue |= (predicate.isWithNot() ? !var.startsWith(text) : var.startsWith(text));
					} else if (predicate.getName().equals(Predicate.BUILDIN_ENDSWITH)) {
						hasTrue |= (predicate.isWithNot() ? !var.endsWith(text) : var.endsWith(text));
					} else if (predicate.getName().equals(Predicate.BUILDIN_EQUALS)) {
						hasTrue |= (predicate.isWithNot() ? !var.equals(text) : var.equals(text));
					}  else if (predicate.getName().equals(Predicate.BUILDIN_LARGETHAN)) {
						hasTrue |= (predicate.isWithNot() ? !(var.compareTo(text) > 0) : (var.compareTo(text) > 0));
					} else if (predicate.getName().equals(Predicate.BUILDIN_LESSTHAN)) {
						hasTrue |= (predicate.isWithNot() ? !(var.compareTo(text) < 0) : (var.compareTo(text) < 0));
					} else {
						throw new RuntimeException(formula.toString() + "内置函数：" + predicate.getName() + " not implemented yet!");
					}
				} else if(predicate.getName().equals(Predicate.BUILDIN_EQUALSATTR) || predicate.getName().equals(Predicate.BUILDIN_HASATTR)) {
					String var = null;
					if(samePramas[paramOffset] == null){
						throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_EQUALSATTR + "或" + Predicate.BUILDIN_HASATTR + " 需要的参数必须在其他谓词中出现, " + formula);
					} else {
						for (int j = 0; j < samePramas[paramOffset].length; j++) {
							if (paramOffset != samePramas[paramOffset][j] && (paramOffset + 1) != samePramas[paramOffset][j]) {
								var = (String) atomParams[samePramas[paramOffset][j]];
								break;
							}
						}
					}
					if (var == null) {
						throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_EQUALSATTR + "或" + Predicate.BUILDIN_HASATTR + " 需要的参数必须在其他谓词中出现, " + formula);
					}
					if (predicate.getName().equals(Predicate.BUILDIN_EQUALSATTR)) {
						String attrKey = (String) predicate.getParams().get(1);//第2个参数属性key
						String attrValue = (String) predicate.getParams().get(2);//第3个参数属性值
						boolean yes = memDB.equalsAttr(var, attrKey, attrValue);
						hasTrue |= (predicate.isWithNot() ? !yes : yes);
					} else {
						String attrKey = (String) predicate.getParams().get(1);//第2个参数属性key
						boolean yes = memDB.hasAttr(var, attrKey);
						hasTrue |= (predicate.isWithNot() ? !yes : yes);
					}
				} else if(predicate.getName().equals(Predicate.BUILDIN_NOWORDBETWEEN) || predicate.getName().equals(Predicate.BUILDIN_NOWORDINSENTENCE)
						|| predicate.getName().equals(Predicate.BUILDIN_ONLYWORDBETWEEN)) {
					int paramsSize = (predicate.getName().equals(Predicate.BUILDIN_NOWORDBETWEEN) || predicate.getName().equals(Predicate.BUILDIN_ONLYWORDBETWEEN)) ? 4 : 3;
					Object[] vars = new Object[paramsSize];
					for(int m=2; m<paramsSize; m++){
						int poffset = paramOffset + m;
						if(samePramas[poffset] == null){//TODO  && !predicate.getParams().get(m).equals("null")
							throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_NOWORDBETWEEN + ", " + Predicate.BUILDIN_NOWORDINSENTENCE + ", " + Predicate.BUILDIN_ONLYWORDBETWEEN) + " 需要的参数必须在其他谓词中出现, " + formula);
						} else if(!predicate.getParams().get(m).equals("null")){
							for (int j = 0; j < samePramas[poffset].length; j++) {
								if (poffset != samePramas[poffset][j] && (poffset + 1) != samePramas[poffset][j]) {
									vars[m] =  atomParams[samePramas[poffset][j]];
									break;
								}
							}
						}
						if (vars[m] == null) {
							boolean legal = false;
							//第0, 1可以有一个为null, 表示只匹配单词或词性
							if (m == 0 && predicate.getParams().get(m).equals("null")) {
								//可以为null, 不匹配word
								legal = true;
							}
							if (m == 1 && predicate.getParams().get(m).equals("null")) {
								if (predicate.getParams().get(0).equals("null")) {
									//不能同时为null
								} else {
									legal = true;
								}
							}
							if (!legal) {
								throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_FOLLOW + " 需要的参数必须在其他谓词中出现, " + formula);
							}
						}
					}
					for (int j = 0; j < 2; j++) {
						if (predicate.getParams().get(j).equals("null")) {
							vars[j] = null;
						} else {
							vars[j] = predicate.getParams().get(j);
						}
					}
					if (vars[0] == null && vars[1] == null) {
						throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_NOWORDBETWEEN + ", " + Predicate.BUILDIN_NOWORDINSENTENCE + ", " + Predicate.BUILDIN_ONLYWORDBETWEEN) + " 前两个参数不能都为null, " + formula);
					}
					try {
						if (predicate.getName().equals(Predicate.BUILDIN_NOWORDBETWEEN) || predicate.getName().equals(Predicate.BUILDIN_ONLYWORDBETWEEN)) {
							String word = (String) vars[0];
							String nature = (String) vars[1];
							int startPos = Integer.parseInt((String) vars[2]);
							int endPos = Integer.parseInt((String) vars[3]);
							boolean yes = predicate.getName().equals(Predicate.BUILDIN_NOWORDBETWEEN) ? memDB.isNoWordBetween(word, nature, startPos, endPos) : memDB.onlyWordBetween(word, nature, startPos, endPos);
							hasTrue |= (predicate.isWithNot() ? !yes : yes);
						} else {
							String word = (String) vars[0];
							String nature = (String) vars[1];
							int sentencePos = Integer.parseInt((String) vars[2]);
							boolean yes = memDB.isNoWordSentence(word, nature, sentencePos);
							hasTrue |= (predicate.isWithNot() ? !yes : yes);
						}
					} catch (Exception e) {
						throw new RuntimeException(formula.toString() + ", 错误位于: " + predicate.toString(), e);
					}
				} else if(predicate.getName().equals(Predicate.BUILDIN_FINDFIRSTBEFORE)
						|| predicate.getName().equals(Predicate.BUILDIN_FINDFIRSTAFTER)
						|| predicate.getName().equals(Predicate.BUILDIN_FINDAROUND)) {
					String var = null;
					if(samePramas[paramOffset] == null){
						throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_FINDFIRSTBEFORE + "或" + Predicate.BUILDIN_FINDFIRSTAFTER + "或" + Predicate.BUILDIN_FINDAROUND + " 需要的参数必须在其他谓词中出现, " + formula);
					} else {
						for (int j = 0; j < samePramas[paramOffset].length; j++) {
							if (paramOffset != samePramas[paramOffset][j] && (paramOffset + 1) != samePramas[paramOffset][j]) {
								var = (String) atomParams[samePramas[paramOffset][j]];
								break;
							}
						}
					}
					if (var == null) {
						throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_FINDFIRSTBEFORE + "或" + Predicate.BUILDIN_FINDFIRSTAFTER + "或" + Predicate.BUILDIN_FINDAROUND + " 需要的参数必须在其他谓词中出现, " + formula);
					}
					try {
						int refPos = Integer.parseInt(var);
						WordPos word = memDB.findBeforeOrAfter(predicate.getName(), refPos, predicate.getParams().get(1)
								, predicate.getParams().get(2).toString(), predicate.getParams().get(3).toString()
								, predicate.getParams().get(4).toString(), predicate.getParams().get(5).toString());
						hasTrue |= (predicate.isWithNot() ? !(word != null) : (word != null));
						if (word != null) {
							searchedParams = new Hashtable<Object, Object>();
							searchedParams.put(predicate.getParams().get(6), "" + word.pos);
							searchedParams.put(predicate.getParams().get(7), word.eWord.getWord());
							searchedParams.put(predicate.getParams().get(8), word.eWord.getNature());
						}
					} catch (Exception e) {
						throw new RuntimeException(formula.toString() + " | " + predicate.toString(), e);
					}
				} else if (predicate.getName().equals(Predicate.BUILDIN_FINDMAXDATE)) {
					WordPos maxDate = memDB.findMaxDate();
					boolean find = maxDate != null;
					if (find) {
						searchedParams = new Hashtable<Object, Object>();
						searchedParams.put(predicate.getParams().get(0), "" + maxDate.pos);
						searchedParams.put(predicate.getParams().get(1), maxDate.eWord.getWord());
						searchedParams.put(predicate.getParams().get(2), maxDate.eWord.getNature());
					}
					hasTrue |= (predicate.isWithNot() ? !find : find);
				} else if (predicate.getName().equals(Predicate.BUILDIN_EXISTWORD)) {
					WordPos existsWord = memDB.findExistsWord(predicate.getParams().get(0).toString(), predicate.getParams().get(1).toString()
							, predicate.getParams().get(2).toString(), predicate.getParams().get(3).toString());
					boolean found = existsWord != null;
					if (found) {
						searchedParams = new Hashtable<Object, Object>();
						searchedParams.put(predicate.getParams().get(4), "" + existsWord.pos);
						searchedParams.put(predicate.getParams().get(5), existsWord.eWord.getWord());
						searchedParams.put(predicate.getParams().get(6), existsWord.eWord.getNature());
					}
					hasTrue |= (predicate.isWithNot() ? !found : found);
				} else if(predicate.getName().equals(Predicate.BUILDIN_FINDTEXT)) {
					int varSize = 2;
					Object[] vars = new Object[varSize];
					for(int m=0; m<varSize; m++){
						int poffset = paramOffset + m;
						if(samePramas[poffset] == null){
							if (m != 1 || m == 1 && !predicate.getParams().get(1).equals("null")) {
								throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_FINDTEXT) + " 需要的参数必须在其他谓词中出现, " + formula);
							}
						} else {
							for (int j = 0; j < samePramas[poffset].length; j++) {
								if (poffset != samePramas[poffset][j] && (poffset + 1) != samePramas[poffset][j]) {
									vars[m] =  atomParams[samePramas[poffset][j]];
									break;
								}
							}
						}
						if (vars[m] == null) {
							if (m == 0) {
								throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_FINDTEXT) + " 需要的posStart参数必须在其他谓词中出现, " + formula);
							} else if (!predicate.getParams().get(1).equals("null")) {
								throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_FINDTEXT) + " 需要的endStart参数必须在其他谓词中出现或为null, " + formula);
							}
						}
					}
					try {
						int i1 = Integer.parseInt((String)vars[0]);//start
						int i2 = Integer.MAX_VALUE;//end, of end of setence
						if (vars[1] != null && !vars[1].equals("null")) {
							i2 = Integer.parseInt((String)vars[1]);
						}
						String text = memDB.findText(i1, i2, predicate.getParams().get(2).equals("true"), predicate.getParams().get(3).equals("true"));
						boolean cres = text.length() > 0;
						hasTrue |= (predicate.isWithNot() ? !cres : cres); 
						if (cres) {
							searchedParams = new Hashtable<Object, Object>();
							searchedParams.put(predicate.getParams().get(4), text);
						}
					} catch (Exception e) {
						throw new RuntimeException(formula.toString(), e);
					}
				} else if(predicate.getName().equals(Predicate.BUILDIN_EXPRESS)) {
					Express express = predicate.getExpress();
					String[] expParams = express.getParamVarNames();
					int[] intParams = new int[expParams.length];
					boolean[] filled = new boolean[intParams.length];
					for (int j = 0; j < seq.length - 1; j++) {
						if (seq[j] > 0) {
							//ArrayList<Object> ps = inAtoms[j][seq[j]-1].getParams();
							Predicate predi = formula.getPredicates().get(j);
							for (int k = 0; k < predi.getParams().size(); k++) {
								String paramName = predi.getParams().get(k).toString();
								for (int l = 0; l < expParams.length; l++) {
									if (!filled[l] && expParams[l] != null && expParams[l].equals(paramName) && !paramName.equals("null")) {
										if (inAtoms[j][seq[j]-1].intParams[k] == Integer.MIN_VALUE) {
											throw new RuntimeException("表达式所指向的参数必须在其他谓词里出现: " + formula + ", " + predicate);
										}
										intParams[l] = inAtoms[j][seq[j]-1].intParams[k];
										filled[l] = true;
									}
								}
							}
						}
					}
					for (int j = 0; j < filled.length; j++) {
						if (!filled[j] && expParams[j] != null) {
							throw new RuntimeException("表达式里有参数不在其他谓词里: " + expParams[j] + ", " + predicate + ", " + formula);
						}
					}
					boolean isTrue = express.caculateExpress(intParams);//true;// = true;// 
					hasTrue |= (predicate.isWithNot() ? !isTrue : isTrue); 
				} else if(predicate.getName().equals(Predicate.BUILDIN_HASFEATURE)) {
					//除了第一个参数是featureName, 其他为位置参数
					int varSize = predicate.getParams().size()-1;
					Object[] vars = new Object[varSize];
					for(int m=0; m<varSize; m++){
						int poffset = paramOffset + m + 1;
						if(samePramas[poffset] == null){
							throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_HASFEATURE) + " 需要的参数必须在其他谓词中出现, " + formula);
						} else {
							for (int j = 0; j < samePramas[poffset].length; j++) {
								if (poffset != samePramas[poffset][j] && (poffset + 1) != samePramas[poffset][j]) {
									vars[m] =  atomParams[samePramas[poffset][j]];
									break;
								}
							}
						}
						if (vars[m] == null) {
							throw new RuntimeException("内置谓词: " + (Predicate.BUILDIN_HASFEATURE) + " 需要的posStart参数必须在其他谓词中出现, " + formula);
						}
					}
					try {
						int[] poses = new int[vars.length];
						for (int j = 0; j < poses.length; j++) {
							poses[j] = Integer.parseInt((String)vars[j]);
						}
						boolean has = memDB.hasFeature(predicate.getParams().get(0).toString(), poses);
						hasTrue |= (predicate.isWithNot() ? !has : has); 
					} catch (Exception e) {
						throw new RuntimeException(formula.toString(), e);
					}
				} else if(predicate.getName().equals(Predicate.BUILDIN_FINDSEQUENCE)) {//TODO
					int varSize = 1;
					Object[] vars = new Object[varSize];
					for(int m=0; m<varSize; m++){
						int poffset = paramOffset + m;
						if(samePramas[poffset] == null){
							throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_FINDSEQUENCE + " 需要的参数必须在其他谓词中出现, " + formula);
						} else {
							for (int j = 0; j < samePramas[poffset].length; j++) {
								if (poffset != samePramas[poffset][j] && (poffset + 1) != samePramas[poffset][j]) {
									vars[m] =  atomParams[samePramas[poffset][j]];
									break;
								}
							}
						}
						if (vars[m] == null) {
							throw new RuntimeException("内置谓词: " + Predicate.BUILDIN_FINDSEQUENCE  + " 需要的参数必须在其他谓词中出现, " + formula);
						}
					}
					try {
						int refPos = Integer.parseInt((String)vars[0]);
						ArrayList<String[]> words = memDB.findSequence(predicate.getName(), refPos, predicate.getParams().get(1), predicate.getParams().get(2));
						hasTrue |= (predicate.isWithNot() ? !(words != null && words.size() != 0) : (words != null  && words.size() != 0));
						if (words != null  && words.size() != 0) {
							searchedParams = new Hashtable<Object, Object>();
							
							String[] strings = words.get(0);
							for (int j2 = 1; j2 < strings.length-1; j2++) {
								searchedParams.put(predicate.getParams().get(j2+2), strings[j2]);
							}
						}
					} catch (Exception e) {
						throw new RuntimeException(formula.toString(), e);
					}
				} else {
				 	throw new RuntimeException("Unimplemented buildin function: " + predicate.getName());
				}
			} else {
				//没有原子，无法证明结果
				broke = true;
				break;
			}
			paramOffset += predicate.getParams().size();
			if (hasTrue) {
				//必须全部为false才能证明推论，所以一旦出现true则推论比不成立，所以跳出不再计算
				break;
			}
		}
		if (!broke && !hasTrue) {
			Hashtable<Object, Object> values = new Hashtable<Object, Object>();
			//得证, TODO 目前仅把最后一项作为结论项
			for (int i = 0; i < seq.length - 1; i++) {
				Predicate predicate = predicates.get(i);
				//seq[i] > 0表示有原子
				if (seq[i] > 0) {
					//TODO 需要优化，上面传了atomParams，应该可以利用
					//当前满足条件的原子
					Atom atom = inAtoms[i][seq[i]-1];
					//谓词的参数
					ArrayList<Object> preParams = predicate.getParams();
					//原子的参数（已证实数据）
					ArrayList<Object> params = atom.getParams();
					for (int j = 0; j < params.size(); j++) {
						Object param = params.get(j);
						Object preSaved = values.get(preParams.get(j));
						if (preSaved != null && !preSaved.equals(param)) {
							throw new RuntimeException("相同参数名的参数应该一致，计算前验证失败!");
						} else {
							values.put(preParams.get(j), param);
						}
					}
				}
			}
			Predicate provedPredicate = predicates.get(predicates.size() - 1);
			ArrayList<Object> newAtomParams = new ArrayList<Object>();
			for (int i = 0; i < provedPredicate.getParams().size(); i++) {
				Object key = provedPredicate.getParams().get(i);
				String keyStr = key.toString();
				boolean idChain = false;
				Object newParam = null;
				if(keyStr.indexOf('.') > 0){
					String[] keyTokens = keyStr.split("\\.");
					if (keyTokens.length > 2) {
						throw new RuntimeException("目前不支持多级id取数据, 形如a.b.c！ key : " + keyStr);
					} else if (keyTokens.length == 2) {
						idChain = true;
						Object atomID = values.get(keyTokens[0]);
						Atom atom = atomManager.getAtom(atomID);
						if (atom == null) {
							throw new RuntimeException("atomID: " + atomID + "找不到对应的事实，来自公式: " + formula);
						}
						Predicate predicate = PredicateManager.getInstance().getPredicateByName(atom.getPredicateName());
						if (predicate == null) {
							throw new RuntimeException("predicate: " + atom.getPredicateName() + "找不到对应的预定义谓词头, 来自公式: " + formula);
						}
						int paramIndex = predicate.getParams().indexOf(keyTokens[1]);
						if (paramIndex == -1) {
							throw new RuntimeException("predicate: " + atom.getPredicateName() + "找不到对应的参数" + keyTokens[1] + ", 来自公式: " + formula);
						}
						newParam = atom.getParams().get(paramIndex);
					}
				}
				if (!idChain) {
					newParam = values.get(key);
				}
				if (newParam == null && searchedParams != null) {
					newParam = searchedParams.get(key);
				}
				if (newParam == null) {
					//支持常量结论参数
					newParam = key;
					//取ID
					if (key.toString().startsWith(Predicate.STR_ID)) {
						for (int j = 0; j < cpAtoms.length; j++) {
							if (cpAtoms[j] != null && predicates.get(j).isDefinedIdName() && predicates.get(j).getIdName().equals(key.toString())) {
								newParam = cpAtoms[j].getID();
								break;
							}
						}
					}
				}
				newAtomParams.add(newParam);
			}
			
			Atom newAtom = new Atom(atomManager, provedPredicate, newAtomParams, provedPredicate.isWithNot() ? false : true, formula.getID(), reasonAtomIds);
			if (allAtoms.get(newAtom.getGlobalKey()) == null) {
				return newAtom;
			} else {
				//System.out.println("得证: " + newAtom);
				return null;
			}
		}
		return null;
	}
}

/**
 * 类似叉积，仅计算出序列
 *
 */
class CrossProduct {
	
	private int[] sizes;
	
	private int[] indexes;
	
	private boolean isFirst = true;
	
	/**
	 * 做增量叉积，否则增量推理运算量太大
	 */
	private int[] lastCrossMax;
	
	/**
	 * 当前增量的列
	 */
	private int incrementIndex = -1;
	
	public CrossProduct(int[] lastCrossMax, int... sizes) {
		this.sizes = sizes;
		this.lastCrossMax = lastCrossMax;
		indexes = new int[this.sizes.length];
		for (int i = 0; i < sizes.length; i++) {
			indexes[i] = sizes[i] == 0 ? 0 : 1;
		}
//		for (int i = 0; i < sizes.length; i++) {
//			if(sizes[i] > lastCrossMax[i]){
//				//第一个需要计数的列
//				indexes[i] = lastCrossMax[i] + 1;
//				incrementIndex = i;
//				break;
//			}
//		}
	}
	
	/**
	 * 序列从1开始，0表示该列没有原子
	 * @return
	 */
	public int[] nextSequence(){
		boolean increased = true;
		if (!isFirst) {
			increased = increase(0);
		} else {
			isFirst = false;
		}
		if(!increased){
			//结束
			return null;
		} else {
			int[] result = new int[sizes.length];
			System.arraycopy(indexes, 0, result, 0, result.length);
			return result;
		}
	}
	
	private boolean increase(int col){
		//当前列已经到了末尾
		if (indexes[col] >= sizes[col]) {
			if (incrementIndex == col) {
				//换到下一个增量列
				boolean found = false;
				for (int i = col + 1; i < sizes.length; i++) {
					if(sizes[i] > lastCrossMax[i]){
						//下一个需要计数的列
						indexes[i] = lastCrossMax[i] + 1;
						incrementIndex = i;
						found = true;
						break;
					}
				}
				if (!found) {
					//取消增量判断, 否则当前列还会被误判
					incrementIndex = -1;
				}
			}
			//当前列是不是最后一列
			if (col >= sizes.length - 1) {
				//当前列是最后一列，全部完成
				return false;
			} else {
				//这一列已经加满，往前看看可加否
				boolean r = increase(col+1);
				if (r) {
					//如果前面可加，则把当前列置为0
					indexes[col] = sizes[col] == 0 ? 0 : 1;
				}
				return r;
			}
		} else {
			indexes[col]++;
			return true;
		}
	}
	
	@Override
	public String toString() {
		ArrayList<Integer> deb = new ArrayList<Integer>();
		for (int i = 0; i < indexes.length; i++) {
			deb.add(indexes[i]);
		}
		return deb.toString();
	}
	
//	CrossProduct sequence = new CrossProduct(1, 0, 2, 3);
//	int c = 0;
//	int[] seq = sequence.nextSequence();
//	while(seq != null){
//		System.out.println(c++ + ": " + sequence.toString());
//		seq = sequence.nextSequence();
//	}
}
