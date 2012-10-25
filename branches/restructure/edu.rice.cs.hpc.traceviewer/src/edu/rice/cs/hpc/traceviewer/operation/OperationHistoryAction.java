package edu.rice.cs.hpc.traceviewer.operation;


import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/******
 * 
 * Base abstract class for undo/redo action menu
 *
 */
public abstract class OperationHistoryAction extends Action
	implements IAction, IOperationHistoryListener, IMenuCreator 
{
	private Menu menu;
	
	public OperationHistoryAction(ImageDescriptor img) {
		super(null, Action.AS_DROP_DOWN_MENU);
		setImageDescriptor(img);
		setMenuCreator(this);
		TraceOperation.getOperationHistory().addOperationHistoryListener(this);
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
		for (int i=operations.length-2; i>=0; i--) {
			final IUndoableOperation op = operations[i];
			if (op.canUndo()) 
			{
				Action action = new Action(op.getLabel()) {
					public void run() {
						execute(op);
						setStatus();
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
		setStatus();
	}
	
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IOperationHistoryListener#historyNotification(org.eclipse.core.commands.operations.OperationHistoryEvent)
	 */
	public void historyNotification(final OperationHistoryEvent event) 
	{
		final IUndoableOperation operation = event.getOperation();
		
		if (operation.hasContext(TraceOperation.undoableContext)) {
			switch(event.getEventType()) {
			case OperationHistoryEvent.OPERATION_ADDED:
			case OperationHistoryEvent.OPERATION_REMOVED:
				setStatus();
				break;
			}
		}
	}
	
	private void setStatus() 
	{
		final IUndoableOperation []ops = getHistory(); 
		setEnabled(ops.length>1);
	}
	
	abstract protected IUndoableOperation[] getHistory(); 
	abstract protected void execute();
	abstract protected void execute(IUndoableOperation operation) ;
}
