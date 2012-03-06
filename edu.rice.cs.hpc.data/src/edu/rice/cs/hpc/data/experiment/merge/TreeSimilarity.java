package edu.rice.cs.hpc.data.experiment.merge;

import java.util.Arrays;
import java.util.Comparator;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;

/******************************************************
 * 
 * Check similarity of two trees
 * 
 *
 ******************************************************/
public class TreeSimilarity {

	final private int MIN_DISTANCE_LOC = 3;
	final private float MIN_DISTANCE_METRIC = (float) 0.15;
	
	// init value of score. 
	// score can be incremented if the confidence of similarity is high,
	// score is decreased if the confidence is lower
	final private int SCORE_INIT = 100;
	
	// maximum score of metric similarity
	final private int SCORE_METRIC_INIT = 100;
	
	// weight (the importance) of the distance of the location
	// for some application, the distance is not important (due to inlining)
	final private float LOC_DISTANCE_WEIGHT = (float) 0.2;
	
	private enum SimilarityType{ SAME, SIMILARY, DIFF }
	
	
	/********
	 * construct similarity class
	 * 
	 * @param offset: metric offset
	 * @param target: the target root scope. the target scope has to be a tree,
	 * 				  it cannot be empty
	 * @param source: the source root scope
	 * 
	 */
	public TreeSimilarity(int offset, RootScope target, RootScope source)
	{
		// merge the root scope
		mergeMetrics(target, source, offset);
		
		// merge the children of the root (tree)
		mergeTree(target, source, offset);
	}
	
	
	
	/****
	 * check similarity between 2 trees, and merge them into the
	 * target tree
	 * 
	 * @param target
	 * @param source
	 */
	private void mergeTree( Scope target, Scope source, int metricOffset)
	{
		TreeNode childrenSource[] = source.getChildren();
		
		// ------------------------------------------------------------
		// case 1: if the source has no children. no need to continue
		// ------------------------------------------------------------
		if (childrenSource == null)
			return;
		
		final Scope sortedSource[] = sortArrayOfNodes( childrenSource );
		
		TreeNode childrenTarget[] = target.getChildren();
		
		// ------------------------------------------------------------
		// case 2: if the target has no children, just add from the source
		// ------------------------------------------------------------
		if (childrenTarget == null) 
		{
			for (Scope childSource: sortedSource)
			{
				addNode(target, childSource);
			}
			return;
		}
		final Scope sortedTarget[] = sortArrayOfNodes( childrenTarget );
		
		// ------------------------------------------------------------
		// case 3: both target and source have children
		// ------------------------------------------------------------
		
		// 3.a: initialize counter to mark that the children source scope hasn't been merged
		for (Scope childSource: sortedSource)
		{
			childSource.setCounter(0);
		}
		
		// 3.b: check for all children in target and source if they are similar
		for (Scope childTarget: sortedTarget) 
		{
			// check if one of the child in the source is similar
			for (Scope childSource: sortedSource)
			{				
				// check if the source has been merged or not
				if (childSource.isCounterZero())
				{
					// check if the scopes are similar
					if (mergeNode(childTarget, childSource, metricOffset))
					{
						// merge the children
						mergeTree( childTarget, childSource, metricOffset );
						break;
					}
				}
			}			
		}
		
		// 3.c: add the remainder scopes that are not merged
		for (Scope childSource: sortedSource) 
		{
			if (childSource.isCounterZero())
			{
				final Scope child = addNode(target, childSource);
				mergeMetrics(child, childSource, metricOffset);
			}
		}
	}
	
