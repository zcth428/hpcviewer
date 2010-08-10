package edu.rice.cs.hpc.data.experiment.scope;

public class CallSiteScopeCallerView extends CallSiteScope {

	private Scope scopeCCT; 
	public int numChildren;
	
	/**
	 * 
	 * @param scope
	 * @param scope2
	 * @param csst
	 * @param id
	 * @param cct
	 */
	public CallSiteScopeCallerView(LineScope scope, ProcedureScope scope2,
			CallSiteScopeType csst, int id, Scope cct) {
		super(scope, scope2, csst, id, cct.getFlatIndex());

		this.scopeCCT = cct;
		numChildren = 0;
	}


	public Scope getScopeCCT() {
		return this.scopeCCT;
	}
	
	public boolean isMyCCT( Scope cct ) {
		return (this.scopeCCT == cct);
	}
}
