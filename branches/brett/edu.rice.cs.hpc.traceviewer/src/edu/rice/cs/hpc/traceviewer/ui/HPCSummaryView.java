package edu.rice.cs.hpc.traceviewer.ui;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.traceviewer.painter.SummaryTimeCanvas;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

public class HPCSummaryView extends ViewPart
{

	public static final String ID = "hpcsummaryview.view";
	
	/**The composite that holds everything in the view*/
	Composite master;
	
	/**The canvas that actually displays this view*/
	SummaryTimeCanvas summaryCanvas;
	
	/**all gui is held here*/
	HPCTraceView traceview;
	
	public void createPartControl(Composite _master)
	{
		master = _master;
		
		try 
		{
			traceview = (HPCTraceView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HPCTraceView.ID);
		}
		catch (PartInitException e) 
		{
			traceview = null;
			e.printStackTrace();
			System.exit(0);
		}
		setupEverything();
		setListener();
	}
	
	private void setupEverything()
	{
		/*************************************************************************
		 * Master Composite
		 */
		
		master.setLayout(new GridLayout());
		master.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		/*************************************************************************
		 * Summary View Canvas
		 */
		
		summaryCanvas = new SummaryTimeCanvas(master);
		summaryCanvas.setLayout(new GridLayout());
		summaryCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		summaryCanvas.setVisible(false);
		
		//traceview.detailCanvas.setSummaryCanvas(summaryCanvas);
	}
	
	private void setListener() {
		ISourceProviderService service = (ISourceProviderService)getSite().getService(ISourceProviderService.class);
		ISourceProvider serviceProvider = service.getSourceProvider(DataService.DATA_UPDATE);
		serviceProvider.addSourceProviderListener( new ISourceProviderListener(){

			public void sourceChanged(int sourcePriority, Map sourceValuesByName) {	}
			public void sourceChanged(int sourcePriority, String sourceName,
					Object sourceValue) {
				// eclipse bug: even if we set a very specific source provider, eclipse still
				//	gather event from other source. we then require to put a guard to avoid this.
				if (sourceName.equals(DataService.DATA_UPDATE)) {
					summaryCanvas.redraw();
				}
			}
		});
	}

	public void updateView(SpaceTimeDataController dataTraces)
	{
		//stData.addPositionListener(this);
		summaryCanvas.setVisible(true);
		//traceview.detailCanvas.rebuffer();
	}
	
	public void setFocus() 
	{
		this.summaryCanvas.setFocus();
	}

}
