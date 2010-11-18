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

package com.onpositive.richtexteditor.io.html_scaner;


/**
 * @author 32kda
 * Indicates  beginning of some tag
 */
public class TagLexEvent extends LexEvent
{
	protected int type;
	protected boolean open = true;
	
	
	/**
	 * @return tag type constant
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type tag type constant to set
	 */
	public void setType(int type)
	{
		this.type = type;
	}

	
	/**
	 * @return is this a open (&lt;x&gt;) or a close (&lt;/x&gt;) tag?  
	 */
	public boolean isOpen()
	{
		return open;
	}

	/**
	 * @param open is this a open (&lt;x&gt;) or a close (&lt;/x&gt;) tag?
	 */	
	public void setOpen(boolean open)
	{
		this.open = open;
	}
	
	/**
	 * Constructor
	 * @param l lex word
	 * @param type tag type constant
	 */
	public TagLexEvent(String l, int type)
	{
		super(l);
		this.type = type;
	}
	
	/**
	 * Basic constructor
	 * @param l lex word
	 * @param type tag type constant to 
	 * @param open is this a open (&lt;x&gt;) or a close (&lt;/x&gt;) tag?
	 */
	public TagLexEvent(String l, int type, boolean open)
	{
		super(l);
		this.type = type;
		this.open = open;
	}
		
}
