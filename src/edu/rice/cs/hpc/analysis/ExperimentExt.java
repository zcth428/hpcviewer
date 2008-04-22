/**
 * 
 */
package edu.rice.cs.hpc.analysis;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.metric.MetricType;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.filters.EmptyMetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.FlatViewInclMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;

/**
 * Class to manage, open and post-processing long and big experiment file.
 * User needs to call the method  openAndprocessExperiment to process the database
 * @author laksono
 *
 */
public class ExperimentExt {

	protected Shell shell;
	protected ExperimentProcessing expProc;
	
	/**
	 * Constructor of the class. 
	 * @param objShell shell window
	 * @param expView experiment-viewer interface to generate scope view
	 * @param exp the copy-constructor for experiemnt
	 */
	public ExperimentExt(Shell objShell, ExperimentView expView, Experiment exp) {
		this.shell = objShell;
		this.expProc = new ExperimentProcessing(objShell, expView, exp);
	}

	/**
	 * constructor with the file
	 * @param objShell shell window
	 * @param expView the viewer interface
	 * @param filename the name of the database file
	 */
	public ExperimentExt(Shell objShell, ExperimentView expView, File filename) {
		this.shell = objShell;
		this.expProc = new ExperimentProcessing(objShell, expView, filename);
	}
	
	/**
	 * Open and processing the experiment database. This method will show a monitor dialog
	 * to show the progress.
	 */
	public void openAndprocessExperiment() {
		ProgressMonitorDialog monitorDlg = new ProgressMonitorDialog(shell); 
        try {
            monitorDlg.run(true, true, this.expProc);
          } catch (InvocationTargetException e) {
            MessageDialog.openError(shell, "Error", e.getMessage());
          } catch (InterruptedException e) {
            MessageDialog.openInformation(shell, "Cancelled", e.getMessage());
          }

	}
}

/**
 * This class represents a long running opening, parsing and postprocessing
 * experiment file.
 *  For some database, it takes really long time, others don't
 */
class ExperimentProcessing extends Experiment implements IRunnableWithProgress {
  // The total sleep time
  private static final int TOTAL_TIME = 8;
  private Shell shell;
  private ExperimentView expViewer;
  /**
   * Experiment with long running processing feature
   * 
   * @param objShell
   * @param indeterminate whether the animation is unknown
   */
  public ExperimentProcessing(Shell objShell, ExperimentView expView, Experiment exp) {
	  super(exp);
	  this.shell = objShell;
	  this.expViewer = expView;
  }

  /**
   * 
   * @param objShell
   * @param filename
   */
  public ExperimentProcessing(Shell objShell, ExperimentView expView, File filename) {
	  super(filename);
	  this.shell =objShell;
	  this.expViewer = expView;
  }
  /**
   * Runs the long running operation: parsing, opening and post-processing (if necessary)
   * 
   * @param monitor the progress monitor
   */
  public void run(IProgressMonitor monitor) throws InvocationTargetException,
      InterruptedException {
	  boolean bSuccessful = false;
	  
	  // ---- begin long operation
	  monitor.beginTask("Processing experiment database", TOTAL_TIME);
	  if (this.parse()) {
	      monitor.worked(1);
	      monitor.subTask("Post-processing the database");
	      bSuccessful = this.postprocess(monitor);
	  }
      monitor.done();
      
      // ----- end long operation
      if (monitor.isCanceled())
        throw new InterruptedException("Experiment database processing has been canceled.");
      else if(bSuccessful) {
    	  this.expViewer.generateView(this);
      }
  }
  
