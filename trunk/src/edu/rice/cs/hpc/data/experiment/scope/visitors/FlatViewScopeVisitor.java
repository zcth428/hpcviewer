package edu.rice.cs.hpc.data.experiment.scope.visitors;

import java.util.*;

import edu.rice.cs.hpc.data.experiment.Experiment;
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
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.scope.filters.ExclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.scope.filters.InclusiveOnlyMetricPropagationFilter;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;


/**
 * Class to create Flat tree based on calling context tree
 * @author laksonoadhianto
 *
 */

public class FlatViewScopeVisitor implements IScopeVisitor {
	private Hashtable<Integer, LoadModuleScope> htFlatLoadModuleScope;
	private Hashtable<Integer, FileScope> htFlatFileScope;
	private Hashtable<Integer, ProcedureScope> htFlatProcScope;
	private Hashtable<Integer, FlatScopeInfo> htFlatScope;
	private Hashtable<Integer, Scope[]> htFlatCostAdded;
	
	private Experiment experiment;
	private RootScope root_ft;
	
	private InclusiveOnlyMetricPropagationFilter inclusive_filter;
	private ExclusiveOnlyMetricPropagationFilter exclusive_filter;
	
	public FlatViewScopeVisitor( Experiment exp, RootScope root) {
		this.experiment = exp;
		
		this.htFlatLoadModuleScope = new Hashtable<Integer, LoadModuleScope>();
		this.htFlatFileScope = new Hashtable<Integer, FileScope>();
		this.htFlatProcScope = new Hashtable<Integer, ProcedureScope> ();
		this.htFlatScope     = new Hashtable<Integer, FlatScopeInfo>();
		this.htFlatCostAdded = new Hashtable<Integer, Scope[]>();
		
		this.root_ft = root;
		
		this.inclusive_filter = new InclusiveOnlyMetricPropagationFilter( exp.getMetrics() );
		this.exclusive_filter = new ExclusiveOnlyMetricPropagationFilter( exp.getMetrics() );
	}
	
	
	public void visit(Scope scope, ScopeVisitType vt) 				{ }
	public void visit(RootScope scope, ScopeVisitType vt) 			{ }
	public void visit(LoadModuleScope scope, ScopeVisitType vt) 	{ }
	public void visit(FileScope scope, ScopeVisitType vt) 			{ }
	public void visit(AlienScope scope, ScopeVisitType vt) 			{ }
	public void visit(StatementRangeScope scope, ScopeVisitType vt) { }
	public void visit(GroupScope scope, ScopeVisitType vt) 			{ }

	public void visit(CallSiteScope scope, ScopeVisitType vt) 		{ add(scope,vt, true, true); }
	public void visit(LineScope scope, ScopeVisitType vt) 			{ add(scope,vt, true, false); }
	public void visit(LoopScope scope, ScopeVisitType vt) 			{ add(scope,vt, true, false); }
	public void visit(ProcedureScope scope, ScopeVisitType vt) 		{ add(scope,vt, true, false); }

	private void add( Scope scope, ScopeVisitType vt, boolean add_file_metrics, boolean add_proc_metrics ) {
		
		if (vt == ScopeVisitType.PreVisit ) {
			//--------------------------------------------------------------------------
			// Pre-visit
			//--------------------------------------------------------------------------
			int id = scope.hashCode();
			Integer objCode = Integer.valueOf(id);
			Scope flat_info[] = this.htFlatCostAdded.get( objCode );
			if (flat_info != null) {
				this.htFlatCostAdded.remove(objCode);
			}

			FlatScopeInfo objFlat = this.getFlatCounterPart(scope);
			if (add_file_metrics) {
				objFlat.addCostIfNecessary(objFlat.flat_lm, scope);
				objFlat.addCostIfNecessary(objFlat.flat_file, scope);
			}
			if (add_proc_metrics) {
				objFlat.addCostIfNecessary(objFlat.flat_proc, scope);
			}
		} else {
			
			//--------------------------------------------------------------------------
			// Post visit
			//--------------------------------------------------------------------------
			Integer objCode = Integer.valueOf(scope.hashCode());
			Scope flat_info[] = this.htFlatCostAdded.get( objCode );
			if (flat_info != null)
				for (int i=0; i<flat_info.length; i++) {
					this.decrementCounter(flat_info[i]);
				}
		}
	}
	
	
	/****---------------------------------------------------------------------------****
	 * decrement scope's counter
	 * @param flat_s
	 ****---------------------------------------------------------------------------****/
	public void decrementCounter(Scope flat_s) {
		if (flat_s != null) {
			if (flat_s.iCounter > 0)
				flat_s.iCounter--;
			else
				System.err.println("FVSV dec counter err: " + flat_s.getName()+"\t"+flat_s.iCounter);
		}
	}
	

