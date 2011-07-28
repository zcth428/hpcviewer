package edu.rice.cs.hpc.traceviewer.spaceTimeData;
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
	
	/**the Scope at the current depth along the path between root Scope and leafScope*/
	private Scope currentDepthScope;
	
	/**the current depth being viewed*/
	private int currentDepth;
	
	/**the list of all functions as strings in the call path*/
	private Vector<String> functionNames;
	
	/**A null function*/
	public static final String NULL_FUNCTION = "-Outside Timeline-";
	
	public CallPath(Scope _leafScope, int _maxDepth, Scope _currentDepthScope, int _currentDepth)
	{
		leafScope = _leafScope;
		maxDepth = _maxDepth;
		currentDepthScope = _currentDepthScope;
		currentDepth = _currentDepth;
		functionNames = new Vector<String>();
	}
	
	public CallPath(Scope _leafScope, int _maxDepth)
	{
		leafScope = _leafScope;
		maxDepth = _maxDepth;
		currentDepth = maxDepth;
		currentDepthScope = leafScope;
		functionNames = new Vector<String>();
	}
	
	/**returns the scope at the given depth that's along the path between the root scope and the leafScope*/
	public Scope getScopeAt(int depth)
	{
		if (depth < 0)
			return null;
		if (depth == currentDepth)
			return currentDepthScope;
		
		int cDepth = currentDepth;
		Scope cDepthScope = currentDepthScope;
		if (depth > currentDepth)
		{
			cDepth = maxDepth;
			cDepthScope = leafScope;
		}
		while(!(cDepthScope.getParentScope() instanceof RootScope) && (cDepth > depth || !(cDepthScope instanceof CallSiteScope || cDepthScope instanceof ProcedureScope)))
		{
			cDepthScope = cDepthScope.getParentScope();
			if((cDepthScope instanceof CallSiteScope) || (cDepthScope instanceof ProcedureScope))
				cDepth--;
			if (cDepthScope.getName().equals("Experiment Aggregate Metrics"))
				System.out.println("wtf");
		}
		return cDepthScope;
		/*Vector<Scope> path = new Vector<Scope>();
		Scope s = leafScope;
		do
		{
			Scope parent = s.getParentScope();
			if ((s instanceof CallSiteScope) || (s instanceof ProcedureScope))
			{
				path.add(0, s);
			}
			s = parent;
		}
		while(s != null && !(s instanceof RootScope));
		return path.get(depth >= maxDepth ? maxDepth-1 : depth);*/
	}
	
	public Scope getBottomScope()
	{
		return leafScope;
	}
	
	public void setBottomScope(Scope _leafScope)
	{
		leafScope = _leafScope;
	}
	
	public Scope getCurrentDepthScope()
	{
		return currentDepthScope;
	}
	
	public void setCurrentDepthScope()
	{
		currentDepthScope = getScopeAt(currentDepth);
	}

	public int getCurrentDepth()
	{
		return currentDepth;
	}
	
	public void updateCurrentDepth(int newDepth)
	{
		/*if (newDepth > maxDepth)
		{
			currentDepthScope = leafScope;
			currentDepth = maxDepth;
			return;
		}
		if (newDepth != currentDepth)
		{
			currentDepthScope = getScopeAt(newDepth);
			currentDepth = newDepth;
		}*/
		currentDepthScope = getScopeAt(newDepth);
		currentDepth = newDepth > maxDepth ? maxDepth : newDepth;
	}
	
	public Vector<String> getAllNames()
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
}