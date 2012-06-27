package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;

import edu.rice.cs.hpc.common.util.ProcedureAliasMap;
import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentWithoutMetrics;
import edu.rice.cs.hpc.data.experiment.InvalExperimentException;
import edu.rice.cs.hpc.data.experiment.extdata.BaseDataFile;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.traceviewer.db.RemoteDataRetriever;
import edu.rice.cs.hpc.traceviewer.painter.ImageTraceAttributes;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeCanvas;
import edu.rice.cs.hpc.traceviewer.painter.SpaceTimeSamplePainter;
import edu.rice.cs.hpc.traceviewer.timeline.ProcessTimeline;

public class SpaceTimeDataControllerRemote extends SpaceTimeDataController {

	// Set dataRetreiver before using any methods besides the constructor!
	RemoteDataRetriever dataRetriever;
	HashMap<Integer, CallPath> scopeMap;
	public final int HEADER_SIZE;
	private final static byte MIN_HEIGHT_FOR_SEPARATOR_LINES = 15;

	public SpaceTimeDataControllerRemote(IWorkbenchWindow _window,
			IStatusLineManager _statusMgr, File expFile) {

		MethodCounts[0]++;

		// statusMgr = _statusMgr;

		attributes = new ImageTraceAttributes();
		ImageTraceAttributes oldAtributes = new ImageTraceAttributes();

		BaseExperiment exp = new ExperimentWithoutMetrics();
		try {
			exp.open(expFile, new ProcedureAliasMap());
		} catch (InvalExperimentException e) {
			System.out.println("Parse error in Experiment XML at line "
					+ e.getLineNumber());
			e.printStackTrace();
			// return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Height = dataTrace.getNumberOfFiles();

		scopeMap = new HashMap<Integer, CallPath>();
		TraceDataVisitor visitor = new TraceDataVisitor(scopeMap);

		// This probably isn't the best way. It seems like ColorTable should be
		// created and initialized by the PaintManager, however initializing the
		// ColorTable requires the experiment file, which the PaintManager
		// should not have because it might be done differently when the data is
		// fetched remotely. Additionally the STDController needs MaxDepth along
		// with PaintManager.
		ColorTable colorTable = new ColorTable(_window.getShell().getDisplay());
		// Initializes the CSS that represents time values outside of the
		// time-line.
		colorTable.addProcedure(CallPath.NULL_FUNCTION);
		int maxDepth = exp.getRootScope().dfsSetup(visitor, colorTable, 1);

		TraceAttribute attribute = exp.getTraceAttribute();
		minBegTime = attribute.dbTimeMin;
		maxEndTime = attribute.dbTimeMax;
		HEADER_SIZE = attribute.dbHeaderSize;

		dbName = exp.getName();

		super.painter = new PaintManager(attributes, oldAtributes, _window,
				_statusMgr, colorTable, maxDepth, minBegTime, this);

	}

	public void setDataRetriever(RemoteDataRetriever _dr) {
		dataRetriever = _dr;
		Height = _dr.Height;
	}

	@Override
	public void setCurrentlySelectedProccess(int ProcessNumber) {
		System.out.println("Calling the unimplemented set selected proc");

	}

	@Override
	public void prepareViewportPainting(boolean changedBounds) {
		System.out.println("Calling the unimplemented prep Viewp Painting");

	}

	@Override
	void prepareDepthViewportPainting() {
		// TODO Auto-generated method stub
		System.out.println("Calling the unimplemented prepDVPainting");
	}

	@Override
	public String[] getTraceDataValuesX() {
		// TEMPORARY FIX!! What do we want to do about this???
		int size = 1037;// Hardcoded value for the sample, that's why this needs
						// to be fixed!
		String[] names = new String[size];
		for (int i = 0; i < names.length; i++) {
			names[i] = String.valueOf(i) + ".0";// Not actually what it even
												// should be because some are
												// skipped, but this is already
												// a bad fix.
		}
		return names;
	}

	@Override
	public void fillTraces(SpaceTimeCanvas canvas, int linesToPaint,
			double xscale, double yscale, boolean changedBounds) {
		if (changedBounds) {
			try {
				traces = dataRetriever.getData(attributes.begProcess,
						attributes.endProcess, minBegTime, maxEndTime,
						attributes.numPixelsV, attributes.numPixelsH, scopeMap);
			} catch (IOException e) {
				// UI Notify user...
				e.printStackTrace();
			}
		}
		renderTraces(canvas, changedBounds, xscale, yscale);// For some reason, when
													// changedBounds is false,
													// everything appears to be
													// zero-length, so all we
													// get is a white screen

	}

	private void renderTraces(SpaceTimeCanvas canvas, boolean changedBounds,
			double scaleX, double scaleY) {
		int width = attributes.numPixelsH;
		//The height of each individual Image, not the full vertical height of the window
		// From ProcessTimeline

		for (int i = 0; i < traces.length; i++) {
			boolean debug = (i % 100 == 0);
			if (debug)
				System.out.println("Rendering: " + i + "/" + traces.length);

			ProcessTimeline nextTrace = traces[i];
			// Again why does traces have null entries??? Grr...
			int imageHeight = (int)(Math.round(scaleY*(nextTrace.line()+1)) - Math.round(scaleY*nextTrace.line()));//The height of each individual Image, not the full vertical height of the window
			if (scaleY > MIN_HEIGHT_FOR_SEPARATOR_LINES)
				imageHeight--;
			else
				imageHeight++;

			Image lineFinal = new Image(canvas.getDisplay(), width, imageHeight);//ImageHeight or 1??
			Image lineOriginal = new Image(canvas.getDisplay(), width,
					imageHeight);
			GC gcFinal = new GC(lineFinal);
			GC gcOriginal = new GC(lineOriginal);

			if (debug)
				System.out.println("About to create spp");
			SpaceTimeSamplePainter spp = this.getPainter()
					.CreateDetailSpaceTimePainter(gcOriginal, gcFinal, scaleX,
							scaleY);
			if (debug)
				System.out.println("spp created, about to paint");
			this.getPainter().paintDetailLine(spp, nextTrace.line(),
					imageHeight, changedBounds);
			if (debug)
				System.out.println("Painted, about to dispose");
			gcFinal.dispose();
			gcOriginal.dispose();
			if (debug)
				System.out.println("Disposed, adding");
			this.getPainter().addNextImage(lineOriginal, lineFinal,
					nextTrace.line());
			if (debug)
				System.out.println("Done");

		}
	}

}