	/****---------------------------------------------------------------------------****
	 * Get the flat counterpart of the scope cct:
	 * - check if the flat counter part already exist
	 * -- if not, create a new one
	 * - get the flat file counter part exists
	 * 
	 * @param scopeCCT
	 * @return
	 ****---------------------------------------------------------------------------****/
	private FlatScopeInfo getFlatScope( Scope cct_s, boolean addCost ) {
		//-----------------------------------------------------------------------------
		// get the flat scope
		//-----------------------------------------------------------------------------
		int id = cct_s.hashCode();
		Integer objCode = Integer.valueOf( id );
		FlatScopeInfo flat_info_s = this.htFlatScope.get( objCode );

		if (flat_info_s == null) {
			//-----------------------------------------------------------------------------
			// Initialize the flat scope
			//-----------------------------------------------------------------------------
			flat_info_s = new FlatScopeInfo();
			
			//-----------------------------------------------------------------------------
			// finding enclosing procedure of this cct scope
			//-----------------------------------------------------------------------------
			ProcedureScope proc_cct_s = this.findEnclosingProcedure(cct_s);
			if (proc_cct_s == null) {
				assert(cct_s instanceof ProcedureScope);
				proc_cct_s = (ProcedureScope) cct_s;
			}
			
			//-----------------------------------------------------------------------------
			// Initialize the load module scope
			//-----------------------------------------------------------------------------
			LoadModuleScope lm = proc_cct_s.getLoadModule();
			// some old database do not provide load module information
			if (lm != null)  {
				flat_info_s.flat_lm = this.htFlatLoadModuleScope.get(lm.hashCode());
				if (flat_info_s.flat_lm == null) {
					// no load module has been created. we allocate a new one
					flat_info_s.flat_lm = (LoadModuleScope) lm.duplicate();
					// attach the load module to the root scope
					this.addToTree(root_ft, flat_info_s.flat_lm);
					// store this module into our dictionary
					this.htFlatLoadModuleScope.put(lm.hashCode(), flat_info_s.flat_lm);
				}
			}


			//-----------------------------------------------------------------------------
			// Initialize the flat file scope
			//-----------------------------------------------------------------------------
			SourceFile src_file = cct_s.getSourceFile();
			Integer objFileID   = src_file.getFileID();
			flat_info_s.flat_file = this.htFlatFileScope.get(objFileID);
			//-----------------------------------------------------------------------------
			// ATTENTION: it is possible that a file can be included into more than one load module
			//-----------------------------------------------------------------------------
			if ( (flat_info_s.flat_file == null) ||
					(flat_info_s.flat_file != null && flat_info_s.flat_file.getParentScope() != flat_info_s.flat_lm) ){
				flat_info_s.flat_file = new FileScope( this.experiment, src_file, objFileID );
				//------------------------------------------------------------------------------
				// if load module is undefined, then we attach the file scope to the root scope
				//------------------------------------------------------------------------------
				if (flat_info_s.flat_lm == null)
					this.addToTree(root_ft, flat_info_s.flat_file);
				else
					this.addToTree(flat_info_s.flat_lm, flat_info_s.flat_file);
				this.htFlatFileScope.put(objFileID, flat_info_s.flat_file);
			}
			
			//-----------------------------------------------------------------------------
			// Initialize the flat proc scope
			//-----------------------------------------------------------------------------
			flat_info_s.flat_proc = this.htFlatProcScope.get(proc_cct_s.hashCode());
			if (flat_info_s.flat_proc == null) {
				// create a new flat procedure scope
				flat_info_s.flat_proc = (ProcedureScope) proc_cct_s.duplicate();
				this.addToTree(flat_info_s.flat_file, flat_info_s.flat_proc);
				this.htFlatProcScope.put(proc_cct_s.hashCode(), flat_info_s.flat_proc);
			}
			
			//-----------------------------------------------------------------------------
			// Initialize the flat scope
			//-----------------------------------------------------------------------------
			if (cct_s instanceof ProcedureScope) {
				flat_info_s.flat_s = flat_info_s.flat_proc;
			} else {
				flat_info_s.flat_s = cct_s.duplicate();
			}
			
			//-----------------------------------------------------------------------------
			// save the info into hashtable
			//-----------------------------------------------------------------------------
			// flat_info_s.initCounter();
			this.htFlatScope.put(objCode, flat_info_s);
		}

		if (addCost)
			flat_info_s.addCost(cct_s);
		
		return flat_info_s;
	}

	
	/**-----------------------------------------------------------------------------------**
	 * construct the flat view of a cct scope
	 * @param cct_s
	 * @param proc_cct_s
	 * @return
	 **-----------------------------------------------------------------------------------**/
	private FlatScopeInfo getFlatCounterPart( Scope cct_s) {
		
		if (cct_s instanceof ProcedureScope) {
			// -----------------------------------------------------------------------------
			// in case of procedure scope (like main):
			//  we create the load module, file scope and proc scope, and that's all
			// -----------------------------------------------------------------------------
			boolean is_alien = ((ProcedureScope) cct_s).isAlien(); 
			FlatScopeInfo objFlat = this.getFlatScope(cct_s, true);
			// -----------------------------------------------------------------------------
			// Bug in hpcprof: it is possible to have two main procedures in a database !!!
			// mark that this is a special case: do not aggregate the children into this node.
			// -----------------------------------------------------------------------------
			if (!is_alien)
				objFlat.flat_s.iCounter++; 
			return objFlat;
			
		} else {
			// -----------------------------------------------------------------------------
			// in case of call site, line scope and loop scope:
			//	
			// -----------------------------------------------------------------------------

			Scope cct_parent_s = cct_s.getParentScope() ;
			Scope flat_enc_s = this.htFlatScope.get(cct_parent_s.hashCode()).flat_s;
			if (flat_enc_s == null) {
				System.err.println("FSV unlikely enc null: " + cct_s.getName() + " parent: " + cct_parent_s.getName());
			}
			if ( cct_parent_s instanceof ProcedureScope ) {
				// ----------------------------------------------
				// will be added to the tree directly
				// ----------------------------------------------
				assert (flat_enc_s != null);
				// if the cct parent is a procedure (s.a main() ), then do not add the child into the flat

			} else if ( cct_parent_s instanceof CallSiteScope ) {
				// ----------------------------------------------
				// parent is a call site
				// ----------------------------------------------
				ProcedureScope proc_cct_s = this.findEnclosingProcedure(cct_s); 
				CallSiteScope cct_cs  = (CallSiteScope) cct_parent_s;
				ProcedureScope cct_ps = cct_cs.getProcedureScope();
				
				if (cct_ps.isAlien()) {
					FlatScopeInfo flat_cct_ps = this.getFlatScope(cct_ps, false);
					this.addToTree(flat_enc_s, flat_cct_ps.flat_s);
					flat_enc_s = flat_cct_ps.flat_s;
				} else if (cct_ps == proc_cct_s) {
					flat_enc_s = this.getFlatScope(proc_cct_s, false).flat_proc;
				} else {
					System.err.println( "FSV: unknown callsite parent : " + cct_ps + " scope: " + cct_s);
				}
				
			} else {
				
				// ----------------------------------------------
				// parent is unknown. can be a line scope or loop scope
				// ----------------------------------------------
				FlatScopeInfo flat_enc_info = this.getFlatScope(cct_parent_s, false);
				flat_enc_s = flat_enc_info.flat_s;
			}
			boolean add_cost = !(cct_s instanceof CallSiteScope);
			FlatScopeInfo objFlat = this.getFlatScope(cct_s, add_cost);
			this.addToTree(flat_enc_s, objFlat.flat_s);
			return objFlat;
		}
		
		
	}
	
	
	/**------------------------------------------------------------------------------**
	 * Add a child as the subscope of a parent
	 * @param parent
	 * @param child
	 **------------------------------------------------------------------------------**/
	private void addToTree( Scope parent, Scope child ) {
		int nkids = parent.getSubscopeCount();
		
		//-------------------------------------------------------------------------------
		// search for the existing kids. If the kid is already added, then we don't need
		// 	to add it again
		//-------------------------------------------------------------------------------
		for (int i=0; i<nkids; i++) {
			Scope kid = parent.getSubscope(i);
			if ( this.isTheSameScope(kid, child) )
				return;
		}
		this.addChild(parent, child);
	}
	

