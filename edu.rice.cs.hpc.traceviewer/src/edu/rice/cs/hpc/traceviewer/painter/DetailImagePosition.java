package edu.rice.cs.hpc.traceviewer.painter;

import org.eclipse.swt.graphics.Image;

public class DetailImagePosition extends ImagePosition {

	final public Image imageOriginal;
	
	public DetailImagePosition(int position, Image image, Image imageOriginal) {
		super(position, image);
		this.imageOriginal = imageOriginal;
	}

}
