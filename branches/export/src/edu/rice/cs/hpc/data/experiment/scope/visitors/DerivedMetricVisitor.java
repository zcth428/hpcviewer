package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;

/**
 * Class to manage the computation of a derived metric
 * @author laksonoadhianto
 *
 */
public class DerivedMetricVisitor extends AbstractInclusiveMetricsVisitor {

	private int iExclusive;
	private int iInclusive;
	private int iBaseMetric;
	private boolean withExclusiveAndInclusive;
	private BaseMetric []arrMetrics;
	//private double excAggregateValue = 0.0;
	
	/**
	 * Constructor
	 * @param metrics: list of metrics
	 * @param filter: filter for the view in which the metric will be inserted
	 * @param iMetricInc: the index position of the inclusive metric
	 * @param iMetricExc: the index position of the exclusive metric
	 */
	public DerivedMetricVisitor(BaseMetric []metrics, MetricValuePropagationFilter filter, int iMetricInc, int iMetricExc) {
		super(metrics, filter);
		this.iExclusive = iMetricExc;
		this.iInclusive = iMetricInc;
		this.withExclusiveAndInclusive = (iMetricExc > 0);
		this.arrMetrics = metrics;
		iBaseMetric = (withExclusiveAndInclusive? iMetricExc:iInclusive);
		assert (iMetricInc > 0);
	}
	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------
	public void visit(RootScope scope, ScopeVisitType vt) { //up(scope, vt);
		if (vt == ScopeVisitType.PostVisit) {
			MetricValue mv = this.arrMetrics[this.iInclusive].getValue(scope);
			scope.setMetricValue(this.iInclusive, mv);
		}
	}
	
	
	/**
	 * computing the derived metric of all scopes 
	 * The first step (PREVISIT) is to compute the exclusive values
	 * The second step is to compute the inclusive values
	 */
	protected void up(Scope scope, ScopeVisitType vt) {
		if (vt == ScopeVisitType.PostVisit) {
			// We compute the inclusive metric only if the information of exclusive metrics is available 
			/*if (withExclusiveAndInclusive)
				super.up(scope, vt);
			else {
				// inclusive only
			}*/
			/*
			if (scope.getParentScope() instanceof RootScope) {
				// the value of the root scope should be the highest of its children
				Scope parent = scope.getParentScope();
				MetricValue mParent = parent.getMetricValue(this.iInclusive);
				MetricValue mKid = scope.getMetricValue(this.iInclusive); 
				if ( mParent.getValue() < mKid.getValue() ) {
					//parent.setMetricValue(this.iInclusive, mKid);
					parent.setMetricValue(this.iInclusive, this.arrMetrics[this.iInclusive].getValue(scope.getParentScope()));
				}
				
				if (withExclusiveAndInclusive) {
					parent.setMetricValue(this.iExclusive, new MetricValue(this.excAggregateValue, 1));
				}
			} */
		} else if (vt == ScopeVisitType.PreVisit) {
			// first, create metric exclusive
			BaseMetric metricExc = this.arrMetrics[iBaseMetric];
			// compute the derived metric of this scope
			MetricValue mv = metricExc.getValue(scope);
			// set the value of the derived metric into the scope
			if (withExclusiveAndInclusive) {
				scope.setMetricValue(this.iExclusive, mv);
				// naive aggregate calculationx`
				//this.excAggregateValue += mv.getValue();
				//System.out.println("dmv: "+scope.getName()+"\t"+mv.getValue()+"\t"+this.excAggregateValue);
			}
			// copy to the inclusive metric
			scope.setMetricValue(this.iInclusive, mv);
		}
	}
	
	/**
	 * Method to accumulate the metric value from the child to the parent
	 * @param parent
	 * @param source
	 */
	protected void accumulateToParent(Scope parent, Scope source) {
		if (withExclusiveAndInclusive)
			// we only compute inclusive metric when the information of exclusive metric is available
			parent.accumulateMetric(source, this.iInclusive, this.iInclusive, filter);
	}
	
	/**
	 * Method to accumulate the metric value from the child to the parent based on a given filter
	 * @param parent
	 * @param source
	 * @param filter
	 */
	protected void accumulateToParent(Scope parent, Scope source, MetricValuePropagationFilter myfilter) {
		if (withExclusiveAndInclusive)
			// we only compute inclusive metric when the information of exclusive metric is available
			parent.accumulateMetric(source, this.iInclusive, this.iInclusive, myfilter);
	}

	/*
	@Override
	protected void accumulateAncestor(Scope scope, Scope parent) {
		// TODO Auto-generated method stub
		
	} */

}
