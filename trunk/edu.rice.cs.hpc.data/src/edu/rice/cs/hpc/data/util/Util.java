//////////////////////////////////////////////////////////////////////////
//																		//
//	Util.java															//
//																		//
//	util.Util -- miscellaneous useful operations						//
//	Last edited: September 18, 2001 at 6:55 pm							//
//																		//
//	(c) Copyright 2001 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.util;


//import java.awt.Color;
//import java.awt.FontMetrics;
import java.io.File;
import java.io.FilenameFilter;
import java.text.NumberFormat;
import java.text.DecimalFormat;
//import javax.swing.JComponent;
//import javax.swing.JPanel;
//import javax.swing.SwingUtilities;




//////////////////////////////////////////////////////////////////////////
//	CLASS UTIL															//
//////////////////////////////////////////////////////////////////////////

 /**
 *
 * Miscellaneous useful operations.
 *
 */


public class Util
{




//////////////////////////////////////////////////////////////////////////
//	PRIVATE CONSTANTS													//
//////////////////////////////////////////////////////////////////////////




/** An array of space characters used to left-pad the output of <code>DecimalFormat</code>. */
protected static final String SPACES = "                                        ";




//////////////////////////////////////////////////////////////////////////
//	STRING CONVERSION													//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns the <code>boolean</code> corresponding to a given string.
 ************************************************************************/
	
public static boolean booleanValue(String s)
{
	return Boolean.valueOf(s).booleanValue();
}




//////////////////////////////////////////////////////////////////////////
//	TEXT FORMATTING														//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns a <code>String</code> of a given number of space characters.
 ************************************************************************/
	
public static String spaces(int count)
{
	Dialogs.Assert(count <= SPACES.length(), "request too long Util::spaces");
	
	return SPACES.substring(0, count);
}




/*************************************************************************
 *	Fits a string into a field of given width, right adjusted.
 ************************************************************************/
	
public static String rightJustifiedField(String s, int fieldWidth)
{
	String field;
	
	int padLeft = fieldWidth - s.length();
	if( padLeft > 0 )
		field = Util.spaces(padLeft) + s;
	else
		field = s.substring(0, fieldWidth);

	return field;
}




/*************************************************************************
 *	Returns the width of a string in a given font on screen.
 ************************************************************************/
	/*
public static int computeStringWidth(String fontPropertyName, String s)
{
	JComponent temp = new JPanel();
	FontMetrics fm  = temp.getFontMetrics(Prefs.getFont(fontPropertyName));
	return SwingUtilities.computeStringWidth(fm, s);
}
*/



/*************************************************************************
 *	Returns a new <code>DecimalFormat</code> object with a given pattern.
 ************************************************************************/
	
public static DecimalFormat makeDecimalFormatter(String pattern)
{
	// make a formatter, checking that the locale allows this
	NumberFormat nf = NumberFormat.getInstance();
	Dialogs.Assert( nf instanceof DecimalFormat, "bad arg to Util::makeDecimalFormatter");
	DecimalFormat df = (DecimalFormat) nf;
	
	// apply the given pattern
	df.applyPattern(pattern);
	
	return df;
}




/*************************************************************************
 *	Formats an <code>int</code> in a given format and field width.
 *
 *	TODO: It might be possible to improve this method's implementation by
 *	using class <code>java.text.FieldPosition</code>.
 *
 ************************************************************************/
	
public static String formatInt(int n, DecimalFormat formatter, int fieldWidth)
{
	return Util.rightJustifiedField(formatter.format(n), fieldWidth);
}




/*************************************************************************
 *	Formats a <code>double</code> in a given format and field width.
 *
 *	TODO: It might be possible to improve this method's implementation by
 *	using class <code>java.text.FieldPosition</code>.
 *
 ************************************************************************/
	
public static String formatDouble(double d, DecimalFormat formatter, int fieldWidth)
{
	return Util.rightJustifiedField(formatter.format(d), fieldWidth);
}


/**
 * Class to filter the list of files in a directory and return only XML files 
 * The filter is basically very simple: if the last 3 letters has "xml" substring
 * then we consider it as XML file.
 * TODO: we need to have a more sophisticated approach to filter only the real XML files
 * @author laksono
 *
 */
public static class FileXMLFilter implements FilenameFilter {
	public boolean accept(File pathname, String sName) {
		int iLength = sName.length();
		if (iLength <4) // the file should contain at least four letters: ".xml"
			return false;
		String sExtension = (sName.substring(iLength-3, iLength)).toLowerCase();
		return (pathname.canRead() && sExtension.endsWith("xml"));
	}
}


/**
 * 
 * @author laksonoadhianto
 *
 */
public static class FileThreadsMetricFilter implements FilenameFilter {
	private String db_glob;
	
	public FileThreadsMetricFilter(String pattern) {
		db_glob = pattern.replace("*", ".*");
	}
	
	public boolean accept(File dir, String name) {
		
		boolean b = name.matches(db_glob);
		return b;
	}
	
}

public static File[] getListOfXMLFiles(String sDir) 
{
	// find XML files in this directory
	File files = new File(sDir);
	// for debugging purpose, let have separate variable
	File filesXML[] = files.listFiles(new FileXMLFilter());
	return filesXML;
}


static public String getObjectID(Object o) {
	return Integer.toHexString(System.identityHashCode(o));
}


static public void println( boolean debug, String str)
{
	if (debug)
		System.out.println(str);
}

//////////////////////////////////////////////////////////////////////////
//	COLOR																//
//////////////////////////////////////////////////////////////////////////




/*************************************************************************
 *	Returns a specified blend of two colors.
 *
 *	@return		(fraction * c1) + ((1.0 - fraction) * c2) in RGB color space
 *
 ************************************************************************/
	/*
public static Color blendColors(Color c1, double fraction1, Color c2)
{
	double fraction2 = 1.0 - fraction1;
	int red   = (int) Math.round( (fraction1 * c1.getRed()  ) + (fraction2 * c2.getRed()  ) );
	int green = (int) Math.round( (fraction1 * c1.getGreen()) + (fraction2 * c2.getGreen()) );
	int blue  = (int) Math.round( (fraction1 * c1.getBlue() ) + (fraction2 * c2.getBlue() ) );
	return new Color(red, green, blue);
}

*/


}








