package edu.rice.cs.hpc.traceviewer.timeline;

/**
 * A thread that renders ProcessTimelines by calling the method in the paint
 * manager. TimelineThread already performs this work, but also gets the data.
 * The RenderThread class is only used in remote mode, since the reading of the
 * data from the stream must be single threaded.
 * 
 * @author Philip Taffet
 * 
 */
//public class RenderThread extends Thread {
	/**
	 * Creates a new RenderThread which will render the specified region
	 * 
	 * @param _min
	 *            The inclusive minimum index of {@link timelines} that will be
	 *            rendered
	 * @param _max
	 *            The exclusive maximum index of {@link timelines} that will be
	 *            rendered
	 * @param timelines
	 *            The array of <code>ProcessTimelines</code> that contains the
	 *            ones that this thread will render
	 */
	
/*final int min, max;
	final ProcessTimeline[] timelines;
	final PaintManager painter;
	final Canvas canvas;
	final boolean changedBounds;
	final double scaleX, scaleY;
	final int width;
final TimelineProgressMonitor monitor;
	public RenderThread(int _min, int _max, ProcessTimeline[] _timelines,
			PaintManager _painter, Canvas _canvas, boolean _changedBounds,
			double _scaleX, double _scaleY, int _width, TimelineProgressMonitor _monitor) {
		min = _min;
		max = _max;
		timelines = _timelines;
		painter = _painter;
		canvas = _canvas;
		changedBounds = _changedBounds;
		scaleX = _scaleX;
		scaleY = _scaleY;
		width = _width;
monitor = _monitor;
	}

	@Override
	public void run() {
		for (int i = min; i < max; i++) {
			painter.renderTrace(canvas, changedBounds, scaleX, scaleY, width,
					timelines[i]);
			monitor.announceProgress();
		}
		super.run();
	}
}*/
