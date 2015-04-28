package edu.rice.cs.hpc.data.experiment;

import java.io.File;
import java.util.List;

import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.util.IUserData;


/****************************************************************************
 * 
 * abstract base experiment that contains metrics. <br/>
 * This class just load metrics without generating callers view and flat view
 *
 ****************************************************************************/
public abstract class BaseExperimentWithMetrics extends BaseExperiment {



	protected List<BaseMetric> metrics;



	public void setMetrics(List<BaseMetric> metricList) {

		metrics = metricList;
	}



	//////////////////////////////////////////////////////////////////////////
	//ACCESS TO METRICS													//
	//////////////////////////////////////////////////////////////////////////


	/*************************************************************************
	 *	Returns the array of metrics in the experiment.
	 ************************************************************************/

	public BaseMetric[] getMetrics()
	{
		return 	this.metrics.toArray(new BaseMetric[0]);
	}


	/*************************************************************************
	 *	Returns the number of metrics in the experiment.
	 ************************************************************************/

	public int getMetricCount()
	{
		return this.metrics.size();
	}




	/*************************************************************************
	 *	Returns the metric with a given index.
	 ************************************************************************/

	public BaseMetric getMetric(int index)
	{

		BaseMetric metric;
		// laks 2010.03.03: bug fix when the database contains no metrics
		try {
			metric = this.metrics.get(index);
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

		final int size = metrics.size();
		
		for (int i=0; i<size; i++) {

			final BaseMetric metric = metrics.get(i);
			if (metric.getShortName().equals(name))
				return metrics.get(i);
		}
		return null;	
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



}
