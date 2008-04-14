package edu.rice.cs.hpc.viewer.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import edu.rice.cs.hpc.data.experiment.ICheckProcess;
/**
 * This class demonstrates JFace's ProgressMonitorDialog class
 */
public class ShowProgressTask implements ICheckProcess {
	
	private Shell objShell;
	private TaskOperation objTask;
	static long lTime;

	/**
	 * Current task has finished, ready to execute the next task
	 * @param str name of the new task to execute
	 */
	public void advance(String str) {
		if(this.objTask != null) {
			this.objTask.advanceTask(str);
		}
		long lCurrent = System.currentTimeMillis();
		long lDuration = lCurrent - lTime;
		System.out.println("Experiment task: "+ str +" = "+lDuration);
		lTime = lCurrent;
	}
  /**
   * ShowProgress constructor
   */
  public ShowProgressTask(Shell shell) {
	  this.objShell = shell;
	  lTime = System.currentTimeMillis();
  }

  /**
   * run the progress dialog
   * @param iTotal
   * @param indeterminate
   */
  public void run(int iTotal, boolean indeterminate) {
      try {
    	  this.objTask = new TaskOperation(iTotal); 
          new ProgressMonitorDialog(objShell).run(true, true, this.objTask);
        } catch (InvocationTargetException e) {
          MessageDialog.openError(objShell, "Error", e.getMessage());
        } catch (InterruptedException e) {
          MessageDialog.openInformation(objShell, "Cancelled", e.getMessage());
        }
  }

  /**
   * Creates the main window's contents
   * 
   * @param parent the main window
   * @return Control
   */

  /**
   * The application entry point
   * 
   * @param args the command line arguments
   */
  public static void main(String[] args) {
	  Display display = new Display();
	  Shell shell = display.getActiveShell();
	  ShowProgressTask task = new ShowProgressTask(shell);
	  task.run(10, true);
  }
}



/**
 * This class represents a long running operation
 */
class TaskOperation implements IRunnableWithProgress {
  // The total sleep time
  private int TOTAL_TIME = 100;
  private int iProgress = 0;
  // The increment sleep time
  private int SLEEPTIME = 100;
  private String sTask;
  private boolean indeterminate;

  /**
   * LongRunningOperation constructor
   * 
   * @param indeterminate whether the animation is unknown
   */
  public TaskOperation(boolean indeterminate) {
    this.indeterminate = indeterminate;
  }

  public TaskOperation(int iTotalTime) {
	    this.TOTAL_TIME = iTotalTime;
  }

  public void advanceTask(String str) {
	  this.sTask = str;
	  this.iProgress++;
  }
  /**
   * Runs the long running operation
   * 
   * @param monitor the progress monitor
   */
  public void run(IProgressMonitor monitor) throws InvocationTargetException,
      InterruptedException {
    monitor.beginTask(this.sTask,
        indeterminate ? IProgressMonitor.UNKNOWN : TOTAL_TIME);
    for (; this.iProgress < TOTAL_TIME && !monitor.isCanceled(); ) {
      Thread.sleep(SLEEPTIME);
      monitor.worked(1);
      monitor.subTask(this.sTask);
    }
    monitor.done();
    if (monitor.isCanceled())
        throw new InterruptedException("The operation was cancelled");
  }
}