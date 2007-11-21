package edu.rice.cs.hpc.analysis;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;

import edu.rice.cs.hpc.data.experiment.*; //Experiment.Experiment;
//import edu.rice.cs.data.Experiment.InvalExperimentException;

import edu.rice.cs.hpc.viewer.scope.ScopeView;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;

/**
 * Class to be used as an interface between the GUI and the data experiment
 * This class should be called from an eclipse view !
 * @author laksono
 *
 */
public class ExperimentView {
	//ScopeView objView;
	private org.eclipse.ui.IWorkbenchPage objPage;		// workbench current page
	private ScopeView []listOfViews; // list of views used
	
	/**
	 * Constructor for Data experiment. Needed to link with the view
	 * @param objTarget: the scope view to link with
	 */
	public ExperimentView(org.eclipse.ui.IWorkbenchPage objTarget) {
		this.objPage = objTarget;
	}
	
	/**
	 * DO NOT CALL THIS CONSTRUCTOR if possible
	 * This will try to find the ScopeView manually and not portable
	 */
	public ExperimentView(){
		objPage = org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	
	/**
	 * A wrapper of loadExperiment() by adding some processing and generate the views
	 * @param sFilename
	 */
	public void loadExperimentAndProcess(String sFilename) {
		Experiment experiment = this.loadExperiment(sFilename);
		if(experiment != null) {
	        experiment.postprocess();
	        this.generateView(experiment);
		}
	}
	/**
	 * Load an XML experiment file based on the filename (uncheck for its inexistence)
	 * This method will display errors whenever encountered
	 * @param sFilename: the xml experiment file
	 */
	public Experiment loadExperiment(String sFilename) {
	       Experiment experiment;
	       org.eclipse.swt.widgets.Shell objShell = this.objPage.getWorkbenchWindow().getShell();
           
           // open the experiment if possible
      try
           {
           experiment = new Experiment(new java.io.File(sFilename));
           experiment.open();
           
      } catch(java.io.FileNotFoundException fnf)
      {
           System.err.println("File not found:" + sFilename +fnf.getMessage());
           MessageDialog.openError(objShell, "Error:File not found", "Cannot find the file "+sFilename);
           experiment = null;
      }
      catch(java.io.IOException io)
      {
           System.err.println("IO error:" +  sFilename +io.getMessage());
           MessageDialog.openError(objShell, "Error: Unable to read", "Cannot read the file "+sFilename);
           experiment = null;
      }
      catch(InvalExperimentException ex)
      {
           String where = sFilename + " " + " " + ex.getLineNumber();
           System.err.println("$" +  where);
           MessageDialog.openError(objShell, "Incorrect Experiment File", "File "+sFilename + " has incorrect tag at line:"+ex.getLineNumber());
           experiment = null;
      } 
      catch(NullPointerException npe)
      {
           System.err.println("$" + npe.getMessage() + sFilename);
           MessageDialog.openError(objShell, "File is invalid", "File has null pointer:"+sFilename + ":"+npe.getMessage());
           experiment = null;
      }
      return experiment;
	}
	
	/**
	 * Retrieve the list of all used views
	 * @return list of views
	 */
	public ScopeView[] getViews() {
		return this.listOfViews;
	}
	/**
	 * Generate multiple views for an experiment depending on the number of root scopes
	 * @param experiment Experiment data
	 */
	public void generateView(Experiment experiment) {
		// optimistic approach: hide all the visible views first
		this.removeViews();
		// next, we retrieve all children of the scope and display them in separate views
		ArrayList<RootScope> rootChildren = (ArrayList<RootScope>)experiment.getRootScopeChildren();
		int nbChildren = rootChildren.size();
		this.listOfViews = new ScopeView[nbChildren];
		for(int k=0;nbChildren>k;k++)
		{
			RootScope child = (RootScope) rootChildren.get(k);
			try {
				ScopeView objView; 
				if(k>0)
					// multiple view: we need to have additional secondary ID
					objView = (ScopeView)this.objPage.showView(edu.rice.cs.hpc.viewer.scope.ScopeView.ID, 
					"view"+child.getRootName(), org.eclipse.ui.IWorkbenchPage.VIEW_VISIBLE);
				else
					// first view: usually already created by default by the perspective
					objView = (ScopeView) this.objPage.showView(edu.rice.cs.hpc.viewer.scope.ScopeView.ID);
				// ATTENTION: for unknown reason, calltree will not display the aggregate values when using the child
				// therefore, we need to create a dummy root then attach it to the children
				// TODO: This should be fix in the Scope class in the future
				if(nbChildren>1) {
					RootScope newRoot = (RootScope)child.getParentScope().duplicate();
					newRoot.addSubscope(child);
					objView.setInput(experiment, newRoot);		// update the data content
				} else
					objView.setInput(experiment, (RootScope)experiment.getRootScope());
				objView.setViewTitle(child.getRootName());	// update the title (do we need this ?)
				this.listOfViews[k] = objView;
			} catch (org.eclipse.ui.PartInitException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * Hide the all the visible views
	 */
	private void removeViews() {
		org.eclipse.ui.IViewReference views[] = this.objPage.getViewReferences();
		int nbViews = views.length;
		
		for(int i=0;i<nbViews;i++)
			this.objPage.hideView(views[i]);
	}
}
