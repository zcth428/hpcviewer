package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.util.Hashtable;
import java.util.LinkedList;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.filters.EmptyMetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

public class CallersViewScopeVisitor implements ScopeVisitor {


	//----------------------------------------------------
	// private data
	//----------------------------------------------------
	protected boolean isdebug = false; // true;
	protected Hashtable/*<int, Scope>*/ calleeht = new Hashtable/*<int, Scope>*/();
	protected Experiment exp;
	protected Scope callersViewRootScope;
	protected MetricValuePropagationFilter filter;
	protected int numberOfPrimaryMetrics;

	//----------------------------------------------------
	// constructor for CallerViewScopeVisitor
	//----------------------------------------------------

	public CallersViewScopeVisitor(Experiment experiment, Scope cvrs, 
			int nMetrics, boolean dodebug, MetricValuePropagationFilter filter) {
		this.exp = experiment;
		this.callersViewRootScope = cvrs;
		this.numberOfPrimaryMetrics = nMetrics;
		this.isdebug = dodebug;
		this.filter = filter;
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(CallSiteScope scope, ScopeVisitType vt) {
		ProcedureScope mycallee  = scope.getProcedureScope();
		if (vt == ScopeVisitType.PreVisit) { // && !mycallee.isAlien()) {
			String procedureName = mycallee.getName();

			trace("handling scope " + procedureName);

			CallSiteScope tmp = (CallSiteScope)scope.duplicate(); // create a temporary scope to accumulate metrics to
			tmp.accumulateMetrics(scope, new EmptyMetricValuePropagationFilter(), numberOfPrimaryMetrics);
			// accumulateMetricsFromMyKids(tmp, scope, filter);
			// Remove linescope-normalization from CS-scope
			for (int i=0; i<numberOfPrimaryMetrics; i++) {
				double lsval = scope.getLineScope().getMetricValue(i).getValue();
				tmp.accumulateMetricValue(i, (lsval<0.0) ? 0.0 : (-1*lsval) );
			}

			// if there are no exclusive costs to attribute from this context, we are done here
			if (!tmp.hasNonzeroMetrics()) return; 

			// Find (or add) callee in top-level hashtable
			// TODO: we should use a fully qualified procedure name (including file, module)
			Scope callee = (Scope) calleeht.get(new Integer(mycallee.hashCode()));
			if (callee == null) {
				callee  = mycallee.duplicate();
				calleeht.put(new Integer(callee.hashCode()), callee);
				callersViewRootScope.addSubscope(callee);
				callee.setParentScope(this.callersViewRootScope);
				exp.getScopeList().addScope(callee);
				trace("added top level entry in bottom up tree");
			}
			callee.accumulateMetrics(tmp, new EmptyMetricValuePropagationFilter(), numberOfPrimaryMetrics);

			//-----------------------------------------------------------------------
			// compute callPath: a chain of my callers
			//
			// we build a chain of callers  by tracing the path from a procedure
			// up to the root of the calling context tree. notice that 
			// this isn't simply a reversal of the path. in the chain of callers,
			// we associate the call site with the caller. in the calling context
			// tree, the call site is paired with the callee. that's why we
			// work with a pair of CallSiteScopes at a time (innerCS and enclosingCS)
			//-----------------------------------------------------------------------
			trace("determine call path for " + procedureName);
			CallSiteScope innerCS = scope;
			LinkedList callPathList = new LinkedList();
			Scope next = scope.getParentScope();
			while (next instanceof CallSiteScope || next instanceof LoopScope ||
					next instanceof ProcedureScope)
			{
				if (!(next instanceof LoopScope)) {
					CallSiteScope enclosingCS = null;
					ProcedureScope mycaller = null;
					if (next instanceof ProcedureScope) {
						mycaller = (ProcedureScope) next.duplicate();
					}
					else if (next instanceof CallSiteScope) {
						enclosingCS = (CallSiteScope) next;
						mycaller = (ProcedureScope) enclosingCS.getProcedureScope().duplicate();
					}
					CallSiteScope callerScope =
						new CallSiteScope((LineScope) innerCS.getLineScope().duplicate(), 
								mycaller,
								CallSiteScopeType.CALL_FROM_PROCEDURE);
					callerScope.accumulateMetrics(tmp, new EmptyMetricValuePropagationFilter(), numberOfPrimaryMetrics);
					callPathList.addLast(callerScope);
					innerCS = enclosingCS;
				}
				next = next.getParentScope();
			}

			//-------------------------------------------------------
			// ensure my call path is represented among my children.
			//-------------------------------------------------------
			mergeCallerPath(callee, callPathList);
		}
	}

	public void visit(Scope scope, ScopeVisitType vt) { }
	public void visit(RootScope scope, ScopeVisitType vt) { }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) { }
	public void visit(FileScope scope, ScopeVisitType vt) { }
	
