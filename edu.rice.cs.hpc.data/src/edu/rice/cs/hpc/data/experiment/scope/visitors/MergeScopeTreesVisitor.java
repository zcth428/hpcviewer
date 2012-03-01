//////////////////////////////////////////////////////////////////////////
//																		//
//	MergeScopeTreesVisitor.java											//
//																		//
//	MergeScopeTreesVisitor -- visitor class to merge two experiments	//
//	Created: May 15, 2007 												//
//																		//
//	(c) Copyright 2007 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////
package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.merge.MatchScope;
import edu.rice.cs.hpc.data.experiment.scope.*;

public class MergeScopeTreesVisitor extends BaseDuplicateScopeTreesVisitor {
	
	final private MatchScope matcher; 
	
	public MergeScopeTreesVisitor(Scope newRoot, int offset) {
		super(newRoot, offset);
		matcher = new MatchScope();
	}
	
	
	@Override
	protected Scope findMatch(Scope parent, Scope toMatch) {
		
		for (int i=0; i< parent.getSubscopeCount(); i++) {
			Scope kid = parent.getSubscope(i);

			if (kid.isCounterZero() && matcher.isMatch(kid, toMatch)) {
				kid.incrementCounter();
				return kid;
			}
		}
		return null;
	}	
}