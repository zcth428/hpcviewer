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

package com.onpositive.richtexteditor.io;

import java.io.PrintWriter;
import java.util.HashMap;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import com.onpositive.richtexteditor.model.BasePartitionLayer;
import com.onpositive.richtexteditor.model.Logger;
import com.onpositive.richtexteditor.model.partitions.BasePartition;


/**
 * @author 32kda
 * Basic abstract class for all classes,
 * which serializes editor's content into text-based formats like html
 */
public abstract class TextSerializer
{

	protected BasePartitionLayer curLayer = null;
	protected FontRegistry fontRegistry;
	protected StyledText editor;
	protected IDocument doc;
	boolean optimizeParagraphs = false;
	boolean isBulletedList = false;
	boolean isNumberedList = false;
	protected HashMap <Integer, Bullet> numberedListsEnds;
	protected static RGB black = new RGB(0,0,0);
	protected static RGB white = new RGB(255,255,255);

	/**
	 * Serializes all contents of document into single string
	 * @return Serialized string
	 */
	public String serializeAllToStr()
	{
		defineNumberedListsEnds();
		String str = "";
		for (int i = 0; i < doc.getNumberOfLines(); i++)
		{
			str = str + getSerializedLine(i) + "\n";
		}
		return getFileStartString() + str + getFileEndString(); 
	}
	
	/**
	 * Serialized selected doc fragment into String
	 * @param selection selection in editor
	 * @return serialized string
	 * @throws BadLocationException (actually never does it on correct selection)
	 */
	public String serializeToStr(Point selection) throws BadLocationException {
		
		defineNumberedListsEnds();
		String str = "";
		int startingPosition=selection.x;
		int endingPosition=selection.y;
		int startLine=doc.getLineOfOffset(startingPosition);
		int endLine=doc.getLineOfOffset(endingPosition);
		IRegion lineInformation = doc.getLineInformation(startLine);
		int length=lineInformation.getOffset()+lineInformation.getLength()-startingPosition;
		if (startLine==endLine)
		{
			str+=getSerializedPartOfLine(startLine,  selection.x, selection.y-selection.x, false);
		}
		else{
		if (length>0)
		{
			str+=getSerializedPartOfLine(startLine,  startingPosition, length, false);
		}
		for (int i = startLine+1; i < endLine; i++)
		{
			str = str + getSerializedLine(i) + "\n";
		}
		lineInformation = doc.getLineInformation(endLine);
		length=endingPosition-lineInformation.getOffset();
		if (length>0){
			str+=getSerializedPartOfLine(endLine,  lineInformation.getOffset(), length, false);
		}
		}
		return getFileStartString() + str + getFileEndString();
	}

	protected void defineNumberedListsEnds()
	{
		for (int i = 0; i < doc.getNumberOfLines(); i++)
		{
			if (editor.getLineBullet(i) != null && 
				editor.getLineBullet(i).type == (ST.BULLET_TEXT | ST.BULLET_NUMBER))
			{
				Bullet curBullet = editor.getLineBullet(i);
				int j = i;
				while (++i < doc.getNumberOfLines())
				{
					 if (editor.getLineBullet(i) == curBullet) j = i;
				}
				i = j + 1; //
				numberedListsEnds.put(j,curBullet);
			}
		}
	}

	/**
	 * Serializes a single line
	 * @param lineNum number of line to serialize
	 * @return seralized doc line
	 */
	public String getSerializedLine(int lineNum)
	{
		int offset = 0, length = 0;		
		try
		{
			length = doc.getLineLength(lineNum);
			offset = doc.getLineOffset(lineNum);
	
		} catch (BadLocationException e)
		{
			Logger.log(e);
		}
		
		return getSerializedPartOfLine(lineNum,  offset, length, true);
	}


