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

public class TraceDataVisitor implements IScopeVisitor {
	protected HashMap<Integer, CallPath> map;

	public TraceDataVisitor(HashMap<Integer, CallPath> _map) {
		map = _map;
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) {  }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { }
	public void visit(FileScope scope, ScopeVisitType vt) { }
	public void visit(ProcedureScope scope, ScopeVisitType vt) { }
	public void visit(AlienScope scope, ScopeVisitType vt) { }
	public void visit(LoopScope scope, ScopeVisitType vt) { }
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
			}
		}
	}
	
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { }
	
	public void visit(CallSiteScope scope, ScopeVisitType vt) { }
	
	public void visit(GroupScope scope, ScopeVisitType vt) { }

	public HashMap<Integer, CallPath> getMap()
	{
		return map;
	}
}
