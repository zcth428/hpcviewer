package edu.rice.cs.hpc.traceviewer.summary;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import edu.rice.cs.hpc.traceviewer.operation.BufferRefreshOperation;
import edu.rice.cs.hpc.traceviewer.operation.TraceOperation;
import edu.rice.cs.hpc.traceviewer.operation.ZoomOperation;
import edu.rice.cs.hpc.traceviewer.painter.AbstractTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.Frame;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.Position;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.data.util.Constants;
import edu.rice.cs.hpc.traceviewer.data.util.Debugger;

/******************************************************************
 * 
 * Canvas class for summary view
 *
 ******************************************************************/
public class SummaryTimeCanvas extends AbstractTimeCanvas 
implements IOperationHistoryListener
{	
	private SpaceTimeDataController dataTraces = null;
	private TreeMap<Integer, Integer> mapStatistics;
	private int totPixels;
	
	private ToolTip tooltip;
	
	/**********************************
	 * Construct a summary canvas without background nor scrollbar
	 * 
	 * @param composite
	 **********************************/
	public SummaryTimeCanvas(Composite composite)
    {
		super(composite, SWT.NO_BACKGROUND);
		
		addCanvasListener();
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
		
		// ------------------------------------------------------------------------------------------
		// setup tooltip information about the percentage of the color appointed by the mouse
		// ------------------------------------------------------------------------------------------
		tooltip = new SummaryTooltip(this) ;
		tooltip.deactivate();
	}
	
	/***
	 * add listeners to this canvas
	 */
	private void addCanvasListener()
	{
		addDisposeListener( new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				disposeResources();
			}
		});
	}
	
	
	/*****
	 * rebuffers the data in the summary time canvas and then asks receiver to paint it again
	 *****/
	private void rebuffer(ImageData detailData)
	{
		if (detailData == null)
			return;

		// ------------------------------------------------------------------------------------------
		// let use GC instead of ImageData since GC allows us to draw lines and rectangles
		// ------------------------------------------------------------------------------------------
		if (imageBuffer != null) {
			imageBuffer.dispose();
		}
		final int viewWidth = getClientArea().width;
		final int viewHeight = getClientArea().height;

		if (viewWidth == 0 || viewHeight == 0)
			return;

		imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		GC buffer = new GC(imageBuffer);
		buffer.setBackground(Constants.COLOR_WHITE);
		buffer.fillRectangle(0, 0, viewWidth, viewHeight);
		
		float yScale = (float)viewHeight / (float)detailData.height;
		float xScale = ((float)viewWidth / (float)detailData.width);
		int xOffset = 0;

		mapStatistics = new TreeMap<Integer, Integer>();
		
		//---------------------------------------------------------------------------
		// needs to be optimized:
		// for every pixel along the width, check the pixel, group them based on color,
		//   count the amount of each group, and draw the pixel
		//---------------------------------------------------------------------------
		for (int x = 0; x < detailData.width; ++x)
		{
			//---------------------------------------------------------------------------
			// use tree map to sort the key of color map
			// without sort, it can be confusing
			//---------------------------------------------------------------------------
			TreeMap<Integer, Integer> sortedColorMap = new TreeMap<Integer, Integer>();

			for (int y = 0; y < detailData.height; ++y)
			{
				int pixelValue = detailData.getPixel(x,y);
				if (sortedColorMap.containsKey(pixelValue))
					sortedColorMap.put( pixelValue , sortedColorMap.get(pixelValue)+1 );
				else
					sortedColorMap.put( pixelValue , 1);
			}
			Set<Integer> set = sortedColorMap.keySet();
			int yOffset = viewHeight;
			
			//---------------------------------------------------------------------------
			// draw the line of a specific color with a specific length from bottom to the top
			// note: the coordinates 0,0 starts from the top-left corner !
			//---------------------------------------------------------------------------
			for (Iterator<Integer> it = set.iterator(); it.hasNext(); ) 
			{
				final Integer pixel = it.next();
				final RGB rgb = detailData.palette.getRGB(pixel);
				final Color c = new Color(getDisplay(), rgb);
				final Integer numCounts = sortedColorMap.get(pixel);
				final int height = Math.round(numCounts * yScale);
				
				buffer.setBackground(c);
				buffer.fillRectangle(xOffset, yOffset-height, (int) Math.max(1, xScale), height);
				
				yOffset -= height;
				c.dispose();
				
				// accumulate the statistics of this pixel
				Integer val = mapStatistics.get(pixel);
				Integer acc = (val==null?  numCounts : val + numCounts);
				mapStatistics.put(pixel, acc);
			}
			xOffset = Math.round(xOffset + xScale);
		}
		totPixels = detailData.width * detailData.height;

		buffer.dispose();
		
		tooltip.activate();

		redraw();
	}
	
	/****
	 * main method to decide whether we want to create a new buffer or just to
	 * redraw the canvas
	 * 
	 * @param _detailData : new data
	 */
	private void refresh(ImageData detailData)
	{
		super.init();		
		rebuffer(detailData);
	}
	
	/********
	 * set the new database
	 * @param data
	 ********/
	public void updateData(SpaceTimeDataController data)
	{
		dataTraces = data;
		setVisible(true);
	}

	/********
	 * release allocated resources
	 ********/
	private void disposeResources()
	{
		if (imageBuffer != null)
			imageBuffer.dispose();
	}
	
	/*****
	 * get the number of pixel per time unit
	 * @return
	 */
	private double getScalePixelsPerTime()
	{
		final int viewWidth = getClientArea().width;

		return (double)viewWidth / (double)getNumTimeDisplayed();
	}
	
	/******
	 * get the time interval displayed on the canvas
	 * @return
	 */
	private long getNumTimeDisplayed()
	{
		return (dataTraces.getAttributes().getTimeInterval());
	}


	/******************************************************************
	 * 
	 * Customized tooltip for summary canvas
	 *
	 ******************************************************************/
	private class SummaryTooltip extends DefaultToolTip
	{
		public SummaryTooltip(Control control) {
			super(control);
		}

		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.window.DefaultToolTip#getText(org.eclipse.swt.widgets.Event)
		 */
		protected String getText(Event event) {
			if (mapStatistics != null ) 
			{
				// ------------------------------------------------
				// copy the area pointed by the mouse to an image
				// ------------------------------------------------
				final Image image = new Image(event.display, 1, 1);
				GC gc = new GC(SummaryTimeCanvas.this);
				gc.copyArea(image, event.x, event.y);
				final ImageData data = image.getImageData();
				gc.dispose();
				
				// ------------------------------------------------
				// get the pixel of the image 
				// ------------------------------------------------
				int pixel = data.getPixel(0, 0); 
				image.dispose();

				// ------------------------------------------------
				// get the number of counts of this pixel
				// ------------------------------------------------
				Integer stat = mapStatistics.get(pixel);
				
				if (stat != null) {
					// ------------------------------------------------
					// compute the percentage
					// ------------------------------------------------
					float percent = (float)100.0 * ((float)stat / (float) totPixels);
					final String percent_str = String.format("%.2f %%", percent);
					return percent_str;
				}
			}
			return null;
		}
		
		@Override
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.window.ToolTip#getLocation(org.eclipse.swt.graphics.Point, org.eclipse.swt.widgets.Event)
		 */
		public Point getLocation(Point tipSize, Event event) {
			return SummaryTimeCanvas.this.toDisplay(event.x + 5, event.y - 15);
		}
	}
	
	//---------------------------------------------------------------------------------------
	// Override methods
	//---------------------------------------------------------------------------------------


	@Override
	public void historyNotification(final OperationHistoryEvent event) {
		// we are not interested with other operation
		if (event.getOperation().hasContext(BufferRefreshOperation.context)) {
			if (event.getEventType() == OperationHistoryEvent.DONE) {
				Debugger.printDebug(1, "STC attributes: ");
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						BufferRefreshOperation operation = (BufferRefreshOperation) event.getOperation();
						refresh(operation.getImageData());
					}
				});
			}
		}
	}

	@Override
	protected void changePosition(Point point) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void changeRegion(int left, int right) 
	{
		final ImageTraceAttributes attributes = dataTraces.getAttributes();
		
		long timeBegin   = attributes.getTimeBegin();
		long topLeftTime = timeBegin + (long)(left / getScalePixelsPerTime());
		long bottomRightTime = timeBegin + (long)(right / getScalePixelsPerTime());
		
		final Position position = attributes.getPosition();
		
		final Frame frame = new Frame(topLeftTime, bottomRightTime,
				attributes.getProcessBegin(), attributes.getProcessEnd(),
				attributes.getDepth(), position.time, position.process );
		try {
			TraceOperation.getOperationHistory().execute(
					new ZoomOperation("Time zoom in", frame), 
					null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}
}