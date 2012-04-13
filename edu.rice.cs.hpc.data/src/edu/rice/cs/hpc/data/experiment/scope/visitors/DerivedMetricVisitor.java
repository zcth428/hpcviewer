package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
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
	private Experiment _experiment;
	
	/**
	 * Constructor
	 * @param metrics: list of metrics
	 * @param filter: filter for the view in which the metric will be inserted
	 * @param iMetricInc: the index position of the inclusive metric
	 * @param iMetricExc: the index position of the exclusive metric
	 */
	public DerivedMetricVisitor(Experiment experiment, MetricValuePropagationFilter filter, int iMetricInc, int iMetricExc) {
		super(experiment, filter);
		this.iExclusive = iMetricExc;
		this.iInclusive = iMetricInc;
		this.withExclusiveAndInclusive = (iMetricExc > 0);
		this._experiment = experiment;
		
		iBaseMetric = (withExclusiveAndInclusive? iMetricExc:iInclusive);
		assert (iMetricInc > 0);
	}
	
	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------
	public void visit(RootScope scope, ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PostVisit) {
			MetricValue mv = this._experiment.getMetric(this.iInclusive).getValue(scope);
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

		} else if (vt == ScopeVisitType.PreVisit) {
			// first, create metric exclusive
			BaseMetric metricExc = this._experiment.getMetric(iBaseMetric);
			// compute the derived metric of this scope
			MetricValue mv = metricExc.getValue(scope);
			// set the value of the derived metric into the scope
			if (withExclusiveAndInclusive) {
				scope.setMetricValue(this.iExclusive, mv);
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
}
