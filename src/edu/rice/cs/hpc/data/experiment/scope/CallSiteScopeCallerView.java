package edu.rice.cs.hpc.data.experiment.scope;

public class CallSiteScopeCallerView extends CallSiteScope {

	private Scope scopeCCT; 
	
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
		super(scope, scope2, csst, id);

		this.scopeCCT = cct;
	}


	public Scope getScopeCCT() {
		return this.scopeCCT;
	}
	
	public boolean isMyCCT( Scope cct ) {
		return (this.scopeCCT == cct);
	}
}
