/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/

package com.onpositive.richtexteditor.io.html;

import java.io.PrintWriter;
import java.util.HashMap;

import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.onpositive.richtexteditor.io.TextSerializer;
import com.onpositive.richtexteditor.model.FontStyle;
import com.onpositive.richtexteditor.model.LayerManager;
import com.onpositive.richtexteditor.model.partitions.BasePartition;
import com.onpositive.richtexteditor.model.partitions.HRPartition;
import com.onpositive.richtexteditor.model.partitions.ImagePartition;
import com.onpositive.richtexteditor.model.partitions.LinkPartition;


/**
 * @author 32kda
 * {@link TextSerializer} impl for serializing into HTML format
 */
public class HTMLSerializer extends TextSerializer
{
	private IHTMLSerializationHelper helper;
	
	
	/**
	 * @return returns a helper
	 */
	public IHTMLSerializationHelper getHelper() {
		return helper;
	}

	/**
	 * @param helper IHTMLSerializationHelper to set 
	 */
	public void setHelper(IHTMLSerializationHelper helper) {
		this.helper = helper;
	}	
	
	
	/**
	 * Basic constructor
	 * @param manager {@link LayerManager} instance
	 */
	public HTMLSerializer(LayerManager manager)
	{
		numberedListsEnds = new HashMap<Integer, Bullet>();
		curLayer = manager.getLayer();
		fontRegistry = manager.getFontRegistry();
		editor = manager.getEditor();
		doc = manager.getDocument();
	}
			
	/**
	 * Serializes all contents to the specific PrintWriter
	 * @param pw PrintWriter
	 */
	public void serializeAll(PrintWriter pw)
	{
		pw.println("<html>");
		
		pw.println("<body >");
			for (int i = 0; i < doc.getNumberOfLines(); i++)
			{
				pw.println(getSerializedLine(i));
			}
		pw.println("</body>");
		pw.println("</html>");
		pw.close();
	}
	
	
	/**
	 * Return an html string representing some partition wiith all needed style tags etc. 
	 * @param partition partition to serialize
	 * @param partitionText text of such partition.
	 * Can differ from partition text, when we want to get the style for the part of the partition
	 * @return html string for partition with styles  
	 */
	public String applyPartitionStyleToString(BasePartition partition, String partitionText)
	{
		if (partition instanceof ImagePartition)
			return getImageStr((ImagePartition)partition);
		if (partition instanceof HRPartition)
			return getHRString((HRPartition)partition);
		
		StringBuilder str = new StringBuilder();
		
		boolean fontTag = false, spanTag = false;
		if (!partition.getFontDataName().equals(FontStyle.NORMAL_FONT_NAME))
		{
			FontData fd = fontRegistry.get(partition.getFontDataName()).getFontData()[0];
			str.append("<span style='font-family:" + fd.getName() + "; font-weight: normal; font-size: " + fd.getHeight() + "pt;' >");
			spanTag = true;
		}
		if (partition.getColorRGB() != null && !partition.getColorRGB().equals(black)) 
		{
			if (!fontTag) {	str.append("<font "); fontTag = true;}
			str.append(getColorStr(partition.getColorRGB()));
		}
		if (partition.getBgColorRGB() != null && !partition.getBgColorRGB().equals(white)) 
		{
			if (!fontTag) {	str.append("<font ");fontTag = true;}
			str.append(getBgColorStr(partition.getBgColorRGB()));
		}
		if (fontTag) {
			str.append(">");
		}
				
		if (partition.isBold()) str.append("<b>");
		if (partition.isItalic()) str.append("<i>");
		if (partition.isUnderlined()) str.append( "<u>");
		if (partition.isStrikethrough()) str.append("<STRIKE>");
		
		
		if (partition instanceof LinkPartition)
		{
			LinkPartition linkPartition = (LinkPartition) partition;
			String url = linkPartition.getUrl();
			if (helper!=null){
				url=helper.getLinkURL(linkPartition);
			}
			str.append("<A href = \"" + url + "\">");			
		}
		if (helper!=null){
			String prefix=helper.getAdditionPartitionPrefix(partition);
			if (prefix!=null){
			str.append(prefix);
			}
		}
		str.append(encodeStrToHTML(partitionText));
		if (helper!=null){
			String prefix=helper.getAdditionPartitionSuffix(partition);
			if (prefix!=null){
			str.append(prefix);
			}
		}
		if (partition instanceof LinkPartition) str.append("</A>");
			
		if (partition.isStrikethrough()) str.append("</STRIKE>");
		if (partition.isUnderlined()) str.append("</u>");
		if (partition.isItalic()) str.append("</i>");
		if (partition.isBold()) str.append("</b>");
		if (fontTag) str.append("</font>");
		if (spanTag) str.append("</span>");
		return str.toString();		
	}
	
