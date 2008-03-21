package edu.rice.cs.hpc.data.experiment.pnode;

import edu.rice.cs.hpc.data.experiment.Experiment;

//////////////////////////////////////////////////////////////////////////
//CLASS PNode                                                                                                            //
//////////////////////////////////////////////////////////////////////////
/**
* A profile node and its data in an HPCView experiment.
*/
public class PNode {
	/** The experiment owning this pnode. */
	protected Experiment experiment;
	
	/** The short name of this pnode, used within an experiment's XML file. */
	protected String shortName;
	
	/** The user-visible name of this pnode. */
	protected String displayName;
	
	/** The identifying information for this profile node. */
	protected String info;
	
	/** The index of this pnode in its experiment's pnode list. */
	protected int index;
	
//////////////////////////////////////////////////////////////////////////
//	INITIALIZATION                                                                                                          //
//////////////////////////////////////////////////////////////////////////
	/*************************************************************************
	 *      Creates a PNode.
	 ************************************************************************/
	public PNode(Experiment experiment,
			String shortName, String displayName, String info)
	{
		// creation arguments
		this.experiment  = experiment;
		this.shortName   = shortName;
		this.displayName = displayName;
		this.info        = info;
	}
	
	/*************************************************************************
	 *      Sets the pnode's index.
	 ************************************************************************/
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	/*************************************************************************
	 *      Returns the pnode's index.
	 ************************************************************************/
	public int getIndex()
	{
		return this.index;
	}
	
//////////////////////////////////////////////////////////////////////////
//	ACCESS TO PNode                                                                                                        //
//////////////////////////////////////////////////////////////////////////
	/*************************************************************************
	 *      Returns the pnode's short (internal) name.
	 ************************************************************************/
	public String getShortName()
	{
		return this.shortName;
	}
	
	/*************************************************************************
	 *      Returns the pnode's user-visible name.
	 ************************************************************************/
	public String getDisplayName()
	{
		return this.displayName;
	}
	
	/*************************************************************************
	 *      Returns the pnode's info string.
	 ************************************************************************/
	public String getInfo()
	{
		return this.info;
	}	

}
