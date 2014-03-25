package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
/******************************************************************
 * An abstract class for the two canvases on the viewer to extend.
 *****************************************************************/
public abstract class SpaceTimeCanvas extends Canvas
{
	enum MouseState { ST_MOUSE_INIT, ST_MOUSE_NONE, ST_MOUSE_DOWN };

	
	/**The SpaceTimeData corresponding to this canvas.*/
	protected SpaceTimeDataController stData;
	
	final protected Rectangle view;
    
    /**Creates a SpaceTimeCanvas with the data _stData and Composite _composite.*/
    public SpaceTimeCanvas(Composite _composite)
    {
		super(_composite, SWT.NO_BACKGROUND);
		view = new Rectangle(0, 0, 0, 0);
	}
        
    /**Conversion factor from actual time to pixels on the x axis. To be implemented in subclasses.*/
    public abstract double getScalePixelsPerTime();
    
    /**Conversion factor from actual processes to pixels on the y axis.  To be implemented in subclasses.*/
    public abstract double getScalePixelsPerRank();
    
    public void setSpaceTimeData(SpaceTimeDataController dataTraces) {
    	this.stData = dataTraces;
    }
}