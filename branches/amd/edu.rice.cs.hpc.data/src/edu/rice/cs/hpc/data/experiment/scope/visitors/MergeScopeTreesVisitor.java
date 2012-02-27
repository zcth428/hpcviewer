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

import java.util.Stack;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

public class MergeScopeTreesVisitor implements IScopeVisitor {
	final private Stack<Scope> scopeStack;
	final private MetricValuePropagationFilter filter;

	public MergeScopeTreesVisitor(Scope newRoot, MetricValuePropagationFilter filter) {
		scopeStack = new Stack<Scope>();
		scopeStack.push(newRoot);
		this.filter = filter;
	}
	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------
	public void visit(RootScope scope, 				ScopeVisitType vt) { 
		if (scope.getType() != RootScopeType.Invisible)	mergeInsert(scope, vt);
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

			final Scope match = findMatch(newParent, scope);
			
			newParent = this.addMetricColumns(newParent, match, scope);
			
			scopeStack.push(newParent);
		} else { // PostVisit
			scopeStack.pop();
		}
	}
	
	private Scope findMatch(Scope parent, Scope toMatch) {
		for (int i=0; i< parent.getSubscopeCount(); i++) {
			Scope kid = parent.getSubscope(i);

			if (kid.getName().equals(toMatch.getName()))
				return kid;
		}
		return null;
	}
	
	private Scope addMetricColumns(Scope parent, Scope target, Scope source) {
		
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
		final int numTargetMetrics = ((Experiment)target.getExperiment()).getMetricCount();
		
		for (int i = 0; i< numSourceMetrics; i++) {
			target.accumulateMetric(source, i, numTargetMetrics+i, filter);
		}
	}
	
}