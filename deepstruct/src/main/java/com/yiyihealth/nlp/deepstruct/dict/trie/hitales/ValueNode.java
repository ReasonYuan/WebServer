package com.yiyihealth.nlp.deepstruct.dict.trie.hitales;

public class ValueNode extends CharNode {
	
	private static final char TRANS_D = 4;//04 EOT (end of transmission)
	
	/**
	 * 规则里配置的＋，表示需要重复1次以上
	 */
	public static final int PLUS_GEONE = 999999999;
	
	/**
	 * 匹配次数
	 */
	private int matchTimes = 0;
	
	public int getMatchTimes() {
		return matchTimes;
	}

	@Override
	protected CharNode createNewNode() {
		return new ValueNode();
	}
	
	@Override
	public TrieNode getNode(char c) {
		if (c >= '0' && c <= '9') {
			return super.getNode(TRANS_D);
		} else {
			TrieNode node = super.getNode(c);
			if (node == null) {
				if (c == '／') {
					//中文到英文
					node = super.getNode('/');
				} else if (c == '－') {
					//中文到英文
					node = super.getNode('-');
				} else if(c == 'o' || c == 'O'){
					//TODO 完善纠正0
					if (super.getChar() == '.' && super.depth > 1) {// || forewardOneChar == '.'
						node = super.getNode(TRANS_D);
					}
				}
			}
			return node;
		}
	}
	
	@Override
	public String getTransChar() {
		if (c == TRANS_D) {
			return "\\d";
		} else {
			return super.getTransChar();
		}
	}
	
	public boolean isEqual(char c){
		if (c >= '0' && c <= '9') {
			return this.c == TRANS_D;
		} else {
			return this.c == c;
		} 
	}
	
	private int parseRepeatTimes(char[] chars, int start) {
		StringBuffer sb = new StringBuffer();
		for (int i = start; i < chars.length; i++) {
			if (i == start && chars[i] != '[') {
				//不需要匹配次数
				break;
			}
			if (chars[i] == '[') {
				sb.setLength(0);
			} else if(chars[i] == ']'){
				String str = sb.toString().trim();
				int times = 0;
				if (str.equals("+")) {
					times = PLUS_GEONE;
				} else {
					try {
						times = Integer.parseInt(str);
						if (times < 1) {
							throw new RuntimeException("重复次数必须>1");
						}
					} catch (Exception e) {
						throw new RuntimeException("数值格式配置错误!", e);
					}
				}
				return times;
			} else {
				sb.append(chars[i]);
			}
		}
		return 0;
	}
	
	@Override
	public TrieNode addNode(String key) {
		char[] chars = key.toCharArray();
		if (chars.length == 0) {
			throw new RuntimeException("key's length must > 0!");
		}
		ValueNode node = this;
		boolean ignore = false;
		boolean istransmean = false;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '[') {
				ignore = true;
			} else if (chars[i] == ']') {
				ignore = false;
			} else if(!ignore){
				if (chars[i] == '\\' && !istransmean) {
					istransmean = true;
				} else {
					char ch = chars[i];
					if (istransmean) {
						if (ch == 'd') {
							ch = TRANS_D;
						}
						istransmean = false;
					}
					//TODO 如果以后各式中有‘d’的真字符，需要增加转义功能
					node = (ValueNode) node.addChar(ch);
					node.matchTimes = parseRepeatTimes(chars, i+1);
				}
			}
		}
		return node;
	}
	
}
