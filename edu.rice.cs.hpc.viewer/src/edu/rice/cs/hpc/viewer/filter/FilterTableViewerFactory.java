package edu.rice.cs.hpc.viewer.filter;

import java.util.Map.Entry;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;

/***************************************************
 * 
 * class to generate a FilterTableViewer-like class
 *
 * since we couldn't derive CheckboxTableViewer, we need to create a class that
 * automatically set and configure CheckboxTableViewer to mimic something  like
 * FilterTableViewer
 ***************************************************/
public class FilterTableViewerFactory  
{	
	static CheckboxTableViewer getTable(Composite parent)
	{
		final CheckboxTableViewer ctv = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.MULTI);
		
		final Table table = ctv.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setToolTipText("Select a filter to delete or check/uncheck a filter to be applied to the views");

		// the content of the table is an array of a map between a string and a boolean
		ctv.setContentProvider( new ArrayContentProvider() );
		
		// customize the text of the items in the table
	    ctv.setLabelProvider(new FilterLabelProvider());
	    
	    // customize the check value
	    ctv.setCheckStateProvider( new ICheckStateProvider() {
			
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}
			
			@Override
			public boolean isChecked(Object element) {
				if (element instanceof Entry<?,?>) 
				{
					Boolean value = (Boolean) ((Entry<String, Boolean>)element).getValue();
					return value.booleanValue();
				}
				return false;
			}
		} );
	    
	    // add a listener everytime the user change the selections
	    ctv.addSelectionChangedListener(new SelectionChangedListener());

	    // add a listener when the user change the value of check box
	    ctv.addCheckStateListener( new ICheckStateListener() {

			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				final Entry<String, Boolean> element = (Entry<String, Boolean>) event.getElement();
				final String key = element.getKey();
				FilterMap map    = FilterMap.getInstance();
				map.put(key, event.getChecked());
				map.save();
			}	    	
	    });
	    
	    // add listener for double-clicking in the table
	    ctv.addDoubleClickListener( new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection != null && !selection.isEmpty()) 
				{
					final Shell shell 				 = Util.getActiveShell();
					final StructuredSelection select = (StructuredSelection) selection;
					final Entry<String, Boolean> item= (Entry<String, Boolean>) select.getFirstElement();
					
					InputDialog dialog = new InputDialog(shell, "Rename filtner", "Enter new filter", 
							item.getKey(), new PatternValidator());
					if (dialog.open() == Window.OK)
					{
						final FilterMap filterMap = FilterMap.getInstance();
						filterMap.update(item.getKey(), dialog.getValue());
						
						// notify the table that we need to refresh the content
						ctv.setInput(filterMap.getEntrySet());
						ctv.refresh();
					}
				}
			}
		});
	    
	    // set comparison class for sorting the content of the table
	    ctv.setComparator( new ViewerComparator() {
	    	@Override
	    	public int compare(Viewer viewer, Object e1, Object e2) 
	    	{
	    		Entry<String, Boolean> item1 = (Entry<String, Boolean>) e1;
	    		Entry<String, Boolean> item2 = (Entry<String, Boolean>) e2;
	    		return (item1.getKey().compareTo(item2.getKey()));
	    	}
	    });
	    
	    
	    // -------------------------------------------------------------------------------------------------
	    // BEGIN: I have no idea what are these below statements, just copied in from the internet
	    // -------------------------------------------------------------------------------------------------
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(ctv);

		int feature = ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL
				| ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TableViewerEditor.create(ctv, null, actSupport, feature);
	    // -------------------------------------------------------------------------------------------------
	    // END: I have no idea what are these above statements, just copied in from the internet
	    // -------------------------------------------------------------------------------------------------

		// set the input of the table
	    FilterMap filterMap = new FilterMap();
	    ctv.setInput(filterMap.getEntrySet());

		return ctv;
	}
	
	private static class SelectionChangedListener implements ISelectionChangedListener
	{
		private final FilterStateProvider provider;
		
		public SelectionChangedListener()
		{
			IWorkbenchWindow winObj = Util.getActiveWindow();
			ISourceProviderService sourceProviderService = (ISourceProviderService) winObj.getService(
					ISourceProviderService.class);
			provider = (FilterStateProvider) sourceProviderService.getSourceProvider(FilterStateProvider.FILTER_STATE_PROVIDER);
		}
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection selection = event.getSelection();
			provider.setSelection(selection);
		}
		
	}
	
	/*********
	 * 
	 * Label provider for the table filter
	 *
	 *********/
	private static class FilterLabelProvider implements ILabelProvider
	{

		@Override
		public void addListener(ILabelProviderListener listener) {}

		@Override
		public void dispose() {}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof Entry<?, ?>)
			{
				// we just show the filter name in the table
				Entry<String, Boolean> entry = (Entry<String, Boolean>) element;
				return entry.getKey();
			}
			return (String) element; // should be an exception ?
		}
	}
}
