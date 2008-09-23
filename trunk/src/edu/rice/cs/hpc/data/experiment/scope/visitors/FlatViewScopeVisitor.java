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
		ProcedureScope proc = findEnclosingProcedure(scope);
		if(proc == null)
			return;
		// ------------- PRE VISIT 
		if (vt == ScopeVisitType.PreVisit) {
			Scope context = scope.getParentScope();
			augmentFlatView(scope, context, proc);
		} 
		// ------------- POST VISIT
		else if(vt == ScopeVisitType.PostVisit) {
			// get the FT's procedure scope
			FileScope file = getFileScope(proc.getSourceFile());
			String procName = proc.getName();
			Hashtable<String, ProcedureScope> htProcCounter = (Hashtable<String, ProcedureScope>)getFileProcHashtable(file);
			if(htProcCounter == null) 
				return;
			ProcedureScope objProcScope = htProcCounter.get(procName);
			// decrement the counter
			if(objProcScope != null) {
				objProcScope.iCounter--;
			}
			
			// get the FT's callsite scope
			Hashtable ht = getProcContentsHashtable(objProcScope);
			int code = getCode(scope);
			Scope flat_s = (Scope) ht.get(new Integer(code));
			if(flat_s != null) {
				flat_s.iCounter--;
			}
		}
	}

	public ProcedureScope findEnclosingProcedure(Scope s)
	{
		Scope parent = s.getParentScope();
		while(true) {
			if (parent instanceof CallSiteScope) {
				ProcedureScope proc = ((CallSiteScope) parent).getProcedureScope();
				if (!proc.isAlien()) return proc;
			}
			if (parent instanceof ProcedureScope) {
				ProcedureScope proc = (ProcedureScope) parent;
				if (!proc.isAlien()) return proc;
			}
			if (parent instanceof RootScope) return null;
			parent = parent.getParentScope();
		}
	}

	protected Hashtable<String, ProcedureScope> getFileProcHashtable(FileScope file) {
		Hashtable<String, ProcedureScope> htProc = (Hashtable) fileprocht.get(file);
		if(htProc == null) {
			htProc = new java.util.Hashtable<String, ProcedureScope>();
			this.fileprocht.put(file, htProc);
		}
		return htProc;

		//Hashtable/*<String, ProcedureScope>*/ procht = (Hashtable) fileprocht.get(file);
		//if (procht == null) {
			//procht = new Hashtable/*<String, ProcedureScope>*/();
			//fileprocht.put(file, procht);
		//}
		//return procht;
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

	/**
	 * Retrieve the scope of the procedure of FT that encloses a scope (line, loop, callsite, ...)
	 *  If the procedure scope doesn't exist, it will create a new one and attach it to the file scope
	 * @param sfile:	file scope
	 * @param procScope: procedure scope from CCT
	 * @param objScopeCCT: the current scope from CCT
	 * @return FT's procedure scope
	 */
	protected ProcedureScope retrieveProcedureScope(SourceFile sfile, Scope procScope, Scope objScopeCCT) {
		// find the file
		FileScope file = getFileScope(sfile);
		// get the list of procedures in this file scope
		Hashtable<String, ProcedureScope> htProcCounter = getFileProcHashtable(file);
		String procName = procScope.getName();
		ProcedureScope objFlatProcScope = htProcCounter.get(procName);
		
		// if the procedure has been in our database, then we just increment the "counter"
		//  	otherwise, we insert it in the database.
		if(objFlatProcScope == null) {
			objFlatProcScope = (ProcedureScope)procScope.duplicate();
			objFlatProcScope.iCounter = 1;
			// java spec wants us to insert the kid first, then link the kid to the parent
			// an attempt to do reverse will throw an exception.... strange :-(
			file.addSubscope(objFlatProcScope);	// add into its file
			// a double linked list: we need to set the parent as well
			objFlatProcScope.setParentScope(file);
			htProcCounter.put(procName, objFlatProcScope);		// save it into our database
		} else {
			objFlatProcScope.iCounter++;	// counting the number call instances of this procedure in this sequence
		}
		return objFlatProcScope;
	}
	/*
	protected ProcedureScope getProcedureScope(SourceFile sfile, Scope procScope, Scope objScopeCCT) {
		FileScope file = getFileScope(sfile);
		String procName = procScope.getName();
		Hashtable/*<String, ProcedureScope>*/ /*procht = getFileProcHashtable(file);
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
*/
	protected Hashtable/*<Integer, Scope>*/ getProcContentsHashtable(ProcedureScope proc) {
		Hashtable/*<Integer, Scope>*/ lsht = (Hashtable) proc_to_proc_contents_ht.get(proc);
		if (lsht == null) {
			lsht = new Hashtable/*<Integer,LineScope>*/();
			proc_to_proc_contents_ht.put(proc, lsht);
		}
		return lsht;
	}

	protected void  augmentFlatView(Scope s, Scope encl_context, ProcedureScope encl_proc) {
		//ProcedureScope flat_encl_proc = getProcedureScope(encl_proc.getSourceFile(), encl_proc, s);
		ProcedureScope flat_encl_proc = retrieveProcedureScope(encl_proc.getSourceFile(), encl_proc, s);
		Hashtable ht = getProcContentsHashtable(flat_encl_proc);
		Scope flat_encl_context;
		if (encl_context instanceof ProcedureScope && encl_proc == (ProcedureScope) encl_context) {
			flat_encl_context = flat_encl_proc;
		} else if (encl_context instanceof CallSiteScope) {
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

	/**
	 * We may need have a standardized hashcode for a scope. Currently, callers view and flat view has different
	 *  	way to compute the hashcode. For the future, we may use SID instead.
	 * @param s
	 * @return
	 */
	protected int getCode(Scope s) {
        int code = s.hashCode(); 
		Scope unique = s;
		while ((unique instanceof ProcedureScope) && (((ProcedureScope) unique).isAlien())) {
		    Scope parent = unique.getParentScope();
		    code ^= parent.hashCode();
		    unique = parent;
		}
		return code;
	}

	protected Scope getFlatCounterpart(Scope s, Scope flat_parent, Hashtable ht) {
		//	new test code
		int code = getCode(s);
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
			// laks: we need to make sure if the call is recursive or not
			flat_s.iCounter = 1;
		} else {
			// in case of callsite, this means we encounter a recursive routine
			flat_s.iCounter++; 
			if(flat_s instanceof CallSiteScope)
				((CallSiteScope) flat_s).isRecursive = true;
		}
		if (s instanceof CallSiteScope) {
			//---------------------------------------------------------------------------------------------------
			// in the calling context view, exclusive costs are used to show the cost at the callsite and for
			// the immediate call. for the flat view, we only want to propagate exclusive costs for the call site,
			// not the cost of the call. thus, don't propagate exclusive costs from the calling context tree to
			// the flat view; only propagate inclusive costs.
			// 2008 06 07 - John Mellor-Crummey
			//---------------------------------------------------------------------------------------------------
			if(flat_s.iCounter == 1)
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
