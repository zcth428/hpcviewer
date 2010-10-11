package edu.rice.cs.hpc.data.experiment.scope;

import edu.rice.cs.hpc.data.experiment.Experiment;

public class EmptyScope extends Scope {

	public EmptyScope(Experiment experiment) {
		super(experiment);
	}

	public Scope duplicate() {
		return null;
	}

	public String getName() {
		return null;
	}

}