	/******
	 * sort an array of nodes
	 * 
	 * @param nodes
	 * @return sorted nodes
	 */
	private Scope [] sortArrayOfNodes(TreeNode []nodes)
	{
		Scope sorted[] = new Scope[nodes.length];
		System.arraycopy(nodes, 0, sorted, 0, nodes.length);
		
		Arrays.sort(sorted, new CompareScope() );
		
		return sorted;
	}
	
	
	/****
	 * merge 2 nodes if they have similarity
	 * @param target
	 * @param source
	 * @return
	 */
	private boolean mergeNode(Scope target, Scope source, int metricOffset)
	{
		final Similarity similar = checkNodesSimilarity( target, source);
		
		if ( (similar.type == SimilarityType.SAME) ||
				(similar.type == SimilarityType.SIMILARY && similar.score>100) )
		{
			// merge the metric
			mergeMetrics(target, source, metricOffset);
			
			// mark the source has been merged
			source.incrementCounter();
			
			return true;
		}
		return false;
	}
	
	
	/****
	 * verify if 2 scopes are exactly the same, almost similar, or completely different.
	 * the highest the score of similarity, the more likely they are similar
	 * 
	 * @param s1
	 * @param s2
	 * @return similarity of the two scopes
	 */
	private Similarity checkNodesSimilarity( Scope s1, Scope s2)
	{
		// check the location
		final boolean same_loc = s1.getFirstLineNumber() == s2.getFirstLineNumber();
		
		// check the adjacency of the location
		final int loc_distance = Math.abs(s1.getFirstLineNumber() - s2.getFirstLineNumber());
		final boolean similar_loc =  loc_distance < MIN_DISTANCE_LOC;
		
		// check the type
		final Class<? extends Scope> c1 = s1.getClass();
		final Class<? extends Scope> c2 = s2.getClass();
		final boolean same_type = (c1 == c2);
		
		// check the metrics
		final float v1 = getAnnotationValue(s1);
		final float v2 = getAnnotationValue(s2);
		final float metric_distance = (float) (Math.abs(v2-v1));
		final boolean similar_metric = ( metric_distance < MIN_DISTANCE_METRIC );

		// check if it's the same name
		final boolean same_name = s1.getName().equals(s2.getName());
		
		final boolean same_children = checkChildrenSimilarity(s1, s2);
		
		Similarity result = new Similarity();

		result.score = (int) (SCORE_INIT + (1-metric_distance) * SCORE_METRIC_INIT);
		result.score -= (loc_distance * LOC_DISTANCE_WEIGHT);

		if (same_type && same_name && similar_metric && same_loc && same_children)
		{
			// we are confident enough that the two scopes are similar
			result.type = SimilarityType.SAME;
		}
		else if (same_type && similar_metric && same_children)
		{
			// we are not confident, but it look like they are similar
			// in this case, the caller has to check if other combinations exist
			result.type = SimilarityType.SIMILARY;
		}
		else if (same_type && similar_metric && same_name && similar_loc)
		{
			// not the same children, but the same location, same metric, same name
			// probably the code has been inlined / outlined
			result.type = SimilarityType.SIMILARY;
		} 
		else
		{
			// we are sure they are not the same
			result.type = SimilarityType.DIFF;
		}
		
		System.out.println("TS " + s1 + " ("+ s1.getCCTIndex() +") vs. " + s2 +   " ("+ s2.getCCTIndex() 
					+ ") : d="+loc_distance+" ("+similar_loc+")" + 
					", c: " + same_type+ ", md: " + metric_distance+ " (" +similar_metric+")" +
					", sn: " + same_name + ", sc: " + same_children+ " rs: " + result.score +" (" 
					+ result.type+ ")");
	
		
		return result;
	}

	
	/****
	 * check if two scopes have the same children
	 * we just use 2 simple properties: 
	 * - it is the same if the number of children are the same, or
	 * - if at least once child is exactly the same
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean checkChildrenSimilarity(Scope s1, Scope s2)
	{
		int c1 = s1.getChildCount();
		int c2 = s2.getChildCount();
		
		// same number of children ?
		if (c1 == c2)
			return true;
		
		// is there a child that exactly the same ?
		for (int i=0; i<c1; i++)
		{
			final Scope cs1 = s1.getSubscope(i);
			
			for (int j=0; j<c2; j++) 
			{
				final Scope cs2 = s2.getSubscope(j);
				
				Similarity s = checkNodesSimilarity(cs1, cs2);
				if (s.type == SimilarityType.SAME)
					return true;
			}
		}
		return false;
	}
	
	/****
	 * find the "value" of a scope. We expect a value to be a relative value
	 * of a scope, compared its siblings. This can be a percentage, or others.
	 * 
	 * @param s
	 * @return
	 */
	private float getAnnotationValue(Scope s)
	{
		final MetricValue mv = s.getMetricValue(0);
		if (MetricValue.isAnnotationAvailable(mv)) 
		{
			return MetricValue.getAnnotationValue(mv);
		}
		else 
		{
			return MetricValue.getValue(mv);
		}
	}
	
	/***
	 * add a child node to the parent
	 * @param parent
	 * @param node
	 * 
	 * @return the new child
	 */
	private Scope addNode(Scope parent, Scope node) 
	{
		Scope copy = node.duplicate();
		parent.addSubscope(copy);
		copy.setParentScope(parent);
		copy.setExperiment( parent.getExperiment() );
		
		return copy;
	}
	
	/****
	 * merging two nodes, and copy the metric
	 * 
	 * @param target
	 * @param source
	 */
	private void mergeMetrics(Scope target, Scope source, int metricOffset)
	{
		source.copyMetrics(target, metricOffset);
	}
	
	
	private class Similarity 
	{
		SimilarityType type;
		int score;
	}
	
	
	private class CompareScope implements Comparator<Scope> 
	{
		@Override
		public int compare(Scope s1, Scope s2) {
			return (int) (s1.getMetricValue(0).getValue() - s2.getMetricValue(0).getValue());
		}
		
	}
	
}
