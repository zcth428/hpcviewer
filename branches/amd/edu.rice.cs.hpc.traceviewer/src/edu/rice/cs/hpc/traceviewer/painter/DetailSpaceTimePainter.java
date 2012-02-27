package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.ColorTable;
import edu.rice.cs.hpc.traceviewer.util.Constants;

public class DetailSpaceTimePainter extends SpaceTimeSamplePainter {
	
	private final int minimumWidthForText;
	
	private final GC gcFinal;

	/**The y scale of the space time canvas to be used while painting.*/
	private final double canvasScaleY;
	

	public DetailSpaceTimePainter(GC _gcOriginal, GC _gcFinal, ColorTable _colorTable,
			double scaleX, double scaleY) {
		
		super(_gcOriginal, _colorTable, scaleX, scaleY);
		minimumWidthForText = _gcFinal.textExtent("0").x;
		gcFinal = _gcFinal;
		canvasScaleY = scaleY;
	}

	
	public void paintSample(int startPixel, int endPixel, int height, String function)
	{
		super.internalPaint(gc, startPixel, endPixel, height, function);
		super.internalPaint(gcFinal, startPixel, endPixel, height, function);
	}
	
	/**Gets the correct color to paint the over depth text and then paints the text, centered, on the process/time block.*/
	public void paintOverDepthText(int odInitPixel, int odFinalPixel, int depth, String function, boolean overDepth, int sampleCount)
	{	
		int box_width = odFinalPixel - odInitPixel;
		
		if (box_width < minimumWidthForText) return;
		
		String decoration = "(" + sampleCount + ")";
		
		if (overDepth) {
			decoration = String.valueOf(depth) + decoration;
		}
		
		Point textSize = gcFinal.textExtent(decoration);

		// want 2 pixels on either side
		if((box_width - textSize.x) >= 4) {
			int box_height = (int) Math.floor(canvasScaleY);
			// want 2 pixels on above and below
			if ((box_height - textSize.y) >= 4) {
				Color bgColor = colorTable.getColor(function);
				gcFinal.setBackground(bgColor);

				// Pick the color of the text indicating sample depth. 
				// If the background is suffciently light, pick black, otherwise white
				if (bgColor.getRed()+bgColor.getBlue()+bgColor.getGreen()>Constants.DARKEST_COLOR_FOR_BLACK_TEXT)
					gcFinal.setForeground(Constants.COLOR_BLACK);
				else
					gcFinal.setForeground(Constants.COLOR_WHITE);
				gcFinal.drawText(decoration, odInitPixel+((box_width - textSize.x)/2), ((box_height - textSize.y)/2));
			}
		}

	}

}
