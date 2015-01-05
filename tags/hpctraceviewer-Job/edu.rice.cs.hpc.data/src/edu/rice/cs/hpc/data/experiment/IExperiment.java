package edu.rice.cs.hpc.data.experiment;

import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;

public interface IExperiment {

	
	public void setRootScope(Scope rootScope);
	public Scope getRootScope();
	
	public RootScope getCallerTreeRoot();
	
	public TreeNode[] getRootScopeChildren();
	
	public IExperiment duplicate();
	
}
