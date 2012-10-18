package edu.rice.cs.hpc.traceviewer.operation;


import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public abstract class OperationHistoryAction extends Action implements IAction,
		IMenuCreator 
{
	private Menu menu;
	
	public OperationHistoryAction(ImageDescriptor img) {
		super(null, Action.AS_DROP_DOWN_MENU);
		setImageDescriptor(img);
		setMenuCreator(this);
	}
	
	@Override
	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		if (menu != null) 
			menu.dispose();
		
		menu = new Menu(parent);
		
		IUndoableOperation[] operations = getHistory();
		
		// create a list of menus of undoable operations
		for (final IUndoableOperation op : operations) {
			if (op.canUndo()) 
			{
				Action action = new Action(op.getLabel()) {
					public void run() {
						execute(op);
					}
				};
				addActionToMenu(menu, action);
			}
		} 
		return menu;
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}
	
	@Override
	public void run() {
		execute();
	}
	
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	abstract protected IUndoableOperation[] getHistory(); 
	abstract protected void execute();
	abstract protected void execute(IUndoableOperation operation) ;
}
