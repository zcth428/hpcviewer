package edu.rice.cs.hpc.traceviewer.timeline;

import java.util.Queue;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import edu.rice.cs.hpc.traceviewer.data.db.BaseDataVisualization;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintThread;
import edu.rice.cs.hpc.traceviewer.painter.ImagePosition;

public class DepthPaintThread extends BasePaintThread {

	private Image image;
	private GC gc;

	public DepthPaintThread(Queue<TimelineDataSet> list, Device device,
			int width) {

		super(list, device, width);
	}

	@Override
	protected void initPaint(Device device, int width, int height) {

		image = new Image(device, width, height);
		gc    = new GC(image);
	}

	@Override
	protected void paint(int position, BaseDataVisualization data, int height) {
		if (position <= data.depth)
			paint(gc, data.x_start, data.x_end, height, data.color);
	}

	@Override
	protected ImagePosition paintFinalize(int linenum) {

		gc.dispose();
		return new ImagePosition(linenum, image);
	}

}
