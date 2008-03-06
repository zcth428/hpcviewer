package edu.rice.cs.hpc.viewer.resources;

import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Singleton class containing global variables for icons 
 * @author laksono
 *
 */
public class Icons {
	// ------------------------- icons path
	public final String ICONPATH="";
	
	// -------------------------- image files
	public Image imgCallFrom;
	public Image imgCallTo;
	public Image imgZoomIn;
	public Image imgZoomOut;
	public Image imgFlatten;
	public Image imgUnFlatten;
	public Image imgHPC;
	public Image imgHPCbig;
	public Image imgResize;
	public Image imgColumns;
	public Image imgFalse;
	public Image imgTrue;
	public Image imgFlame;
	
	//-------------------------- image descriptor
	public ImageDescriptor imdCallFrom;
	public ImageDescriptor imdCallTo;
	public ImageDescriptor imdZoomIn;
	public ImageDescriptor imdZoomOut;
	public ImageDescriptor imdFlatten;
	public ImageDescriptor imdUnFlatten;
	public ImageDescriptor imdHPC;
	public ImageDescriptor imdHPCbig;
	public ImageDescriptor imdResize;
	public ImageDescriptor imdColumns;
	public ImageDescriptor imdFalse;
	public ImageDescriptor imdTrue;
	public ImageDescriptor imdFlame;

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
		imdResize = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"resizeColumns.gif");
		imdColumns =  ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"checkColumns.gif");
		this.imdTrue = ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"True.png");
		this.imdFalse =  ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"False.png");
		this.imdFlame =  ImageDescriptor.createFromFile(this.getClass(), this.ICONPATH+"Flame.png");
		
		imgCallFrom = this.imdCallFrom.createImage();
		imgCallTo = this.imdCallTo.createImage();
		imgZoomIn = this.imdZoomIn.createImage();
		imgZoomOut = this.imdZoomOut.createImage();
		imgFlatten = this.imdFlatten.createImage();
		imgUnFlatten = this.imdUnFlatten.createImage();
		imgHPC = this.imdHPC.createImage();
		imgHPCbig = this.imdHPCbig.createImage();
		imgResize = this.imdResize.createImage();
		imgColumns = this.imdColumns.createImage();
		this.imgTrue = this.imdTrue.createImage();
		this.imgFalse = this.imdFalse.createImage();
		this.imgFlame = this.imdFlame.createImage();
	}
	
	public void disposeIcon() {
		try {
			imgCallFrom.dispose();
			imgCallTo.dispose();
			imgZoomIn.dispose();
			imgZoomOut.dispose();
			imgFlatten.dispose();
			imgUnFlatten.dispose();
			imgHPC.dispose();
			imgHPCbig.dispose();
			imgResize.dispose();
			this.imgColumns.dispose();
			this.imgTrue.dispose();
			this.imgFalse.dispose();
			this.imgFlame.dispose();
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
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
			Icons.__singleton.disposeIcon();
		}
		
	}
}
