package edu.rice.cs.hpc.traceviewer.painter;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.ISourceProviderService;

import edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
import edu.rice.cs.hpc.traceviewer.timeline.TimelineThread;

/******************************************************
 * 
 * Painting class for detail view (space-time view)
 *
 ******************************************************/
public class DetailViewPaint extends BaseViewPaint {
	
	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeFinalLines;
	
	/**The composite images created by painting all of the samples in a given line to it.*/
	private Image[] compositeOrigLines;

	private final GC masterGC;
	private final GC origGC;
	
	final private ProcessTimelineService ptlService;
	
	public DetailViewPaint(final GC masterGC, final GC origGC, SpaceTimeDataController _data,
			ImageTraceAttributes _attributes, boolean _changeBound,
			IWorkbenchWindow window, ExecutorService threadExecutor) 
	{
		super(_data, _attributes, _changeBound, window, threadExecutor);
		this.masterGC = masterGC;
		this.origGC   = origGC;

		ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(
				ISourceProviderService.class);
		ptlService = (ProcessTimelineService) sourceProviderService.
				getSourceProvider(ProcessTimelineService.PROCESS_TIMELINE_PROVIDER); 
	}

	@Override
	protected boolean startPainting(int linesToPaint, boolean changedBounds) {
		compositeOrigLines = new Image[linesToPaint];
		compositeFinalLines = new Image[linesToPaint];

		return true;
	}

	@Override
	protected void endPainting(int linesToPaint, double xscale, double yscale) {
		for (int i = 0; i < linesToPaint; i++) {
			int yposition = (int) Math.round(i * yscale);
			if (compositeOrigLines[i] != null) {
				origGC.drawImage(compositeOrigLines[i], 0, yposition);
				masterGC.drawImage(compositeFinalLines[i], 0, yposition);
			}
		}
		
		// disposing resources
		for (int i=0; i<linesToPaint; i++) {
			if (compositeOrigLines[i] != null)
				compositeOrigLines[i].dispose();
			
			if (compositeFinalLines[i] != null)
				compositeFinalLines[i].dispose();
		}
	}

	@Override
	protected int getNumberOfLines() {
		return Math.min(attributes.numPixelsV, attributes.getProcessInterval() );
	}

	@Override
	protected Callable<Integer> getTimelineThread(SpaceTimeCanvas canvas, double xscale,
			double yscale) {

		return new TimelineThread(this.window, controller, ptlService, changedBounds, canvas, compositeOrigLines,
				compositeFinalLines, attributes.numPixelsH, xscale, 
				yscale, monitor);
	}

	@Override
	protected void launchDataGettingThreads(boolean changedBounds,
			int numThreads) throws IOException {
		controller.fillTracesWithData( changedBounds, numThreads);
	}	
}
