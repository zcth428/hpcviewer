package edu.rice.cs.hpc.traceviewer.painter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import edu.rice.cs.hpc.traceviewer.util.Constants;

public class SummaryTimeCanvas extends Canvas implements PaintListener
{
	
	/**image data that describes current image in detail canvas*/
	private ImageData detailData;
	
	private Image imageBuffer;
	
	public SummaryTimeCanvas(Composite composite, SpaceTimeDetailCanvas _detailCanvas)
    {
		super(composite, SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL);
		
		detailData = null;
		this.getVerticalBar().setVisible(false);
		this.getHorizontalBar().setVisible(false);
		
		this.addCanvasListener();
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
	
	public void updateData(ImageData _detailData)
	{
		detailData = _detailData;
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

		//sets up newImage based on how detailCanvas currently looks
		ImageData newImage = detailData.scaledTo(viewWidth, viewHeight);
		final int PIXEL_WHITE = detailData.palette.getPixel(Constants.COLOR_WHITE.getRGB());
		final int PIXEL_BLACK = detailData.palette.getPixel(Constants.COLOR_BLACK.getRGB());
		
		//---------------------------------------------------------------------------
		// needs to be optimized:
		// for every pixel along the width, check the pixel, group them based on color,
		//   count the amount of each group, and draw the pixel
		//---------------------------------------------------------------------------
		for (int x = 0; x < detailData.width; ++x)
		{
			HashMap<Integer, Integer> colorMap = new HashMap<Integer, Integer>();
			int nonWhite = 0;
			for (int y = 0; y < detailData.height; ++y)
			{
				int pixelValue = detailData.getPixel(x,y);
				if (pixelValue != PIXEL_WHITE && pixelValue != PIXEL_BLACK)
				{
					nonWhite++;
					if (colorMap.containsKey(pixelValue))
						colorMap.put( pixelValue , colorMap.get(pixelValue)+1 );
					else
						colorMap.put( pixelValue , 1);
				}
			}
			
			//---------------------------------------------------------------------------
			// use tree map to sort the key of color map
			// without sort, it can be confusing
			//---------------------------------------------------------------------------
			Map<Integer, Integer> sortedColorMap = new TreeMap<Integer, Integer>(colorMap);
			Set<Integer> set = sortedColorMap.keySet();
			int yOffset = 0;
			
			//---------------------------------------------------------------------------
			// needs to be optimized:
			// for every color draw the pixels according to its length
			//---------------------------------------------------------------------------
			for (Iterator<Integer> it = set.iterator(); it.hasNext(); ) {
				Integer color = it.next();
				int height = (int)(colorMap.get(color)/((double)nonWhite)*viewHeight);
				for (int y = 0; y < height; ++y)
				{
					newImage.setPixel(x, yOffset+y, color);
				}
				yOffset+=height;
			}
		}
		imageBuffer = new Image(getDisplay(), newImage);
		
		redraw();
	}
	
	public void refresh(ImageData _detailData)
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
}