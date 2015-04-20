package edu.rice.cs.hpc.data.experiment;

import java.io.File;
import java.io.InputStream;

import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.TreeNode;
import edu.rice.cs.hpc.data.experiment.scope.visitors.DisposeResourcesVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpc.data.filter.IFilterData;
import edu.rice.cs.hpc.data.util.IUserData;


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
	
	/*** filter version of root scope ***/
	protected RootScope rootFilterScope;

	/** version of the database **/
	protected String version;

	protected ExperimentFileXML fileXML;

	private TraceAttribute attribute;
	
	private IFilterData filter;
	
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
		if (filter != null && filter.isFilterEnabled() && rootFilterScope != null) {
			return rootFilterScope;
		}
		return rootScope;
	}

	
	/****
	 * retrieve the root scope of caller tree (bottom-up view)
	 * 
	 * @return root scope
	 */
	public RootScope getCallerTreeRoot() {
		RootScope root = (RootScope) getRootScope();
		if (root.getSubscopeCount()==3) {
			
			Scope scope = root.getSubscope(1);
			if (scope instanceof RootScope)
				return (RootScope) scope;
			
		}
		return null;
	}

	

	public TreeNode[] getRootScopeChildren() {
		RootScope root = (RootScope) getRootScope();

		if (root != null)
			return root.getChildren();
		else
			return null;
	}
	
	
	/****
	 * open a database
	 * @param fileExperiment
	 * @param userData
	 * @throws Exception
	 */
	public void open(File fileExperiment, IUserData<String, String> userData, boolean need_metric)
			throws	Exception
	{
		// protect ourselves against filename being `foo' with no parent
		// information whatsoever.
		//this.fileExperiment = fileExperiment;
		
		if (fileXML == null) {
			fileXML = new ExperimentFileXML();
		}
		fileXML.parse(fileExperiment, this, need_metric, userData);		
	}
	
	
	/******
	 * This method is used for opening XML from a remote machine
	 *  
	 * @param expStream : remote input stream
	 * @param userData : customized user data
	 * @param name : the remote directory
	 * @throws Exception 
	 *****/
	public void open(InputStream expStream, IUserData<String, String> userData,
		String name) throws Exception {
	
		if (fileXML == null) {
			fileXML = new ExperimentFileXML();
		}
		fileXML.parse(expStream, name, this, false, userData);
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
	return configuration.getName(ExperimentConfiguration.NAME_EXPERIMENT);
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

public ExperimentConfiguration getConfiguration()
{
	return this.configuration;
}


/*************************************************************************
 *	Returns the default directory from which to resolve relative paths.
 ************************************************************************/
	
public File getDefaultDirectory()
{
	return getXMLExperimentFile().getParentFile();
}

public File getXMLExperimentFile() {
	return this.fileXML.getFile();
}



public void dispose()
{
	DisposeResourcesVisitor visitor = new DisposeResourcesVisitor();
	rootScope.dfsVisitScopeTree(visitor);
	this.rootScope = null;
	
	if (rootFilterScope != null)
	{
		rootFilterScope.dfsVisitScopeTree(visitor);
		rootFilterScope = null;
	}
}


/*************************************************************************
 * Filter the cct 
 * <p>caller needs to call postprocess to ensure the callers tree and flat
 * tree are alsi filtered </p>
 * @param filter
 *************************************************************************/
public void filter(IFilterData filter)
{
	this.filter     = filter;
	// create the invisible main root
	rootFilterScope = new RootScope(this,  rootScope.getName(), rootScope.getType());
	rootFilterScope.setExperiment(this);
	
	// duplicate and filter the cct
	RootScope rootCCT 		   = (RootScope) rootScope.getChildAt(0);
	FilterScopeVisitor visitor = new FilterScopeVisitor(rootFilterScope, rootCCT, filter);
	rootCCT.dfsVisitFilterScopeTree(visitor);
	
	if (rootCCT.getType() == RootScopeType.CallingContextTree) {
		filter_finalize(rootFilterScope, (RootScope) rootFilterScope.getChildAt(0), filter);
	}
}

/************************************************************************
 * In case the experiment has a CCT, continue to create callers tree and
 * flat tree for the finalization.
 * 
 * @param rootMain
 * @param rootCCT
 * @param filter
 ************************************************************************/
abstract protected void filter_finalize(RootScope rootMain, RootScope rootCCT, IFilterData filter);

}
