package edu.rice.cs.hpc.viewer.filter;

import java.util.ArrayList;
import java.util.Arrays;

import edu.rice.cs.hpc.common.filter.FilterAttribute;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.filter.service.FilterMap;

/*********************************************************************************
 * 
 * abstract class to filter a scope
 * 
 * If a scope is to be excluded, first we check whether we need to scope the scope or not
 * For callers and flat view, if the scope is an instance of ProcedureScope, we should
 *   skip all its children as well. Excluding a procedure but including its children will
 *   be illogical.
 * Second, if the scope is not to be skipped, we should check the children as well:
 *   we'll exclude its children that fit the filter. 
 *   If all children are excluded, then we return null (everything is excluded).
 *
 *********************************************************************************/
public abstract class AbstractFilterScope 
{
	/******
	 * Filtering a list of nodes if the filter is enabled
	 * If the filter is disabled, we return the list itself.
	 * 
	 * 
	 * @param scopes : a list of nodes
	 * @return a list of filtered nodes
	 */
    public Object[] filter(Object parent, Object []scopes)
    {
		FilterMap filterMap = FilterMap.getInstance();
		return filter(filterMap, parent, scopes, 2);
    }

    /********
     * Filtering 
     * 
     * @param filterMap
     * @param parent
     * @param scopes
     * @param occurence
     * @return
     */
    private Object[] filter(FilterMap filterMap, Object parent, Object []scopes, int occurence)
    {
    	if (occurence > 0 && scopes != null)
    	{
			((Scope) parent).resetFilteredMetric();
    		final ArrayList<Object> list = new ArrayList<Object>();
    		
    		// check all the children of this scope. If a child is to be filtered, its values may need to be
    		// 	merged to the parent
    		for (Object child : scopes)
    		{
    			Scope node = (Scope) child;
        		// check the element: if it has to be excluded, we need to skip the element
        		// and return its descendants 
    			final FilterAttribute attribute = filterMap.getFilterAttribute(node.getName());
    			if (attribute == null)
    			{
    				// the child is included, we're fine
    				list.add(node);
    				
    				// due to Eclipse's bug (or feature) we need to check the grand children:
    				// the reason is that Eclipse we load a node and its children when needed,
    				// then decide to display the content of the children (the content of the current
    				//  node has been decided). To update the current content, we should be able
    				//  to use refreshElement() but unfortunately this API assumes that no change of
    				//  the number of children, and it always assume everything is the same except  
    				//  the value of the node.
    				// This of course wrong, and makes an empty row if the child has been filtered.
    				// The only solution is we need to check the descendants (3 levels not only 2)
    				filter(filterMap, node, node.getChildren(), occurence-1);
    				
    			} else {
    				// check whether we can skip the node and its descendants as well
    				// note: for inclusive filtering, we surely omit its descendants
    				if ((attribute.filterType == FilterAttribute.Type.Exclusive ) &&
    						!hasToSkip(node, attribute.filterType))
    				{
    					// we shouldn't skip the children. Let's filter the children.
    					// We need recursively check whether its children can be included
    					Object []descendants = getChildren(node, attribute.filterType);
    					list.addAll( Arrays.asList(descendants) );
    				}
    				// we omit this child, let merge the metric to the parent
    				merge((Scope)parent, node, attribute.filterType);
    			}
    		}
    		return list.toArray();
    	}
    	return null;
    }
    
    /**************
     * utility for the derived classes to merge a metric to other node
     * 
     * @param root
     * @param target
     * @param metric_exclusive_index
     * @param mvChild
     */
	public void mergeMetricToParent(RootScope root, Scope target, 
			int metric_exclusive_index, MetricValue mvChild)
	{
		MetricValue mvParentExc = target.getMetricValue(metric_exclusive_index);
		MetricValue mvParentFilteredExc = target.getFilteredMetric(metric_exclusive_index);
		float value = 0;
		if (mvParentFilteredExc.getValue() <= 0) {
			// the filtered value is not initialized, let assign with value of the exclusive metric
			value = mvParentExc.getValue();
		}
		// update the filtered value
		value             = mvParentFilteredExc.getValue() + mvChild.getValue();
		float rootValue   = root.getMetricValue(metric_exclusive_index).getValue();
		float annotation  = value / rootValue;
		MetricValue mv    = new MetricValue(value, annotation);
		target.setFilteredMetric(metric_exclusive_index, mv);
	}

    
    /****
     * get an array of the scope's children in case for exclusive filter
     * (for inclusive filter, there's no children)
     * 
     * @param scope : the parent scope
     * @return scope's children
     */
    abstract protected Object[] getChildren(Scope scope, FilterAttribute.Type filterType);
    
    /****
     * flag whether the scope and its children need to be skipped or not
     * For Callers view and Flat view, we should skip a filtered procedure
     * 
     * @param scope
     * @return boolean true if we need to skip. false otherwise
     */
    abstract protected boolean hasToSkip(Scope scope, FilterAttribute.Type filterType);
    
    
    abstract protected void merge(Scope parent, Scope child, FilterAttribute.Type filterType);

}
