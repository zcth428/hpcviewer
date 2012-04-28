//////////////////////////////////////////////////////////////////////////
//																		//
//	ExperimentMerger.java												//
//																		//
//	ExperimentMerger -- class to merge two Experiments					//
//	Created: May 7, 2007 												//
//																		//
//	(c) Copyright 2007-2012 Rice University. All rights reserved.		//
//																		//
//////////////////////////////////////////////////////////////////////////
package edu.rice.cs.hpc.data.experiment.merge;

import java.io.File;
import java.util.*;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.ExperimentConfiguration;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.visitors.*;
import edu.rice.cs.hpc.data.util.Constants;

/****
 * Merging experiments
 *
 * Steps:
 * 
 * 1. create the new experiment 
 * 2. add metrics 		-->	add into metric list 
 * 3. add raw metrics 	-->	add into a list
 * 4. add trace data 	-->	add into a list
 * 5. merge the experiments
 */
public class ExperimentMerger 
{
	static final private boolean with_raw_metrics = false;
	
	/**
	 * Merging two experiments, and return the new experiment
	 * 
	 * @param exp1
	 * @param exp2
	 * @return
	 */
	static public Experiment merge(Experiment exp1, Experiment exp2) {
		
		// -----------------------------------------------
		// step 1: create new base Experiment
		// -----------------------------------------------
		Experiment merged = exp1.duplicate();
		
		final ExperimentConfiguration configuration = new ExperimentConfiguration();
		configuration.setName( exp1.getName() + " U " + exp2.getName() );
		configuration.searchPaths = exp1.getConfiguration().searchPaths;
		
		merged.setConfiguration( configuration );

		// Add tree1, walk tree2 & add; just CCT/Flat
		RootScope rootScope = new RootScope(merged, "Merged Experiment","Invisible Outer Root Scope", RootScopeType.Invisible);
		merged.setRootScope(rootScope);
				
		// -----------------------------------------------
		// step 2: combine all metrics
		// -----------------------------------------------
		MergeMetric objMetrics = buildMetricList(merged, exp1.getMetrics(), exp2.getMetrics());
		merged.setMetrics(objMetrics.metrics);
		
		// -----------------------------------------------
		// step 3: mark the new experiment file
		// -----------------------------------------------
		File file1 = exp1.getXMLExperimentFile();
		String parent_dir = file1.getParentFile().getParent();
		final File fileMerged  = new File( parent_dir + "/merged/" + Constants.DATABASE_FILENAME); 
		merged.setXMLExperimentFile( fileMerged );

		// -----------------------------------------------
		// step 4: create cct root
		// -----------------------------------------------		

		// -----------------------------------------------
		// step 5: merge the two experiments
		// -----------------------------------------------

		mergeScopeTrees(exp1,new DuplicateScopeTreesVisitor(rootScope, objMetrics.pointerMetric1,1));		
		
		RootScope root1 = (RootScope) merged.getRootScopeChildren()[0];		
		RootScope root2 = (RootScope) exp2.getRootScopeChildren()[0];		
		
		final int metricCount = exp1.getMetricCount();
		final TreeSimilarity similar = new TreeSimilarity(metricCount, root1, root2, objMetrics);
		
		return merged;
	}
	
	
	/***
	 * combine metrics from exp 1 and exp 2
	 * 
	 * @param exp
	 * @param m1
	 * @param m2
	 * @return
	 */
	private static MergeMetric buildMetricList(Experiment exp, BaseMetric[] m1, BaseMetric[] m2) 
	{
		MergeMetric objMetric = new MergeMetric();
		
		final ArrayList<BaseMetric> metricList = new ArrayList<BaseMetric>(); 
		BaseMetric copyM1[] = new BaseMetric[m1.length];
		BaseMetric copyM2[] = new BaseMetric[m2.length];
		
		System.arraycopy(m1, 0, copyM1, 0, m1.length);
		System.arraycopy(m2, 0, copyM2, 0, m2.length);
		
		objMetric.pointerMetric1 = new int[m1.length];
		objMetric.pointerMetric2 = new int[m2.length];
		
		// ----------------------------------------------------------------
		// step 1: add the first metrics into the merged experiment
		// ----------------------------------------------------------------
		int i1 = 0;
		for(BaseMetric metric1: copyM1) {
			int i2 = 0;
			for (BaseMetric metric2: copyM2) {
				if (metric2 != null)
					if (metric1.getNativeName().equals(metric2.getNativeName())) {
						copyM1[i1] = null;
						copyM2[i2] = null;
					
						objMetric.pointerMetric1[i1] = metricList.size();
						objMetric.pointerMetric2[i2] = metricList.size();
						metricList.add(metric1);
						break;
					}
				i2++;
			}
			i1++;
		}
		// add the reminder of metric m1
		i1 = 0;
		for (BaseMetric metric1: copyM1) {
			if (metric1 != null) {
				objMetric.pointerMetric1[i1] = metricList.size();
				metricList.add(metric1);
			}
			i1++;
		}
		// add the reminder of metric m2
		i1 = 0;
		for (BaseMetric metric2: copyM2) {
			if (metric2 != null) {
				objMetric.pointerMetric2[i1] = metricList.size();
				metricList.add(metric2);
			}
			i1++;
		}
		
		// reorder metric index and ID
		for (int i=0; i<metricList.size(); i++) {
			BaseMetric metric = metricList.get(i);
			metric.setIndex(i);
			metric.setShortName(String.valueOf(i));
		}
		objMetric.metrics = metricList;
		return objMetric;
	}


	/***
	 * recursively merge trees
	 * 
	 * @param exp2
	 * @param visitor
	 */
	private static void mergeScopeTrees(Experiment exp2, 
			BaseDuplicateScopeTreesVisitor visitor) {

		RootScope root2 = (RootScope) exp2.getRootScopeChildren()[0];		

		root2.dfsVisitScopeTree(visitor);
	}
	
	/***
	 * merge two metric raws
	 * 
	 * @param raws1
	 * @param raws2
	 * @return
	 */
	private static MetricRaw[] buildMetricRaws( MetricRaw raws1[], MetricRaw raws2[]) 
	{
		MetricRaw rawList[] = new MetricRaw[ raws1.length + raws2.length ];
		
		for (int i=0; i<raws1.length; i++)
		{
			rawList[i] = (MetricRaw) raws1[i].duplicate();
			setMetricCombinedName(1, rawList[i]);
		}
		
		for (int i=0; i<raws2.length; i++)
		{
			rawList[i + raws1.length] = (MetricRaw) raws2[i].duplicate();
			setMetricCombinedName(2, rawList[i + raws1.length]);
		}
		
		return rawList;
	}

	/***
	 * create a new metric name based on the offset of the experiment and the metric
	 * 
	 * @param offset
	 * @param m
	 */
	private static void setMetricCombinedName( int offset, BaseMetric m )
	{
		m.setDisplayName( offset + "-" + m.getDisplayName() );
	}
}


