package com.yiyihealth.nlp.deepstruct.analysis;

import java.util.ArrayList;

import com.yiyihealth.nlp.deepstruct.dict.Term;

public class PathNode {
	
	protected ArrayList<PathNode> children = new ArrayList<PathNode>();
	
	protected ArrayList<PathNode> parents = new ArrayList<PathNode>();
	
	private Term token;
	
	private PathNode(Term token) {
		this.token = token;
	}
	
	public Term getToken(){
		return token;
	}
	
	public void addToken(Term token){
		PathNode node = new PathNode(token);
		children.add(node);
		node.parents.add(this);
	}
	
}
