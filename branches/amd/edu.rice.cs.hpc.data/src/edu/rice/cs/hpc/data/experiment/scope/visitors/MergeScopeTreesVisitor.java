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

import edu.rice.cs.hpc.data.experiment.scope.*;

public class MergeScopeTreesVisitor extends BaseDuplicateScopeTreesVisitor {

	public MergeScopeTreesVisitor(Scope newRoot, int offset) {
		super(newRoot, offset);
	}
	
	
	@Override
	protected Scope findMatch(Scope parent, Scope toMatch) {
		for (int i=0; i< parent.getSubscopeCount(); i++) {
			Scope kid = parent.getSubscope(i);

			if (kid.isCounterZero() && isTheSame(kid, toMatch)) {
				kid.incrementCounter();
				return kid;
			}
		}
		return null;
	}
	
	
	/****
	 * check if two scopes are "hierarchically" the same
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean isTheSame(Scope s1, Scope s2)
	{
		boolean ret = false;
		String s = "diff";
		
		if (s1 instanceof RootScope && s2 instanceof RootScope) 
		{	// skip the root scope
			ret = true;
		} else if (s1.hashCode() == s2.hashCode())
		{
			// exactly the same flat id, check if hierarchically the same
			int d1 = s1.getCCTIndex();
			int d2 = s2.getCCTIndex();

			// the same cct ?
			if (d1 == d2) {
				// ideal case: the same cct
				ret = true;
				s = "scct";
			} else {
				// the same flat index, but different cct index
				// this may be the case of recursive function of fib(a) + fib(b)
				if (hasTheSameNumberOfSiblings(s1, s2)) {
					// has the same number of siblings
					ret = true;
					s = "ssi";
				} 			
			}
			System.out.println("MSTV ["+this.scopeStack.size()+"] " + s1 + ", c1: " + s1.getCCTIndex() + 
					", c2: " + s2.getCCTIndex() + "\ts: " + s);
			
		} else if (s1.getName().equals(s2.getName())) 
		{
			// different hash index, but the same name
			// this can be because of optimization
			
			if (hasTheSameNumberOfSiblings(s1, s2)) {
				// has the same number of siblings: it's likely to be the same code
				ret = true;
				s = "ssi";
			} 			
			System.out.println("MSTV ["+this.scopeStack.size()+"] " + s1 + ", c1: " + s1.getCCTIndex() + 
					", c2: " + s2.getCCTIndex() + "\ts: " + s);
		}
		return ret;
	}	
	
	
	private boolean hasTheSameNumberOfSiblings(Scope s1, Scope s2) 
	{
		// exactly the same name, but perhaps different line number
		final Scope p1 = s1.getParentScope();
		final Scope p2 = s2.getParentScope();
		int d1 = p1.getChildCount();
		int d2 = p2.getChildCount();
		
		return (d1==d2);
	}
}