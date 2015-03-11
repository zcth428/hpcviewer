package edu.rice.cs.hpc.viewer.scope;

import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.filter.AbstractFilterScope;

/************************************************************************
 * 
 * content provider for CCT view
 *
 ************************************************************************/
public class ScopeTreeContentProvider extends AbstractContentProvider 
{

	@Override
	protected AbstractFilterScope getFilter() {

		return new AbstractFilterScope() {
			
			@Override
			protected boolean hasToSkip(Scope scope) {
				return false;
			}
			
			@Override
			protected Object[] getChildren(Scope scope) {
				return ScopeTreeContentProvider.this.getChildren(scope);
			}
		};
	}
}