	public void visit(ProcedureScope scope, ScopeVisitType vt) { 
		ProcedureScope mycallee  = scope;
		if (vt == ScopeVisitType.PreVisit) { // && !mycallee.isAlien()) {
			String procedureName = mycallee.getName();
			trace("handling scope " + procedureName);

			ProcedureScope tmp = (ProcedureScope)scope.duplicate(); // create a temporary scope to accumulate metrics to
			tmp.accumulateMetrics(scope, new EmptyMetricValuePropagationFilter(), numberOfPrimaryMetrics);

			// if there are no exclusive costs to attribute from this context, we are done here
			if (!tmp.hasNonzeroMetrics()) return; 

			// Find (or add) callee in top-level hashtable
			// TODO: we should use a fully qualified procedure name (including file, module)
			Scope callee = (Scope) calleeht.get(new Integer(mycallee.hashCode()));
			if (callee == null) {
				callee  = mycallee.duplicate();
				calleeht.put(new Integer(callee.hashCode()), callee);
				callersViewRootScope.addSubscope(callee);
				callee.setParentScope(this.callersViewRootScope);
				exp.getScopeList().addScope(callee);
				trace("added top level entry in bottom up tree");
			}
			callee.accumulateMetrics(tmp, new EmptyMetricValuePropagationFilter(), 
					numberOfPrimaryMetrics);
		}
	}
	
	public void visit(AlienScope scope, ScopeVisitType vt) { }
	public void visit(LoopScope scope, ScopeVisitType vt) { }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { }
	public void visit(LineScope scope, ScopeVisitType vt) { }
	public void visit(GroupScope scope, ScopeVisitType vt) { }

	//----------------------------------------------------
	// helper functions  
	//----------------------------------------------------

	protected void mergeCallerPath(Scope callee, LinkedList callerPathList) 
	{
		if (callerPathList.size() == 0) return; // merging an empty path is trivial

		CallSiteScope first = (CallSiteScope) callerPathList.removeFirst();

		// -------------------------------------------------------------------------
		// attempt to merge first node on caller path with existing caller of callee  
		//--------------------------------------------------------------------------
		int nCallers = callee.getSubscopeCount();
		for (int i = 0; i < nCallers; i++) {
			CallSiteScope existingCaller = (CallSiteScope) callee.getSubscope(i);

			// if first matches an existing caller
			if (existingCaller.getLineScope().isequal(first.getLineScope()) &&
					(existingCaller.getName()).equals(first.getName())) {

				// add metric values for first to those of existingCaller. 
				existingCaller.accumulateMetrics(first, filter, numberOfPrimaryMetrics);

				// merge rest of call path as a child of existingCaller.
				mergeCallerPath(existingCaller, callerPathList);

				return; // merged with existing child. nothing left to do.
			}
		}

		//----------------------------------------------
		// no merge possible. add new path into tree.
		//----------------------------------------------
		callee.addSubscope(first);
		first.setParentScope(callee);
		for (Scope prev = first; callerPathList.size() > 0; ) {
			Scope next = (Scope) callerPathList.removeFirst();
			prev.addSubscope(next);
			next.setParentScope(prev);
			prev = next;
		}
	}
/*
	protected void accumulateMetricsFromLineScopeKids(Scope target, Scope source, 
			MetricValuePropagationFilter filter) {
		int nkids = source.getSubscopeCount();
		for (int i = 0; i < nkids; i++) {
			Scope child = source.getSubscope(i);
			if (child instanceof LineScope) target.accumulateMetrics(child, filter, numberOfPrimaryMetrics);
		}
	}

	protected void accumulateMetricsFromMyKids(Scope target, Scope source, 
			MetricValuePropagationFilter filter) {
		int nkids = source.getSubscopeCount();
		for (int i = 0; i < nkids; i++) {
			Scope child = source.getSubscope(i);
			if (child instanceof LineScope || child instanceof CallSiteScope) 
				target.accumulateMetrics(child, filter, numberOfPrimaryMetrics);
			if (child instanceof LoopScope || child instanceof ProcedureScope)
				accumulateMetricsFromMyKids(target, child, filter); 
		}
	}
*/
	//----------------------------------------------------
	// debugging support 
	//----------------------------------------------------

	void trace(String msg) {
		if (isdebug) System.out.println(msg);
	}
}
