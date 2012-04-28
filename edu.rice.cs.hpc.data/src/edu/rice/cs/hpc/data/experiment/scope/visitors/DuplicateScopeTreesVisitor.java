package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class DuplicateScopeTreesVisitor extends BaseDuplicateScopeTreesVisitor {

	public DuplicateScopeTreesVisitor(Scope newRoot, int offset[], int factor) {
		super( newRoot, offset, factor );
	}
	
	//@Override
	protected Scope findMatch(Scope parent, Scope toMatch) {
		// for duplication, everything matches
		return null;
	}

}
