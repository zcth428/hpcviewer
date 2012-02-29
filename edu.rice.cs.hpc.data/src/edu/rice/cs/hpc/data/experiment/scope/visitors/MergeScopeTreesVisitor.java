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
	private int level = 0;

	public MergeScopeTreesVisitor(Scope newRoot, int offset) {
		super(newRoot, offset);
	}
	
	
	@Override
	protected Scope findMatch(Scope parent, Scope toMatch) {
		for (int i=0; i< parent.getSubscopeCount(); i++) {
			Scope kid = parent.getSubscope(i);

			if (isTheSame(kid, toMatch))
				return kid;
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
		
		if (s1 instanceof RootScope && s2 instanceof RootScope) 
		{	// skip the root scope
			ret = true;
		} else if (s1.getName().equals(s2.getName()))
		{
			// exactly the same name, check if hierarchically the same
			final Scope p1 = s1.getParentScope();
			final Scope p2 = s2.getParentScope();
			
			if (p1.getChildCount() == p2.getChildCount()) {
				ret = true;
			}
		}
		return ret;
		
	}
	
	
}