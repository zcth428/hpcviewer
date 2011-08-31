package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.ColorTable;
import edu.rice.cs.hpc.traceviewer.util.Constants;

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
	
	private final int minimumWidthForText;
	
	/*****************************************************************************************************
	 * Creates a SpaceTimeSamplePainter that will draw to the GC _gc, with the master canvas 'canvas' and
	 * scales in the x and y directions as scaleX and scaleY, respectively.
	 ****************************************************************************************************/
	public SpaceTimeSamplePainter(GC _gc, ColorTable _colorTable, double scaleX, double scaleY)
	{
		gc = _gc;
		canvasScaleY = scaleY;
		colorTable = _colorTable;
		minimumWidthForText = gc.textExtent("0").x;
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
		int box_width = odFinalPixel - odInitPixel;
		
		if (box_width < minimumWidthForText) return;
		
		String overDepthText = String.valueOf(depth);
		Point textSize = gc.textExtent(overDepthText);

		// want 2 pixels on either side
		if((box_width - textSize.x) >= 4) {
			int box_height = (int) Math.floor(canvasScaleY);
			// want 2 pixels on above and below
			if ((box_height - textSize.y) >= 4) {
				Color bgColor = colorTable.getColor(function);
				gc.setBackground(bgColor);

				// Pick the color of the text indicating sample depth. 
				// If the background is suffciently light, pick black, otherwise white
				if (bgColor.getRed()+bgColor.getBlue()+bgColor.getGreen()>Constants.DARKEST_COLOR_FOR_BLACK_TEXT)
					gc.setForeground(Constants.COLOR_BLACK);
				else
					gc.setForeground(Constants.COLOR_WHITE);
				gc.drawText(overDepthText, odInitPixel+((box_width - textSize.x)/2), ((box_height - textSize.y)/2));
			}
		}

	}
}
