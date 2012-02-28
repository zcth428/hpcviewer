package edu.rice.cs.hpc.viewer.actions;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.ExperimentMerger;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;

public class MergeDatabase extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		final Experiment[] dbArray = vWin.getExperiments();

		if (dbArray.length > 1) {
			ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), dbArray, 
					new ArrayContentProvider(), new ExperimentLabelProvider(), "Select the databases to merge:");
				dlg.setTitle("Select Databases");
				dlg.open();
				Object[] selectedDatabases = dlg.getResult();

				if ((selectedDatabases != null) && (selectedDatabases.length == 2)) {

					final Experiment db1 = (Experiment) selectedDatabases[0];
					final Experiment db2 = (Experiment) selectedDatabases[1];

					final Experiment expMerged = ExperimentMerger.merge(db1, db2);
					
					ExperimentView ev = new ExperimentView(window.getActivePage());
					ev.generateView(expMerged);
				}
		}
		
		return null;
	}

	
	private class ExperimentLabelProvider extends LabelProvider {
		
		public String getText(Object element) 
		{
			final Experiment exp = (Experiment) element;
			final File file = exp.getXMLExperimentFile();
			
			final String path = file.getAbsolutePath();
			return path;
		}
	}
}
