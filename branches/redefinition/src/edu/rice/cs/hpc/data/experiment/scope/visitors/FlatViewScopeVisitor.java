package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.util.*;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.AlienScope;
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
import edu.rice.cs.hpc.data.experiment.scope.ScopeVisitor;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.filters.EmptyMetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.MetricValuePropagationFilter;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;

public class FlatViewScopeVisitor implements ScopeVisitor {
	private final ExclusiveOnlyMetricPropagationFilter exclusiveOnly;
	private final InclusiveOnlyMetricPropagationFilter inclusiveOnly;
	//----------------------------------------------------
	// private data
	//----------------------------------------------------
	protected boolean isdebug = false; // true;

	protected Hashtable/*<SourceFile, FileScope>*/ fileht = new Hashtable/*<SourceFile, FileScope>*/();

	protected Hashtable/*<FileScope, Hashtable<String, ProcedureScope>>*/ fileprocht = 
		new Hashtable/*<FileScope, Hashtable<String, ProcedureScope>>*/();

	protected Hashtable/*<ProcedureScope, Hashtable<Integer, Scope>>*/ proc_to_proc_contents_ht = 
		new Hashtable/*<ProcedureScope, Hashtable<Integer, Scope>>*/();

	Experiment exp;
	Scope flatViewRootScope;
	int numberOfPrimaryMetrics;
	MetricValuePropagationFilter filter;

	int iMetric=-1; // metric index
	//----------------------------------------------------
	// constructor for FlatViewScopeVisitor
	//----------------------------------------------------

	public FlatViewScopeVisitor(Experiment experiment, Scope fvrs, int nMetrics, boolean dodebug,
			MetricValuePropagationFilter filter) {
		this.flatViewRootScope = fvrs;
		this.exp = experiment;
		this.isdebug = dodebug;
		this.numberOfPrimaryMetrics = nMetrics;
		this.filter = filter;
		
		BaseMetric[] metrics = exp.getMetrics();
		exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(metrics);
		inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(metrics);
	}

	public FlatViewScopeVisitor(Experiment experiment, Scope fvrs, int nMetrics, boolean dodebug,
			MetricValuePropagationFilter filter, int iMetricIndex) {
		this.flatViewRootScope = fvrs;
		this.exp = experiment;
		this.isdebug = dodebug;
		this.numberOfPrimaryMetrics = nMetrics;
		this.filter = filter;
		
		BaseMetric[] metrics = exp.getMetrics();
		exclusiveOnly = new ExclusiveOnlyMetricPropagationFilter(metrics);
		inclusiveOnly = new InclusiveOnlyMetricPropagationFilter(metrics);
		
		this.iMetric = iMetricIndex;
	}

	//----------------------------------------------------
	// visitor pattern instantiations for each Scope type
	//----------------------------------------------------

