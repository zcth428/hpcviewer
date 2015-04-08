package edu.rice.cs.hpc.viewer.scope.flat;

import edu.rice.cs.hpc.common.filter.FilterAttribute;
import edu.rice.cs.hpc.common.filter.FilterAttribute.Type;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.filter.AbstractFilterScope;
import edu.rice.cs.hpc.viewer.scope.AbstractContentProvider;

public class FlatViewContentProvider extends AbstractContentProvider {

	@Override
	protected AbstractFilterScope getFilter(BaseExperiment experiment) {
		return new FilterScope();
	}

    private class FilterScope extends AbstractFilterScope
    {


		@Override
		protected boolean hasToSkip(Scope scope, FilterAttribute.Type filterType) {
			if (scope instanceof ProcedureScope) {
				return true;
			}
			return false;
		}

		@Override
		protected Object[] getChildren(Scope scope, Type filterType) {
			return FlatViewContentProvider.this.getChildren(scope);
		}

		@Override
		protected void merge(Scope parent, Scope child, Type filterType) {
			// TODO Auto-generated method stub
			
		}
    }    

}
