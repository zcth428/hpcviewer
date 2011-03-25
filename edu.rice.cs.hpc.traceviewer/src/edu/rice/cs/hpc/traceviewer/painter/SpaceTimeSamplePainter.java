package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.ColorTable;

/***************************************************************
 * A painter that actually paints the samples onto an image
 * and also handles over-depth text.
 **************************************************************/

public class SpaceTimeSamplePainter
{	
	/**The y scale of the space time canvas to be used while painting.*/
	private final double canvasScaleY;
	
	/**The color table that will be used to paint this canvas.*/
	private final ColorTable colorTable;
	
	/**The GC that the painter paints the samples onto.*/
	private final GC gc;
	
	/**The minimum height the samples need to be in order to paint the white separator lines.*/
	public final static byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;
	
	/**The darkest color for black over depth text (switch to white if the sum of the 
	 * R, G, and B components is less than this number).*/
	public final static short DARKEST_COLOR = 384;
	
	/*****************************************************************************************************
	 * Creates a SpaceTimeSamplePainter that will draw to the GC _gc, with the master canvas 'canvas' and
	 * scales in the x and y directions as scaleX and scaleY, respectively.
	 ****************************************************************************************************/
	public SpaceTimeSamplePainter(GC _gc, ColorTable _colorTable, double scaleX, double scaleY)
	{
		gc = _gc;
		canvasScaleY = scaleY;
		colorTable = _colorTable;
	}
	
	/**********************************************************************************
	 * Paints a rectangle to the GC gc from startPixel to endPixel with height = height
	 * and color = the color that corresponds to function in the colorTable.
	 *********************************************************************************/
	//We don't need to worry about y-coordinates because the sample is painted to an image of height = height
	//that just corresponds to one line, and these images are then compiled into the master image
	public void paintSample(int startPixel, int endPixel, int height, String function)
	{
		// sets up the rectangle to be filled
		int rectWidth = endPixel - startPixel;
		if(rectWidth == 0)
			return;

		gc.setBackground(colorTable.getColor(function));
		gc.fillRectangle(startPixel, 0, rectWidth, height);
	}
	
	/**Gets the correct color to paint the over depth text and then paints the text, centered, on the process/time block.*/
	public void paintOverDepthText(int odInitPixel, int odFinalPixel, int depth, String function)
	{
		Color bgColor = colorTable.getColor(function);
		gc.setBackground(bgColor);
		
		//Sets the color that the over depth text should be
		if (bgColor.getRed()+bgColor.getBlue()+bgColor.getGreen()>DARKEST_COLOR)
			gc.setForeground(SpaceTimeDetailCanvas.black);
		else
			gc.setForeground(SpaceTimeDetailCanvas.white);
		
		String overDepthText = String.valueOf(depth);
		Point textSize = gc.textExtent(overDepthText);
		Point rectangleSize = new Point(odFinalPixel - odInitPixel,(int)Math.floor(canvasScaleY));
		if((rectangleSize.x - textSize.x) > 1 && (rectangleSize.y - textSize.y) > 1)
			gc.drawText(overDepthText, odInitPixel+((rectangleSize.x - textSize.x)/2), ((rectangleSize.y - textSize.y)/2));
	}
}