	protected String getHRString(HRPartition partition)
	{
		String str = "<hr";
		if (partition.getColorRGB() != null)
			str = str + getColorStr(partition.getColorRGB());
		str = str + ">";
		return str;
	}


	protected String encodeStrToHTML(String str)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++)
		{
			if (builder.length()!=0)
			{
				if (Character.isWhitespace(builder.charAt(builder.length()-1)))
				{
						if (str.charAt(i) == ' ')
						{
							builder.append("&nbsp;");
							continue;
						}
				}
			}
			builder.append(str.charAt(i));
		}
		String str2 = builder.toString();
		str2.replace(" ", "&nbsp;");
		str2.replace("<", "&lt;");
		str2.replace(">", "&gt;");
		str2.replace("&", "&amp;");
		
		return str2;
	}
	
	protected String getImageStr(ImagePartition partition)
	{
		String imageFileName = partition.getImageFileName();
		if (helper!=null){
			imageFileName=helper.getImageLocation(partition);
		}
		return "<IMG src=\"" + imageFileName + "\">";
	}


	protected String getFontFaceStr(String fontDataName)
	{
		return " face = \"" + fontRegistry.get(fontDataName).getFontData()[0].getName() +
			   "\" height = \"" + fontRegistry.get(fontDataName).getFontData()[0].getHeight() + "px\"";
	}
	
	protected String getFontCSSStr(String fontDataName)
	{
		return " font-family : " + fontRegistry.get(fontDataName).getFontData()[0].getName() + ";\n" +
			   " font-size :" + fontRegistry.get(fontDataName).getFontData()[0].getHeight() + "px;";
	}
	
	protected String getBodyFontParametersStr()
	{
		 String str = "<style type=\"text/css\">\n";
		 str = str + "body\n{\n";
		 str = str + getFontCSSStr(FontStyle.NORMAL_FONT_NAME);
		 str = str + "}\n</style>\n";
        return str;
	}
	
	protected String getColorStr(RGB color)
	{		
		return " color = \"" + getRGBColorHexStr(color) + "\""; 
	}
	
	protected String getBgColorStr(RGB color)
	{
		return " style=\"background-color:" + getRGBColorHexStr(color) + "\"";
	}
	
	protected String getRGBColorHexStr(RGB color)
	{
		String r = Integer.toHexString(color.red);
		if (r.length() == 1) r = "0" + r;
		String g = Integer.toHexString(color.green);
		if (g.length() == 1) g = "0" + g;
		String b = Integer.toHexString(color.blue);
		if (b.length() == 1) b = "0" + b;
		return "#" + r + g + b; 
	}


	
	protected String getCenterAlignAttributeString()
	{
		return "align = \"center\"";
	}


	
	protected String getFileEndString()
	{
		return "</body></html>";
	}


	
	protected String getFileStartString()
	{
		return 	"<html>\n<body>\n";// + getBodyFontParametersStr();
	}


	
	protected String getJustifyAlignAttributeString()
	{
		return "align = \"justify\"";
	}


	
	protected String getLeftAlignAttributeString()
	{
		return "align = \"left\"";
	}


	
	protected String getLineBreakString()
	{
		return "<br>";
	}


	
	protected String getParagraphEndString()
	{		
		return "</P>";
	}


	
	protected String getParagraphStartString()
	{
		return "<P  style=\"margin: 4;\"";
	}


	
	protected String getRightAlignAttributeString()
	{
		return 	"align = \"right\"";
	}


	
	protected String getTagCloseString()
	{
		return ">";
	}


	
	protected String getBulletedListCloseString()
	{
		return "</ul>";
	}


	
	protected String getBulletedListOpenString()
	{
		return "<ul>";
	}


	
	protected String getListElementStartString()
	{
		return "<li>";
	}


	
	protected String getNumberedListCloseString()
	{
		return "</ol>";
	}


	
	protected String getNumberedListOpenString()
	{
		return "<ol>";
	}
	
}
