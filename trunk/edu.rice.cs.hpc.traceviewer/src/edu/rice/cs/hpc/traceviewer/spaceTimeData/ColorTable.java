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
/**************************************************************
 * A data structure designed to hold all the name-color pairs
 * needed for the actual drawing.
 **************************************************************/
public class ColorTable implements IProcedureTable
{
	static final private int COLOR_ICON_SIZE = 12;
	static private ColorImagePair IMAGE_WHITE;
	
	// data members
	HashMap<String, ColorImagePair> colorMatcher;
	
	/**All of the function names stored in this colorTable.*/
	ArrayList<String> procNames;
	
	/**The display this ColorTable uses to generate the random colors.*/
	Display display;
	
	/**Creates a new ColorTable with Display _display.*/
	public ColorTable(Display _display)
	{
		procNames = new ArrayList<String>();
		display = _display;
		IMAGE_WHITE = new ColorImagePair(display.getSystemColor(SWT.COLOR_WHITE));
	}
	
	/**
	 * Dispose the allocated resources
	 */
	public void dispose() {
		for (ColorImagePair pair: colorMatcher.values()) {
			pair.dispose();
		}
	}
	
	/**Returns the color in the colorMatcher that corresponds to name.*/
	public Color getColor(String name)
	{
		return colorMatcher.get(name).getColor();
	}
	
	public Image getImage(String name) {
		ColorImagePair cipair = colorMatcher.get(name);
		if (cipair != null) {
			return colorMatcher.get(name).getImage();
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
		if (true) {
			// rework the color assignment to use a single random number stream
			Random r = new Random((long)612543231);
			int cmin = 16;
			int cmax = 200 - cmin;
			for (int l=0; l<procNames.size(); l++) {
				String procName = procNames.get(l);
				if (procName != CallPath.NULL_FUNCTION) {
					Color c = new Color(display, 
								cmin + r.nextInt(cmax), 
								cmin + r.nextInt(cmax), 
								cmin + r.nextInt(cmax));
					colorMatcher.put(procName, new ColorImagePair(c));
				} else {
					colorMatcher.put(procName, IMAGE_WHITE);
				}
			}

		} else {
			Random red = new Random((long)612543231);
			Random blue = new Random((long)91028735);
			Random green = new Random((long)19238479);

			for(int l=0;l<procNames.size();l++)
			{
				String procName = procNames.get(l);

				if(procName!=CallPath.NULL_FUNCTION)
				{
					Color c = new Color(display, red.nextInt(200),green.nextInt(200),blue.nextInt(200));
					colorMatcher.put(procName, new ColorImagePair(c));
				}
				else
				{
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