package edu.rice.cs.hpc.viewer.actions;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final ViewerWindow vWin = ViewerWindowManager.getViewerWindow(window);
		final Experiment[] dbArray = vWin.getExperiments();

		// merge is enabled if the number of open databases is more than 1
		if (dbArray.length > 1) 
		{
			final Experiment db1;
			final Experiment db2;

			// if we have 2 open database, we can go directly merging them
			// otherwise we should ask user to select which database to be merged
			if (dbArray.length == 2)
			{
				db1 = (Experiment) dbArray[0];
				db2 = (Experiment) dbArray[1];
				
			} else
			{
				// selecting database
				ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), dbArray, 
						new ArrayContentProvider(), new ExperimentLabelProvider(), "Select two databases to merge:");
				dlg.setTitle("Merging database");
				dlg.open();
				Object[] selectedDatabases = dlg.getResult();

				if ((selectedDatabases != null) && (selectedDatabases.length == 2)) {

					db1 = (Experiment) selectedDatabases[0];
					db2 = (Experiment) selectedDatabases[1];
				} else
				{
					// either only select one or none of cancel
					return null;
				}
			}
			// try to asynchronously merge the experiments. it may take some time to finish
			Display display = HandlerUtil.getActiveShell(event).getDisplay();
			display.asyncExec(new Runnable(){

				////@Override
				public void run() {
					final Experiment expMerged = ExperimentMerger.merge(db1, db2, ExperimentMerger.MergeType.TOP_DOWN, false);

					ExperimentView ev = new ExperimentView(window.getActivePage());
					ev.generateView(expMerged);
				}				
			});
		}
		else
		{
			MessageDialog.openError( window.getShell(), "Error merging database", 
					"The number of open database has to be at least 2 to enable to merge");
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
