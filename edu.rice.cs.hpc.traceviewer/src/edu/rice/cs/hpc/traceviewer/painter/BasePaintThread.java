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
	final private int numberOfTotalLines;
	
	final protected SpaceTimeDataController stData;
	final private AtomicInteger timelineDone;
	
	/****
	 * constructor of the class, requiring a queue of list of data (per line) to be
	 * visualized on a set of images. The queue can be thread-safe (in case of multithreaded)
	 * or unsafe (case of single threaded). 
	 * <p>
	 * To retrieve the list of images, the caller needs to call the method get() from
	 * {@link java.util.concurrent.Callable} 
	 * 
	 * @param list : the queue of TimelineDataSet data. Use a thread-safe queue for multi-threads
	 * @param numberOfTotalLines : number of total images or lines
	 * @param device : the display device used to create images. Cannot be null
	 * @param width : the width of the view
	 */
	public BasePaintThread( SpaceTimeDataController stData, Queue<TimelineDataSet> list, 
			int numberOfTotalLines, AtomicInteger paintDone,
			Device device, int width) {
		
		Assert.isNotNull(device);
		Assert.isNotNull(list);
		
		this.list = list;
		this.numberOfTotalLines = numberOfTotalLines;
		this.timelineDone = paintDone;
		
		this.device = device;
		this.stData = stData;
		
		this.width = width;
		listOfImages = new ArrayList<ImagePosition>(list.size());
	}
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public List<ImagePosition> call() throws Exception {

		while( ! list.isEmpty() 				 		      // while there are tasks to do 
				||								 		      // or  
				numberOfTotalLines>getNumberOfCreatedData()   // the data collection threads have not finished 
				|| 											  // or	the paint threads haven't finished the job
				timelineDone.get()>0 ) 					  	  //
		{
			// ------------------------------------------------------------------
			// get the task to do from the list and compute the height and the position
			// if the list is empty, it means the data collection threads haven't finished 
			//	their work yet. It's better to wait and sleep a bit 
			// ------------------------------------------------------------------

			TimelineDataSet setDataToPaint = list.poll();
			if (setDataToPaint == null) {
				Thread.sleep(40);
				continue;
			}
			final int height = setDataToPaint.getHeight();
			final int position = setDataToPaint.getLineNumber();
			
			// ------------------------------------------------------------------
			// initialize the painting, the derived class has to create image ready
			// ------------------------------------------------------------------
			initPaint(device, width, height);

			// ------------------------------------------------------------------
			// a line can contains many trace data (one trace data equals one rectangle)
			// we just assume here that each trace data is different and each 
			//	has different color
			// ------------------------------------------------------------------
			for(BaseDataVisualization data : setDataToPaint.getList()) 
			{
				// ------------------------------------------------------------------
				// paint the image
				// ------------------------------------------------------------------
				paint(position, data, height);
			}
			// ------------------------------------------------------------------
			// finalize phase
			// ------------------------------------------------------------------
			final ImagePosition imgPos = finalizePaint(position);
			
			listOfImages.add(imgPos);
		}

		return listOfImages;
	}
	
	/*****
	 * Abstract method to initialize the paint. 
	 * The derived class can use this method to create images and GC before painting it
	 * 
	 * @param device : device to create the image
	 * @param width : the width of the image
	 * @param height : the height of the image
	 */
	abstract protected void initPaint(Device device, int width, int height);
	
	/*****
	 * the actual method to paint a trace image
	 * 
	 * @param position : the rank or the position line number of the image
	 * @param data : the data to be painted
	 * @param height : the height of the image
	 */
	abstract protected void paint(int position, BaseDataVisualization data, int height);
	
	/********
	 * Finalizing the image. 
	 * 
	 * @param linenum : the position of the line number of the image
	 * @return
	 */
	abstract protected ImagePosition finalizePaint(int linenum);
	
	abstract protected int getNumberOfCreatedData();
	
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
