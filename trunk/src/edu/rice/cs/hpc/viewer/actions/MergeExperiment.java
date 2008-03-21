package edu.rice.cs.hpc.viewer.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.rice.cs.hpc.viewer.merge.MergeExperimentDialog;
import edu.rice.cs.hpc.viewer.merge.Merging;

public class MergeExperiment  implements IWorkbenchWindowActionDelegate {

	IWorkbenchWindow wParent;
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		this.wParent = window;
		
	}

	public void run(IAction action) {
		// TODO Auto-generated method stub
		//MergeWizard objWizard = new MergeWizard();
		//System.out.println(this.getClass()+": run action");
		//WizardDialog dialog = new WizardDialog(this.wParent.getShell(), new MergeWizard());
		//dialog.open();
		MergeExperimentDialog dialog = new MergeExperimentDialog(this.wParent.getShell());
		dialog.open();
		if(dialog.getReturnCode() == org.eclipse.jface.dialogs.IDialogConstants.OK_ID) {
			//System.out.println(this.getClass()+": run done");
			Merging objMerging = new Merging(this.wParent.getShell(), this.wParent);
			String sFile1 = dialog.getFirstFilename();
			String sFile2 = dialog.getSecondFilename();
			objMerging.Merge(sFile1, sFile2);
			//System.out.println(this.getClass()+": run finish");
		} else 
			System.out.println("Merging databases has been canceled");

	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
