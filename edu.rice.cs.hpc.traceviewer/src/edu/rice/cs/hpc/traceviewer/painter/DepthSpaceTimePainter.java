package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import edu.rice.cs.hpc.traceviewer.data.graph.ColorTable;

/*******************************************************
 * 
 * Painter class for depth view
 *
 *******************************************************/
public class DepthSpaceTimePainter extends SpaceTimeSamplePainter {

	public DepthSpaceTimePainter(GC _gc, ColorTable _colorTable, double scaleX,
			double scaleY) {

		super(_gc, _colorTable, scaleX, scaleY);
	}

	@Override
	public void paintSample(int startPixel, int endPixel, int height,
			Color color) {

		internalPaint(gc, startPixel, endPixel, height, color);
	}

}
