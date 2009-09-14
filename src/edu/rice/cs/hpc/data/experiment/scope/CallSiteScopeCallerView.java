package edu.rice.cs.hpc.data.experiment.scope;

public class CallSiteScopeCallerView extends CallSiteScope {

	private Scope scopeCCT; 
	public CallSiteScopeCallerView(LineScope scope, ProcedureScope scope2,
			CallSiteScopeType csst, int id, Scope cct) {
		super(scope, scope2, csst, id);

		this.scopeCCT = cct;
	}

	public Scope getScopeCCT() {
		return this.scopeCCT;
	}
}
