//////////////////////////////////////////////////////////////////
//																//
//	Scope.java													//
//																//
//																//
//	(c) Copyright 2015 Rice University. All rights reserved.	//
//																//
//	$LastChangedDate$		
//  $LastChangedBy$ 					//
//////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.experiment.scope;


import java.util.Iterator;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.BaseExperimentWithMetrics;
import edu.rice.cs.hpc.data.experiment.metric.AggregateMetric;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.visitors.FilterScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.visitors.IScopeVisitor;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;


 
//////////////////////////////////////////////////////////////////////////
//	CLASS SCOPE							//
//////////////////////////////////////////////////////////////////////////

/**
 *
 * A scope in an HPCView experiment.
 *
 * FIXME: do we want to merge the functionality of Scope and Scope.Node?
 * it's kind of irritating to have the two things be distinct and having
 * objects which point at each other makes me a little uneasy.
 */


public abstract class Scope extends TreeNode
{
//////////////////////////////////////////////////////////////////////////
//PUBLIC CONSTANTS						//
//////////////////////////////////////////////////////////////////////////


/** The value used to indicate "no line number". */
public static final int NO_LINE_NUMBER = -169; // any negative number other than -1

static public final int SOURCE_CODE_UNKNOWN = 0;
static public final int SOURCE_CODE_AVAILABLE = 1;
static public final int SOURCE_CODE_NOT_AVAILABLE= 2;

/** The current maximum number of ID for all scopes	 */
static protected int idMax = 0;

/** The experiment owning this scope. */
protected BaseExperiment experiment;

/** The source file containing this scope. */
protected SourceFile sourceFile;

/** the scope identifier */
protected int flat_node_index;

/** The first line number of this scope. */
protected int firstLineNumber;

/** The last line number of this scope. */
protected int lastLineNumber;

/** The metric values associated with this scope. */
private MetricValue[] metrics;
private MetricValue[] combinedMetrics;

/** source citation */
protected String srcCitation;

/**
 * FIXME: this variable is only used for the creation of callers view to count
 * 			the number of instances. To be removed in the future
 */
private int iCounter;
// --------------------------

//the cpid is removed in hpcviewer, but hpctraceview still requires it in order to dfs
protected int cpid;
//--------------------------

public int iSourceCodeAvailability = Scope.SOURCE_CODE_UNKNOWN;



//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Creates a Scope object with associated source line range.
 ************************************************************************/
	
public Scope(BaseExperiment experiment, SourceFile file, int first, int last, int cct_id, int flat_id)
{
	super(cct_id);
	
	// creation arguments
	this.experiment = experiment;
	this.sourceFile = file;
	this.firstLineNumber = first;
	this.lastLineNumber = last;

	this.srcCitation = null;
	this.flat_node_index = flat_id;
	this.cpid = -1;
	this.iCounter  = 0;
}


public Scope(BaseExperiment experiment, SourceFile file, int first, int last, int cct_id, int flat_id, int cpid)
{
	this(experiment, file, first, last, cct_id, flat_id);
	this.cpid = cpid;
}



/*************************************************************************
 *	Creates a Scope object with associated source file.
 ************************************************************************/
	
public Scope(BaseExperiment experiment, SourceFile file, int scopeID)
{
	this(experiment, file, Scope.NO_LINE_NUMBER, Scope.NO_LINE_NUMBER, scopeID, scopeID);
}


public int getFlatIndex() {
	return this.flat_node_index;
}

public int getCCTIndex() {
	return (Integer) getValue(); //this.cct_node_index;
}

//////////////////////////////////////////////////////////////////////////
// DUPLICATION														//
//////////////////////////////////////////////////////////////////////////



/*************************************************************************
 *	Creates a Scope object with no associated source file.
 ************************************************************************/
	
public abstract Scope duplicate();



//////////////////////////////////////////////////////////////////////////
//	SCOPE DISPLAY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the user visible name for this scope.
 *
 *	Subclasses should override this to implement useful names.
 *
 ************************************************************************/
	
public abstract String getName();



/*************************************************************************
 *	Returns the short user visible name for this scope.
 *
 *	This name is only used in tree views where the scope's name appears
 *	in context with its containing scope's name.
 *
 *	Subclasses may override this to implement better short names.
 *
 ************************************************************************/
	
public String getShortName()
{
	return this.getName();
}

//////////////////////////////////////////////////////////////////////////
// counter														//
//////////////////////////////////////////////////////////////////////////


public void incrementCounter() {
	this.iCounter++;
}

public void decrementCounter() {
	if (this.isCounterPositif())
		this.iCounter--;
	else {
		System.err.println("Scope " + this.getName() + " [" + this.getCCTIndex() + "/" + this.flat_node_index + "]"  + " has non-positive counter");
	}
}

public void setCounter(int counter) {
	this.iCounter = counter;
}

public int getCounter() {
	return this.iCounter;
}

public boolean isCounterPositif() {
	return this.iCounter>0;
}

public boolean isCounterZero() {
	return (this.iCounter == 0);
}

/*************************************************************************
 * Returns which processor was active
 ************************************************************************/

public int getCpid()
{
	return cpid;
}


/*************************************************************************
 *	Sets the value of the cpid
 ************************************************************************/

public void setCpid(int _cpid)
{
	this.cpid = _cpid;
}

/*************************************************************************
 *	Returns the tool tip for this scope.
 ************************************************************************/
	
public String getToolTip()
{
	return this.getSourceCitation();
}




/*************************************************************************
 *	Converts the scope to a <code>String</code>.
 *
 *	<p>
 *	This method is for the convenience of <code>ScopeTreeModel</code>,
 *	which passes <code>Scope</code> objects to the default tree cell
 *	renderer.
 *
 ************************************************************************/
	
public String toString()
{
	return this.getName();
}


public int hashCode() {
	return this.flat_node_index;
}



/*************************************************************************
 *	Returns a display string describing the scope's source code location.
 ************************************************************************/
	
protected String getSourceCitation()
{
	if (this.srcCitation == null)  {
		
		srcCitation = this.getSourceCitation(sourceFile, firstLineNumber, lastLineNumber);
	}

	return srcCitation;
}


private String getSourceCitation(SourceFile sourceFile, int line1, int line2)
{

		// some scopes such as load module, doesn't have a source code file (they are binaries !!)
		// this hack will return the name of the scope instead of the citation file
		if (sourceFile == null) {
			return this.getName();
		}
		return sourceFile.getName() + ": " + this.getLineOnlyCitation(line1, line2);

}




/*************************************************************************
 *	Returns a display string describing the scope's line number range.
 ************************************************************************/
	
protected String getLineNumberCitation()
{
	return this.getLineNumberCitation(firstLineNumber, lastLineNumber);
}


private String getLineNumberCitation(int line1, int line2)
{
	String cite;

	// we must display one-based line numbers
	int first1 = 1 + line1;
	int last1  = 1 + line2;

	if(line1 == Scope.NO_LINE_NUMBER) {
		cite = "";	// TEMPORARY: is this the right thing to do?
	} else if(line1 == line2)
		cite = "line" + " " + first1;
	else
		cite = "lines" + " " + first1 + "-" + last1;

	return cite;
}


private String getLineOnlyCitation(int line1, int line2) {
	String cite;

	// we must display one-based line numbers
	int first1 = 1 + line1;
	int last1  = 1 + line2;

	if(line1 == Scope.NO_LINE_NUMBER) {
		cite = "";	// TEMPORARY: is this the right thing to do?
	} else if(line1 == line2)
		cite = String.valueOf(first1);
	else
		cite = first1 + "-" + last1;

	return cite;
}

//////////////////////////////////////////////////////////////////////////
//	ACCESS TO SCOPE														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the source file of this scope.
 *
 *	<p>
 *	<em>TEMPORARY: This assumes that each scope "has" (i.e. intersects)
 *	at most one source file -- not true for discontiguous scopes.</em>
 *
 ************************************************************************/
	
public SourceFile getSourceFile()
{
	return this.sourceFile;
}


/*************************************************************************
 *	Returns the first line number of this scope in its source file.
 *
 *	<p>
 *	<em>TEMPORARY: This assumes that each scope "has" (i.e. intersects)
 *	at most one source file -- not true for discontiguous scopes.</em>
 *
 ************************************************************************/
	
public int getFirstLineNumber()
{
	return this.firstLineNumber;
}




/*************************************************************************
 *	Returns the last line number of this scope in its source file.
 *
 *	<p>
 *	<em>TEMPORARY: This assumes that each scope "has" (i.e. intersects)
 *	at most one source file -- not true for discontiguous scopes.</em>
 *
 ************************************************************************/
	
public int getLastLineNumber()
{
	return this.lastLineNumber;
}




//////////////////////////////////////////////////////////////////////////
//	SCOPE HIERARCHY														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the parent scope of this scope.
 ************************************************************************/
	
public Scope getParentScope()
{
	return (Scope) this.getParent();
}


/*************************************************************************
 *	Sets the parent scope of this scope.
 ************************************************************************/
	
public void setParentScope(Scope parentScope)
{
	this.setParent(parentScope);
}




/*************************************************************************
 *	Returns the number of subscopes within this scope.
 ************************************************************************/
	
public int getSubscopeCount()
{
	return this.getChildCount();
}




/*************************************************************************
 *	Returns the subscope at a given index.
 ************************************************************************/
	
public Scope getSubscope(int index)
{
	Scope child = (Scope) this.getChildAt(index);
	return child;
}


/*************************************************************************
 *	Adds a subscope to the scope.
 ************************************************************************/
	
public void addSubscope(Scope subscope)
{
	this.add(subscope);
}



//////////////////////////////////////////////////////////////////////////
//	ACCESS TO METRICS													//
//////////////////////////////////////////////////////////////////////////

public boolean hasMetrics() 
{
	return (metrics != null);
}

public boolean hasNonzeroMetrics() {
	if (this.hasMetrics())
		for (int i = 0; i< this.metrics.length; i++) {
			MetricValue m = this.getMetricValue(i);
			if (!MetricValue.isZero(m))
				return true;
		}
	return false;
}


//////////////////////////////////////////////////////////////////////////
// EXPERIMENT DATABASE 													//
//////////////////////////////////////////////////////////////////////////
public BaseExperiment getExperiment() {
	return experiment;
}

public void setExperiment(BaseExperiment exp) {
	this.experiment = exp;
}


//===================================================================
//						METRICS
//===================================================================


/*************************************************************************
 *	Returns the value of a given metric at this scope.
 ************************************************************************/
	
public MetricValue getMetricValue(BaseMetric metric)
{
	int index = metric.getIndex();
	MetricValue value = getMetricValue(index);

	// compute percentage if necessary
	Scope root = this.experiment.getRootScope();
	if((this != root) && (! MetricValue.isAnnotationAvailable(value)))
	{
		MetricValue total = root.getMetricValue(metric);
		if(MetricValue.isAvailable(total))
			MetricValue.setAnnotationValue(value, MetricValue.getValue(value)/MetricValue.getValue(total));
	} 

	return value;
}


/***
  overload the method to take-in the index ---FMZ
***/

public MetricValue getMetricValue(int index)
{
	MetricValue value;
        if(this.metrics != null && index < this.metrics.length)
           {
                value = this.metrics[index];
           }
        else
                value = MetricValue.NONE;

        return value;
}


/*************************************************************************
 *	Sets the value of a given metric at this scope.
 ************************************************************************/
public void setMetricValue(int index, MetricValue value)
{
	ensureMetricStorage();
	this.metrics[index] = value;
}

/*************************************************************************
 *	Add the metric cost from a source with a certain filter for all metrics
 ************************************************************************/
public void accumulateMetrics(Scope source, MetricValuePropagationFilter filter, int nMetrics) {
	for (int i = 0; i< nMetrics; i++) {
		this.accumulateMetric(source, i, i, filter);
	}
}

/*************************************************************************
 *	Add the metric cost from a source with a certain filter for a certain metric
 ************************************************************************/
public void accumulateMetric(Scope source, int src_i, int targ_i, MetricValuePropagationFilter filter) {
	if (filter.doPropagation(source, this, src_i, targ_i)) {
		MetricValue m = source.getMetricValue(src_i);
		if (m != MetricValue.NONE && MetricValue.getValue(m) != 0.0) {
			this.accumulateMetricValue(targ_i, MetricValue.getValue(m));
		}
	}
}

/*************************************************************************
 * Laks: accumulate a metric value (used to compute aggregate value)
 * @param index
 * @param value
 ************************************************************************/
private void accumulateMetricValue(int index, double value)
{
	ensureMetricStorage();
	if (index >= this.metrics.length) 
		return;

	MetricValue m = this.metrics[index];
	if (m == MetricValue.NONE) {
		this.metrics[index] = new MetricValue(value);
	} else {
		// TODO Could do non-additive accumulations here?
		MetricValue.setValue(m, MetricValue.getValue(m) + value);
	}
}

/**************************************************************************
 * copy metric values into the backup 
 **************************************************************************/
public void backupMetricValues() {
	if (this.metrics == null)
		return;
	
	if (!(experiment instanceof BaseExperimentWithMetrics))
		return;
	
	this.combinedMetrics = new MetricValue[this.metrics.length];
	
	for(int i=0; i<this.metrics.length; i++) {
		MetricValue value = this.metrics[i];
		BaseMetric metric = ((BaseExperimentWithMetrics)this.experiment).getMetric(i);
		
		//------------------------------------------------------------------
		// if the value is not availabe we do NOT store it but instead we
		//    assign to MetricValue.NONE
		//------------------------------------------------------------------
		if (MetricValue.isAvailable(value)) {
			//----------------------------------------------------------------------
			// derived incremental metric type needs special treatment: 
			//	their value changes in finalization phase, while others don't
			//----------------------------------------------------------------------
			if (metric instanceof AggregateMetric)
				this.combinedMetrics[i] = 
					new MetricValue(MetricValue.getValue(value), 
							MetricValue.getAnnotationValue(value));
			else 
				this.combinedMetrics[i] = value;
		} else {
			// the metric has no value available
			this.combinedMetrics[i] = MetricValue.NONE;
		}
	}
}

/***************************************************************************
 * retrieve the default metrics
 * @return
 */
public MetricValue[] getMetricValues() {
	return this.metrics;
}

/***************************************************************************
 * set the default metrics
 * @param values
 */
public void setMetricValues(MetricValue values[]) {	
	this.metrics = values;
}


/***************************************************************************
 * retrieve the backup metrics
 * @return
 ***************************************************************************/
public MetricValue[] getCombinedValues() {
	
	assert (this.isExperimentHasMetrics());
	
	final BaseExperimentWithMetrics _exp = (BaseExperimentWithMetrics) experiment;
	MetricValue [] values = new MetricValue[_exp.getMetricCount()];
	//boolean printed = false;

	for (int i=0; i<values.length; i++) {
		BaseMetric m = _exp.getMetric(i);
		if (m instanceof AggregateMetric) {
			if (this.combinedMetrics == null) {
				/*if (!printed) {
					System.err.println("scope: " + this + "\t(" + this.getClass() + ") has no backup metrics.");
					printed = true;
				}*/
				values[i] = this.metrics[i];
			} else 
				values[i] = this.combinedMetrics[i];
		} else if (m instanceof DerivedMetric) {
			values[i] = MetricValue.NONE;
		} else {
			values[i] = this.metrics[i];
		}
	}
	return values;
}


/**************************************************************************
 * combining metric from source. use this function to combine metric between
 * 	different views
 * @param source
 * @param filter
 **************************************************************************/
public void combine(Scope source, MetricValuePropagationFilter filter) {
	
	assert (this.isExperimentHasMetrics());
	
	final BaseExperimentWithMetrics _exp = (BaseExperimentWithMetrics) experiment;

	int nMetrics = _exp.getMetricCount();
	for (int i=0; i<nMetrics; i++) {
		BaseMetric metric = _exp.getMetric(i);
		if (metric instanceof AggregateMetric) {
			//--------------------------------------------------------------------
			// aggregate metric need special treatment when combining two metrics
			//--------------------------------------------------------------------
			AggregateMetric aggMetric = (AggregateMetric) metric;
			if (filter.doPropagation(source, this, i, i)) {
				aggMetric.combine(source, this);
			}
		} else {
			this.accumulateMetric(source, i, i, filter);
		}
	}
}


/**********************************************************************************
 * Safely combining metrics from another scope. 
 * This method checks if the number of metrics is the same as the number of metrics
 * 	in the experiment. If not, it generates additional metrics
 * this method is used for dynamic metrics creation such as when computing metrics
 * 	in caller view (if a new metric is added)
 * @param source
 * @param filter
 **********************************************************************************/
public void safeCombine(Scope source, MetricValuePropagationFilter filter) {
	ensureMetricStorage();
	this.combine(source, filter);
}

/*************************************************************************
 *	Makes sure that the scope object has storage for its metric values.
 ************************************************************************/
	
protected void ensureMetricStorage()
{
	
	assert (this.isExperimentHasMetrics());

	final BaseExperimentWithMetrics _exp = (BaseExperimentWithMetrics) experiment;

	if(this.metrics == null)
		this.metrics = this.makeMetricValueArray();
	// Expand if metrics not as big as experiment's (latest) metricCount
	if(this.metrics.length < _exp.getMetricCount()) {
		MetricValue[] newMetrics = this.makeMetricValueArray();
		for(int i=0; i<this.metrics.length; i++)
			newMetrics[i] = metrics[i];
		this.metrics = newMetrics;
	}
}




/*************************************************************************
 *	Gives the scope object storage for its metric values.
 ************************************************************************/
	
private MetricValue[] makeMetricValueArray()
{
	
	assert (this.isExperimentHasMetrics());

	final BaseExperimentWithMetrics _exp = (BaseExperimentWithMetrics) experiment;
	final int metricsNeeded= _exp.getMetricCount();

	MetricValue[] array = new MetricValue[metricsNeeded];
	for(int k = 0; k < metricsNeeded; k++)
		array[k] = MetricValue.NONE;
	return array;
}



/*************************************************************************
 * Copies defensively the metric array into a target scope
 * Used to implement duplicate() in subclasses of Scope  
 ************************************************************************/

public void copyMetrics(Scope targetScope, int offset) {

	if (!this.hasMetrics())
		return;
	
	targetScope.ensureMetricStorage();
	for (int k=0; k<this.metrics.length && k<targetScope.metrics.length; k++) {
		MetricValue mine = null;
		MetricValue crtMetric = this.metrics[k];

		if ( MetricValue.isAvailable(crtMetric) && MetricValue.getValue(crtMetric) != 0.0) { // there is something to copy
			mine = new MetricValue();
			MetricValue.setValue(mine, MetricValue.getValue(crtMetric));

			if (MetricValue.isAnnotationAvailable(crtMetric)) {
				MetricValue.setAnnotationValue(mine, MetricValue.getAnnotationValue(crtMetric));
			} 
		} else {
			mine = MetricValue.NONE;
		}
		targetScope.metrics[k+offset] = mine;
	}
}

protected boolean isExperimentHasMetrics()
{
	return (this.experiment instanceof BaseExperimentWithMetrics);
}


//////////////////////////////////////////////////////////////////////////
//support for visitors													//
//////////////////////////////////////////////////////////////////////////

public void dfsVisitScopeTree(IScopeVisitor sv) {
	accept(sv, ScopeVisitType.PreVisit);
	int nKids = getSubscopeCount();
	for (int i=0; i< nKids; i++) {
		Scope childScope = getSubscope(i);
		if (childScope != null)
			childScope.dfsVisitScopeTree(sv);
	}
	accept(sv, ScopeVisitType.PostVisit);
}

public void accept(IScopeVisitor visitor, ScopeVisitType vt) {
	visitor.visit(this, vt);
}

/*******
 * depth first search scope with checking whether we should go deeper or not
 * 
 * @param sv
 */
public void dfsVisitFilterScopeTree(FilterScopeVisitor sv) {
	accept(sv, ScopeVisitType.PreVisit);
	if (sv.needToContinue())
	{
		// during the process of filtering, it is possible the tree has been changed
		// and the some children may be removed. It is safe to use iterator instead
		// of traditional array iteration
		Iterator<TreeNode> iterator = getIterator();
		if (iterator != null)
		{
			for (int i=0; i<getChildCount(); i++)
			{
				Scope scope = (Scope) getChildAt(i);
				if (scope != null)
				{
					scope.dfsVisitFilterScopeTree(sv);
				}
			}
		}
	}
	accept(sv, ScopeVisitType.PostVisit);
}

@Override
/*
 * (non-Javadoc)
 * @see edu.rice.cs.hpc.data.experiment.scope.TreeNode#dispose()
 */
public void dispose()
{
	super.dispose();
	experiment 		= null;
	metrics 		= null;
	srcCitation		= null;
	combinedMetrics = null;
}

}
