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
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric.AnnotationType;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.scope.filters.*;
import edu.rice.cs.hpc.data.experiment.scope.visitors.*;
import edu.rice.cs.hpc.data.experiment.xml.ExperimentFileXML;
import edu.rice.cs.hpc.data.util.IUserData;

import java.io.File;
import java.io.IOException;
import java.util.*;


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


public class Experiment extends BaseExperimentWithMetrics implements IExperiment
{
	// thread level database
	private MetricRaw[] metrics_raw;



	//////////////////////////////////////////////////////////////////////////
	//	PERSISTENCE															//
	//////////////////////////////////////////////////////////////////////////


	/*************************************************************************
	 * Opens the experiment 
	 * 
	 * @param 		experimentFile : file to be parsed
	 * @exception 	IOException if experiment file can't be read.
	 * @exception 	InvalExperimentException if file contents are
	 * 			 	not a valid experiment.
	 *************************************************************************/
	
	public void open(File experimentFile, IUserData<String, String> userData)
			throws Exception
	{
		this.fileExperiment = experimentFile;
		new ExperimentFileXML().parse(fileExperiment, this, true,
				userData);		
	}

	
	/*************************************************************************
	 *	Sets the experiment's metric list.
	 *
	 *	This method is to be called only once, during initialization
	 *
	 ************************************************************************/

	public void setMetrics(List<BaseMetric> metricList)
	{
		super.setMetrics(metricList);

		// initialize metric access data structures
		int count = metricList.size();

		for( int k = 0;  k < count;  k++ )
		{	
			BaseMetric m = this.metrics.get(k);
			m.setIndex(k);
		}
	}




	public int getMajorVersion()
	{
		if (this.version == null)
			return 1;
		int ip = this.version.indexOf('.');
		return Integer.parseInt(this.version.substring(0, ip));
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
			if (mv != MetricValue.NONE && MetricValue.getValue(mv) != 0.0) {
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

	/***
	 * Preparing the tree for caller view. Since we will create the tree dynamically,
	 * 	we need to create at least the root. All the children will be created by
	 * 	createCallersView() method.
	 * 
	 * @param callingContextViewRootScope
	 * @return
	 */
	protected RootScope prepareCallersView(Scope callingContextViewRootScope)
	{
		RootScope callersViewRootScope = new RootScope(this,"Callers View","Callers View", RootScopeType.CallerTree);
		beginScope(callersViewRootScope);
		
		return callersViewRootScope;
	}
	
	/***
	 * create callers view
	 * @param callingContextViewRootScope
	 * @return
	 */
	public RootScope createCallersView(Scope callingContextViewRootScope, RootScope callersViewRootScope)
	{
		EmptyMetricValuePropagationFilter filter = new EmptyMetricValuePropagationFilter();

		CallersViewScopeVisitor csv = new CallersViewScopeVisitor(this, callersViewRootScope, 
				this.getMetricCount(), false, filter);
		callingContextViewRootScope.dfsVisitScopeTree(csv);

		// compute the aggregate metrics
		// bug fix 2008.10.21 : we don't need to recompute the aggregate metrics here. Just copy it from the CCT
		//	This will solve the problem where there is only nested loops in the programs
		callersViewRootScope.accumulateMetrics(callingContextViewRootScope, filter, this.getMetricCount());
		
		AbstractFinalizeMetricVisitor diVisitor = new FinalizeMetricVisitorWithBackup(this.getMetrics());
		this.finalizeAggregateMetrics(callersViewRootScope, diVisitor);
		
		// bug fix 2010.06.17: move the percent after finalization
		addPercents(callersViewRootScope, callersViewRootScope);

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
			BaseMetric metric = this.getMetric(i);
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

		for (TreeNode node: this.rootScope.getChildren()) {
			Scope scope = (Scope) node;
			if ( (scope instanceof RootScope) && 
					((RootScope)scope).getType()==RootScopeType.CallerTree )
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
			if (callerView) {
				prepareCallersView(callingContextViewRootScope);
			}

			//----------------------------------------------------------------------------------------------
			// Flat View
			//----------------------------------------------------------------------------------------------
			Scope flatViewRootScope = null;
			// While creating the flat tree, we attribute the cost for procedure scopes
			// One the tree has been created, we compute the inclusive cost for other scopes
			flatViewRootScope = createFlatView(callingContextViewRootScope);

			//----------------------------------------------------------------------------------------------
			// FINALIZATION
			//----------------------------------------------------------------------------------------------
			AbstractFinalizeMetricVisitor diVisitor = new FinalizeMetricVisitor(this.getMetrics());

			this.finalizeAggregateMetrics(flatViewRootScope, diVisitor);	// flat view

			diVisitor = new FinalizeMetricVisitorWithBackup(this.getMetrics());

			this.finalizeAggregateMetrics(callingContextViewRootScope, diVisitor);		// cct

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
	private void finalizeAggregateMetrics(Scope root, AbstractFinalizeMetricVisitor diVisitor) {
		if (! checkExistenceOfDerivedIncr())
			return;
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
			AnnotationType annotationType, MetricType metricType) {


		// laks 2010.02.27: for aggregate metric, we need to know the ID of the last metric, then increment this ID
		//					for the new metric
		// if the last metric has index 7 and ID 10, then the new metric has index 8 and ID 11
		int metricLastIndex = this.getMetricCount() -1;
		BaseMetric metricLast = this.getMetric(metricLastIndex);
		String metricLastID = metricLast.getShortName();
		metricLastIndex = Integer.valueOf(metricLastID) + 1;
		metricLastID = String.valueOf(metricLastIndex);

		DerivedMetric objMetric = new DerivedMetric(scopeRoot, expFormula, sName, metricLastID, this.getMetricCount(), 
				annotationType, MetricType.INCLUSIVE);

		this.metrics.add(objMetric);

		int iInclusive = this.getMetricCount() - 1;
		int iExclusive = -1;		// at the moment we do not support exclusive/inclusive derived metric

		InclusiveOnlyMetricPropagationFilter rootInclProp = new InclusiveOnlyMetricPropagationFilter(this);

		for (int i=0; i<this.rootScope.getSubscopeCount(); i++) {
			RootScope rootScope = (RootScope) this.rootScope.getSubscope(i);

			DerivedMetricVisitor csv = new DerivedMetricVisitor(this, rootInclProp, iInclusive, iExclusive );
			rootScope.dfsVisitScopeTree(csv);

		}

		return objMetric;
	}



	public Experiment duplicate() {

		Experiment copy = new Experiment();
		copy.configuration = configuration;
		copy.fileExperiment = fileExperiment;
		
		return copy;
	}


	public File getXMLExperimentFile() {
		return this.fileExperiment;
	}

	public void setXMLExperimentFile(File file) {
		this.fileExperiment = file;
	}

	public void setMetricRaw(MetricRaw []metrics) {
		this.metrics_raw = metrics;
	}


	public MetricRaw[] getMetricRaw() {
		return this.metrics_raw;
	}


}
