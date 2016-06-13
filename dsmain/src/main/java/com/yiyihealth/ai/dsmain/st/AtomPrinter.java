package com.yiyihealth.ai.dsmain.st;

import com.yiyihealth.ds.esearch.fol.Atom;
import com.yiyihealth.ds.esearch.fol.InherenceEngine;

public class AtomPrinter {
	
	private InherenceEngine engine;
	
	public AtomPrinter(InherenceEngine engine) {
		this.engine = engine;
	}

	public void printAtoms(){
//		printAtoms(engine.queryAtom("HaveDrugBadEffects"));
//		printAtoms(engine.queryAtom("HaveDrugGoodEffects"));
//		printAtoms(engine.queryAtom("HaveDrugJiLiangAt"));
//		printAtoms(engine.queryAtom("HaveDrugPinciAt"));
//		printAtoms(engine.queryAtom("DefaulAdmissionDate"));
//		printAtoms(engine.queryAtom("DefaulOutAdminDate"));
//		printAtoms(engine.queryAtom("RecordType"));
//		printAtoms(engine.queryAtom("ContextDate"));
//		printAtoms(engine.queryAtom("MaxDate"));
//		printAtoms(engine.queryAtom("IsNowContextDate"));
		
//		printAtoms(engine.queryAtom("HaveOutTimeWord"));
//		printAtoms(engine.queryAtom("RefOutAdminDate"));
//		printAtoms(engine.queryAtom("RefAdmissionDate"));
//		printAtoms(engine.queryAtom("DateAndHeading"));
//		printAtoms(engine.queryAtom("StopDrug"));
//		printAtoms(engine.queryAtom("IsContextDateAndHeading"));
//		printAtoms(engine.queryAtom("IsAdmission"));
//		printAtoms(engine.queryAtom("IsOutAdmin"));
//		printAtoms(engine.queryAtom("BriefWordAndHeading"));
//		printAtoms(engine.queryAtom("IsSymptom"));
//		printAtoms(engine.queryAtom("IsRegion"));
//		printAtoms(engine.queryAtom("IsRegionQ"));
//		printAtoms(engine.queryAtom("TestItemAndDes"));
//		printAtoms(engine.queryAtom("TagDate"));
//		printAtoms(engine.queryAtom("NoTimeTagDate"));
		
		printAtoms(engine.queryAtom("IsSymptom"));
		printAtoms(engine.queryAtom("PaiBiSymptom"));
		printAtoms(engine.queryAtom("TmpSymptom"));
		printAtoms(engine.queryAtom("isNoSymptom"));
		
		printAtoms(engine.queryAtom("IsSymptomAndRegion"));
		printAtoms(engine.queryAtom("IsSRG"));
		printAtoms(engine.queryAtom("FollowSymptom"));
		printAtoms(engine.queryAtom("Youyin"));
		printAtoms(engine.queryAtom("YouyinAndZhengzhuang"));
		printAtoms(engine.queryAtom("FinalReSymptom"));
		
		printAtoms(engine.queryAtom("ReSymptom"));
		printAtoms(engine.queryAtom("RealSymptom"));
		printAtoms(engine.queryAtom("AllSymptom"));
		printAtoms(engine.queryAtom("ReSymptomAndB"));
		
		printAtoms(engine.queryAtom("IsSymptom"));
		
		
		printAtoms(engine.queryAtom("ReYouyinQS"));
		printAtoms(engine.queryAtom("NewSymptom"));
		printAtoms(engine.queryAtom("TestSymptomAndYouyin"));
		printAtoms(engine.queryAtom("TestSymptomAndFollow"));
		
		printAtoms(engine.queryAtom("FinalReSymptom"));
		printAtoms(engine.queryAtom("ReSymptomAndBan"));
		printAtoms(engine.queryAtom("SymptomAndYouyin"));
		printAtoms(engine.queryAtom("TestItemAndDes"));
		printAtoms(engine.queryAtom("ReDiag"));
		
	}
	
	private static void printAtoms(Atom[] atoms) {
		for (int i = 0; i < atoms.length; i++) {
			System.out.println(i + ": " + atoms[i].toString());
		}
	}
}
