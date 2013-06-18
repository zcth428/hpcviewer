package edu.rice.cs.hpc.data.experiment.scope.visitors;

import edu.rice.cs.hpc.data.experiment.metric.AggregateMetric;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.GroupScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitType;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;

public abstract class AbstractFinalizeMetricVisitor implements IScopeVisitor {
	private BaseMetric metrics[];

	public AbstractFinalizeMetricVisitor(BaseMetric _metrics[]) {
		this.metrics = _metrics;
	}
	
    public void visit(LineScope scope, ScopeVisitType vt) { process(scope, vt); }
    public void visit(StatementRangeScope scope, ScopeVisitType vt)  {process(scope, vt); }
    public void visit(LoopScope scope, ScopeVisitType vt)  { process(scope, vt);}
    public void visit(CallSiteScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(ProcedureScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(FileScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(GroupScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(LoadModuleScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(RootScope scope, ScopeVisitType vt)  { process(scope, vt); }
    public void visit(Scope scope, ScopeVisitType vt)  { process(scope, vt); }
    

    
    private void process( Scope scope, ScopeVisitType vt) {
    	if (vt == ScopeVisitType.PreVisit ) {
    		this.setValue(scope);
    	}
    }

    
    protected void setValue_internal ( Scope scope) {   	
    	for (int i=0; i<this.metrics.length; i++ ) {
    		if (metrics[i] instanceof AggregateMetric) {
    			AggregateMetric agg = (AggregateMetric) metrics[i];
    			agg.finalize(scope);
    		}
    	}
    }
    
    protected abstract void setValue( Scope scope );

}
