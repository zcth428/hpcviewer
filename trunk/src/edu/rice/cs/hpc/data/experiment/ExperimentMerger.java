//////////////////////////////////////////////////////////////////////////
//																		//
//	ExperimentMerger.java												//
//																		//
//	ExperimentMerger -- class to merge two Experiments					//
//	Created: May 7, 2007 												//
//																		//
//	(c) Copyright 2007 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////
package edu.rice.cs.hpc.data.experiment;

import java.util.*;

import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.filters.*;
import edu.rice.cs.hpc.data.experiment.scope.visitors.*;
import edu.rice.cs.hpc.data.experiment.source.*;

public class ExperimentMerger {
	public Experiment merge(Experiment exp1, Experiment exp2) {
		// create new base Experiment
		Experiment merged = new Experiment(exp1);
		
		// union SourceFile lists,
		List files = unionSourceFiles(exp1.files, exp2.files);
		merged.setSourceFiles(files);
		
		// append metricList
		List metrics = buildMetricList(merged, exp1.getMetrics(), exp2.getMetrics());
		merged.setMetrics(metrics);

		// union ScopeLists
		List scopeList = unionScopeLists(exp1.getScopeList(), exp2.getScopeList());
		
		// Add tree1, walk tree2 & add; just CCT/Flat
		RootScope rootScope = new RootScope(merged, "Merged Experiment","Invisible Outer Root Scope", RootScopeType.Invisible);
		merged.setScopes(scopeList, rootScope);

		mergeScopeTrees(merged, exp1, 0);
		mergeScopeTrees(merged, exp2, exp1.getMetricCount());
		
		return merged;
	}
	
	private List unionSourceFiles(SourceFile[] f1, SourceFile[] f2) {
		Vector files = new Vector();
		List sf1 = Arrays.asList(f1);
		List sf2 = Arrays.asList(f2);
		// union these lists (if !contains?)
		// TODO [me] how to handle new srcs in f2? copy to defaultDir? or use absolute path to dDir2?
		files.addAll(sf1);
		files.addAll(sf2);
		return files;
	}
	
	private List buildMetricList(Experiment exp, BaseMetric[] m1, BaseMetric[] m2) {
		Vector metricList = new Vector();
		for (int i=0; i<m1.length; i++) {
			Metric m = (Metric) m1[i];
			// TODO [me] change (display/native) names (+ Exp#)
			String shortName = "" + metricList.size();
			Metric newM = new Metric(exp, shortName, 
					m.getNativeName(), m.getDisplayName(),
					m.getDisplayed(), m.getPercent(), 
					m.getSamplePeriod(), m.getMetricType(), m.getPartnerIndex());
			metricList.add(newM);
		}
		for (int i=0; i<m2.length; i++) {
			Metric m = (Metric)m2[i];
			// TODO [me] change (display/native) names (+ Exp#)
			String shortName = "" + metricList.size();
			MetricType metricType = m.getMetricType();
			int partnerIndex = m.getPartnerIndex();
			if (partnerIndex != Metric.NO_PARTNER_INDEX)
				partnerIndex = m1.length + partnerIndex;
			Metric newM = new Metric(exp, shortName, 
						m.getNativeName(), m.getDisplayName(),
						m.getDisplayed(), m.getPercent(), 
						m.getSamplePeriod(), metricType, partnerIndex);
			metricList.add(newM);
		}
		
		return metricList;
	}
	
	private List unionScopeLists(ScopeList s1, ScopeList s2) {
		Vector scopes = new Vector();
		for (int i=0; i<s1.getSize(); i++) {
			scopes.add(s1.getScopeAt(i));
		}
		for (int i=0; i<s2.getSize(); i++) {
			Scope scope = s2.getScopeAt(i);
			if (!(scopes.contains(scope)))
				scopes.add(scope);
		}
		return scopes;
	}
	
	public void mergeScopeTrees(Experiment exp1, Experiment exp2, int offset) {
		EmptyMetricValuePropagationFilter emptyFilter = new EmptyMetricValuePropagationFilter();
		Scope root1 = exp1.getRootScope();
		Scope root2 = exp2.getRootScope();
		
		MergeScopeTreesVisitor mv = new MergeScopeTreesVisitor(root1, offset, emptyFilter);

		root2.dfsVisitScopeTree(mv);
	}	
}


