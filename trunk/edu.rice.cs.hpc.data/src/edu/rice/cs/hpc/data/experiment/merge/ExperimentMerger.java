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
	static public enum MergeType {TOP_DOWN, BOTTOM_UP, FLAT};
	
	/**
	 * Merging two experiments, and return the new experiment
	 * 
	 * @param exp1 : first database 
	 * @param exp2 : second database
	 * @param type : root to merge (cct, bottom-up tree, or flat tree)
	 * @param verbose : true if the verbose mode is on
	 *  
	 * @return
	 */
	static public Experiment merge(Experiment exp1, Experiment exp2, MergeType type, boolean verbose) {
		
		File file1 = exp1.getXMLExperimentFile();
		String parent_dir = file1.getParentFile().getParent() + File.separator + "merged" + File.separator;

		return merge(exp1, exp2, type, parent_dir, verbose);
	}
	
	static public Experiment merge(Experiment exp1, Experiment exp2, MergeType type, 
			String parent_dir, boolean verbose) {
		
		// -----------------------------------------------
		// step 1: create new base Experiment
		// -----------------------------------------------
		Experiment merged = exp1.duplicate();
		
		final ExperimentConfiguration configuration = new ExperimentConfiguration();
		configuration.setName( exp1.getName() + " & " + exp2.getName() );
		configuration.searchPaths = exp1.getConfiguration().searchPaths;
		
		merged.setConfiguration( configuration );

		// Add tree1, walk tree2 & add; just CCT/Flat
		RootScope rootScope = new RootScope(merged, "Merged Experiment","Invisible Outer Root Scope", RootScopeType.Invisible);
		merged.setRootScope(rootScope);
				
		// -----------------------------------------------
		// step 2: combine all metrics
		// -----------------------------------------------
		List<BaseMetric> metrics = buildMetricList(merged, exp1.getMetrics(), exp2.getMetrics());
		merged.setMetrics(metrics);
		
		if (with_raw_metrics)
		{
			final MetricRaw metricRaw[] = buildMetricRaws( exp1.getMetricRaw(), exp2.getMetricRaw() );
			merged.setMetricRaw(metricRaw);
		}

		// -----------------------------------------------
		// step 3: mark the new experiment file
		// -----------------------------------------------

		final File fileMerged  = new File( parent_dir + File.separator + Constants.DATABASE_FILENAME); 
		merged.setXMLExperimentFile( fileMerged );

		// -----------------------------------------------
		// step 4: create roots
		// -----------------------------------------------		

		int root_type = 0;
		switch (type) {
		case TOP_DOWN:
			root_type = 0; break;
		case BOTTOM_UP:
			root_type = 1; break;
		case FLAT:
			root_type = 2; break;
		}
		
		RootScope root2 = (RootScope) exp2.getRootScopeChildren()[root_type];	

		RootScope root2_copy = new RootScope(root2.getExperiment(), 
				"copy root 2","Invisible Outer Root Scope", RootScopeType.Invisible);

		DuplicateScopeTreesVisitor visitor = new DuplicateScopeTreesVisitor(root2_copy);
		root2.dfsVisitScopeTree(visitor);
		
		// -----------------------------------------------
		// step 5: merge the two experiments
		// -----------------------------------------------

		mergeScopeTrees(exp1,new DuplicateScopeTreesVisitor(rootScope), root_type);		
		
		RootScope root1 = (RootScope) merged.getRootScopeChildren()[0];	
		RootScope root2_copy_cct = (RootScope) root2_copy.getChildAt(0);

		final int metricCount = exp1.getMetricCount();
		new TreeSimilarity(metricCount, root1, root2_copy_cct, verbose);
		
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
	private static ArrayList<BaseMetric> buildMetricList(Experiment exp, BaseMetric[] m1, BaseMetric[] m2) 
	{
		final ArrayList<BaseMetric> metricList = new ArrayList<BaseMetric>( m1.length + m2.length );
		
		// ----------------------------------------------------------------
		// step 1: add the first metrics into the merged experiment
		// ----------------------------------------------------------------
		for (int i=0; i<m1.length; i++) {
			// add metric into the merged list
			BaseMetric mm = m1[i].duplicate();
			
			setMetricCombinedName(1, mm);
			
			metricList.add(mm);
		}
		
		// attention: hpcprof doesn't guarantee the ID of metric starts with zero
		//	we should make sure that the next ID for the second group of metrics is not
		//	overlapped with the ID from the first group of metrics
		final int m1_last = m1.length - 1;
		final int m1_last_shortname = Integer.valueOf(m1[m1_last].getShortName());
		int m1_last_index = Math.max(m1_last_shortname, m1[m1_last].getIndex()) + 1;
		
		// ----------------------------------------------------------------
		// step 2: append the second metrics, and reset the index and the key
		// ----------------------------------------------------------------
		for (int i=0; i<m2.length; i++) {
			final BaseMetric m = m2[i].duplicate();

			setMetricCombinedName(2, m);
			
			// recompute the index of the metric from the second experiment
			final int index_new = m1_last_index + m.getIndex();
			m.setIndex( index_new );
			
			// reset the key
			m.setShortName( String.valueOf(index_new) );
			
			metricList.add(m);
		}
		
		return metricList;
	}


	/***
	 * recursively merge trees
	 * 
	 * @param exp2
	 * @param visitor
	 */
	private static void mergeScopeTrees(Experiment exp2, 
			BaseDuplicateScopeTreesVisitor visitor, int iRoot) {

		RootScope root2 = (RootScope) exp2.getRootScopeChildren()[iRoot];		

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

