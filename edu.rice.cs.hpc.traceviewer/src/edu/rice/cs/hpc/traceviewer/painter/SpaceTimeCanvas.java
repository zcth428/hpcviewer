package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.PaintManager;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeDataController;
/******************************************************************
 * An abstract class for the two canvases on the viewer to extend.
 *****************************************************************/
public abstract class SpaceTimeCanvas extends Canvas
{
	enum MouseState { ST_MOUSE_INIT, ST_MOUSE_NONE, ST_MOUSE_DOWN };

	
	/**The SpaceTimeData corresponding to this canvas.*/
	protected SpaceTimeDataController stData;
	protected PaintManager painter;
	protected ImageTraceAttributes attributes;
	
    /**The width of the current screen in this canvas.*/
    int viewWidth;
    
    /**The height of the current screen in this canvas.*/
	int viewHeight;
	
	/**The top left pixel's x location.*/
	long topLeftPixelX;
	/**The top left pixel's y location.*/
	long topLeftPixelY;
    
    /**Creates a SpaceTimeCanvas with the data _stData and Composite _composite.*/
    public SpaceTimeCanvas(Composite _composite)
    {
		super(_composite, SWT.NO_BACKGROUND);
	}
    
    
    /**Conversion factor from actual time to pixels on the x axis. To be implemented in subclasses.*/
    public abstract double getScaleX();
    
    /**Conversion factor from actual processes to pixels on the y axis.  To be implemented in subclasses.*/
    public abstract double getScaleY();
    
        
    
    public void setSpaceTimeData(SpaceTimeDataController dataTraces) {
    	this.stData = dataTraces;
    	this.attributes = stData.getAttributes();
    	this.painter = stData.getPainter();
    }
}