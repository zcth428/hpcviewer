package edu.rice.cs.hpc.viewer.framework;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.jface.action.Action;

import edu.rice.cs.hpc.viewer.help.*;
/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IWorkbenchAction aboutAction; // about dialog box
	private IWorkbenchAction showPreference;
	private Action showContentHelpAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		aboutAction = ActionFactory.ABOUT.create(window); // about action
		aboutAction.setDescription("hpcviewer is a user interface for interactive exploration of performance data. hpcviewer is built upon the Eclipse Rich Client Platform.");
	    register(aboutAction);

	    this.showContentHelpAction = new HelpAction(window);
	    register(this.showContentHelpAction);

	    this.showPreference = ActionFactory.PREFERENCES.create(window);
	    register(this.showPreference);
	}

	protected void fillMenuBar(IMenuManager menuBar) {

		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
		// Add a group marker indicating where action set menus will appear.
		   menuBar.add(new org.eclipse.jface.action.GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		   menuBar.add(helpMenu);
		// Help
		helpMenu.add(aboutAction);
		helpMenu.add(this.showContentHelpAction);
		
	}

}
