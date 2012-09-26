package edu.rice.cs.hpc.data.experiment.scope;


public class RootScopeType {
	public final static RootScopeType Flat = new RootScopeType("Flat");
	public final static RootScopeType CallingContextTree = new RootScopeType("CallingContextTree");
	// Laks 2008.08.07: We need separate type of rootscope for caller-tree since the view may have
	//	different actions compared to other trees
	public final static RootScopeType CallerTree = new RootScopeType("CallerTree");
	public final static RootScopeType Invisible = new RootScopeType("Invisible");
	public String toString() { return value; }
	
	private String value;
	protected RootScopeType(String value) { this.value = value; };
}