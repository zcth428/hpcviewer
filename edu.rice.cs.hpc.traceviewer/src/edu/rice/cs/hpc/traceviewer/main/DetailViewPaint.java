package edu.rice.cs.hpc.traceviewer.main;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.traceviewer.actions.OptionRecordsDisplay;
import edu.rice.cs.hpc.traceviewer.data.db.TimelineDataSet;
import edu.rice.cs.hpc.traceviewer.painter.BasePaintThread;
import edu.rice.cs.hpc.traceviewer.painter.BaseViewPaint;
import edu.rice.cs.hpc.traceviewer.painter.ISpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.ImagePosition;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.BaseTimelineThread;

/******************************************************
 * 
 * Painting class for detail view (space-time view)
 *
 ******************************************************/
public class DetailViewPaint extends BaseViewPaint {
		
	/** maximum number of records to display **/
	static public final int MAX_RECORDS_DISPLAY = 99;
	/** text when we reach the maximum of records to display **/
	static public final String TOO_MANY_RECORDS = ">" + String.valueOf(MAX_RECORDS_DISPLAY) ;
	
	final private Point maxTextSize;

	private final GC masterGC;
	private final GC origGC;
	
	final private ProcessTimelineService ptlService;
	final private boolean debug;
	
	public DetailViewPaint(final GC masterGC, final GC origGC, SpaceTimeDataController _data,
			ImageTraceAttributes _attributes, boolean _changeBound,
			IWorkbenchWindow window, ISpaceTimeCanvas canvas, ExecutorService threadExecutor) 
	{
		super("Main trace view", _data, _attributes, _changeBound, window, canvas, threadExecutor);
		this.masterGC = masterGC;
		this.origGC   = origGC;

		ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(
				ISourceProviderService.class);
		ptlService = (ProcessTimelineService) sourceProviderService.
				getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER); 
		
		// check if we need to print the text information on the canvas
		
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		final Command showCount = commandService.getCommand( OptionRecordsDisplay.commandId );
		final State state = showCount.getState(RegistryToggleState.STATE_ID);
		if (state != null) {
			Boolean isDebug = (Boolean) state.getValue();
			debug = isDebug.booleanValue();
		} else {
			debug = false;
		}
		// initialize the size of maximum text
		//	the longest text should be: ">99(>99)"
		maxTextSize = masterGC.textExtent(TOO_MANY_RECORDS + "(" + TOO_MANY_RECORDS + ")");
	}

	@Override
	protected boolean startPainting(int linesToPaint, int numThreads, boolean changedBounds) {

		return true;
	}

	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsV, attributes.getProcessInterval() );
	}

	@Override
	protected BaseTimelineThread getTimelineThread(ISpaceTimeCanvas canvas, double xscale,
			double yscale, Queue<TimelineDataSet> queue, AtomicInteger timelineDone
			, IProgressMonitor monitor) {

		return new TimelineThread(this.window, controller, ptlService, changedBounds,   
				yscale, queue, timelineDone, monitor);
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) throws IOException {
		controller.fillTracesWithData( changedBounds, numThreads);
	}

	@Override
	protected BasePaintThread getPaintThread(
			Queue<TimelineDataSet> queue, int numLines, AtomicInteger timelineDone, Device device, int width) {

		return new DetailPaintThread( controller, queue, numLines, timelineDone, device, width, maxTextSize, debug);
	}

	@Override
	protected void drawPainting(ISpaceTimeCanvas canvas,
			ImagePosition imagePosition) {
		
		DetailImagePosition imgDetailLine = (DetailImagePosition)imagePosition;
		double yscale = Math.max(canvas.getScalePixelsPerRank(), 1);

		int yposition = (int) Math.round(imgDetailLine.position * yscale);
		// put the image onto the canvas
		masterGC.drawImage(imgDetailLine.image, 0, yposition);
		origGC.drawImage(imgDetailLine.imageOriginal, 0, imgDetailLine.position);
		
		imgDetailLine.image.dispose();
		imgDetailLine.imageOriginal.dispose();
	}
}
