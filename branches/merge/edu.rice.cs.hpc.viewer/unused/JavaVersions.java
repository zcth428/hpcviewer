//////////////////////////////////////////////////////////////////////////
//																		//
//	JavaVersions-J2.java												//
//																		//
//	JavaVersions -- version-related code (Java 2.0 implementation)		//
//	Last edited: January 3, 2002 at 3:24 pm								//
//																		//
//	(c) Copyright 2002 Rice University. All rights reserved.			//
//																		//
//////////////////////////////////////////////////////////////////////////




package edu.rice.cs.hpc.data.util;




//////////////////////////////////////////////////////////////////////////
//	CLASS JAVA-VERSIONS													//
//////////////////////////////////////////////////////////////////////////

/**
 *
 *	Code which is necessarily dependent on the version of Java in use.
 *
 */
 
 
public class JavaVersions
{

	
/** The Java version for which this executable was built ("new"). */
protected static final String EXPECTED_VERSION = "1.2";




/*************************************************************************
 *	Returns whether the running version of Java matches the version for
 *	which this executable was built.
 ************************************************************************/
	
public static void ensureVersion()
{
	String runningVersion = System.getProperty("java.version");
	boolean ok = runningVersion.compareTo(EXPECTED_VERSION) >= 0;
		
	if( ! ok )
		Dialogs.fail2(Strings.NEED_NEW_JAVA, runningVersion);
}



/*************************************************************************
 *	Returns the default renderer for a given column of a table cell.
 *
 *	Unfortunately there is no way to do this that works in both Java 1.1.8
 *	and Java 2.0. Worse yet, the one that works in 2.0 won't even compile
 *	in 1.1.8, so a runtime test cannot be used.
 *
 ************************************************************************/
/*	
public static TableCellRenderer getDefaultTableCellRenderer(JTable table, int column)
{
	// for old Java
//	return table.getColumnModel().getColumn(column).getHeaderRenderer();
	
	// for current Java 2.0
	return table.getTableHeader().getDefaultRenderer();
}

*/


}
