package edu.rice.cs.hpc.data.experiment;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public interface IExperiment {

	
	public void setRootScope(Scope rootScope);
	public Scope getRootScope();
	
	public RootScope getCallerTreeRoot();
	
	public Object[] getRootScopeChildren();
	
	public IExperiment duplicate();
	
}
