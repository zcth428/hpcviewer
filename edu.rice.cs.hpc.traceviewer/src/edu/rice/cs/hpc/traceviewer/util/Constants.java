package edu.rice.cs.hpc.traceviewer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class Constants {
	
	public final static Color COLOR_WHITE = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    public final static Color COLOR_BLACK = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    
	/**The darkest color for black over depth text (switch to white if the sum of the 
	 * R, G, and B components is less than this number).*/
	public final static short DARKEST_COLOR_FOR_BLACK_TEXT = 384;
	
	/**The min number of time units you can zoom in*/
	public final static int MIN_TIME_UNITS_DISP = 1;
	
}
