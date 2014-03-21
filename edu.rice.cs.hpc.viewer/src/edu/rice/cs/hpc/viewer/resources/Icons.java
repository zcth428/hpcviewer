package edu.rice.cs.hpc.viewer.resources;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import edu.rice.cs.hpc.viewer.framework.Activator;

/**
 * Singleton class containing global variables for icons 
 * @author laksono
 *
 */
public class Icons {
	
	final static public String Image_CallFrom = "CallFrom.gif";
	final static public String Image_CallTo = "CallTo.gif";
	final static public String Image_CallFromDisabled = "CallFromDisabled.gif";
	final static public String Image_CallToDisabled = "CallToDisabled.gif";
	final static public String Image_ZoomIn = "ZoomIn.gif";
	final static public String Image_ZoomOut = "ZoomOut.gif";
	final static public String Image_Flatten = "Flatten.gif";
	final static public String Image_Unflatten = "Unflatten.gif";
	final static public String Image_CheckColumns = "checkColumns.gif";
	final static public String Image_FlameIcon = "flameIcon.gif";
	final static public String Image_FnMetric = "FnMetric.gif";
	final static public String Image_FontBigger = "FontBigger.gif";
	final static public String Image_FontSmaller = "FontSmaller.gif";
	final static public String Image_SaveCSV = "savecsv.gif";
	final static public String Image_Graph = "Graph.png";
	
	static private Icons __singleton=null;
	static private final AtomicBoolean isInitialized = new AtomicBoolean(false);
	
	/********
	 * get the instance of the current icons class
	 * We just want to make sure only ONE initialization for 
	 * image creation and registration for each class.
	 * 
	 * Although initialization for each object doesn't harm, but
	 * it's useless and time consuming 
	 * 
	 * @return
	 ********/
	static public Icons getInstance() {
		if (Icons.__singleton == null) {
			Icons.__singleton = new Icons();
		}
		return Icons.__singleton;
	}	
	
	/*************
	 * initialize images. The method only needs to be called once for the whole
	 * window lifespan. Athough calling this multiple times is theoretically
	 * harmless (never tried).
	 * 
	 * @param registry
	 *************/
	static public void init(ImageRegistry registry) {
		
		if (isInitialized.compareAndSet(false, true)) {
			registerImage(registry, Image_CallFrom);
			registerImage(registry, Image_CallTo);
			registerImage(registry, Image_CallFromDisabled);
			registerImage(registry, Image_CallToDisabled);
			
			registerImage(registry, Image_ZoomIn);
			registerImage(registry, Image_ZoomOut);
			registerImage(registry, Image_Flatten);
			registerImage(registry, Image_Unflatten);
			
			registerImage(registry, Image_CheckColumns);
			registerImage(registry, Image_FlameIcon);

			registerImage(registry, Image_FnMetric);
			registerImage(registry, Image_FontBigger);
			registerImage(registry, Image_FontSmaller);
			registerImage(registry, Image_SaveCSV);
			registerImage(registry, Image_Graph);
		}
	}
	
	static public Image getImage(final String desc) {
		final ImageRegistry registry = getRegistry();
		
		return registry.get(desc);
	}
	
	static private ImageRegistry getRegistry() {
    	// prepare the icon
		AbstractUIPlugin plugin = Activator.getDefault();
		ImageRegistry imageRegistry = plugin.getImageRegistry();
		
		return imageRegistry;
	}
	
	static private void registerImage(ImageRegistry registry, String key) {
		final ImageDescriptor desc = ImageDescriptor.createFromFile(Icons.class, key);
		registry.put(key, desc.createImage());
	}
}
