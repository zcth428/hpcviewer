package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;

import edu.rice.cs.hpc.traceviewer.actions.OptionRecordsDisplay;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.ColorTable;
import edu.rice.cs.hpc.traceviewer.util.Constants;

/*******************
 * 
 * Class to paint space-time canvas
 * 
 * This class will paint not only the rectangle, but also the over-depth text
 * 	and the number of trace records (if the menu is checked)
 *
 */
public class DetailSpaceTimePainter extends SpaceTimeSamplePainter {
	
	private final int minimumWidthForText;
	
	private final GC gcFinal;

	/**The y scale of the space time canvas to be used while painting.*/
	private final double canvasScaleY;
	
	private final Command showCount;
	
	private final boolean needToShowRecords;

	/** maximum number of records to display **/
	static private final int MAX_RECORDS_DISPLAY = 99;
	
	/** text when we reach the maximum of records to display **/
	static private final String TOO_MANY_RECORDS = ">" + String.valueOf(MAX_RECORDS_DISPLAY) ;
	
	final private Point MAX_TEXT_SIZE;
	
	/***
	 * create a painter for space time canvas
	 * 
	 * @param _gcOriginal
	 * @param _gcFinal
	 * @param _colorTable
	 * @param scaleX
	 * @param scaleY
	 */
	public DetailSpaceTimePainter(IWorkbenchWindow winObj, GC _gcOriginal, GC _gcFinal, ColorTable _colorTable,
			double scaleX, double scaleY) {
		
		super(_gcOriginal, _colorTable, scaleX, scaleY);
		minimumWidthForText = _gcFinal.textExtent("0").x;
		gcFinal = _gcFinal;
		canvasScaleY = scaleY;
		
		ICommandService commandService = (ICommandService) winObj.getService(ICommandService.class);
		showCount = commandService.getCommand( OptionRecordsDisplay.commandId );
		
		// initialize if we need to show the number of trace records or not
		needToShowRecords = needToShowRecords();
		
		// initialize the size of maximum text
		//	the longest text should be: ">99(>99)"
		MAX_TEXT_SIZE = gcFinal.textExtent(TOO_MANY_RECORDS + "(" + TOO_MANY_RECORDS + ")");
	}

	
	public void paintSample(int startPixel, int endPixel, int height, String function)
	{
		super.internalPaint(gc, startPixel, endPixel, height, function);
		super.internalPaint(gcFinal, startPixel, endPixel, height, function);
	}
	
	/**Gets the correct color to paint the over depth text and then paints the text, centered, on the process/time block.*/
	public void paintOverDepthText(int odInitPixel, int odFinalPixel, int depth, String function, boolean overDepth, int sampleCount)
	{	
		if (!overDepth && !needToShowRecords)
			return;
		
		final int box_width = odFinalPixel - odInitPixel;
		
		if (box_width < minimumWidthForText)
			return;
		
		String decoration = "";
		
		if (overDepth) {
			decoration = String.valueOf(depth);
		}
		
		if (needToShowRecords) {
			String count = String.valueOf(sampleCount);
			if (sampleCount>MAX_RECORDS_DISPLAY)
				count = TOO_MANY_RECORDS;
			decoration +=  "(" + count + ")";
		}

		// want 2 pixels on either side
		if((box_width - MAX_TEXT_SIZE.x) >= 4) {
			int box_height = (int) Math.floor(canvasScaleY);
			// want 2 pixels on above and below
			if ((box_height - MAX_TEXT_SIZE.y) >= 4) {
				Color bgColor = colorTable.getColor(function);
				gcFinal.setBackground(bgColor);

				// Pick the color of the text indicating sample depth. 
				// If the background is suffciently light, pick black, otherwise white
				if (bgColor.getRed()+bgColor.getBlue()+bgColor.getGreen()>Constants.DARKEST_COLOR_FOR_BLACK_TEXT)
					gcFinal.setForeground(Constants.COLOR_BLACK);
				else
					gcFinal.setForeground(Constants.COLOR_WHITE);
				
				Point textSize = gcFinal.textExtent(decoration);
				gcFinal.drawText(decoration, odInitPixel+((box_width - textSize.x)/2), ((box_height - textSize.y)/2));
			}
		}
	}

	/***
	 * verify if the menu "Show trace records" is checked
	 * 
	 * @return true of the menu is checked. false otherwise
	 */
	private boolean needToShowRecords()
	{
		boolean isDebug = false;

		final State state = showCount.getState(RegistryToggleState.STATE_ID);
		if (state != null)
		{
			final Boolean b = (Boolean) state.getValue();
			isDebug = b.booleanValue();
		}
		return isDebug;
	}
}
