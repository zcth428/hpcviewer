package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.util.Stack;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.filters.EmptyMetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

public class DuplicateScopeTreesVisitor implements IScopeVisitor {

	final private Stack<Scope> scopeStack;
	final private MetricValuePropagationFilter filter;

	
	public DuplicateScopeTreesVisitor(Scope newRoot) {
		scopeStack = new Stack<Scope>();
		scopeStack.push(newRoot);
		this.filter = new EmptyMetricValuePropagationFilter();
	}

	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------
	public void visit(RootScope scope, 				ScopeVisitType vt) { 
		if (scope.getType() != RootScopeType.Invisible)	
			mergeInsert(scope, vt);
	}
	public void visit(LoadModuleScope scope, 		ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(FileScope scope, 				ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(GroupScope scope, 			ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(Scope scope, 					ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(CallSiteScope scope, 			ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(ProcedureScope scope, 		ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(LoopScope scope, 				ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(StatementRangeScope scope, 	ScopeVisitType vt) { mergeInsert(scope, vt); }
	public void visit(LineScope scope, 				ScopeVisitType vt) { mergeInsert(scope, vt); }

	
	private void mergeInsert(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PreVisit) {
			Scope newParent = (Scope)scopeStack.peek();
			
			newParent = this.addMetricColumns(newParent, null, scope);
			
			scopeStack.push(newParent);
			
		} else { // PostVisit
			scopeStack.pop();
		}
	}

	/*****
	 * add child and its metric values if needed
	 * 
	 * @param parent
	 * @param target
	 * @param source
	 * @return
	 */
	protected Scope addMetricColumns(Scope parent, Scope target, Scope source) {
		
		if (target == null) {
			// no target scope; create it under parent, and copy over source metrics
			target = source.duplicate();
			parent.addSubscope(target);
			target.setParentScope(parent);
			
			target.setExperiment(parent.getExperiment());
			
			if (target instanceof CallSiteScope) {
				((CallSiteScope)target).getLineScope().setExperiment(parent.getExperiment());
				((CallSiteScope)target).getProcedureScope().setExperiment(parent.getExperiment());
			}
		} // else match! just copy source's metrics over to target
		
		accumulateMetrics(target, source, filter);

		if (source instanceof CallSiteScope && target instanceof CallSiteScope) {
			accumulateMetrics(	((CallSiteScope)target).getLineScope(),
								((CallSiteScope)source).getLineScope(), filter);
		}
		
		return target;
	}
	
	protected void accumulateMetrics(Scope target, Scope source, MetricValuePropagationFilter filter) {
		
		final Experiment experiment = (Experiment)source.getExperiment();
		final int numSourceMetrics = experiment.getMetricCount();
		
		for (int i = 0; i< numSourceMetrics; i++) {
			target.accumulateMetric(source, i, i, filter);
		}
	}

}
