package edu.rice.cs.hpc.data.experiment.scope;

import java.util.ArrayList;

/**
 * Class to provide a flexible list of nodes
 * @author laksono
 *
 */
public class ArrayOfNodes extends ArrayList<Scope.Node> {
	int iLevel;
	public ArrayOfNodes(int iRank) {
		super();
		this.iLevel = iRank;
	}

}
