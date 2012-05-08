package edu.rice.cs.hpc.data.experiment.merge;

import java.util.Arrays;
import java.util.Comparator;

import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.data.experiment.scope.visitors.DuplicateScopeTreesVisitor;

/******************************************************
 * 
 * Check similarity of two trees
 * 
 *
 ******************************************************/
public class TreeSimilarity {

	final private boolean debug = false;
	
	
	private enum SimilarityType{ SAME, SIMILAR, DIFF }
	
	
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
				addSubTree(target, childSource, metricOffset);
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
						// DFS: recursively, merge the children if they are similar
						// the recursion will stop when all children are different
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
				addSubTree(target, childSource, metricOffset);
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
				(similar.type == SimilarityType.SIMILAR) )
		{
			// merge the metric
			mergeMetrics(target, source, metricOffset);
			
			// mark the source has been merged
			source.incrementCounter();
			
			return true;
		}
		return false;
	}
	
	/***
	 * check similarity between two scopes without checking the children
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int getScopeSimilarityScore ( Scope s1, Scope s2 )
	{
		// check the adjacency of the location
		final int loc_distance = Math.abs(s1.getFirstLineNumber() - s2.getFirstLineNumber());
		
		// check the type
		final boolean same_type = areSameType( s1, s2 );
		
		// check the metrics
		final float metric_distance = getMetricDistance( s1, s2 );

		// check if it's the same name
		final boolean same_name = areSameName( s1, s2 );
		
		int score = (int) (Constants.SCORE_INIT + (1-metric_distance) * Constants.WEIGHT_METRIC);
		
		int score_loc = (int) Math.max(Constants.WEIGHT_LOCATION, loc_distance * Constants.WEIGHT_LOCATION_COEF);
		
		score -= score_loc;

		// hack: if both s1 and s2 are call sites, their name should be the same
		//	we need to be careful with false positive where similar metric, with similar children 
		//	and similar line number with different function name happens
		if (same_type && (s1 instanceof CallSiteScope))
		{
			score += (same_name? 3:-3) * Constants.WEIGHT_NAME;
		} else
		{
			score += (same_name ? Constants.WEIGHT_NAME : 0);
		}
		
		score += (same_type ? Constants.WEIGHT_TYPE : 0);
		
		return score;
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
		Similarity result = new Similarity();
		result.type = SimilarityType.DIFF;
		result.score = getScopeSimilarityScore( s1, s2 );
		
		// check if the children are the same
		result.score += getChildrenSimilarityScore( s1, s2 );

		if (result.score>260)
		{
			// we are confident enough that the two scopes are similar
			result.type = SimilarityType.SAME;
		}
		else if (result.score>200)
		{
			// we are not confident, but it look like they are similar
			// in this case, the caller has to check if other combinations exist
			result.type = SimilarityType.SIMILAR;
		}
		
		if (debug)
		{
			System.out.println("TS " + s1 + " [" + s1.getCCTIndex()+"] \tvs.\t " +s2  + " ["+ s2.getCCTIndex()
					+"]\t s: " + result.score +"\t t: " + result.type);
		}
		return result;
	}

	/***
	 * check if the string has an underscore suffix
	 * @param s
	 * @return
	 */
	private boolean hasUnderscoreSuffix(String s)
	{
		final int l = s.length();
		return (s.charAt( l - 1) == '_');
	}
	
	/**
	 * check if the name of two scopes are similar 
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean areSameName( Scope s1, Scope s2 )
	{
		if (s1 instanceof CallSiteScope && s2 instanceof CallSiteScope) 
		{
			final String n1 = s1.getName();
			final String n2 = s2.getName();
			
			int diff = Math.abs( n1.compareTo(n2) );
			if (diff == 1)
			{
				return (hasUnderscoreSuffix(n1) || hasUnderscoreSuffix(n2));
			}
			return (diff == 0);
		}
		else 
		{
			return s1.getName().equals(s2.getName());
		}
	}
	
	private boolean areSameType( Scope s1, Scope s2)
	{
		final Class<? extends Scope> c1 = s1.getClass();
		final Class<? extends Scope> c2 = s2.getClass();
		
		return (c1 == c2); 
	}
	
	private boolean areSameMetric( float metricDistance )
	{
		return ( metricDistance < Constants.MIN_DISTANCE_METRIC );
	}
	
	private float getMetricDistance( Scope s1, Scope s2 )
	{
		final float v1 = getAnnotationValue(s1);
		final float v2 = getAnnotationValue(s2);
		return (float) (Math.abs(v2-v1));
	}
	
	/****
	 * check if the two nodes are lexicographically the same: 
	 * 			 metric, type, name and location
	 * this function is different than checkNodesSimilarity() which is
	 * 	judged based on the similarity distance. 
	 * 
	 * warning:  this doesn't include the descedants
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean areSameNodes( Scope s1, Scope s2 )
	{
		final float m = getMetricDistance( s1, s2 );
		final boolean is_same_type = areSameType( s1, s2 );
		final boolean is_same_metric = areSameMetric( m );
		
		if (is_same_type && is_same_metric)
		{
			if ( areSameName( s1, s2 ) )
				// same name, same type and same metric: 
				//	definitely the same nodes
				return true;
			else if ( is_same_type &&  
					 (s1.getClass() == LoopScope.class || 
					  s1.getClass() == LineScope.class ) ) 
			{
				// the same type but different name:
				//  check for line scope and loop scope are in the same file
				return s1.getSourceFile().getName().equals(s2.getSourceFile().getName());
			}
		}
		
		return false;
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
	private boolean areSameChildren(Scope s1, Scope s2, int currDepth)
	{
		// check if we go too far or not
		if (currDepth > Constants.MAX_LEN_DFS)
			return false;
		
		// both should have the same children
		if (s1.getChildCount()==0 || s2.getChildCount()==0)
			return false;
		
		final Scope sortedS1[] = sortArrayOfNodes( s1.getChildren() );
		final Scope sortedS2[] = sortArrayOfNodes( s2.getChildren() );

		// we only check with limited number of children
		// there's no need to check all children
		int c1 = Math.min( Constants.MAX_LEN_BFS, sortedS1.length );
		int c2 = Math.min( Constants.MAX_LEN_BFS, sortedS2.length );
		
		// is there a child that is exactly the same ?
		for (int i=0; i<c1 ; i++)
		{
			final Scope cs1 = sortedS1[i];
			
			for (int j=0; j<c2; j++) 
			{
				final Scope cs2 = sortedS2[j];
				
				if (areSameNodes( cs1, cs2 ))
					return true;
				else 
				{ 	// not the same nodes: check their descendants
					// check if one (or all) of them are aliens
					final boolean cs1_is_alien = isAlien( cs1 );
					final boolean cs2_is_alien = isAlien( cs2 );
					Scope next1 = s1, next2 = s2;
					
					// for alien procedure: we will go deep to their children
					//	the number of depth will be limited to MAX_LEN_DFS
					if (cs1_is_alien)
						next1 = cs1;
					if (cs2_is_alien)
						next2 = cs2;
					
					if (cs1_is_alien || cs2_is_alien)
					{
						if (areSameChildren(next1, next2, currDepth + 1))
							return true;
					} else
					{						
						final boolean areLoops = (cs1 instanceof LoopScope && cs2 instanceof LoopScope);
						if (areLoops)
						{
							if (areSameChildren( cs1, cs2, currDepth + 1))
								return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/***
	 * return true if the scope is an alien
	 * 
	 * @param scope
	 * @return
	 */
	private boolean isAlien( Scope scope )
	{
		if (scope instanceof ProcedureScope)
		{
			return ((ProcedureScope) scope).isAlien();
		}
		return false;
	}
	
	/****
	 * return the score for children similarity
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	private int getChildrenSimilarityScore( Scope s1, Scope s2 )
	{
		final boolean is_same = areSameChildren( s1, s2, 0 );
		int score = 0;
		if (is_same)
		{
			score = Constants.WEIGHT_CHILDREN;
		}
		return score;
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
	
	/****
	 * recursively add subtree (and the metrics) to the parent
	 * @param parent : parent target
	 * @param node : source nodes to be copied
	 * @param metricOffset : offset of the metric
	 */
	private void addSubTree(Scope parent, Scope node, int metricOffset)
	{
		DuplicateScopeTreesVisitor visitor = new DuplicateScopeTreesVisitor(parent, metricOffset);
		node.dfsVisitScopeTree(visitor);
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
	
	
	/***
	 * Reverse order comparison to sort array of scopes based on their first metric
	 * this comparison has problem when the two first metrics are equal, but
	 * it's closed enough to our needs. I don't think we need more sophisticated stuff
	 */
	private class CompareScope implements Comparator<Scope> 
	{
		////@Override
		public int compare(Scope s1, Scope s2) {
			return (int) (s2.getMetricValue(0).getValue() - s1.getMetricValue(0).getValue());
		}
	}
	
	private class Constants 
	{
		static final private float MIN_DISTANCE_METRIC = (float) 0.15;
		
		// init value of score. 
		// score can be incremented if the confidence of similarity is high,
		// score is decreased if the confidence is lower
		static final private int SCORE_INIT = 100;
		
		// ------------------------------------------------------------------------
		// weight of different parameters: metric, location, name, children, ..
		// the higher the weight, the more important in similarity comparison
		// ------------------------------------------------------------------------
		// maximum score of metric similarity
		static final private int WEIGHT_METRIC = 100;
		
		// score if the two scopes are the same
		static final private int WEIGHT_NAME = 30;
		
		// score for the case with the same children
		static final private int WEIGHT_CHILDREN = 80;
		
		// same types (loop vs. loop, line vs. line, ...)
		static final private int WEIGHT_TYPE = 20;
		
		// weight (the importance) of the distance of the location
		// for some application, the distance is not important (due to inlining)
		static final private float WEIGHT_LOCATION_COEF = (float) 0.2;
		static final private int WEIGHT_LOCATION = 20;
		
		// ------------------------------------------------------------------------
		// pruning the search of descendants to avoid long useless search
		// ------------------------------------------------------------------------
		
		// Maximum length of breadth first search
		static final private int MAX_LEN_BFS = 4;
		
		// maximum length of depth first search
		static final private int MAX_LEN_DFS = 4;
	}
}