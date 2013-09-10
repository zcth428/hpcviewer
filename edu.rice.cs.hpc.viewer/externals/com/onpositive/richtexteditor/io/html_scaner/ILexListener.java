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
 * LexListener basic interface
 */
public interface ILexListener
{
	/**
	 * Main scanner/parser event handling mehod
	 * @param event LexEvent instance
	 */
	public void handleLexEvent(LexEvent event);
}
