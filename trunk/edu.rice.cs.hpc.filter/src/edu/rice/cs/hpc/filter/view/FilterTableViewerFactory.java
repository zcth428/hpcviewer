package edu.rice.cs.hpc.filter.view;

import java.util.Map.Entry;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import edu.rice.cs.hpc.data.filter.FilterAttribute;
import edu.rice.cs.hpc.filter.service.FilterMap;


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
		//columnType.setEditingSupport(new ComboEditingSupport(ctv));
		
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
	
	
	/**********************************************************
	 * 
	 * Class to show a combo box inside a table cell 
	 * The combo contains any enumerations in {@link edu.rice.cs.hpc.data.filter.FilterAttribute.Type }
	 *
	 **********************************************************/
	/*private static class ComboEditingSupport extends EditingSupport
	{
		final private ComboBoxCellEditor editor;
		
		public ComboEditingSupport(TableViewer viewer) {
			super(viewer);
			
			String []items = FilterAttribute.getFilterNames();
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
	}*/
}
