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
		} else if (s1.hashCode() == s2.hashCode())
		{
			// exactly the same name, check if hierarchically the same
			final Scope p1 = s1.getParentScope();
			final Scope p2 = s2.getParentScope();
			String s = "diff";
			
			// the same cct ?
			int d1 = s1.getCCTIndex();
			int d2 = s2.getCCTIndex();

			if (d1 == d2) {
				ret = true;
				s = "scct";
			} else {
				
				d1 = s1.getChildCount();
				d2 = s2.getChildCount();
				
				if (d1 == d2) {
					// has the same number of children
					for (int i=0; i<d1 && ret; i++) {
						ret = (s1.getSubscope(i).getFlatIndex() == s2.getSubscope(i).getFlatIndex());
					}
					if (ret)
						s = "scc";
				} 			
			}
			System.out.println("MSTV ["+this.scopeStack.size()+"] " + s1 + ", c1: " + s1.getCCTIndex() + 
					", c2: " + s2.getCCTIndex() + "\ts: " + s);
		}
		return ret;
	}
	
	private int getDepth(Scope scope) 
	{
		int depth = 0;
		Scope parent = scope.getParentScope();
		while (parent != null && !(parent instanceof RootScope)) {
			depth++;
			parent = parent.getParentScope();
		}
		return depth;
	}
	
}