package edu.rice.cs.hpc.data.experiment.scope;

/***
 * Class to generate and manage scope ID 
 * @author laksonoadhianto
 *
 */
public class ScopeID {
	// initialize the current ID with the MAX integer value
	static private int currentID = Integer.MAX_VALUE; 

	
	/**
	 * Reserve an ID and return it
	 * @return
	 */
	static public int getID() {
		ScopeID.currentID--;
		return ScopeID.currentID;
	}
}