	private String getSerializedPartOfLine(int lineNum, int offset, int length, boolean appendParagraphs) {
		String str = "";
		
		if (editor.getLineBullet(lineNum) != null)
		{
			if (editor.getLineBullet(lineNum).type == ST.BULLET_DOT)
			{
				if (!isBulletedList)
				{
					isBulletedList = true;
					str = str + getBulletedListOpenString() + "\n";
				}
				str = str + getListElementStartString();
			}
			else if (editor.getLineBullet(lineNum).type == (ST.BULLET_TEXT | ST.BULLET_NUMBER))
			{
				if (!isNumberedList)
				{
					isNumberedList = true;
					str = str + getNumberedListOpenString() + "\n";
				}
				str = str + getListElementStartString();				
			}
		}
		else
		{
			if (isBulletedList)
			{
				str = str + "\n" + getBulletedListCloseString();
				isBulletedList = false;
			}
			else if (isNumberedList 
					 && numberedListsEnds.get(lineNum - 1) != null 
					 && numberedListsEnds.get(lineNum - 1).type == (ST.BULLET_TEXT | ST.BULLET_NUMBER))
			{
				str = str + "\n" + getNumberedListCloseString();
				isNumberedList = false;				
			}
				
		}
		try
		{
			if (doc.get(doc.getLineOffset(lineNum), doc.getLineLength(lineNum)).trim().length()==0) return str + "<br>";
				
			if (appendParagraphs && !(optimizeParagraphs && lineNum > 0 && linesEqualsByParagraphStyle(lineNum, lineNum - 1)))
			{
				int align = editor.getLineAlignment(lineNum);
				str = str + getParagraphStartString();
				if (align == SWT.LEFT) 
				{
					if (editor.getLineJustify(lineNum)) str = str + getJustifyAlignAttributeString();
					else str = str + getLeftAlignAttributeString();
				}
				else if (align == SWT.RIGHT) str = str + getRightAlignAttributeString();
				else if (align == SWT.CENTER) str = str + getCenterAlignAttributeString();
				
				str = str + getTagCloseString();
			}
		} catch (BadLocationException e)
		{
			Logger.log(e);
		}
		BasePartition startPartition = (BasePartition) curLayer.getPartitionAtOffset(offset);
		BasePartition endPartition = (BasePartition) curLayer.getPartitionAtOffset(offset + length - 1);
		if (startPartition == null) return str;
		if (startPartition == endPartition)
		{
			str = str + applyPartitionStyleToString(startPartition,startPartition.getTextRegion(offset,length));
		}
		else
		{
			str = str + applyPartitionStyleToString(startPartition, startPartition.getTextFromOffset(offset));
			for (int i = startPartition.getPosition() + 1; i < endPartition.getPosition(); i++)
			{
				BasePartition curPartition = (BasePartition) curLayer.get(i);
				str = str + applyPartitionStyleToString(curPartition, curPartition.getText());
			}
			str = str + applyPartitionStyleToString(endPartition, endPartition.getTextUpToOffset(offset + length));
		}
		
		if (optimizeParagraphs && lineNum < doc.getNumberOfLines() - 1 && editor.getLineAlignment(lineNum) == editor.getLineAlignment(lineNum + 1))
			str = str + getLineBreakString();
		else if (appendParagraphs) str = str + getParagraphEndString();
				
		return str;
	}

	protected abstract String applyPartitionStyleToString(BasePartition startPartition,
			String textRegion);
	
	protected abstract String getFileStartString();
	protected abstract String getFileEndString();
	protected abstract String getLineBreakString();
	protected abstract String getParagraphStartString();
	protected abstract String getParagraphEndString();
	protected abstract String getLeftAlignAttributeString();
	protected abstract String getRightAlignAttributeString();
	protected abstract String getCenterAlignAttributeString();
	protected abstract String getJustifyAlignAttributeString();
	protected abstract String getTagCloseString();
	protected abstract String getBulletedListOpenString();
	protected abstract String getBulletedListCloseString();
	protected abstract String getNumberedListOpenString();
	protected abstract String getNumberedListCloseString();
	protected abstract String getListElementStartString();
	

	
	/**
	 * Checks, that line1 and line2 have equal paragraph style 
	 * @param line1 first line to compare
	 * @param line2 second line to compare
	 * @return true if equal, false otherwise
	 */
	public boolean linesEqualsByParagraphStyle(int line1, int line2)
	{
		if (editor.getLineAlignment(line1) == editor.getLineAlignment(line2) &&
			editor.getLineJustify(line1) == editor.getLineJustify(line2)) return true;
		return false;
	}

	public abstract void serializeAll(PrintWriter pw);


}
