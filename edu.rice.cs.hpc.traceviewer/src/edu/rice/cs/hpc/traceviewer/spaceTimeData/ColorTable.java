package edu.rice.cs.hpc.traceviewer.spaceTimeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import edu.rice.cs.hpc.data.util.IProcedureTable;
import edu.rice.cs.hpc.traceviewer.util.ProcedureClassMap;
/**************************************************************
 * A data structure designed to hold all the name-color pairs
 * needed for the actual drawing.
 **************************************************************/
public class ColorTable implements IProcedureTable
{
	static final private int COLOR_ICON_SIZE = 12;
	static private ColorImagePair IMAGE_WHITE;
	static private ColorImagePair IMAGE_GRAY;
	
	// data members
	HashMap<String, ColorImagePair> colorMatcher;
	
	/**All of the function names stored in this colorTable.*/
	ArrayList<String> procNames;
	
	/**The display this ColorTable uses to generate the random colors.*/
	Display display;
	
	private final ProcedureClassMap classMap;
	
	/**Creates a new ColorTable with Display _display.*/
	public ColorTable(Display _display)
	{
		procNames = new ArrayList<String>();
		display = _display;
		IMAGE_WHITE = new ColorImagePair(display.getSystemColor(SWT.COLOR_WHITE));
		IMAGE_GRAY = new ColorImagePair(display.getSystemColor(SWT.COLOR_GRAY));
		
		classMap = new ProcedureClassMap();
	}
	
	/**
	 * Dispose the allocated resources
	 */
	public void dispose() {
		for (ColorImagePair pair: colorMatcher.values()) {
			pair.dispose();
		}
	}
	
	/**
	 * Returns the color in the colorMatcher that corresponds to the name's class
	 * @param name
	 * @return
	 */
	public Color getColor(String name)
	{
		final String procClass = getProcedureClass( name );
		return colorMatcher.get(procClass).getColor();
	}
	
	/**
	 * returns the image that corresponds to the name's class
	 * @param name
	 * @return
	 */
	public Image getImage(String name) 
	{
		final String procClass = getProcedureClass( name );
		final ColorImagePair cipair = colorMatcher.get(procClass);
		if (cipair != null) {
			return cipair.getImage();
		} else {
			return null;
		}
	}
	
	
	/*********************************************************************
	 * Fills the colorMatcher with unique "random" colors that correspond
	 * to each function name in procNames.
	 *********************************************************************/
	public void setColorTable()
	{
		//This is where the data file is converted to the colorTable using colorMatcher.
		//creates name-function-color colorMatcher for each function.
		colorMatcher = new HashMap<String,ColorImagePair>();
		{
			// rework the color assignment to use a single random number stream
			Random r = new Random((long)612543231);
			int cmin = 16;
			int cmax = 200 - cmin;
			for (int l=0; l<procNames.size(); l++) {
				
				String procName = procNames.get(l);
				
				if (procName != CallPath.NULL_FUNCTION) {
					String procClass = getProcedureClass( procName );
					
					if (!colorMatcher.containsKey(procClass)) {
						
						if (procClass.equals(ProcedureClassMap.CLASS_IDLE)) 
						{
							colorMatcher.put(ProcedureClassMap.CLASS_IDLE, IMAGE_GRAY);								
						} else 
						{
							Color c = new Color(display, 
									cmin + r.nextInt(cmax), 
									cmin + r.nextInt(cmax), 
									cmin + r.nextInt(cmax));
							colorMatcher.put(procClass, new ColorImagePair(c));
						}
					}
				} else {
					colorMatcher.put(procName, IMAGE_WHITE);
				}
			}
		}
	}
	
	/************************************************************************
	 * Adds a name to the list of function names in this ColorTable.
	 * NOTE: Doesn't create a color for this name. All the color creating
	 * is done in setColorTable.
	 * @param name The function name to be added.
	 ************************************************************************/
	public void addProcedure(String name)
	{
		if(!procNames.contains(name))
			procNames.add(name);
	}
	
	/***********************************************************************
	 * return the class of the procedure (if exists), otherwise return the 
	 * 	procedure name itself
	 * 
	 * @param name
	 * @return
	 ***********************************************************************/
	private String getProcedureClass( String name ) 
	{
		String procClass = this.classMap.get(name);
		if (procClass != null)
			return procClass;
		else
			return name;
	}
	
	/************************************************************************
	 * class to pair color and image
	 * @author laksonoadhianto
	 *
	 ************************************************************************/
	private class ColorImagePair {
		private Color color;
		private Image image;
		
		/****
		 * create a color-image pair
		 * @param color c
		 */
		ColorImagePair(Color c) {
			// create an empty image filled with color c
			PaletteData pdata = new PaletteData(new RGB[]{c.getRGB()});
			ImageData idata = new ImageData(COLOR_ICON_SIZE, COLOR_ICON_SIZE, 1, pdata);
			this.image = new Image(display, idata);
			this.color = c;
		}
		
		/***
		 * get the color 
		 * @return
		 */
		public Color getColor() {
			return this.color;
		}
		
		/***
		 * get the image
		 * @return
		 */
		public Image getImage() {
			return this.image;
		}
		
		public void dispose() {
			this.color.dispose();
			this.image.dispose();
		}
	}
}