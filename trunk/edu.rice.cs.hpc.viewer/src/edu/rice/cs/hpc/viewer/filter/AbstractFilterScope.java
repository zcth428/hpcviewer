package edu.rice.cs.hpc.viewer.filter;

import java.util.ArrayList;

import edu.rice.cs.hpc.data.experiment.scope.Scope;

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
    public Object[] filter(Object []scopes)
    {
		FilterMap filter = FilterMap.getInstance();
		if (!filter.isFilterEnabled() || scopes == null)
			return scopes;
		
		// check the element if it has to be excluded, we need to skip the element
		// and return its descendants 
		final ArrayList<Object> list = new ArrayList<Object>();
		for (Object child : scopes)
		{
			Scope node = (Scope) child;
			if (filter.select(node.getName()))
			{
				// the child is included, we're fine
				list.add(node);
			} else {
				// check whether we can skip the node
				if (!hasToSkip(node))
				{
					// we shouldn't skip the children. Let's filter the children.
					// We need recursively check whether its children can be included
					Object []descendants = getChildren(node);
					if (descendants != null)
					{
						for(Object descendant : descendants)
						{
							list.add(descendant);
						}
					}
				}
			}
		}
		return list.toArray();
    }

    /****
     * get an array of the scope's children
     * 
     * @param scope : the parent scope
     * @return scope's children
     */
    abstract protected Object[] getChildren(Scope scope);
    
    /****
     * flag whether the scope and its children need to be skipped or not
     * For Callers view and Flat view, we should skip a filtered procedure
     * 
     * @param scope
     * @return boolean true if we need to skip. false otherwise
     */
    abstract protected boolean hasToSkip(Scope scope);
    
}
