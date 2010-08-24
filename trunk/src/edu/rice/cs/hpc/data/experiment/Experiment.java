//////////////////////////////////////////////////////////////////////////
//																		//
//	Experiment.java														//
//																		//
//	experiment.Experiment -- an open HPCView experiment					//
//	Last edited: January 15, 2002 at 12:37 am							//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment;


import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.filters.*;
import edu.rice.cs.hpc.data.experiment.scope.visitors.*;
import edu.rice.cs.hpc.data.experiment.source.*;
import edu.rice.cs.hpc.data.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.eclipse.jface.viewers.TreeNode;

//math expression
import com.graphbuilder.math.*;

//////////////////////////////////////////////////////////////////////////
//	CLASS EXPERIMENT													//
//////////////////////////////////////////////////////////////////////////

/**
 *
 * An HPCView experiment and its data.
 *
 */


public class Experiment
{

static final public int ROOT_CALLING_CONTEXT = 0;
static final public int ROOT_CALLER = 0;
static final public int ROOT_FLAT = 0;
	
/** The file containing the experiment. */
protected ExperimentFile experimentFile;

/** The directory from which to resolve relative source file paths. */
protected File defaultDirectory;

/**
 * The experiment (XML) file 
 * @author laksono
 */
protected File fileExperiment;

/** The experiment's configuration. */
protected ExperimentConfiguration configuration;

/** version of the database **/
protected String version;

/** ----------------- DICTIONARIES -----------------  **/
protected Hashtable<Integer, LoadModuleScope> hashLoadModuleTable;
protected HashMap<Integer,SourceFile> hashFileTable;


/** The experiment's metrics. */
protected Vector<BaseMetric> metricList;

/** The experiment's root scope. */
protected Scope rootScope;

/** A mapping from internal name strings to metric objects. */
protected HashMap metricMap;

//------------------------------------------------------------
// thread level database
//------------------------------------------------------------
private MetricRaw[] metrics_raw;
private ThreadLevelDataManager threadsData = null;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates an Experiment object from a file.
 *
 *	@param filename		A path to the file containing the experiment.
 *	@exception			IOException if file can't be opened for reading.
 *	@exception			InvalExperimentException if file contents are
 *							not a valid experiment.
 *
 ************************************************************************/
	
public Experiment(File filename)
{
	this.experimentFile   = ExperimentFile.makeFile(filename);
	this.fileExperiment = filename;
	// protect ourselves against filename being `foo' with no parent
	// information whatsoever.
	this.defaultDirectory = filename.getAbsoluteFile().getParentFile();
}


/*************************************************************************
 *	Creates a basic Experiment object from another Experiment
 ************************************************************************/
public Experiment(Experiment exp) 
{
	this.configuration = exp.configuration;
	this.defaultDirectory = exp.getDefaultDirectory();
	this.experimentFile = null;
	this.fileExperiment = exp.getXMLExperimentFile();
}



//////////////////////////////////////////////////////////////////////////
//	PERSISTENCE															//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Opens the experiment from its file.
 *
 *	@exception			IOException if experiment file can't be read.
 *	@exception			InvalExperimentException if file contents are
 *							not a valid experiment.
 *
 ************************************************************************/
	
public void open()
throws
	IOException,
	InvalExperimentException
{
	// parsing may throw exceptions
	this.experimentFile.parse(this);
}




/*************************************************************************
 *	Closes the experiment.
 ************************************************************************/
	
public void close()
{
	// nothing
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
 *	Sets the experiment's metric list.
 *
 *	This method is to be called only once, during initialization
 *
 ************************************************************************/
	
public void setMetrics(List metricList)
{
	this.metricList = new Vector(metricList);

	// initialize metric access data structures
	int count = metricList.size();
	this.metricMap = new HashMap(count);
	for( int k = 0;  k < count;  k++ )
	{	
		BaseMetric m = (BaseMetric)this.metricList.get(k);
		m.setIndex(k);
		this.metricMap.put(m.getShortName(), m);
	}
}


/*************************************************************************
 *  finalize the database
 *  we will reorder the ID of metrics in order to make it more make sense
 *  	for users (they don't really care the value, as long as it's in 
 *  	good simple ordered ID)
 *************************************************************************/
public void finalizeDatabase()
{
	this.metricMap.clear();
	int nbMetrics = this.metricList.size();
	for (int i=0; i<nbMetrics; i++) {
		BaseMetric m = (BaseMetric) this.metricList.get(i);
		String 	sID;		
		if (m instanceof AggregateMetric) {
			// for aggregate metric we don't reorder the ID
			sID = m.getShortName();
		} else {
			sID = String.valueOf(i);
			m.setShortName(sID);				// rename the ID
		}
		this.metricMap.put(sID, m);			// put it back into the map
	}
}


/*************************************************************************
 *	Sets the experiment's scope list and root scope.
 *
 *	This method is to be called only once, during <code>Experiment.open</code>.
 *
 ************************************************************************/
	
public void setScopes(List scopeList, Scope rootScope)
{
	this.rootScope = rootScope;
}


public void setVersion (String v) 
{
	this.version = v;
}

public int getMajorVersion()
{
	if (this.version == null)
		return 1;
	int ip = this.version.indexOf('.');
	return Integer.parseInt(this.version.substring(0, ip));
}

//////////////////////////////////////////////////////////////////////////
// Experiment Merging													//
//////////////////////////////////////////////////////////////////////////
public static Experiment merge(Experiment exp1, Experiment exp2)
{
	return new ExperimentMerger().merge(exp1, exp2);
}

//////////////////////////////////////////////////////////////////////////
// Postprocessing														//
//////////////////////////////////////////////////////////////////////////
protected void accumulateMetricsFromKids(Scope target, Scope source, MetricValuePropagationFilter filter) {
	int nkids = source.getSubscopeCount();
	for (int i = 0; i < nkids; i++) {
		Scope child = source.getSubscope(i);
		if (child instanceof LoopScope) {
			accumulateMetricsFromKids(target, child, filter);
		}
		target.accumulateMetrics(child, filter, this.getMetricCount());
	}
}

protected void copyMetric(Scope target, Scope source, int src_i, int targ_i, MetricValuePropagationFilter filter) {
	if (filter.doPropagation(source, target, src_i, targ_i)) {
		MetricValue mv = source.getMetricValue(src_i);
		if (mv != MetricValue.NONE && mv.getValue() != 0.0) {
			target.setMetricValue(targ_i, mv);
		}
	}
}

/*************************************************************************
 *	Adds a new scope subtree to the scope tree (& scope list)
 ************************************************************************/
public void beginScope(Scope scope)
{
	Scope top = this.getRootScope();
	top.addSubscope(scope);
	scope.setParentScope(top);
}


protected RootScope createCallersView(Scope callingContextViewRootScope)
{
	EmptyMetricValuePropagationFilter filter = new EmptyMetricValuePropagationFilter();

	RootScope callersViewRootScope = new RootScope(this,"Callers View","Callers View", RootScopeType.CallerTree);
	beginScope(callersViewRootScope);
	
	CallersViewScopeVisitor csv = new CallersViewScopeVisitor(this, callersViewRootScope, 
			this.getMetricCount(), false, filter);
	callingContextViewRootScope.dfsVisitScopeTree(csv);

	// compute the aggregate metrics
	// bug fix 2008.10.21 : we don't need to recompute the aggregate metrics here. Just copy it from the CCT
	//	This will solve the problem where there is only nested loops in the programs
	callersViewRootScope.accumulateMetrics(callingContextViewRootScope, filter, this.getMetricCount());
	return callersViewRootScope;
}

protected Scope createFlatView(Scope callingContextViewRootScope)
{
	Scope flatViewRootScope = new RootScope(this, "Flat View", "Flat View", RootScopeType.Flat);
	beginScope(flatViewRootScope);
	
	FlatViewScopeVisitor fv = new FlatViewScopeVisitor(this, (RootScope) flatViewRootScope);

	callingContextViewRootScope.dfsVisitScopeTree(fv);
	
	EmptyMetricValuePropagationFilter filter = new EmptyMetricValuePropagationFilter();
	flatViewRootScope.accumulateMetrics(callingContextViewRootScope, filter	, getMetricCount());

	return flatViewRootScope;
}



protected void addInclusiveMetrics(Scope scope, MetricValuePropagationFilter filter)
{
	InclusiveMetricsScopeVisitor isv = new InclusiveMetricsScopeVisitor(this, filter);
	scope.dfsVisitScopeTree(isv);
}

private void computeExclusiveMetrics(Scope scope) {
	ExclusiveCallingContextVisitor visitor = new ExclusiveCallingContextVisitor(this);
	scope.dfsVisitScopeTree(visitor);
}

protected void copyMetricsToPartner(Scope scope, MetricType sourceType, MetricValuePropagationFilter filter) {
	for (int i = 0; i< this.getMetricCount(); i++) {
		BaseMetric metric = (BaseMetric)this.getMetric(i);
		// Laksono 2009.12.11: aggregate metrc doesn't have partner
		if (metric instanceof Metric) {
			if (metric.getMetricType() == sourceType) {
				// laksono hack bug fix: the partner is "always" the next metric
				int partner = i+1; 
				copyMetric(scope, scope, i, partner, filter);
			}
		} else if (metric instanceof AggregateMetric) {
			if (metric.getMetricType() == MetricType.EXCLUSIVE ) {
				int partner = ((AggregateMetric)metric).getPartner();
				String partner_id = String.valueOf(partner);
				BaseMetric partner_metric = this.getMetric( partner_id );
				// case for old database: no partner information
				if (partner_metric != null) {
					MetricValue partner_value = scope.getMetricValue( partner_metric );
					scope.setMetricValue( i, partner_value);
				}
			}
		}
	}
}

protected void addPercents(Scope scope, RootScope totalScope)
{	
	PercentScopeVisitor psv = new PercentScopeVisitor(this.getMetricCount(), totalScope);
	scope.dfsVisitScopeTree(psv);
}

/**
 * generic post-processing: by default we show all views
 */
public void postprocess()  {
	this.postprocess(true);
}


/*****
 * return a tree root
 * @return
 */
public RootScope getCallerTreeRoot() {
	
	if (this.rootScope.getSubscopeCount()==3) {
		
		Scope scope = this.rootScope.getSubscope(1);
		if (scope instanceof RootScope)
			return (RootScope) scope;
		
	}
	return null;
}

/**
 * Post-processing for CCT:
 * @param:
 * 	callerView: compute caller view if true.
 * 
 * Step 1: normalizing CCT view
 *  - normalize line scope, which means to add the cost of line scope into call site scope
 *  - compute inclusive metrics for I
 *  - compute inclusive metrics for X
 *  Step 2: create call view (if enabled) and flat view
 */
public void postprocess(boolean callerView) {
	if (this.rootScope.getSubscopeCount() <= 0) return;
	// Get first scope subtree: CCT or Flat
	Scope firstSubTree = this.rootScope.getSubscope(0);
	if (!(firstSubTree instanceof RootScope)) return;
	RootScopeType firstRootType = ((RootScope)firstSubTree).getType();
	
	if (firstRootType.equals(RootScopeType.CallingContextTree)) {
		// accumulate, create views, percents, etc
		Scope callingContextViewRootScope = firstSubTree;

		EmptyMetricValuePropagationFilter emptyFilter = new EmptyMetricValuePropagationFilter();
		InclusiveOnlyMetricPropagationFilter rootInclProp = new InclusiveOnlyMetricPropagationFilter(this);

		//----------------------------------------------------------------------------------------------
		// Inclusive metrics
		//----------------------------------------------------------------------------------------------
		if (this.inclusiveNeeded()) {
			// TODO: if the metric is a derived metric then DO NOT do this process !
			addInclusiveMetrics(callingContextViewRootScope, rootInclProp);
			this.computeExclusiveMetrics(callingContextViewRootScope);
		}

		copyMetricsToPartner(callingContextViewRootScope, MetricType.INCLUSIVE, emptyFilter);

		//----------------------------------------------------------------------------------------------
		// Callers View
		//----------------------------------------------------------------------------------------------
		Scope callersViewRootScope = null;
		if (callerView) {
			callersViewRootScope = createCallersView(callingContextViewRootScope);
		}
		
		//----------------------------------------------------------------------------------------------
		// Flat View
		//----------------------------------------------------------------------------------------------
		Scope flatViewRootScope = null;
		// While creating the flat tree, we attribute the cost for procedure scopes
		// One the tree has been created, we compute the inclusive cost for other scopes
		flatViewRootScope = (RootScope) createFlatView(callingContextViewRootScope);

		//----------------------------------------------------------------------------------------------
		// FINALIZATION
		//----------------------------------------------------------------------------------------------
		if (callerView)	{												// caller view
				this.finalizeAggregateMetrics(callersViewRootScope);
				// bug fix 2010.06.17: move the percent after finalization
				addPercents(callersViewRootScope, (RootScope) callersViewRootScope);
		}
		
		this.finalizeAggregateMetrics(flatViewRootScope);				// flat view
		
		this.finalizeAggregateMetrics(callingContextViewRootScope);		// cct
		
		// Laks 2008.06.16: adjusting the percent based on the aggregate value in the calling context
		addPercents(callingContextViewRootScope, (RootScope) callingContextViewRootScope);
		addPercents(flatViewRootScope, (RootScope) callingContextViewRootScope);
		
	} else if (firstRootType.equals(RootScopeType.Flat)) {
		addPercents(firstSubTree, (RootScope) firstSubTree);
	} else {
		// ignore; do no nothing
	}
}

/**
 * check the existence of an aggregate metric  
 * If the metric is an aggregate, we need to initialize them !
 * @return
 */
private boolean checkExistenceOfDerivedIncr() {
	boolean isAggregate = false;
	for (int i=0; i<this.getMetricCount(); i++) {
		BaseMetric metric = this.getMetric(i);
		boolean is_aggregate = (metric instanceof AggregateMetric); 
		if (is_aggregate) {
			isAggregate |= is_aggregate;
			AggregateMetric aggMetric = (AggregateMetric) metric;
			// hack: initialize the metric here
			aggMetric.init(this);
		}
	}
	return isAggregate;
}


/**
 * finalizing metric values (only for aggregate metric from hpcprof-mpi)
 * @param root
 */
private void finalizeAggregateMetrics(Scope root) {
	if (! checkExistenceOfDerivedIncr())
		return;
	DerivedIncrementalVisitor diVisitor = new DerivedIncrementalVisitor(this.getMetrics());
	root.dfsVisitScopeTree(diVisitor);
}

/**
 * Check if an inclusive computation is needed or not
 * @return
 */
private boolean inclusiveNeeded() {
	boolean isNeeded = false;
	for (int i=0; !isNeeded && i<this.getMetricCount(); i++) {
		BaseMetric m = this.getMetric(i);
		isNeeded = !( (m instanceof FinalMetric) || (m instanceof AggregateMetric) );//.getMetricType() != MetricType.PREAGGREGATE;
	}
	return isNeeded;
}
//////////////////////////////////////////////////////////////////////////
//Compute Derived Metrics												//
//////////////////////////////////////////////////////////////////////////

/**
 * Create a derived metric based on formula expression
 * @param scopeRoot
 * @param expFormula
 * @return
 */
public DerivedMetric addDerivedMetric(RootScope scopeRoot, Expression expFormula, String sName, 
		boolean bPercent, MetricType metricType) {
	
	// laks 2010.02.27: for aggregate metric, we need to know the ID of the last metric, then increment this ID
	//					for the new metric
	// if the last metric has index 7 and ID 10, then the new metric has index 8 and ID 11
	int metricLastIndex = this.getMetricCount() -1;
	BaseMetric metricLast = this.getMetric(metricLastIndex);
	String metricLastID = metricLast.getShortName();
	metricLastIndex = Integer.valueOf(metricLastID) + 1;
	metricLastID = String.valueOf(metricLastIndex);
	
	DerivedMetric objMetric = new DerivedMetric(scopeRoot, expFormula, sName, metricLastID, this.getMetricCount(), 
			bPercent, MetricType.INCLUSIVE);
	
	this.metricList.add(objMetric);
	this.metricMap.put(objMetric.getShortName(), objMetric);

	int iInclusive = this.getMetricCount() - 1;
	int iExclusive = -1;		// at the moment we do not support exclusive/inclusive derived metric
	
	InclusiveOnlyMetricPropagationFilter rootInclProp = new InclusiveOnlyMetricPropagationFilter(this);
	
	for (int i=0; i<this.rootScope.getSubscopeCount(); i++) {
		RootScope rootScope = (RootScope) this.rootScope.getSubscope(i);

		DerivedMetricVisitor csv = new DerivedMetricVisitor(this, rootInclProp, iInclusive, iExclusive );
		rootScope.dfsVisitScopeTree(csv);

		//DerivedPercentVisitor psv = new DerivedPercentVisitor(this.getMetrics(), rootScope, iInclusive, iExclusive);
		//rootScope.dfsVisitScopeTree(psv);

	}

	return objMetric;
}

//////////////////////////////////////////////////////////////////////////
//	ACCESS TO CONFIGURATION												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the name of the experiment.
 ************************************************************************/
	
public String getName()
{
	return this.configuration.getName();
}




/*************************************************************************
 *	Returns the default directory from which to resolve relative paths.
 ************************************************************************/
	
public File getDefaultDirectory()
{
	return this.defaultDirectory;
}




/*************************************************************************
 *	Returns the number of search paths in the experiment.
 ************************************************************************/
	
public int getSearchPathCount()
{
	return this.configuration.getSearchPathCount();
}





/*************************************************************************
 *	Returns the search path with a given index.
 ************************************************************************/
	
public File getSearchPath(int index)
{
	return this.configuration.getSearchPath(index);
}



//////////////////////////////////////////////////////////////////////////
//	ACCESS TO METRICS													//
//////////////////////////////////////////////////////////////////////////


/*************************************************************************
 *	Returns the array of metrics in the experiment.
 ************************************************************************/
	
public BaseMetric[] getMetrics()
{
	return 	this.metricList.toArray(new BaseMetric[0]);
	//return 	(Metric[])this.metricList.toArray(new Metric[0]);
}


/*************************************************************************
 *	Returns the number of metrics in the experiment.
 ************************************************************************/
	
public int getMetricCount()
{
	return this.metricList.size();
}




/*************************************************************************
 *	Returns the metric with a given index.
 ************************************************************************/
	
public BaseMetric getMetric(int index)
{
	BaseMetric metric;
	// laks 2010.03.03: bug fix when the database contains no metrics
	try {
		metric = this.metricList.get(index);
	} catch (Exception e) {
		// if the metric doesn't exist or the index is out of range, return null
		metric = null;
	}
	return metric;
}




/*************************************************************************
 *	Returns the metric with a given internal name.
 ************************************************************************/
	
public BaseMetric getMetric(String name)
{
	BaseMetric metric = (BaseMetric) this.metricMap.get(name);
	
	if (metric == null) {
		// backward compatibility: do not throw wn exception ! some old databases 
		//	have no partner information
		// throw new RuntimeException("Unknown Metric " + name );
		System.err.println("Unknown metric: " + name);
	}
	return metric;
}



//////////////////////////////////////////////////////////////////////////
//	ACCESS TO SCOPES													//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the root scope of the experiment's scope tree.
 ************************************************************************/
	
public Scope getRootScope()
{
	return this.rootScope;
}


public TreeNode[] getRootScopeChildren() {
	return this.rootScope.getChildren();
}

//============================================================================
// DICTIONARY
//============================================================================
public void setFileTable( HashMap<Integer, SourceFile> fileTable) {
	this.hashFileTable = fileTable;
	//arrSourceFiles = fileTable;
}


/*************************************************************************
 *	Returns the number of file scopes in the experiment.
 *
 *	Note that this is not the same as the number of source files, since
 *	not every source file is a scope.
 *
 ************************************************************************/
	
public int getFileScopeCount()
{
	// file scopes are exactly the immediate children of the root (program) scope
	return this.rootScope.getSubscopeCount();
}




/*************************************************************************
 *	Returns the file scope with a given index.
 ************************************************************************/
	
public FileScope getFileScope(int index)
{
	return (FileScope) this.rootScope.getSubscope(index);
}

public File getXMLExperimentFile() {
	return this.fileExperiment;
}

public void setMetricRaw(MetricRaw []metrics) {
	this.metrics_raw = metrics;
	if (this.metrics_raw != null)
		this.threadsData = new ThreadLevelDataManager(this);
}


public MetricRaw[] getMetricRaw() {
	return this.metrics_raw;
}


public ThreadLevelDataManager getThreadLevelDataManager() {
	return this.threadsData;
}


private class CreateCallersViewThread extends Thread {
	private RootScope callersViewRootScope;
	private RootScope callingContextViewRootScope;
	
	public CreateCallersViewThread(RootScope cctRootScope) {
		super();
		this.callingContextViewRootScope = cctRootScope;
	}
	
	public void run() {
		callersViewRootScope = createCallersView(callingContextViewRootScope);
		addPercents(callersViewRootScope, (RootScope) callingContextViewRootScope);
	}
	
	public RootScope getRootScope() {
		return callersViewRootScope;
	}
	
}


private class CreateFlatViewThread extends Thread {
	private RootScope flatViewRootScope;
	private RootScope callingContextViewRootScope;
	
	public CreateFlatViewThread(RootScope cctRootScope) {
		super();
		this.callingContextViewRootScope = cctRootScope;
	}
	
	public void run() {
		// While creating the flat tree, we attribute the cost for procedure scopes
		// One the tree has been created, we compute the inclusive cost for other scopes
		flatViewRootScope = (RootScope) createFlatView(callingContextViewRootScope);
	}
	
	public RootScope getRootScope() {
		return flatViewRootScope;
	}
	
}

//======================================================================================
//	UNIT TEST
//======================================================================================

/**
 * unit test for this class
 * @param argv
 */
	static public void main(String argv[]) {
       Experiment experiment;
	   String sFilename = argv[0];
	           // open the experiment if possible
	    try
	        {
	    	   File objFile = new File(sFilename);
	    	   if (objFile.isDirectory()) {
	    		   File files[] = Util.getListOfXMLFiles(sFilename);
	    		   boolean done = false;
	    		   for (int i=0; i<files.length && !done; i++) {
	    			   
	    		   }
	    	   }
	           experiment = new Experiment(objFile);
	           // laks: try to debug to verify if apache xml is accessible
	           System.out.print("DataExperiment: Opening file:"+sFilename);
	           experiment.open();
	           System.out.println(" is succeded !");
	           
	      } catch(java.io.FileNotFoundException fnf)
	      {
	           System.err.println("$Error:File not found" + sFilename);
	           experiment = null;
	      }
	      catch(java.io.IOException io)
	      {
	           System.err.println("$Error: Unable to read" +  sFilename);
	           experiment = null;
	      }
	      catch(InvalExperimentException ex)
	      {
	           String where = sFilename + "  has incorrect tag at line: " + ex.getLineNumber();
	           System.err.println("$" +  where);
	           experiment = null;
	      }
	      catch(NullPointerException npe)
	      {
	           System.err.println("$File has null pointer:" + npe.getMessage() + sFilename);
	           experiment = null;
	      }
	}
}
