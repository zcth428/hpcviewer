package edu.rice.cs.hpc.filter.view;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.ui.Util;
import edu.rice.cs.hpc.data.filter.FilterAttribute;
import edu.rice.cs.hpc.filter.pattern.PatternValidator;
import edu.rice.cs.hpc.filter.service.FilterMap;
import edu.rice.cs.hpc.filter.service.FilterStateProvider;
import edu.rice.cs.hpc.filter.view.FilterTableViewerFactory;
import edu.rice.cs.hpc.filter.view.IFilterView;

/************************************************************************
 * 
 * Special view class to show the list of filters
 *
 ************************************************************************/
public class FilterView extends ViewPart implements IFilterView
{
	final static public String	 	 ID = "edu.rice.cs.hpc.filter.view.FilterView";
	private CheckboxTableViewer 	 tableViewer;
	private ISourceProviderListener  sourceProviderListener;
	private FilterStateProvider 	 serviceProvider;
	private ICheckStateListener 	 checkStateListener;
	private SelectionChangedListener selectionChangedListener;
	
	public FilterView() {
		new FilterMap();
	}

	@Override
	public void createPartControl(Composite parent) {
		
		// -----------------------------------------------------------------
		// set the table
		// -----------------------------------------------------------------
		tableViewer = FilterTableViewerFactory.getTable(parent);
		
		final Table table = tableViewer.getTable();
		table.setLinesVisible(true);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(table);

		table.setVisible(true);
		table.pack();

		// -----------------------------------------------------------------
		// get filter state provider
		// -----------------------------------------------------------------
		final ISourceProviderService service   = (ISourceProviderService)getSite().getWorkbenchWindow().
				getService(ISourceProviderService.class);
		serviceProvider  = (FilterStateProvider) service.getSourceProvider(FilterStateProvider.FILTER_REFRESH_PROVIDER);
		

		// -----------------------------------------------------------------
	    // add a listener when the user change the value of check box
		// -----------------------------------------------------------------
		checkStateListener = new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				final Entry<String, FilterAttribute> element = (Entry<String, FilterAttribute>) event.getElement();
				final String key = element.getKey();
				FilterMap map    = FilterMap.getInstance();
				
				// get the original attribute to be modified
				FilterAttribute attribute = element.getValue();
				attribute.enable = event.getChecked();
				
				// save to the registry
				map.put(key, attribute);
				map.save();
				
				// notify the changes
				serviceProvider.refresh();
			}	    	
	    };
		tableViewer.addCheckStateListener( checkStateListener );
	    
		// -----------------------------------------------------------------
	    // add a listener everytime the user change the selections
		// -----------------------------------------------------------------
		selectionChangedListener = new SelectionChangedListener();
		tableViewer.addSelectionChangedListener(selectionChangedListener);

		// -----------------------------------------------------------------
	    // add listener for double-clicking in the table
		// -----------------------------------------------------------------
		tableViewer.addDoubleClickListener( new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection != null && !selection.isEmpty()) 
				{
					final Shell shell 				 = Util.getActiveShell();
					final StructuredSelection select = (StructuredSelection) selection;
					final Entry<String, FilterAttribute> item= (Entry<String, FilterAttribute>) select.getFirstElement();
					
					InputDialog dialog = new InputDialog(shell, "Rename a filter", 
							"Use a glob pattern to define a filter. For instance, a MPI* will filter all MPI routines", 
							item.getKey(), new PatternValidator());
					if (dialog.open() == Window.OK)
					{
						final FilterMap filterMap = FilterMap.getInstance();
						filterMap.update(item.getKey(), dialog.getValue());
						
						// notify the table and others that we need to refresh the content
						serviceProvider.refresh();
					}
				}
			}
		});

		// -----------------------------------------------------------------
		// set the additional listener for this view
		// -----------------------------------------------------------------
		sourceProviderListener = new ISourceProviderListener() {
			
			@Override
			public void sourceChanged(int sourcePriority, String sourceName,
					Object sourceValue) 
			{
				// refresh the table whenever there's a new fresh data
				if (sourceName.equals(FilterStateProvider.FILTER_REFRESH_PROVIDER)) {
					// enable of disable filter
					FilterMap map = FilterMap.getInstance();
					tableViewer.setInput(map.getEntrySet());
					tableViewer.refresh();
				}
			}
			
			@Override
			public void sourceChanged(int sourcePriority, Map sourceValuesByName) {}
		};
		serviceProvider.addSourceProviderListener( sourceProviderListener );
	}

	@Override
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}


	@Override
	public void refresh() {
		FilterMap map = FilterMap.getInstance();
		tableViewer.setInput(map.getEntrySet());
		tableViewer.refresh();
	}

	@Override
	public Object[] getSelectedElements() {
		final Table table = tableViewer.getTable();
		TableItem []items = table.getSelection();
		
		if (items == null || items.length == 0)
			return null;
					
		Entry<String, Boolean> []elements = new Entry [items.length];
		int i = 0;
		for (TableItem item : items)
		{
			elements[i] = (Entry<String, Boolean>) item.getData();
			i++;
		}
		return elements;
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose()
	{
		tableViewer.removeCheckStateListener(checkStateListener);
		tableViewer.removeSelectionChangedListener(selectionChangedListener);
		
		serviceProvider.removeSourceProviderListener(sourceProviderListener);
		serviceProvider.setSelection(null);
		
		super.dispose();
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

}
