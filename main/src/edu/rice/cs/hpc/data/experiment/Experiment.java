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


import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.Scope.Node;
import edu.rice.cs.hpc.data.experiment.scope.filters.*;
import edu.rice.cs.hpc.data.experiment.scope.visitors.*;
import edu.rice.cs.hpc.data.experiment.source.*;
import edu.rice.cs.hpc.data.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


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


/** The file containing the experiment. */
protected ExperimentFile experimentFile;

/** The directory from which to resolve relative source file paths. */
protected File defaultDirectory;

/** The experiment's configuration. */
protected ExperimentConfiguration configuration;

/** The experiment's source files. */
protected SourceFile[] files;

/** The experiment's metrics. */
protected Vector metricList;

/** The experiment's scopes. */
protected ScopeList scopes;

/** The experiment's root scope. */
protected Scope rootScope;

/** A mapping from internal name strings to metric objects. */
protected HashMap metricMap;

//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////

/*************************************************************************
 *	Creates an empty Experiment.
 ************************************************************************/
	
public Experiment()
{
	//Dialogs.notImplemented("Experiment() constructor");
}




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
// laks: no exception needed
 /* *throws
	IOException,
	InvalExperimentException*/
{
	this.experimentFile   = ExperimentFile.makeFile(filename);
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
	
	// setSourceFiles(files); // union sourcefiles later
	// setMetrics(metricList);	// sets metrics (w/ index) and metricMap
	// setScopes(scopes, rootScope); // union scopeLists, and build new scopeTree from rootScope
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
 *	Sets the experiment's source file list.
 *
 *	This method is to be called only once, during <code>Experiment.open</code>.
 *
 ************************************************************************/
	
public void setSourceFiles(List sourceFileList)
{
	this.files = (SourceFile[]) sourceFileList.toArray(new SourceFile[0]);
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
		Metric m = (Metric)this.metricList.get(k);
		m.setIndex(k);
		this.metricMap.put(m.getShortName(), m);
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
	this.scopes    = new ArrayScopeList(this, scopeList, Strings.ALL_SCOPES);
	this.rootScope = rootScope;
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
	this.scopes.addScope(scope);
}

Scope createCallersView(Scope callingContextViewRootScope)
{
	EmptyMetricValuePropagationFilter filter = new EmptyMetricValuePropagationFilter();

	Scope callersViewRootScope = new RootScope(this,"Callers View","Callers View", RootScopeType.CallTree);
	beginScope(callersViewRootScope);
	
	CallersViewScopeVisitor csv = new CallersViewScopeVisitor(this, callersViewRootScope, 
			this.getMetricCount(), false, filter);
	callingContextViewRootScope.dfsVisitScopeTree(csv);
	accumulateMetricsFromKids(callersViewRootScope, callersViewRootScope, filter);
	
	return callersViewRootScope;
}

Scope createFlatView(Scope callingContextViewRootScope)
{
	MetricValuePropagationFilter fvf = new FlatViewMetricPropagationFilter();

	Scope flatViewRootScope = new RootScope(this, "Flat View", "Flat View", RootScopeType.Flat);
	beginScope(flatViewRootScope);
	
	FlatViewScopeVisitor fsv = new FlatViewScopeVisitor(this, flatViewRootScope, 
			this.getMetricCount(), false, fvf);
	callingContextViewRootScope.dfsVisitScopeTree(fsv);

	return flatViewRootScope;
}

void normalizeLineScopes(Scope scope, MetricValuePropagationFilter filter)
{
	NormalizeLineScopesVisitor nls = new NormalizeLineScopesVisitor(this.getMetricCount(), filter);
	scope.dfsVisitScopeTree(nls);
}
// Laks 03.18.2008: nobody uses this method
/*
void report(RootScope scope)
{
	ReportScopeVisitor rsv = new ReportScopeVisitor();
	scope.dfsVisitScopeTree(rsv);
	System.out.print("report view total for scope " + scope.getRootName() + " = ");
	System.out.println(rsv.total);
}
*/
void addInclusiveMetrics(Scope scope, MetricValuePropagationFilter filter)
{
	InclusiveMetricsScopeVisitor isv = new InclusiveMetricsScopeVisitor(this.getMetricCount(), filter);
	scope.dfsVisitScopeTree(isv);
}

void copyMetricsToPartner(Scope scope, MetricType sourceType, MetricValuePropagationFilter filter) {
	for (int i = 0; i< this.getMetricCount(); i++) {
		Metric metric = this.getMetric(i);
		if (metric.getMetricType() == sourceType) {
			copyMetric(scope, scope, i, metric.getPartnerIndex(), filter);
		}
	}
}

void addPercents(Scope scope, RootScope totalScope)
{
	PercentScopeVisitor psv = new PercentScopeVisitor(this.getMetricCount(), totalScope);
	scope.dfsVisitScopeTree(psv);
}

public void postprocess() {
	if (this.rootScope.getSubscopeCount() <= 0) return;
	// Get first scope subtree: CCT or Flat
	Scope firstSubTree = this.rootScope.getSubscope(0);
	if (!(firstSubTree instanceof RootScope)) return;
	RootScopeType firstRootType = ((RootScope)firstSubTree).getType();
	
	if (firstRootType.equals(RootScopeType.CallTree)) {
		// accumulate, create views, percents, etc
		Scope callingContextViewRootScope = firstSubTree;
	
		EmptyMetricValuePropagationFilter emptyFilter = new EmptyMetricValuePropagationFilter();
		InclusiveOnlyMetricPropagationFilter rootInclProp = new InclusiveOnlyMetricPropagationFilter(this.getMetrics());

//		normalizeLineScopes(callingContextViewRootScope, rootInclProp); // Incl only
		normalizeLineScopes(callingContextViewRootScope, emptyFilter); // normalize all
		// report((RootScope) callingContextViewRootScope);

		addInclusiveMetrics(callingContextViewRootScope, rootInclProp);
		addInclusiveMetrics(callingContextViewRootScope, 
		  new ExclusiveOnlyMetricPropagationFilter(this.getMetrics()));
//		  new CallingContextTreeInclMetricPropagationFilter(this.getMetrics()));

		copyMetricsToPartner(callingContextViewRootScope, MetricType.INCLUSIVE, emptyFilter);


		// Callers View
		Scope callersViewRootScope = createCallersView(callingContextViewRootScope);
		copyMetricsToPartner(callersViewRootScope, MetricType.EXCLUSIVE, emptyFilter);

		//		callingContextViewRootScope.copyMetrics(callersViewRootScope);

		// Flat View
		Scope flatViewRootScope = createFlatView(callingContextViewRootScope);
		// report((RootScope) flatViewRootScope);
		addInclusiveMetrics(flatViewRootScope, new FlatViewInclMetricPropagationFilter(this.getMetrics()));
		flatViewRootScope.accumulateMetrics(callingContextViewRootScope, rootInclProp, this.getMetricCount());

		addPercents(callingContextViewRootScope, (RootScope) callingContextViewRootScope);

		addPercents(callersViewRootScope, (RootScope) callingContextViewRootScope);

		addPercents(flatViewRootScope, (RootScope) callingContextViewRootScope);
	} else if (firstRootType.equals(RootScopeType.Flat)) {
		addPercents(firstSubTree, (RootScope) firstSubTree);
	} else {
		// ignore; do no postprocessing
	}
}

//////////////////////////////////////////////////////////////////////////
//Compute Derived Metrics												//
//////////////////////////////////////////////////////////////////////////
public void addComputedMetrics(int nMetrics, double scaling)
{
	// Setup metrics
	for (int i=0; i<nMetrics; i++) {
		Metric m = this.getMetric(i);
		// TODO [me] better names? %? 
		MetricType metricType = m.getMetricType();
		String shortName = "S" + m.getShortName();
		String nativeName = m.getNativeName();
		String type = (metricType==MetricType.INCLUSIVE) ? "I":"E";
		String stype = (scaling==1.0) ? "W":"S";
		String oldDisplay = m.getDisplayName();
		String displayName = "X"+stype+"("+type+",n) " + oldDisplay.substring(0, oldDisplay.length()-4);
		boolean showPercent = m.getPercent();
		int partnerIndex = m.getPartnerIndex();
		if (partnerIndex != Metric.NO_PARTNER_INDEX)
			partnerIndex += this.getMetricCount() - m.getIndex();
		Metric cm = new Metric(this, shortName, nativeName, displayName,
	              true, showPercent, m.getSamplePeriod(), metricType, partnerIndex);
		this.addMetric(cm);
	}
	
	// Walk scope tree, compute derived metrics
	ComputedMetricVisitor cmv = new ComputedMetricVisitor(nMetrics, this.getRootScope().getSubscope(0), scaling);
	this.rootScope.dfsVisitScopeTree(cmv);
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
//	ACCESS TO SOURCE FILES												//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the number of source files in the experiment.
 ************************************************************************/
	
public int getSourceFileCount()
{
	return this.files.length;
}




/*************************************************************************
 *	Returns the source file with a given index.
 ************************************************************************/
	
public SourceFile getSourceFile(int index)
{
	return this.files[index];
}




//////////////////////////////////////////////////////////////////////////
//	ACCESS TO METRICS													//
//////////////////////////////////////////////////////////////////////////


/*************************************************************************
 *	Returns the array of metrics in the experiment.
 ************************************************************************/
	
public Metric[] getMetrics()
{
	return 	(Metric[])this.metricList.toArray(new Metric[0]);
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
	
public Metric getMetric(int index)
{
	return (Metric)this.metricList.get(index);
}




/*************************************************************************
 *	Returns the metric with a given internal name.
 ************************************************************************/
	
public Metric getMetric(String name)
{
	Metric metric = (Metric) this.metricMap.get(name);
	Dialogs.Assert(metric != null, " Null in getMetric");
	return metric;
}

public void addMetric(Metric m)
{
	m.setIndex(this.getMetricCount());
	m.setShortName(""+m.getIndex());
	this.metricList.add(m);
	this.metricMap.put(m.getShortName(), m);
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

public ArrayList getRootScopeChildren()
{
	ArrayList rootScopeChildren = new ArrayList();
	for(Enumeration e =this.rootScope.getTreeNode().children();e.hasMoreElements();)
	{
		rootScopeChildren.add(((Node)e.nextElement()).getScope());
	}
	return rootScopeChildren;
}




/*************************************************************************
 *	Returns a list of all the scopes in the experiment.
 ************************************************************************/
	
public ScopeList getScopeList()
{
	return this.scopes;
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

	static public void main(String argv[]) {
       Experiment experiment;
	   String sFilename = argv[0];
	           // open the experiment if possible
	    try
	           {
	           experiment = new Experiment(new java.io.File(sFilename));
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
