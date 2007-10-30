//////////////////////////////////////////////////////////////////////////
//																		//
//	Icons.java															//
//																		//
//	util.Icons -- icons appearing in the HPCViewer user interface		//
//	Last edited: January 14, 2002 at 6:18 pm							//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.util;


import javax.swing.*;
import java.net.URL;
import java.lang.Integer;





//////////////////////////////////////////////////////////////////////////
//	CLASS ICONS															//
//////////////////////////////////////////////////////////////////////////

/**
 *
 * The icons used in HPCViewer's user interface.
 *
 */
 
public class Icons
{

private static final String RICE           = "edu/rice/cs/hpcviewer/icons";

// paths within Sun's Swing look & feel image library "jlfgr-1_0.jar"
private static final String SUN_GENERAL    = "toolbarButtonGraphics/general";
private static final String SUN_MEDIA      = "toolbarButtonGraphics/media";
private static final String SUN_NAVIGATION = "toolbarButtonGraphics/navigation";


private static final int TOOLBAR_SIZE = 16;
//private static final int TOOLBAR_SIZE = 24;




public static ImageIcon BACK		= Icons.get(SUN_MEDIA,      "StepBack",			TOOLBAR_SIZE);
public static ImageIcon FIND		= Icons.get(SUN_GENERAL,    "Find",				TOOLBAR_SIZE);
public static ImageIcon FIND_AGAIN	= Icons.get(SUN_GENERAL,    "FindAgain",		TOOLBAR_SIZE);
public static ImageIcon FIRST		= Icons.get(SUN_MEDIA,      "Rewind",			TOOLBAR_SIZE);
public static ImageIcon FLATTEN		= Icons.get(RICE,   		"Flatten",			0);
public static ImageIcon FORWARD		= Icons.get(SUN_MEDIA,      "StepForward",		TOOLBAR_SIZE);
public static ImageIcon GOTO		= Icons.get(SUN_GENERAL,    "Open",				TOOLBAR_SIZE);
public static ImageIcon HELP		= Icons.get(SUN_GENERAL,    "Help",				TOOLBAR_SIZE);
public static ImageIcon IN			= Icons.get(SUN_NAVIGATION, "Down",				TOOLBAR_SIZE);
public static ImageIcon METRIC_GOTO = Icons.get(RICE,           "info",		0);

public static ImageIcon METRIC_UP	= Icons.get(RICE,           "ZoomInSmall",	0);
public static ImageIcon METRIC_DOWN	= Icons.get(RICE,           "ZoomOutSmall",	0);
public static ImageIcon NEXT		= Icons.get(SUN_NAVIGATION, "Forward",			TOOLBAR_SIZE);
public static ImageIcon OUT			= Icons.get(SUN_NAVIGATION, "Up",				TOOLBAR_SIZE);
public static ImageIcon PREV		= Icons.get(SUN_NAVIGATION, "Back",				TOOLBAR_SIZE);
public static ImageIcon STOP		= Icons.get(SUN_GENERAL,    "Stop",				TOOLBAR_SIZE);
public static ImageIcon UNFLATTEN	= Icons.get(RICE,		    "Unflatten",		0);
public static ImageIcon ZOOM_IN		= Icons.get(RICE,   		"ZoomInLarge",	0);
public static ImageIcon ZOOM_OUT	= Icons.get(RICE,			"ZoomOutLarge",	0);
public static ImageIcon CALL_TO	    = Icons.get(RICE,  			"CallTo",	0);
public static ImageIcon CALL_FROM	= Icons.get(RICE,  			"CallFrom",	0);




//////////////////////////////////////////////////////////////////////////
//	IMAGE LOADING														//
//////////////////////////////////////////////////////////////////////////




/************************************************************************
 *	Loads an image resource expected to be part of the application.
 *
 *	@param pathPrefix	Prefix of path to the icon to be loaded.
 *	@param name			Name of the icon to be loaded.
 *	@param size			Pixel size of the icon to be loaded.
 *
 *	@return				An <code>ImageIcon</code> containing the image.
 *
 *	@exception			<code>ExceptionInInitializerError</code>
 *							if icon cannot be found.
 *
 ************************************************************************/
	
public static ImageIcon get(String pathPrefix, String name, int size)
{
	String ssize = (size > 0 ? (new Integer(size)).toString() : "");
	String path = pathPrefix + "/" + name + ssize + ".gif";
	URL url = ClassLoader.getSystemResource(path);
	if( url == null )
		throw new ExceptionInInitializerError("Icons: Can't load image " + path);
		
	String description = "Icons." + name;
	ImageIcon icon = new ImageIcon(url, description);	
	return icon;
}


}	// end class Images









