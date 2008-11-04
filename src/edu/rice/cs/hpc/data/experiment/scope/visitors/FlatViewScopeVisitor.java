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
	private final EmptyMetricValuePropagationFilter noFilter;
	//----------------------------------------------------
	// private data
	//----------------------------------------------------
	protected boolean isdebug = false; // true;

	protected Hashtable/*<SourceFile, FileScope>*/ fileht = new Hashtable/*<SourceFile, FileScope>*/();

	protected Hashtable/*<FileScope, Hashtable<String, ProcedureScope>>*/ fileprocht = 
		new Hashtable/*<FileScope, Hashtable<String, ProcedureScope>>*/();

	protected Hashtable/*<ProcedureScope, Hashtable<Integer, Scope>>*/ proc_to_proc_contents_ht = 
		new Hashtable/*<ProcedureScope, Hashtable<Integer, Scope>>*/();

	/**
	 * Stack to store the flat procedure scope of a cct's callsite scope
	 */
	private Stack<ProcedureScope> stackProcScope  = new Stack<ProcedureScope>();
	/**
	 * Stack to store flat file scope 
	 */
	private Stack<FileScope> stackFileScope  = new Stack<FileScope>();
	
	private Experiment exp;
	private Scope flatViewRootScope;
	private int numberOfPrimaryMetrics;
	private MetricValuePropagationFilter filter;

	private FileScope EMPTY_FILE_SCOPE = new FileScope(null, null);
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
		noFilter = new EmptyMetricValuePropagationFilter();
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

	/**
	 * Add a scope into flat view tree
	 * @param scope: the current scope from the CCT
	 * @param vt: type of visit: PREVISIT or POSTVISIT
	 * 
	 * CCT: 
	 * 	M - F - X - F - Y - F
	 * 	  - F	
	 * FT:
	 *  File
	 *   - M
	 *   - F - Y - F - X - M
	 *   	 - X - F - M
	 *   	 - M
	 *   - Y - F - X - F - M
	 *   - X - F - M
	 * 
	 * Algorithm:
	 * PreVisit:
	 * 	- encProc = getEclosingProcedure(scope)
	 *  - if (encProc is not null)
	 *  	scopeFile = getFlatFileScope(scope)
	 *  	if(scopeFile is null) then 
	 *  		create scopefile, add it into hashtable and the tree
	 *  	
	 *  	scopeProc = getFlatProcScope(scopeFile, scope)
	 *  	if(scopeProc is null) then
	 *  		create scopeProc, add it into hashtable, and the tree
	 *  
	 *  	scopeFlat = getFlatScope(scopeProc, scope)
	 *  	if scopeFlat is null then
	 *  		create scopeFlat, add it into hashtable
	 *   	
	 *   	if(scope is callsite) then
	 *   		if(scopeFlat first time appear in the tree)
	 *   			scopeFlat.cost += scope.cost
	 *   		if(scopeFlat is callsite)
	 *   			scopeFlat.lineScope += scope.lineScope.cost
	 *   	else
	 *   		scopeFlat.cost += scope.cost
	 */
	public void add(Scope scope, ScopeVisitType vt) { 
		ProcedureScope proc = findEnclosingProcedure(scope);
		// ------------- PRE VISIT 
		if (vt == ScopeVisitType.PreVisit) {
			if(proc == null) {
				// When the proc is null, it can be either the child of root scope, or an anomaly
				// for the child of the root scope, we need to include it into Flat tree and assign the cost to the scope
				if(scope instanceof ProcedureScope) {
					proc = (ProcedureScope) scope;
					// create a new scope into flat tree
					FlatFileProcedure objFlat = retrieveProcedureScope(proc.getSourceFile(), proc, scope);
					ProcedureScope flat_encl_proc = objFlat.objProc;
					// assign the cost to this scope
					flat_encl_proc.mergeMetric(scope, this.inclusiveOnly);
					flat_encl_proc.iCounter++;
					this.attributeCostToFlatFile(objFlat.objFile, scope);
				}
				return;
			}

			Scope context = scope.getParentScope();
			// -------------------------------------------------------
			// When the scope is a call site, we will assign the cost of the call site into its "inclosing" procedure
			// By doing so, (1) we avoid to recompute the total of the procedure; and (2) we have a consistent cost 
			//		compared to the caller tree view
			// -------------------------------------------------------
			// construct the flat tree of this scope
			augmentFlatView(scope, context, proc);
			
		} 
		// ------------- POST VISIT
		else if(vt == ScopeVisitType.PostVisit) {
			if(proc == null) 
				return;
			if(scope instanceof CallSiteScope) {
				try {
					ProcedureScope scopeProc = this.stackProcScope.pop();
					// we have already computed this scope, so let's decrement the counter
					scopeProc.iCounter--;
					if(scopeProc.iCounter<0) {
						trace("\tError:"+scopeProc.getName()+"\t"+scopeProc.iCounter);
					}
				} catch (java.util.EmptyStackException e) {
					trace("Empty stack for procedure ! Scope = "+scope.getName()+"\tparent="+scope.getParentScope().getName());
				}
			}
			try {
				FileScope scopeFile = this.stackFileScope.pop();
				if(scopeFile != this.EMPTY_FILE_SCOPE) {
					if(scopeFile.iCounter>0)
						scopeFile.iCounter--;
					else
						trace("ERROR: scopeFile counter is negative "+scopeFile.getName()+"\tscope:"+scope.getName());
				}
			} catch (java.util.EmptyStackException e) {
				trace("Empty stack for file ! Scope = "+scope.getName());
			}
		}
	}

	/**
	 * Iteratively finding an enclosing procedure of a CCT scope
	 * @param s
	 * @return
	 */
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

	/**
	 * Retrieve the hashtable that containing list of procedures within a file scope
	 * If the file doesn't exist in the database, return a new hashtable
	 * @param file
	 * @return
	 */
	protected Hashtable<String, ProcedureScope> getFileProcHashtable(FileScope file) {
		Hashtable<String, ProcedureScope> htProc = (Hashtable) fileprocht.get(file);
		if(htProc == null) {
			htProc = new java.util.Hashtable<String, ProcedureScope>();
			this.fileprocht.put(file, htProc);
		}
		return htProc;
	}

	/**
	 * Retrieve the flat file scope of the CCT source file
	 * If the flat file scope doesn't exist, create a new one, and attach it to flat tree
	 * @param sfile
	 * @return
	 */
	protected FileScope getFileScope(SourceFile sfile, Scope scopeCCT) {
		FileScope file = (FileScope) fileht.get(sfile);
		if (file == null) {
			file  = new FileScope(exp, sfile);
			// the first time a routine in this file has been called
			file.iCounter = 0;
			fileht.put(sfile, file);
			flatViewRootScope.addSubscope(file);
			file.setParentScope(this.flatViewRootScope);
			exp.getScopeList().addScope(file);
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
	protected FlatFileProcedure retrieveProcedureScope(SourceFile sfile, Scope procScope, Scope scopeCCT) {
		// find the file
		FileScope file = null;
		if(sfile == null) {
			trace("--- ERROR: "+procScope.getName()+" file is null");
			return null;
		} else {
			file = getFileScope(sfile, procScope);
		}
		// get the list of procedures in this file scope
		Hashtable<String, ProcedureScope> htProcCounter = getFileProcHashtable(file);
		String procName = procScope.getName();
		ProcedureScope objFlatProcScope = htProcCounter.get(procName);
		
		// if the procedure has been in our database, then we just increment the "counter"
		//  	otherwise, we insert it in the database.
		if(objFlatProcScope == null) {
			objFlatProcScope = (ProcedureScope)procScope.duplicate();
			objFlatProcScope.iCounter = 0;
			// java spec wants us to insert the kid first, then link the kid to the parent
			// an attempt to do reverse will throw an exception.... strange :-(
			file.addSubscope(objFlatProcScope);	// add into its file
			// a double linked list: we need to set the parent as well
			objFlatProcScope.setParentScope(file);
			htProcCounter.put(procName, objFlatProcScope);		// save it into our database
		} else {
			// must be a recursive routine
		}
		FlatFileProcedure objFlat = new FlatFileProcedure(file, objFlatProcScope);
		return objFlat;
	}

	/**
	 * Return the hashtable containing a list of scopes in flat tree indexed by a code (TBD)
	 * @param proc
	 * @return
	 */
	protected Hashtable/*<Integer, Scope>*/ getProcContentsHashtable(ProcedureScope proc) {
		Hashtable/*<Integer, Scope>*/ lsht = (Hashtable) proc_to_proc_contents_ht.get(proc);
		if (lsht == null) {
			lsht = new Hashtable/*<Integer,LineScope>*/();
			proc_to_proc_contents_ht.put(proc, lsht);
		}
		return lsht;
	}

	/**
	 * Assign the inclusive cost of a CCT scope into FT's procedure scope
	 * @param flat_encl_proc
	 * @param scopeCCT
	 */
	private void attributeCostToFlatProc(ProcedureScope flat_encl_proc, Scope scopeCCT) {
		flat_encl_proc.iCounter++;
		// In case of recursive procedure, we don't accumulate the cost of its descendants
		if(flat_encl_proc.iCounter == 1) {
			// partially fix bug when the procedure has nested loops.
			flat_encl_proc.accumulateMetrics(scopeCCT, this.inclusiveOnly, this.numberOfPrimaryMetrics);
		}
		// use stack to save the state. This will be used for post visit
		this.stackProcScope.push(flat_encl_proc);
	}
	
	/**
	 * Attribute the inclusive cost of a CCT scope into FT's file scope
	 * @param objFile
	 * @param scopeCCT
	 */
	private void attributeCostToFlatFile(FileScope objFile, Scope scopeCCT) {
		if(objFile != null) {
			objFile.iCounter++;
			if(objFile.iCounter == 1) {
				objFile.accumulateMetrics(scopeCCT, this.inclusiveOnly, this.numberOfPrimaryMetrics);
			}
			this.stackFileScope.push(objFile);
		}
	}

	/**
	 * Prepare the creation of flat tree.
	 * @param s
	 * @param encl_context
	 * @param encl_proc
	 * @return
	 */
	protected ProcedureScope  augmentFlatView(Scope s, Scope encl_context, ProcedureScope encl_proc) {
		FlatFileProcedure objFlat = retrieveProcedureScope(encl_proc.getSourceFile(), encl_proc, s);
		ProcedureScope flat_encl_proc = objFlat.objProc;
		
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
		
		// ----------------
		// Exclusive cost attribution by copying from CCT to FT
		if (s instanceof CallSiteScope) {
			ProcedureScope scopeProc = ( (CallSiteScope) s).getProcedureScope();
			FlatFileProcedure objFlatProc = retrieveProcedureScope(scopeProc.getSourceFile(), scopeProc, s);
			// attribute the cost of this call site into procedure level (inclusively)
			this.attributeCostToFlatProc(objFlatProc.objProc, s);
			// attribute the cost to flat scope 
			this.attributeCostToFlatFile(objFlatProc.objFile, s);
		} else {
			FileScope scopeFile = (FileScope) this.fileht.get(s.getSourceFile()); 
			if(scopeFile == null) {
				// there is no file scope for this procedure (it may be inlined ?)
				// so we put an empty object into the stack
				this.stackFileScope.push(this.EMPTY_FILE_SCOPE);
				return null;
			}
			// add the cost of this scope into the file scope
			this.attributeCostToFlatFile(scopeFile, s);
		}
		// attribute the cost of this scope into its file
		
		
		return flat_encl_proc;
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

	/**
	 * Construct a flat tree.
	 * Algorithm:
	 * 	- get the flat tree scope version of the CCT scope. 
	 * 		* If the scope doesn't exist, create a new one and attach it to the parent
	 *  - If the CCT scope is a callsite, then:
	 *  	* inclusive metric: assign the CCT scope's cost into flat scope
	 *  	* exclusive metric: assign the cost of the call into flat scope
	 *  - otherwise: assign the cost to flat scope
	 * @param s
	 * @param flat_parent
	 * @param ht
	 * @return
	 */
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
		}
		if (s instanceof CallSiteScope) {
			//---------------------------------------------------------------------------------------------------
			// in the calling context view, exclusive costs are used to show the cost at the callsite and for
			// the immediate call. for the flat view, we only want to propagate exclusive costs for the call site,
			// not the cost of the call. thus, don't propagate exclusive costs from the calling context tree to
			// the flat view; only propagate inclusive costs.
			// 2008 06 07 - John Mellor-Crummey
			//---------------------------------------------------------------------------------------------------
			//flat_s.accumulateMetrics(s, inclusiveOnly, this.numberOfPrimaryMetrics);
			// Laks 2008.10.21 bug fix: do not add "blindly" the descendant. Instead, we just compare if the kid
			//							is smaller (which is very likely) or not. If the former is the case, then assign it.
			flat_s.mergeMetric(s, inclusiveOnly);
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
	
	private class FlatFileProcedure {
		public FileScope objFile;
		public ProcedureScope objProc;
		
		public FlatFileProcedure(FileScope file, ProcedureScope proc) {
			objFile = file;
			objProc = proc;
		}
	} 
}