  /**
   * Parsing the XML file
   * @return true if everything is OK, false otherwise
   */
  public boolean parse() {
	  String sFilename = this.fileExperiment.getName();
	  boolean bSuccess = false;
	  try {
		  this.experimentFile.parse(this);
		  bSuccess = true;
      } catch(java.io.FileNotFoundException fnf)
      {
           System.err.println("File not found:" + fnf.getMessage());
           MessageDialog.openError(this.shell, "Error:File not found", "Cannot find the file "+sFilename);
      }
      catch(java.io.IOException io)
      {
           System.err.println("IO error:" +  io.getMessage());
           MessageDialog.openError(this.shell, "Error: Unable to read", "Cannot read the file "+sFilename);
      }
      catch(InvalExperimentException ex)
      {
           String where = ""+ ex.getLineNumber();
           System.err.println("$" +  where);
           MessageDialog.openError(this.shell, "Incorrect Experiment File", "File "+this.experimentFile + 
        		   " has incorrect tag at line:"+ex.getLineNumber());
      } 
      catch(NullPointerException npe)
      {
           System.err.println("$" + npe.getMessage() + sFilename);
           MessageDialog.openError(this.shell, "File is invalid", "File has null pointer:"+sFilename + ":"+npe.getMessage());
      }
      return bSuccess;
  }
  
  /**
   * Long post-processing
   * @param monitor
   * @return
   */
  public boolean postprocess(IProgressMonitor monitor) {
		if (this.rootScope.getSubscopeCount() <= 0) return false;
		// Get first scope subtree: CCT or Flat
		Scope firstSubTree = this.rootScope.getSubscope(0);
		if (!(firstSubTree instanceof RootScope)) return false;
		RootScopeType firstRootType = ((RootScope)firstSubTree).getType();
		
		if (firstRootType.equals(RootScopeType.CallTree)) {
			// accumulate, create views, percents, etc
			Scope callingContextViewRootScope = firstSubTree;

			// laks: prepare metrics
			monitor.setTaskName("Metric preparation .... ");
			EmptyMetricValuePropagationFilter emptyFilter = new EmptyMetricValuePropagationFilter();
			InclusiveOnlyMetricPropagationFilter rootInclProp = new InclusiveOnlyMetricPropagationFilter(this.getMetrics());
			if(monitor.isCanceled()) return false;
			else  monitor.worked(1);

			// Laks: normalize the line scope
			monitor.setTaskName("Calling context: Normalize line scope .... ");
			normalizeLineScopes(callingContextViewRootScope, emptyFilter); // normalize all
			if(monitor.isCanceled()) return false;
			else  monitor.worked(1);

			// DFS computation for inclusive metrics
			monitor.setTaskName("Calling context: Computing inclusive metrics .... ");
			addInclusiveMetrics(callingContextViewRootScope, rootInclProp);
			addInclusiveMetrics(callingContextViewRootScope, 
			  new ExclusiveOnlyMetricPropagationFilter(this.getMetrics()));

			copyMetricsToPartner(callingContextViewRootScope, MetricType.INCLUSIVE, emptyFilter);
			if(monitor.isCanceled()) return false;
			else  monitor.worked(1);


			// Callers View
			monitor.setTaskName("Caller view: creation .... ");
			Scope callersViewRootScope = createCallersView(callingContextViewRootScope);
			copyMetricsToPartner(callersViewRootScope, MetricType.EXCLUSIVE, emptyFilter);
			if(monitor.isCanceled()) return false;
			else  monitor.worked(1);

			// Flat View
			monitor.setTaskName("Flat view: creation .... ");
			Scope flatViewRootScope = createFlatView(callingContextViewRootScope);
			addInclusiveMetrics(flatViewRootScope, new FlatViewInclMetricPropagationFilter(this.getMetrics()));
			flatViewRootScope.accumulateMetrics(callingContextViewRootScope, rootInclProp, this.getMetricCount());
			if(monitor.isCanceled()) return false;
			else  monitor.worked(1);

			monitor.setTaskName("Computing percentage .... ");
			addPercents(callingContextViewRootScope, (RootScope) callingContextViewRootScope);
			addPercents(callersViewRootScope, (RootScope) callingContextViewRootScope);
			addPercents(flatViewRootScope, (RootScope) callingContextViewRootScope);
			if(monitor.isCanceled()) return false;
			else  monitor.worked(1);

		} else if (firstRootType.equals(RootScopeType.Flat)) {
			addPercents(firstSubTree, (RootScope) firstSubTree);
		} else {
			// ignore; do no postprocessing
		}
		 return true;
	}
}