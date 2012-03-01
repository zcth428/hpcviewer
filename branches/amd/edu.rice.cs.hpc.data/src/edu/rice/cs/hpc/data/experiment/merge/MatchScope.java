package edu.rice.cs.hpc.data.experiment.merge;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class MatchScope  {


	final private boolean debug = true;


	private boolean hasTheSameNumberOfSiblings(Scope s1, Scope s2) 
	{
		// exactly the same name, but perhaps different line number
		final Scope p1 = s1.getParentScope();
		final Scope p2 = s2.getParentScope();
		int d1 = p1.getChildCount();
		int d2 = p2.getChildCount();
		
		return (d1==d2);
	}
	
	
	private void println(String str) 
	{
		if (debug) {
			System.out.println("MSTV: "+str);
		}
	}


	public boolean isMatch( Scope s1, Scope s2 ) {

		boolean ret = false;
		String s = "diff";
		
		if (s1 instanceof RootScope && s2 instanceof RootScope) 
		{	// skip the root scope
			ret = true;
			s = "root";
		} 
		else if (s1 instanceof LoopScope && s2 instanceof LoopScope) 
		{
			ret = isMatch( (LoopScope)s1, (LoopScope) s2);
			s = "loop-" + ret;
		}
		else if (s1.hashCode() == s2.hashCode())
		{
			// exactly the same flat id, check if hierarchically the same
			int d1 = s1.getCCTIndex();
			int d2 = s2.getCCTIndex();

			// the same cct ?
			if (d1 == d2) {
				// ideal case: the same cct. this happens in some first ancestors
				ret = true;
				s = "scct";
			} else {
				// the same flat index, but different cct index
				// this may be the case of recursive function of fib(a) + fib(b)
				if (hasTheSameNumberOfSiblings(s1, s2)) {
					// has the same number of siblings
					ret = true;
					s = "ssi1";
				} 
			}
			
		} else if (s1.getName().equals(s2.getName())) 
		{
			// different hash index, but the same name
			// this can be because of optimization
			
			if (hasTheSameNumberOfSiblings(s1, s2)) {
				// has the same number of siblings: it's likely to be the same code
				ret = true;
				s = "ssi2";
			} 			
		}
		println(s+ ": " + s1.getName() +", h: " + s1.hashCode() + ", cct:" + s1.getCCTIndex() + " vs. " + s2.getCCTIndex() );
		return ret;

	}


	/***
	 * comparing loops is tricky since the compiler can highly optimize it and
	 * 	we lose information
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	private boolean isMatch( LoopScope l1, LoopScope l2 ) 
	{
		boolean ret = false;
		
		if (l1.getName().equals(l2.getName()))
			// same file, same line number
			ret = true;
		else 
		{
			// look at the children of the loop. See if they are mostly the same
			final int c1 = l1.getChildCount();
			final int c2 = l2.getChildCount();
			int count = c1;
			
			// number the children can be different due to compiler optimization
			if (c2<count)
				count = c2;
			
			int numMatches = 0;
			for (int i=0; i<count; i++) 
			{
				Scope s1 = l1.getSubscope(i);
				Scope s2 = l2.getSubscope(i);
				
				if ( isMatch(s1, s2) )
					numMatches++;
			}
			// match if most of the children are the same
			ret = (  0.6 <= ((float)numMatches/(float)count) );
		}
		
		return ret;
	}

	
	private boolean isMatch( CallSiteScope c1, CallSiteScope c2 ) 
	{
		final ProcedureScope p1 = c1.getProcedureScope();
		final ProcedureScope p2 = c2.getProcedureScope();
		
		return ( c1.getName().equals(c2.getName()) && 
				p1.getName().equals(p2.getName()) );
	}

}
