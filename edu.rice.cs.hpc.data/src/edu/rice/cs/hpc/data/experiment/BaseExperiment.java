package edu.rice.cs.hpc.data.experiment;

import java.io.File;
import java.io.InputStream;

import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.visitors.DisposeResourcesVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpc.data.filter.IFilterData;
import edu.rice.cs.hpc.data.util.IUserData;


/***
 * Base abstract experiment that handle only call path.
 * 
 * No metric is associated in this experiment
 *
 */
public abstract class BaseExperiment implements IExperiment 
{
	/** The experiment's configuration. */
	protected ExperimentConfiguration configuration;

	protected RootScope rootScope;
	
	/** version of the database **/
	protected String version;

	private TraceAttribute attribute;

	protected IDatabaseRepresentation databaseRepresentation;
	
	/***
	 * the root scope of the experiment
	 * 
	 * @param the root scope
	 */
	public void setRootScope(Scope rootScope) {
		this.rootScope = (RootScope) rootScope;
	}

	
	/***
	 * retrieve the root scope.
	 * 
	 * @return the root scope
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
		RootScope root = (RootScope) getRootScope();
		if (root.getSubscopeCount()==3) {
			
			Scope scope = root.getSubscope(1);
			if (scope instanceof RootScope)
				return (RootScope) scope;
			
		}
		return null;
	}

	

	public Object[] getRootScopeChildren() {
		RootScope root = (RootScope) getRootScope();

		if (root != null)
			return root.getChildren();
		else
			return null;
	}
	
	
	/****
	 * open a local database
	 * 
	 * @param fileExperiment : file of the experiment xml
	 * @param userData : map of user preferences
	 * @param need_metric : whether we need to assign metrics or not
	 * @throws Exception
	 */
	public void open(File fileExperiment, IUserData<String, String> userData, boolean need_metric)
			throws	Exception
	{
		databaseRepresentation = new LocalDatabaseRepresentation(fileExperiment, this, userData, need_metric);
		databaseRepresentation.open();
		open_finalize();
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
		databaseRepresentation = new RemoteDatabaseRepresentation(this, expStream, userData, name);
		databaseRepresentation.open();
		open_finalize();
	}

	/******
	 * Reopening and reread the database and refresh the tree.<br/>
	 * If the database has not been opened, it throws an exception.
	 * @throws Exception
	 */
	public void reopen() throws Exception
	{
		if (databaseRepresentation != null)
		{
			databaseRepresentation.open();
			open_finalize();
		} else {
			throw new Exception("Database has not been opened.");
		}
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
		return databaseRepresentation.getXMLFile().getFile();
	}


	/*****
	 * disposing the experiment resources.
	 */
	public void dispose()
	{
		DisposeResourcesVisitor visitor = new DisposeResourcesVisitor();
		rootScope.dfsVisitScopeTree(visitor);
		this.rootScope = null;
	}


	/*************************************************************************
	 * Filter the cct 
	 * <p>caller needs to call postprocess to ensure the callers tree and flat
	 * tree are alsi filtered </p>
	 * @param filter
	 *************************************************************************/
	public void filter(IFilterData filter)
	{
		// TODO :  we assume the first child is the CCT
		final RootScope rootCCT = (RootScope) rootScope.getChildAt(0);

		// duplicate and filter the cct
		FilterScopeVisitor visitor = new FilterScopeVisitor(rootCCT, filter);
		rootCCT.dfsVisitFilterScopeTree(visitor);

		if (rootCCT.getType() == RootScopeType.CallingContextTree) {
			filter_finalize(rootCCT, filter);
		}
	}

	/************************************************************************
	 * In case the experiment has a CCT, continue to create callers tree and
	 * flat tree for the finalization.
	 * 
	 * @param rootCCT
	 * @param filter
	 ************************************************************************/
	abstract protected void filter_finalize(RootScope rootMain, IFilterData filter);

	abstract protected void open_finalize();

}
