package edu.rice.cs.hpc.viewer.filter;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

/************************************************************************
 * 
 * Special view class to show the list of filters
 *
 ************************************************************************/
public class FilterView extends ViewPart implements IFilterView
{
	final static public String ID = "edu.rice.cs.hpc.viewer.filter.FilterView";
	private CheckboxTableViewer tableViewer;
	private ISourceProviderListener listener;
	private FilterStateProvider serviceProvider;
	
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
		// set the additional listener for this view
		// -----------------------------------------------------------------
		final ISourceProviderService service   = (ISourceProviderService)getSite().getWorkbenchWindow().getService(ISourceProviderService.class);
		serviceProvider  = (FilterStateProvider) service.getSourceProvider(FilterStateProvider.FILTER_REFRESH_PROVIDER);
		listener = new ISourceProviderListener() {
			
			@Override
			public void sourceChanged(int sourcePriority, String sourceName,
					Object sourceValue) 
			{
				// refresh the table whenever there's a new fresh data
				if (sourceName.equals(FilterStateProvider.FILTER_REFRESH_PROVIDER)) {
					if (sourceValue instanceof FilterMap)
					{
						FilterMap map = (FilterMap) sourceValue;
						tableViewer.setInput(map.getEntrySet());
					}
					tableViewer.refresh();
				}
			}
			
			@Override
			public void sourceChanged(int sourcePriority, Map sourceValuesByName) {}
		};
		serviceProvider.addSourceProviderListener( listener );
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
	public void dispose()
	{
		serviceProvider.removeSourceProviderListener(listener);
		serviceProvider.setSelection(null);
		super.dispose();
	}
}
