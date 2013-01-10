package edu.rice.cs.hpc.traceviewer.painter;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.rice.cs.hpc.traceviewer.operation.BufferRefreshOperation;
import edu.rice.cs.hpc.traceviewer.util.Constants;

public class SummaryTimeCanvas extends Canvas implements PaintListener, IOperationHistoryListener
{
	
	private ImageData detailData;
	private Image imageBuffer;
	
	public SummaryTimeCanvas(Composite composite)
    {
		super(composite, SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL);
		
		detailData = null;
		this.getVerticalBar().setVisible(false);
		this.getHorizontalBar().setVisible(false);
		
		this.addCanvasListener();
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(this);
	}
	
	public void addCanvasListener()
	{
		addPaintListener(this);
		
		addListener(SWT.Resize, new Listener(){
			public void handleEvent(Event event)
			{
				final int viewWidth = getClientArea().width;
				final int viewHeight = getClientArea().height;
				
				if (viewWidth > 0 && viewHeight > 0)
				{
					getDisplay().asyncExec(new ResizeThread( new SummaryBufferPaint()));
				}
			}
		});
	}
	
	public void paintControl(PaintEvent event)
	{
		if (detailData == null || imageBuffer == null)
			return;
		
		final int viewWidth = getClientArea().width;
		final int viewHeight = getClientArea().height;
		
		try
		{
			event.gc.drawImage(imageBuffer, 0, 0, viewWidth, viewHeight, 0, 0, viewWidth, viewHeight);
		} 
		catch (Exception e)
		{
			// An exception "Illegal argument" will be raised if the resize method is not "fast" enough to create the image
			//		buffer before the painting is called. Thus, it causes inconsistency between the size of the image buffer
			//		and the size of client area. 
			//		If this happens, either we wait for the creation of image buffer, or do nothing. 
			//		I prefer to do nothing for the scalability reason.
			return;
		}
	}


	public void setPosition(Position position)
	{
	}
	
	/*rebuffers the data in the summary time canvas and then asks receiver to paint it again*/
	public void rebuffer()
	{
		if (detailData == null)
			return;
		int viewWidth = getClientArea().width;
		int viewHeight = getClientArea().height;
		if (viewWidth==0 && viewHeight==0)
		{
			viewWidth = 10;
			viewHeight = 10;
		}

		// ------------------------------------------------------------------------------------------
		// scale the original "detail" image according to the size of summary view. 
		// we will use this "scaled" image to scan all pixels and compute the statistics.
		// NOTE: if the original image is much larger than the summary view (which is most of the case)
		//  we can gain the speed. 
		// ------------------------------------------------------------------------------------------
		ImageData scaledImage = detailData.scaledTo(viewWidth, viewHeight);
		
		final int PIXEL_LIGHT = detailData.palette.getPixel(Constants.COLOR_WHITE.getRGB());
		final int PIXEL_DARK = detailData.palette.getPixel(Constants.COLOR_BLACK.getRGB());

		// ------------------------------------------------------------------------------------------
		// let use GC instead of ImageData since GC allows us to draw lines and rectangles
		// ------------------------------------------------------------------------------------------
		if (imageBuffer != null) {
			imageBuffer.dispose();
		}
		imageBuffer = new Image(getDisplay(), viewWidth, viewHeight);
		GC buffer = new GC(imageBuffer);
		buffer.setBackground(Constants.COLOR_WHITE);
		buffer.fillRectangle(0, 0, viewWidth, viewHeight);

		//---------------------------------------------------------------------------
		// needs to be optimized:
		// for every pixel along the width, check the pixel, group them based on color,
		//   count the amount of each group, and draw the pixel
		//---------------------------------------------------------------------------
		for (int x = 0; x < scaledImage.width; ++x)
		{
			//---------------------------------------------------------------------------
			// use tree map to sort the key of color map
			// without sort, it can be confusing
			//---------------------------------------------------------------------------
			TreeMap<Integer, Integer> sortedColorMap = new TreeMap<Integer, Integer>();
			for (int y = 0; y < scaledImage.height; ++y)
			{
				int pixelValue = scaledImage.getPixel(x,y);
				if (pixelValue != PIXEL_LIGHT && pixelValue != PIXEL_DARK)
				{
					if (sortedColorMap.containsKey(pixelValue))
						sortedColorMap.put( pixelValue , sortedColorMap.get(pixelValue)+1 );
					else
						sortedColorMap.put( pixelValue , 1);				
				}
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
				final RGB rgb = scaledImage.palette.getRGB(pixel);
				final Color c = new Color(getDisplay(), rgb);
				final int height = (int)(sortedColorMap.get(pixel));
				
				buffer.setForeground(c);
				buffer.drawLine(x, yOffset, x, yOffset-height);
				
				yOffset -= height;
			}
		}
		buffer.dispose();
		
		redraw();
	}
	
	private void refresh(ImageData _detailData)
	{
		//if we are already printing out the correct thing, then just redraw - else, perform necessary calculations
		if (_detailData.equals(detailData))
		{
			redraw();
		}
		else
		{
			detailData = _detailData;
			rebuffer();
			redraw();
		}
	}
	
	//---------------------------------------------------------------------------------------
	// PRIVATE CLASS
	//---------------------------------------------------------------------------------------

	private class SummaryBufferPaint implements BufferPaint
	{
		public void rebuffering()
		{
			rebuffer();
		}
	}

	@Override
	public void historyNotification(final OperationHistoryEvent event) {
		// we are not interested with other operation
		if (event.getOperation().hasContext(BufferRefreshOperation.context)) {
			if (event.getEventType() == OperationHistoryEvent.DONE) {
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
}