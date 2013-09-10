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

/**
 * @author kor
 * Basic interface of html loader
 */
public interface IHTMLLoader extends ITextLoader
{
	/**
	 * set the parent's path of the HTML file. This parent path is useful to locate 
	 * 	embedded texts and images inside the HTML
	 * @param sPath: the path of the parent
	 */
	public void setParentPath(String sPath);
}