	/**------------------------------------------------------------------------------**
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 **------------------------------------------------------------------------------**/
	private boolean isTheSameScope(Scope s1, Scope s2) {
		
		// are s1 and s2 the same class ?
		if ( s1.getClass() != s2.getClass() )
			return false;
		
		return (s1.hashCode() == s2.hashCode());
	}
	
	
	private void addChild(Scope parent, Scope child) {
		parent.addSubscope(child);
		child.setParentScope(parent);
	}
	
	
	/**------------------------------------------------------------------------------**
	 * Iteratively finding an enclosing procedure of a CCT scope
	 * @param cct_s
	 * @return
	 **------------------------------------------------------------------------------**/
	private ProcedureScope findEnclosingProcedure(Scope cct_s)
	{
		if (cct_s instanceof ProcedureScope) 
			return (ProcedureScope) cct_s;
		Scope parent = cct_s.getParentScope();
		while(parent != null) {
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
		return null;
	}

	
	/*************************************************************************
	 * Each scope in the flat view has to be linked with 3 enclosing scopes:
	 * - load module
	 * - file
	 * - procedure
	 * @author laksonoadhianto
	 *************************************************************************/
	private class FlatScopeInfo {
		LoadModuleScope flat_lm;
		FileScope flat_file;
		ProcedureScope flat_proc;
		Scope flat_s;
		

		/**------------------------------------------------------------------------------**
		 * check if a scope has been assigned as the outermost instance
		 * @param scope
		 * @return
		 **------------------------------------------------------------------------------**/
		private boolean isOutermostInstance(Scope scope) {
			return scope.iCounter == 1;
		}
		
		
		/**------------------------------------------------------------------------------**
		 * add the cost of the cct into the flat scope if necessary
		 * @param flat_s
		 * @param cct_s
		 **------------------------------------------------------------------------------**/
		public void addCostIfNecessary( Scope flat_s, Scope cct_s ) {
			if (flat_s == null)
				return;
			
			flat_s.iCounter++;
			if (isOutermostInstance(flat_s)) {
				flat_s.combine(cct_s, inclusive_filter);
			}
			flat_s.combine(cct_s, exclusive_filter);
			
			Scope arr_new_scopes[]; 
			Integer objCode = cct_s.hashCode();
			Scope scope_added[] = htFlatCostAdded.get( objCode );
			if (scope_added != null) {
				int nb_scopes = scope_added.length;
				arr_new_scopes = new Scope[nb_scopes+1];
				System.arraycopy(scope_added, 0, arr_new_scopes, 0, nb_scopes);
				arr_new_scopes[nb_scopes] = flat_s;
			} else {
				arr_new_scopes = new Scope[1];
				arr_new_scopes[0] = flat_s;
			}
			htFlatCostAdded.put(objCode, arr_new_scopes);
		}
		
		
		/**------------------------------------------------------------------------------**
		 * Add the cost of a CCT node into FT node:
		 * - load module, file and proc: intrinsic
		 * @param cct_s
		 **------------------------------------------------------------------------------**/
		public void addCost( Scope cct_s ) {
			if (flat_s != null && flat_s != flat_proc)
				this.addCostIfNecessary(flat_s, cct_s);

		}
	}
}
