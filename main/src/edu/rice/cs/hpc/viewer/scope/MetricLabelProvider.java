package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import edu.rice.cs.hpc.data.experiment.metric.Metric;
import edu.rice.cs.hpc.data.experiment.metric.MetricValue;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class MetricLabelProvider extends ColumnLabelProvider {
	private Metric metric;

	public MetricLabelProvider(Metric metricNew) {
		super();
		// TODO Auto-generated constructor stub
		this.metric = metricNew;
	}
	
	public String getText(Object element) {
		String text = "-";
		if ((metric != null) && (element instanceof Scope.Node)) {
			MetricValue mv;
			Scope.Node node = (Scope.Node) element;
			// laks
			mv = node.getScope().getMetricValue(metric);
			if(mv.getPercentValue() == 0.0) text = "";
			else{
					text = metric.getDisplayFormat().format(mv);
				//if (text.compareTo("-1.00e00       ") == 0) text = "0.0%";
			}
		}
		return text;
	}
	

}
