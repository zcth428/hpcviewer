package edu.rice.cs.hpc.viewer.scope;

import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.filter.AbstractFilterScope;

public class FlatViewContentProvider extends AbstractContentProvider {

	@Override
	protected AbstractFilterScope getFilter() {
		return new FilterScope();
	}

    private class FilterScope extends AbstractFilterScope
    {

		@Override
		protected Object[] getChildren(Scope scope) {
			return FlatViewContentProvider.this.getChildren(scope);
		}

		@Override
		protected boolean hasToSkip(Scope scope) {
			if (scope instanceof ProcedureScope) {
				return true;
			}
			return false;
		}
    }    

}
