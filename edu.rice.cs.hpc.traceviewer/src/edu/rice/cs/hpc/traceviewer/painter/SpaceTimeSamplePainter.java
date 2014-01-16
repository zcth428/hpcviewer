package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.ColorTable;

/***************************************************************
 * A painter that actually paints the samples onto an image
 * and also handles over-depth text.
 **************************************************************/

public abstract class SpaceTimeSamplePainter
{	
	/**The color table that will be used to paint this canvas.*/
	protected final ColorTable colorTable;
	
	/**The GC that the painter paints the samples onto.*/
	protected final GC gc;
	
	/*****************************************************************************************************
	 * Creates a SpaceTimeSamplePainter that will draw to the GC _gc, with the master canvas 'canvas' and
	 * scales in the x and y directions as scaleX and scaleY, respectively.
	 ****************************************************************************************************/
	public SpaceTimeSamplePainter(GC _gc, ColorTable _colorTable, double scaleX, double scaleY)
	{
		gc = _gc;
		colorTable = _colorTable;
	}
	
	/**********************************************************************************
	 * Paints a rectangle to the GC gc from startPixel to endPixel with height = height
	 * and color = the color that corresponds to function in the colorTable.
	 *********************************************************************************/
	abstract public void paintSample(int startPixel, int endPixel, int height, Color color);
	
	//We don't need to worry about y-coordinates because the sample is painted to an image of height = height
	//that just corresponds to one line, and these images are then compiled into the master image
	protected void internalPaint(GC gcToDraw, int startPixel, int endPixel, int height, Color color)
	{
		// sets up the rectangle to be filled
		int rectWidth = endPixel - startPixel;
		if(rectWidth == 0)
			return;

		gcToDraw.setBackground(color);
		gcToDraw.fillRectangle(startPixel, 0, rectWidth, height);
	}
	
}
