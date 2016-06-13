package com.miltidim.nlp.deepstruct;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FolTest extends TestCase {
	
//	static FolTheoremProver e;
	
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public FolTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HitalesForestTest.class );
    }

	public void test3() throws Exception {
//		FolParser parser = new FolParser();	
//		String source = "Animal = {horse, cow, lion} \n"
//				+ "Plant = {grass, tree} \n"
//				+ "type(Eats(Animal, Plant)) \n"
//				+ "forall X: (!Eats(X,tree)) \n"
//				+ "Eats(cow, grass) \n"
//				+ "forall X: (!Eats(cow, X) || Eats(horse, X)) \n"
//				+ "exists X: (Eats(lion, X))";
//		FolBeliefSet b = parser.parseBeliefBase(source);
//		printer.printBase(b);
//		System.out.println(printer);
//		assertFalse(e.query(b, (FolFormula)parser.parseFormula("Eats(lion, tree)")));
//		assertFalse(e.query(b, (FolFormula)parser.parseFormula("!Eats(lion, grass)")));
//		//is not true according to the solver
//		//assertTrue(e.query(b, (FolFormula)parser.parseFormula("Eats(lion, grass)")));
//		assertFalse(e.query(b, (FolFormula)parser.parseFormula("Eats(horse, tree)")));
//		assertTrue(e.query(b, (FolFormula)parser.parseFormula("!Eats(horse, tree)")));
//		assertTrue(e.query(b, (FolFormula)parser.parseFormula("Eats(horse, grass)")));
//		assertTrue(e.query(b, (FolFormula)parser.parseFormula("exists X: (forall Y: (!Eats(Y, X)))")));
//		assertFalse(e.query(b, (FolFormula)parser.parseFormula("forall X: (forall Y: (Eats(Y, X)))")));
//		assertTrue(e.query(b, (FolFormula)parser.parseFormula("!(forall X: (forall Y: (Eats(Y, X))))")));
	}
}