	public void visit(Scope scope, ScopeVisitType vt) 				{ }
	public void visit(RootScope scope, ScopeVisitType vt) 			{ }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) 	{ }
	public void visit(FileScope scope, ScopeVisitType vt) 			{ }
	public void visit(AlienScope scope, ScopeVisitType vt) 			{ }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { }
	public void visit(GroupScope scope, ScopeVisitType vt) 			{ }

	public void visit(CallSiteScope scope, ScopeVisitType vt) 		{ add(scope,vt); }
	public void visit(LineScope scope, ScopeVisitType vt) 			{ add(scope,vt); }
	public void visit(LoopScope scope, ScopeVisitType vt) 			{ add(scope,vt); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) 		{ add(scope,vt); }

	//----------------------------------------------------
	// helper functions  
	//----------------------------------------------------

	public void add(Scope scope, ScopeVisitType vt) { 
		if (vt == ScopeVisitType.PreVisit) {
			ProcedureScope proc = findEnclosingProcedure(scope);
			if (proc != null) { 
				Scope context = scope.getParentScope();
				augmentFlatView(scope, context, proc);
			}
		}
	}

	public ProcedureScope findEnclosingProcedure(Scope s)
	{
		Scope parent = s.getParentScope();
		while(true) {
			if (parent instanceof CallSiteScope) {
				if (!((CallSiteScope) parent).getProcedureScope().isAlien()) break;
			}
			if (parent instanceof RootScope) return null;
			parent = parent.getParentScope();
		}
		if (parent instanceof CallSiteScope) {
			ProcedureScope proc = ((CallSiteScope) parent).getProcedureScope();
			return proc;
		}
		return null;
	}

	protected Hashtable/*<String, ProcedureScope>*/ getFileProcHashtable(FileScope file) {
		Hashtable/*<String, ProcedureScope>*/ procht = (Hashtable) fileprocht.get(file);
		if (procht == null) {
			procht = new Hashtable/*<String, ProcedureScope>*/();
			fileprocht.put(file, procht);
		}
		return procht;
	}

	protected FileScope getFileScope(SourceFile sfile) {
		FileScope file = (FileScope) fileht.get(sfile);
		if (file == null) {
			file  = new FileScope(exp, sfile);
			//file.flatten(); // LA 03.25.2008: we do not need flattening here
			// FileScopes initially flattened in the FlatView
			fileht.put(sfile, file);
			flatViewRootScope.addSubscope(file);
			file.setParentScope(this.flatViewRootScope);
			exp.getScopeList().addScope(file);
//			eb.recordOuterScope(file); NOT THIS
			trace("added file " + file.getName() + " in flat view.");
		}
		return file;
	}

	protected ProcedureScope getProcedureScope(SourceFile sfile, Scope procScope) {
		FileScope file = getFileScope(sfile);
		String procName = procScope.getName();
		Hashtable/*<String, ProcedureScope>*/ procht = getFileProcHashtable(file);
		ProcedureScope proc = (ProcedureScope) procht.get(procName);

		if (proc == null) {
			proc  = (ProcedureScope) procScope.duplicate();
			procht.put(procName, proc);

			file.addSubscope(proc);
			proc.setParentScope(file);

			trace("added procedure " + procName + " in flat view.");
		}
		return proc;
	}

	protected Hashtable/*<Integer, Scope>*/ getProcContentsHashtable(ProcedureScope proc) {
		Hashtable/*<Integer, Scope>*/ lsht = (Hashtable) proc_to_proc_contents_ht.get(proc);
		if (lsht == null) {
			lsht = new Hashtable/*<Integer,LineScope>*/();
			proc_to_proc_contents_ht.put(proc, lsht);
		}
		return lsht;
	}

	protected void  augmentFlatView(Scope s, Scope encl_context, ProcedureScope encl_proc) {
		ProcedureScope flat_encl_proc = getProcedureScope(encl_proc.getSourceFile(), encl_proc);
		Hashtable ht = getProcContentsHashtable(flat_encl_proc);
		Scope flat_encl_context;
		if (encl_context instanceof CallSiteScope) {
			CallSiteScope csp = (CallSiteScope) encl_context;
			ProcedureScope called = csp.getProcedureScope();
			if (called.isAlien()) flat_encl_context = getFlatCounterpart(encl_context, flat_encl_proc,ht);
			else if (called == encl_proc) flat_encl_context = flat_encl_proc;
			else {
			     trace("error!");
			     flat_encl_context = flat_encl_proc;
			}
		}
		else 
			flat_encl_context = getFlatCounterpart(encl_context, flat_encl_proc, ht);
		getFlatCounterpart(s, flat_encl_context, ht);
	}


	protected Scope getFlatCounterpart(Scope s, Scope flat_parent, Hashtable ht) {
	        int code = s.hashCode(); 
		//	new test code
		Scope unique = s;
		int indent = 0;
		while ((unique instanceof ProcedureScope) && (((ProcedureScope) unique).isAlien())) {
		    Scope parent = unique.getParentScope();
		    //System.err.println("code = " + code);
		    code ^= parent.hashCode();
		    int i = 0;
		    //for (i = 0; i < indent; i++) System.err.print(" "); 
		    //System.err.println("adjusting hash code by " + parent.hashCode());
		    indent++;
		    unique = parent;
		}
		Scope flat_s = (Scope) ht.get(new Integer(code));
		// -- this line is the problem -- returns scopes with and without isAlien set; multiple distinct instantiations of a callsite within a scope
		if (s instanceof LoopScope && flat_s != null && !(flat_s instanceof LoopScope)) {
			trace("error");
		}
		if (flat_s == null) {
			flat_s  =  s.duplicate();
			ht.put(new Integer(code), flat_s);

			if (flat_parent instanceof CallSiteScope && !((CallSiteScope) flat_parent).getProcedureScope().isAlien()) {
				trace("getFlatCounterpart error" + flat_s.getName());
			}

			flat_parent.addSubscope(flat_s);
			flat_s.setParentScope(flat_parent);

			trace("added flat counterpart " + flat_s.getName() + " in flat view.");
		}
		if (s instanceof CallSiteScope) {
			//---------------------------------------------------------------------------------------------------
			// in the calling context view, exclusive costs are used to show the cost at the callsite and for
			// the immediate call. for the flat view, we only want to propagate exclusive costs for the call site,
			// not the cost of the call. thus, don't propagate exclusive costs from the calling context tree to
			// the flat view; only propagate inclusive costs.
			// 2008 06 07 - John Mellor-Crummey
			//---------------------------------------------------------------------------------------------------
			flat_s.accumulateMetrics(s, inclusiveOnly, this.numberOfPrimaryMetrics);
			if (flat_s instanceof CallSiteScope) {
				//---------------------------------------------------------------------------------------------------
				// for the flat view, we only want to propagate exclusive costs for the call site,
				// not the cost of the call. thus, we propagate costs attributed to the line scope inside the 
				// CallSiteScope, which corresponds to costs attributed to the call site only, not the inclusive
				// cost of the call. do this only for CallSiteScopes.
				// 2008 06 07 - John Mellor-Crummey
				//---------------------------------------------------------------------------------------------------
				((CallSiteScope) flat_s).getLineScope().accumulateMetrics(
							((CallSiteScope) s).getLineScope(), exclusiveOnly, this.numberOfPrimaryMetrics);

			}
		} else {
			flat_s.accumulateMetrics(s, filter, this.numberOfPrimaryMetrics);
		}

		return flat_s;
	}
	
	//----------------------------------------------------
	// debugging support 
	//----------------------------------------------------

	void trace(String msg) {
		if (isdebug) System.out.println(msg);
	}
}
