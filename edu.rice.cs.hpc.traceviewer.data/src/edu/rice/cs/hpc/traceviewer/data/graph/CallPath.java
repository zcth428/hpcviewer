package edu.rice.cs.hpc.traceviewer.data.graph;
import java.util.Vector;

import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class CallPath
{

	/**the Scope at the current cpid*/
	private Scope leafScope;
	
	/**the depth of leafScope (where current cpid is)*/
	private int maxDepth;
	
	/**the list of all functions as strings in the call path*/
	private Vector<String> functionNames;
	
	/**the list of all data object names in the call path*/
	private Vector<String> dataNames;

	/**A null function*/
	public static final String NULL_FUNCTION = "-Outside Timeline-";
	
	public CallPath(Scope _leafScope, int _maxDepth, Scope _currentDepthScope, int _currentDepth)
	{
		leafScope = _leafScope;
		maxDepth = _maxDepth;
		functionNames = new Vector<String>();
		dataNames = new Vector<String>();
	}
	
	public CallPath(Scope _leafScope, int _maxDepth)
	{
		this(_leafScope, _maxDepth, null, _maxDepth);
	}
	
	/**returns the scope at the given depth that's along the path between the root scope and the leafScope*/
	public Scope getScopeAt(int depth)
	{
		if (depth < 0)
			return null;
		
		int cDepth = maxDepth;
		Scope cDepthScope = leafScope;

		while(!(cDepthScope.getParentScope() instanceof RootScope) && 
				(cDepth > depth || !(cDepthScope instanceof CallSiteScope || cDepthScope instanceof ProcedureScope)))
		{
			cDepthScope = cDepthScope.getParentScope();
			if((cDepthScope instanceof CallSiteScope) || (cDepthScope instanceof ProcedureScope))
				cDepth--;
		}
		
		assert (cDepthScope instanceof CallSiteScope || cDepthScope instanceof ProcedureScope);

		return cDepthScope;
	}
	
	public Scope getBottomScope()
	{
		return leafScope;
	}
	
	public void setBottomScope(Scope _leafScope)
	{
		leafScope = _leafScope;
	}
	
	/*************************************
	 * retrieve the list of function names of this call path
	 * 
	 * @return vector of procedure names
	 ************************************/
	public Vector<String> getFunctionNames()
	{
		if (functionNames.isEmpty())
		{
			Scope currentScope = leafScope;
			int depth = maxDepth;
			while(depth > 0)
			{
				if ((currentScope instanceof CallSiteScope) || (currentScope instanceof ProcedureScope))
				{
					functionNames.add(0, currentScope.getName());
					depth--;
				}
				currentScope = currentScope.getParentScope();
			}
		}
		return functionNames;
	}
	
	public Vector<String> getDataNames()
	{
		return dataNames;
	}
	
	/*******************************
	 * Retrieve the maximum depth of this call path
	 * 
	 * @return the max depth
	 *******************************/
	public int getMaxDepth()
	{
		return maxDepth;
	}
}