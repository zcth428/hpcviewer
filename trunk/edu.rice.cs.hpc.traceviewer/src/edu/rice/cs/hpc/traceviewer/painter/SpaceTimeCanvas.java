package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
/******************************************************************
 * An abstract class for the two canvases on the viewer to extend.
 *****************************************************************/
public abstract class SpaceTimeCanvas extends Canvas
{
	private static final long serialVersionUID = 1L;
	
	/**The SpaceTimeData corresponding to this canvas.*/
	SpaceTimeData stData;
	
    /**The width of the current screen in this canvas.*/
    int viewWidth;
    
    /**The height of the current screen in this canvas.*/
	int viewHeight;
	
	/**The top left pixel's x location.*/
	long topLeftPixelX;
	/**The top left pixel's y location.*/
	long topLeftPixelY;
    
    /**The beginning/end time being viewed now.*/
    long begTime;
    long endTime;
    
    /**The first process being viewed now.*/
    double begProcess;
    /**The last process being viewed now.*/
    double endProcess;
	
    /**Creates a SpaceTimeCanvas with the data _stData and Composite _composite.*/
    public SpaceTimeCanvas(Composite _composite)
    {
		super(_composite, SWT.NO_BACKGROUND | SWT.H_SCROLL | SWT.V_SCROLL);
	}
    
    
    /**Conversion factor from actual time to pixels on the x axis. To be implemented in subclasses.*/
    public abstract double getScaleX();
    
    /**Conversion factor from actual processes to pixels on the y axis.  To be implemented in subclasses.*/
    public abstract double getScaleY();
    
        
    
    public void setSpaceTimeData(SpaceTimeData _stData) {
    	this.stData = _stData;
    }
}