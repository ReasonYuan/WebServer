package com.yiyihealth.ds.esearch.fol;

import java.io.Serializable;

public class AtomID implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3243420103373449609L;
	
	private final String id;
	
	private final String formulaId;
	
	private final String[] atomIds;

	public AtomID(String id, String formulaId, String[] atomIds){
		this.id = id;
		this.formulaId = formulaId;
		this.atomIds = atomIds;
	}
	
	public String getId() {
		return id;
	}
	
	public String getForumaId() {
		return formulaId;
	}
	
	public String[] getIndexInForuma() {
		return atomIds;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return id;
	}
}
