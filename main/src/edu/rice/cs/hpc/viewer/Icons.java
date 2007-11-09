package edu.rice.cs.hpc.viewer;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Singleton class containing global variables for icons 
 * @author laksono
 *
 */
public class Icons {
	// ------------------------- icons path
	public final String ICONPATH="../../../../../../icons/";
	
	// -------------------------- image files
	public Image imgCallFrom;
	public Image imgCallTo;
	public Image imgZoomIn;
	public Image imgZoomOut;
	public Image imgFlatten;
	public Image imgUnFlatten;
	public Image imgHPC;
	public Image imgHPCbig;
	
	//-------------------------- image descriptor
	public ImageDescriptor imdCallFrom;
	public ImageDescriptor imdCallTo;
	public ImageDescriptor imdZoomIn;
	public ImageDescriptor imdZoomOut;
	public ImageDescriptor imdFlatten;
	public ImageDescriptor imdUnFlatten;
	public ImageDescriptor imdHPC;
	public ImageDescriptor imdHPCbig;

	static private Icons __singleton=null;
	
	public void createIcons() {
		imdCallFrom = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"CallFrom.gif");
		imdCallTo = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"CallTo.gif");
		imdZoomIn = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"Zoom in large.gif");
		imdZoomOut = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"Zoom out large.gif");
		imdFlatten = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"Flatten.gif");
		imdUnFlatten = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"Unflatten.gif");
		imdHPC = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"hpc-16x16.png");
		imdHPCbig = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"hpc-160x160.png");
		
		imgCallFrom = this.imdCallFrom.createImage();
		imgCallTo = this.imdCallTo.createImage();
		imgZoomIn = this.imdZoomIn.createImage();
		imgZoomOut = this.imdZoomOut.createImage();
		imgFlatten = this.imdFlatten.createImage();
		imgUnFlatten = this.imdUnFlatten.createImage();
		imgHPC = this.imdHPC.createImage();
		imgHPCbig = this.imdHPCbig.createImage();
	}
	
	static public Icons getInstance() {
		if (Icons.__singleton == null) {
			Icons.__singleton = new Icons();
			Icons.__singleton.createIcons();
		}
		return Icons.__singleton;
	}
	
	static public void dispose() {
		if (Icons.__singleton != null) {
			
		}
		
	}
}
