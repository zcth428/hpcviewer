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

import edu.rice.cs.hpc.traceviewer.events.ITraceDepth;
import edu.rice.cs.hpc.traceviewer.events.ITracePosition;
import edu.rice.cs.hpc.traceviewer.painter.DepthTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.services.DataService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;

public class HPCDepthView extends ViewPart implements ITraceDepth, ITracePosition
{
	public static final String ID = "hpcdepthview.view";
	
	/**The composite that holds everything in the view*/
	Composite master;
	
	/**The max depth of the space time data.*/
	int maxDepth;
	
	/** Paints and displays the detail view. */
	DepthTimeCanvas depthCanvas;
	
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
		 * Depth View Canvas
		 */
		
		depthCanvas = new DepthTimeCanvas(master);
		depthCanvas.setLayout(new GridLayout());
		depthCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		depthCanvas.setVisible(false);		
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
					depthCanvas.refresh();
				}
			}
		});
	}

	public void updateView(SpaceTimeData _stData)
	{
		this.depthCanvas.updateView(_stData);
		_stData.addDepthListener(this);
		_stData.addPositionListener(this);
		depthCanvas.setVisible(true);
	}

	public void setFocus()
	{
		this.depthCanvas.setFocus();
	}

	public void setDepth(int new_depth) {
		this.depthCanvas.setDepth(new_depth);
	}

	public void setPosition(Position position) {
		this.depthCanvas.setPosition(position);
	}
}
