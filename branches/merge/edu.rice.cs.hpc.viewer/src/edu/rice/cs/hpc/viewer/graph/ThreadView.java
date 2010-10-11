package edu.rice.cs.hpc.viewer.graph;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.MetricRaw;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

public class ThreadView extends ViewPart {
	final static public String id = "edu.rice.cs.hpc.viewer.graph.ThreadView";
	
	private TableViewer table;
	
	@Override
	public void createPartControl(Composite parent) {

		table = new TableViewer(parent, SWT.VIRTUAL);
		
		TableViewerColumn column = new TableViewerColumn(table, SWT.LEFT, 0);
		column.setLabelProvider(new ColumnLabelProvider() );
		column.getColumn().setText("Process.Threads");
		column.getColumn().setWidth(200);
		
		DeferredContentProvider content_provider = new DeferredContentProvider( 
				Collator.getInstance(Locale.US) );
		
		table.setContentProvider(content_provider);
		
        GridData data = new GridData(GridData.FILL_BOTH);
        table.getTable().setLayoutData(data);
        
	}

	@Override
	public void setFocus() {

		table.getTable().setFocus();
	}

	
	public void showScope( Scope scope ) {
		Experiment experiment = scope.getExperiment();
		MetricRaw metrics[] = experiment.getMetricRaw();
		
		ThreadLevelDataManager objDataManager = new ThreadLevelDataManager(experiment);
		HashMap<Double, DataModel> ll;
		
		for ( MetricRaw metric: metrics ) {
			TableViewerColumn column = new TableViewerColumn( table, SWT.RIGHT, metric.getID()+1 );
			column.getColumn().setText( metric.getDisplayName() );

			double ids[] = objDataManager.getProcessIDsDouble(metric.getID());
			DataModel data = new DataModel();
			double value[];
			
			try {
				value = objDataManager.getMetrics(metric, scope.getCCTIndex() );
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	class DataModel {
		public double process_id;
		public double metric_value[];
		
	}
	
}
