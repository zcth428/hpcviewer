package edu.rice.cs.hpc.viewer.actions;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.merge.ExperimentMerger;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.window.ViewerWindow;
import edu.rice.cs.hpc.viewer.window.ViewerWindowManager;


/*******************************************************************
 * 
 * command action to merge two databases (at the moment)
 * 
 * Databases have to be loaded first before merged, and the user
 * needs to decide which databases to be combined
 * 
 *******************************************************************/
public class MergeDatabase extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		final Experiment[] dbArray = vWin.getExperiments();

		if (dbArray.length > 1) 
		{
			ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), dbArray, 
					new ArrayContentProvider(), new ExperimentLabelProvider(), "Select two databases to merge:");
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


	/*****
	 * 
	 * label for the list of databases
	 *
	 */
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
