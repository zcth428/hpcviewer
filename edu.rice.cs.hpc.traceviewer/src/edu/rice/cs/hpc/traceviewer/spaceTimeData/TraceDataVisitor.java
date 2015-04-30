package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.HashMap;

import edu.rice.cs.hpc.data.experiment.scope.AlienScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpc.data.util.IProcedureTable;

import edu.rice.cs.hpc.traceviewer.data.graph.CallPath;
import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;

/**********************************************************
 * Visitor class for gathering procedure names and the 
 * maximum depth.
 * 
 * To get the maximum depth, the caller requires to instantiate
 * the class, call dfsVisitScopeTree() method from the CCT root
 * and call the method getMaxDepth()
 **********************************************************/
public class TraceDataVisitor implements IScopeVisitor 
{
	final private HashMap<Integer, CallPath> map;
	final private IProcedureTable colorTable;
	private int maxDepth = 0;

	public TraceDataVisitor() {
		map = new HashMap<Integer, CallPath>();
		colorTable = new ColorTable();
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) {  }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { }
	public void visit(FileScope scope, ScopeVisitType vt) { }
	public void visit(AlienScope scope, ScopeVisitType vt) { }
	public void visit(LoopScope scope, ScopeVisitType vt) { }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { }	
	public void visit(GroupScope scope, ScopeVisitType vt) { }

	public void visit(ProcedureScope scope, ScopeVisitType vt) { 
		addProcedure(scope);
	}

	public void visit(CallSiteScope scope, ScopeVisitType vt) { 
		addProcedure(scope);
	}

	public void visit(LineScope scope, ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PreVisit) {
			int cpid = scope.getCpid();
			if (cpid > 0)
			{
				Scope cur = scope;
				int depth = 0;
				do
				{
					Scope parent = cur.getParentScope();
					if((cur instanceof CallSiteScope) || (cur instanceof ProcedureScope))
						++depth;
					cur = parent;
				}
				while(cur != null && !(cur instanceof RootScope));
				this.map.put(cpid, new CallPath(scope, depth));
				maxDepth = Math.max(maxDepth, depth);
			}
		}
	}
	
	/****
	 * get the maximum depth from the tree traversal based on the scope
	 * where this visitor is used.
	 * 
	 * @return the maximum depth of a given scope 
	 */
	public int getMaxDepth()
	{
		return maxDepth;
	}
	
	/****
	 * get the map of cpid and its call path
	 * @return a hash map
	 */
	public HashMap<Integer, CallPath> getMap()
	{
		return map;
	}
	
	/****
	 * get the list of procedure names used in a given scope.
	 * 
	 * @return the instance of IProcedureTable
	 */
	public IProcedureTable getProcedureTable()
	{
		return colorTable;
	}
	
	private void addProcedure(Scope scope)
	{
		colorTable.addProcedure(scope.getName());
	}
}
