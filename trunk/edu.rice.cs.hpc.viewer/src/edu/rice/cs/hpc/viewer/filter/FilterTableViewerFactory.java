package edu.rice.cs.hpc.viewer.filter;

import java.util.Map.Entry;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.common.filter.FilterAttribute;
import edu.rice.cs.hpc.common.filter.FilterMap;
import edu.rice.cs.hpc.common.filter.FilterStateProvider;
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

		// ----------------------------------------------------------------------------
		// pattern column
		// ----------------------------------------------------------------------------
		final TableViewerColumn columnPattern = new TableViewerColumn(ctv, SWT.LEFT);
		TableColumn col = columnPattern.getColumn();
		col.setText("Pattern");
		col.setWidth(100);
		col.setToolTipText("Select a filter to delete or check/uncheck a filter pattern to enable/disable");
		columnPattern.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				Entry<String, FilterAttribute> item = (Entry<String, FilterAttribute>) cell.getElement();
				cell.setText(item.getKey());
			}
		});
		
		// ----------------------------------------------------------------------------
		// type column
		// ----------------------------------------------------------------------------
		final TableViewerColumn columnType = new TableViewerColumn(ctv, SWT.LEFT);
		col = columnType.getColumn();
		col.setText("Type");
		col.setWidth(100);
		col.setToolTipText("Select a type of filtering: exclusive means only the filtered scope is elided while inclusive means the filtered scope and its descendants are elided.");
		columnType.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell) {
				Entry<String, FilterAttribute> item = (Entry<String, FilterAttribute>) cell.getElement();
				cell.setText(item.getValue().getFilterType());
			}
		});
		columnType.setEditingSupport(new ComboEditingSupport(ctv));
		
		// the content of the table is an array of a map between a string and a boolean
		ctv.setContentProvider( new ArrayContentProvider() );
		
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
					FilterAttribute value = (FilterAttribute) ((Entry<String, FilterAttribute>)element).getValue();
					return value.enable.booleanValue();
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
				final Entry<String, FilterAttribute> element = (Entry<String, FilterAttribute>) event.getElement();
				final String key = element.getKey();
				FilterMap map    = FilterMap.getInstance();
				FilterAttribute attribute = new FilterAttribute();
				attribute.enable = event.getChecked();
				map.put(key, attribute);
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
					final Entry<String, FilterAttribute> item= (Entry<String, FilterAttribute>) select.getFirstElement();
					
					InputDialog dialog = new InputDialog(shell, "Rename a filter", 
							"Use a glob pattern to define a filter. For instance, a MPI* will filter all MPI routines", 
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
	    		Entry<String, FilterAttribute> item1 = (Entry<String, FilterAttribute>) e1;
	    		Entry<String, FilterAttribute> item2 = (Entry<String, FilterAttribute>) e2;
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
	    FilterMap filterMap = FilterMap.getInstance();
	    ctv.setInput(filterMap.getEntrySet());
	    
	    table.setHeaderVisible(true);
	    table.pack();

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
	
	/**********************************************************
	 * 
	 * Class to show a combo box inside a table cell 
	 * The combo contains any enumerations in {@link edu.rice.cs.hpc.common.filter.FilterAttribute.Type }
	 *
	 **********************************************************/
	private static class ComboEditingSupport extends EditingSupport
	{
		final private ComboBoxCellEditor editor;
		
		public ComboEditingSupport(TableViewer viewer) {
			super(viewer);
			
			// fill the content of the combo box
			FilterAttribute.Type []types = FilterAttribute.Type.values();
			String []items = new String[types.length];
			for(int i=0; i<types.length; i++)
			{
				items[i] = types[i].name();
			}
			editor = new ComboBoxCellEditor(viewer.getTable(), items);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Entry<?, ?>)
			{
				Entry<String, FilterAttribute> item = (Entry<String, FilterAttribute>) element;
				FilterAttribute att = item.getValue();
				return att.filterType.ordinal();
			}
			return 0;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof Entry<?, ?>)
			{
				Entry<String, FilterAttribute> item = (Entry<String, FilterAttribute>) element;
				if (value instanceof Integer)
				{
					FilterAttribute att = item.getValue();
					Integer ordinal     = (Integer) value;
					FilterAttribute.Type typeNew  = FilterAttribute.Type.values()[ordinal];
					att.filterType 				  = typeNew;
					
					String key = item.getKey();
					
					// save it to the global world
					FilterMap map 		   = FilterMap.getInstance();
					map.put(key, att);
					getViewer().update(element, null);
				}
			}
		}		
	}
}
