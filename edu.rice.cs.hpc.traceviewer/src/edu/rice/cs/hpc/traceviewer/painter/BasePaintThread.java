package edu.rice.cs.hpc.traceviewer.painter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Device;
import edu.rice.cs.hpc.traceviewer.data.db.BaseDataVisualization;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.painter.ImagePosition;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;

/*****************************************************************
 *
 * Abstract base class thread to paint a canvas that can be either 
 * 	detail canvas or depth canvas.
 * 
 * The class has a future variable List<ImagePosition> which is
 *
 *****************************************************************/
public abstract class BasePaintThread implements Callable<List<ImagePosition>> {

	final protected Device device;
	final protected int width;

	final private Queue<TimelineDataSet> list;
	final private List<ImagePosition> listOfImages;
	final private AtomicInteger counter;
	
	final private SpaceTimeDataController stData;
	
	/****
	 * constructor of the class, requiring a queue of list of data (per line) to be
	 * visualized on a set of images. The queue can be thread-safe (in case of multithreaded)
	 * or unsafe (case of single threaded) 
	 * 
	 * The class will return a list of images.
	 * 
	 * @param list : the queue of TimelineDataSet data. Use a thread-safe queue for multi-threads
	 * @param device : the display device used to create images. Cannot be null
	 * @param width : the width of the view
	 */
	public BasePaintThread( SpaceTimeDataController stData, Queue<TimelineDataSet> list, AtomicInteger counter, 
			Device device, int width) {
		
		Assert.isNotNull(device);
		Assert.isNotNull(list);
		
		this.list = list;
		this.counter = counter;
		this.device = device;
		this.stData = stData;
		
		this.width = width;
		listOfImages = new ArrayList<ImagePosition>(list.size());
	}
	
	@Override
	public List<ImagePosition> call() throws Exception {
		
		while( ! list.isEmpty() ||  counter.get()>stData.getNumberOfLines() ) 
		{
			TimelineDataSet setDataToPaint = list.poll();
			if (setDataToPaint == null) {
				Thread.sleep(40);
				continue;
			}
			final int height = setDataToPaint.getHeight();
			final int position = setDataToPaint.getLineNumber();
			
			initPaint(device, width, height);

			for(BaseDataVisualization data : setDataToPaint.getList()) 
			{
				paint(position, data, height);
			}
			
			final ImagePosition imgPos = paintFinalize(position);
			listOfImages.add(imgPos);
		}
		return listOfImages;
	}
	
	abstract protected void initPaint(Device device, int width, int height); 
	abstract protected void paint(int position, BaseDataVisualization data, int height);
	abstract protected ImagePosition paintFinalize(int linenum);
	
	/***
	 * basic method to paint on a gc
	 * 
	 * @param gc
	 * @param p_start
	 * @param p_end
	 * @param height
	 * @param color
	 */
	protected void paint(GC gc, int p_start, int p_end, int height, Color color) {
		
		int width = p_end - p_start;
		if (width <= 0)
			return;
		
		gc.setBackground(color);
		gc.fillRectangle(p_start, 0, width, height);
	}
}
