package edu.rice.cs.hpc.data.experiment;

import java.io.File;

import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;


/***
 * Base abstract experiment that handle only call path.
 * 
 * No metric is associated in this experiment
 *
 */
public abstract class BaseExperiment implements IExperiment {


	/** The experiment's configuration. */
	protected ExperimentConfiguration configuration;

	protected RootScope rootScope;

	/** version of the database **/
	protected String version;

	private TraceAttribute attribute;

	/** The directory from which to resolve relative source file paths. */
	protected File defaultDirectory;

	
	/****
	 * constructor to create an experiment
	 * 
	 * @param filename: the filename of the database (*.xml file)
	 */
	public BaseExperiment(File filename) {
		// protect ourselves against filename being `foo' with no parent
		// information whatsoever.
		this.defaultDirectory = filename.getAbsoluteFile().getParentFile();
	}
	
	
	/***
	 * the root scope of the experiment
	 * 
	 * @param the root scope
	 */
	public void setRootScope(Scope rootScope) {
		this.rootScope = (RootScope) rootScope;
	}

	
	/***
	 * retrieve the root scope
	 */
	public Scope getRootScope() {
		return rootScope;
	}

	
	/****
	 * retrieve the root scope of caller tree (bottom-up view)
	 * 
	 * @return root scope
	 */
	public RootScope getCallerTreeRoot() {
		
		if (this.rootScope.getSubscopeCount()==3) {
			
			Scope scope = this.rootScope.getSubscope(1);
			if (scope instanceof RootScope)
				return (RootScope) scope;
			
		}
		return null;
	}

	

	public TreeNode[] getRootScopeChildren() {
		return this.rootScope.getChildren();
	}
	
	
	public void open(File fileExperiment)
			throws	Exception
	{
		// parsing may throw exceptions
		new ExperimentFileXML().parse(fileExperiment, this, false);
	}
	
	


	public void setVersion (String v) 
	{
		this.version = v;
	}

	public void setTraceAttribute(TraceAttribute _attribute) {
		this.attribute = _attribute;
	}


	public TraceAttribute getTraceAttribute() {
		return this.attribute;
	}



/*************************************************************************
 *	Returns the name of the experiment.
 ************************************************************************/
	
public String getName()
{
	return this.configuration.getName();
}





/*************************************************************************
 *	Sets the experiment's configuration.
 *
 *	This method is to be called only once, during <code>Experiment.open</code>.
 *
 ************************************************************************/
	
public void setConfiguration(ExperimentConfiguration configuration)
{
	this.configuration = configuration;
}

/*************************************************************************
 *	Returns the default directory from which to resolve relative paths.
 ************************************************************************/
	
public File getDefaultDirectory()
{
	return this.defaultDirectory;
}




}
