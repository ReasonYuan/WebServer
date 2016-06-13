package com.yiyihealth.ds.esearch.fol;

import java.io.Serializable;
import java.util.Hashtable;

public class AtomManager implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4333095259159420797L;

	private Hashtable<Object, Atom> atoms = new Hashtable<Object, Atom>();
	
	public AtomManager() {
	}

	public void addAtom(Atom atom){
		atoms.put(atom.getID(), atom);
	}
	
	public Atom getAtom(Object id){
		return atoms.get(id);
	}
	
}
