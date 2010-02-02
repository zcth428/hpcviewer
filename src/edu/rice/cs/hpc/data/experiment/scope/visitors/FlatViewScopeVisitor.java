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


/*************************************************************************************************
 * Class to create Flat tree based on calling context tree
 * @author laksonoadhianto
 *
 *************************************************************************************************/

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

	public void visit(CallSiteScope scope, ScopeVisitType vt) 		{ 
		add(scope,vt, true, false); 
	}
	public void visit(LineScope scope, ScopeVisitType vt) 			{ 
		boolean to_be_added = !(scope.getParentScope() instanceof LoopScope);
		add(scope,vt, to_be_added, to_be_added); 
	}
	public void visit(LoopScope scope, ScopeVisitType vt) 			{
		boolean add_inclusive = !(scope.getParentScope() instanceof LoopScope);
		add(scope,vt, add_inclusive, true); 
	}
	public void visit(ProcedureScope scope, ScopeVisitType vt) 		{ add(scope,vt, true, true); }

	
	/****---------------------------------------------------------------------------------------------****
	 * Create or add a flat scope based on the scope from CCT
	 * @param scope
	 * @param vt
	 * @param add_inclusive
	 * @param add_exclusive
	 ****---------------------------------------------------------------------------------------------****/
	private void add( Scope scope, ScopeVisitType vt, boolean add_inclusive, boolean add_exclusive ) {
		
		int id = this.getID(scope); 
		//Integer objCode = Integer.valueOf(id);

		if (vt == ScopeVisitType.PreVisit ) {
			//--------------------------------------------------------------------------
			// Pre-visit
			//--------------------------------------------------------------------------
			Scope flat_info[] = this.htFlatCostAdded.get( id );
			if (flat_info != null) {
				this.htFlatCostAdded.remove(id);
			}

			FlatScopeInfo objFlat = this.getFlatCounterPart(scope, id);
			
			addCostIfNecessary(objFlat.flat_lm, scope, add_inclusive, add_exclusive);
			addCostIfNecessary(objFlat.flat_file, scope, add_inclusive, add_exclusive);
			addCostIfNecessary(objFlat.flat_proc, scope, add_inclusive, add_exclusive);

		} else {
			
			//--------------------------------------------------------------------------
			// Post visit
			//--------------------------------------------------------------------------
			Scope flat_info[] = this.htFlatCostAdded.get( id );
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
	private void decrementCounter(Scope flat_s) {
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
	private FlatScopeInfo getFlatScope( Scope cct_s ) {
		//-----------------------------------------------------------------------------
		// get the flat scope
		//-----------------------------------------------------------------------------
		int id = this.getID(cct_s);

		//Integer objCode = Integer.valueOf( id );
		FlatScopeInfo flat_info_s = this.htFlatScope.get( id );
		
		if (flat_info_s == null) {
			//-----------------------------------------------------------------------------
			// Initialize the flat scope
			//-----------------------------------------------------------------------------
			flat_info_s = new FlatScopeInfo();
			
			//-----------------------------------------------------------------------------
			// finding enclosing procedure of this cct scope:
			// if it is a call site, then the file and the module can be found in the scope
			// for others, we need to find the enclosing procedure iteratively
			//-----------------------------------------------------------------------------
			ProcedureScope proc_cct_s;
			if (cct_s instanceof CallSiteScope) {
				proc_cct_s = ((CallSiteScope)cct_s).getProcedureScope();
			} else {
				proc_cct_s = this.findEnclosingProcedure(cct_s);
			}
			if (proc_cct_s == null) {
				throw new RuntimeException("Cannot find the enclosing procedure for " + cct_s);
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
			int fileID   = src_file.getFileID();
			flat_info_s.flat_file = this.htFlatFileScope.get(fileID);
			//-----------------------------------------------------------------------------
			// ATTENTION: it is possible that a file can be included into more than one load module
			//-----------------------------------------------------------------------------
			if ( (flat_info_s.flat_file == null) ){
				flat_info_s.flat_file = this.createFileScope(src_file, flat_info_s.flat_lm);
				
			} else {
				
				LoadModuleScope flat_parent_lm = (LoadModuleScope) flat_info_s.flat_file.getParentScope();
				if (flat_parent_lm == null) {
					// this will be very unlikely, unless we have bugs
					throw new RuntimeException("Flat view creation: " + flat_info_s.flat_file+"\t CCT: " + cct_s);					
				}
				// check if the load module the existing file is the same with the scope's load module
				if (flat_parent_lm.hashCode() != flat_info_s.flat_lm.hashCode() ) {
					// the same file in different load module scope !!!
					flat_info_s.flat_file = this.createFileScope(src_file, flat_info_s.flat_lm);
				}

			}
			
			//-----------------------------------------------------------------------------
			// Initialize the flat proc scope
			//-----------------------------------------------------------------------------
			if (cct_s instanceof CallSiteScope)
				// we need to know which procedure that calls this subroutine
				proc_cct_s = ((CallSiteScope)cct_s).getProcedureScope();
			
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
			this.htFlatScope.put(id, flat_info_s);
		}
		
		return flat_info_s;
	}


	/**-----------------------------------------------------------------------------------**
	 * Create a new file scope (this procedure will NOT check if the file already exists or not) !
	 * @param src_file
	 * @param lm_s
	 * @return
	 **-----------------------------------------------------------------------------------**/
	private FileScope createFileScope(SourceFile src_file, LoadModuleScope lm_s) {
		int fileID = src_file.getFileID();
		FileScope file_s =  new FileScope( this.experiment, src_file, fileID );
		//------------------------------------------------------------------------------
		// if load module is undefined, then we attach the file scope to the root scope
		//------------------------------------------------------------------------------
		if (lm_s == null)
			this.addToTree(root_ft, file_s);
		else
			this.addToTree(lm_s, file_s);
		this.htFlatFileScope.put(fileID, file_s);

		return file_s;
	}
	
	
	/**-----------------------------------------------------------------------------------**
	 * construct the flat view of a cct scope
	 * @param cct_s
	 * @param proc_cct_s
	 * @return
	 **-----------------------------------------------------------------------------------**/
	private FlatScopeInfo getFlatCounterPart( Scope cct_s, int id) {
		
		if (cct_s instanceof ProcedureScope) {
			// -----------------------------------------------------------------------------
			// in case of procedure scope (like main):
			//  we create the load module, file scope and proc scope, and that's all
			// -----------------------------------------------------------------------------
			boolean is_alien = ((ProcedureScope) cct_s).isAlien(); 
			FlatScopeInfo objFlat = this.getFlatScope(cct_s);
			// -----------------------------------------------------------------------------
			// Bug in hpcprof: it is possible to have two main procedures in a database !!!
			// mark that this is a special case: do not aggregate the children into this node.
			// -----------------------------------------------------------------------------
			if (is_alien)
				objFlat.flat_s.iCounter++; 
			return objFlat;
			
		} else {
			// -----------------------------------------------------------------------------
			// in case of call site, line scope and loop scope:
			//	
			// -----------------------------------------------------------------------------

			Scope cct_parent_s = cct_s.getParentScope() ;
			int parent_id = this.getID(cct_parent_s);
			FlatScopeInfo objInfo = this.htFlatScope.get(parent_id);
			if (objInfo == null) {
				throw new RuntimeException("Flat view: unable to find the scope for " + cct_parent_s);
			}
			
			Scope flat_enc_s = objInfo.flat_s;
			if (flat_enc_s == null) {
				throw new RuntimeException("FSV unlikely enc null: " + cct_s.getName() + " parent: " + cct_parent_s.getName());
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
					FlatScopeInfo flat_cct_ps = this.getFlatScope(cct_ps);
					this.addToTree(flat_enc_s, flat_cct_ps.flat_s);
					flat_enc_s = flat_cct_ps.flat_s;
				} else if (cct_ps == proc_cct_s) {
					flat_enc_s = this.getFlatScope(proc_cct_s).flat_proc;
				} else {
					System.err.println( "FSV: unknown callsite parent : " + cct_ps + " scope: " + cct_s);
				}
				
			} else {
				
				// ----------------------------------------------
				// parent is unknown. can be a line scope or loop scope
				// ----------------------------------------------
				FlatScopeInfo flat_enc_info = this.getFlatScope(cct_parent_s);
				flat_enc_s = flat_enc_info.flat_s;
			}
			FlatScopeInfo objFlat = this.getFlatScope(cct_s);
			this.addToTree(flat_enc_s, objFlat.flat_s);

			//---------------------------------------------------------------------------------------------------
			// in the calling context view, exclusive costs are used to show the cost at the callsite and for
			// the immediate call. for the flat view, we only want to propagate exclusive costs for the call site,
			// not the cost of the call. thus, don't propagate exclusive costs from the calling context tree to
			// the flat view; only propagate inclusive costs.
			// 2008 06 07 - John Mellor-Crummey
			//---------------------------------------------------------------------------------------------------
			boolean add_exclusive = true;
			if ((objFlat.flat_s instanceof CallSiteScope) && (cct_s instanceof CallSiteScope))
				add_exclusive = false;

			this.addCostIfNecessary(objFlat.flat_s, cct_s, true, add_exclusive);
			return objFlat;
		}
		
		
	}
	
	
	/**------------------------------------------------------------------------------**
	 * Retrieve the ID given a scope
	 * @param scope
	 * @return
	 **------------------------------------------------------------------------------**/
	private int getID( Scope scope ) {
		int id = scope.hashCode();

		return id;
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
	 * check if two scopes have the same "content"
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
	
	
	/**------------------------------------------------------------------------------**
	 * add a child to the parent
	 * @param parent
	 * @param child
	 **------------------------------------------------------------------------------**/
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

	
	/**------------------------------------------------------------------------------**
	 * check if a scope has been assigned as the outermost instance
	 * @param scope
	 * @return
	 **------------------------------------------------------------------------------**/
	private boolean isOutermostInstance(Scope scope) {
		return scope.iCounter == 1;
	}

	
	/**------------------------------------------------------------------------------**
	 * add the cost of the cct into the flat scope if "necessary"
	 * Necessary means: add the inclusive cost if the cct scope if the outermost scope
	 * @param flat_s
	 * @param cct_s
	 **------------------------------------------------------------------------------**/
	private void addCostIfNecessary( Scope flat_s, Scope cct_s, boolean add_inclusive, boolean add_exclusive ) {
		if (flat_s == null)
			return;
		
		flat_s.iCounter++;
		if (isOutermostInstance(flat_s)) {
			if (add_inclusive)
				flat_s.combine(cct_s, inclusive_filter);
		}
		if (add_exclusive)
			flat_s.combine(cct_s, exclusive_filter);
		
		//-----------------------------------------------------------------------
		// store the flat scopes that have been updated  
		//-----------------------------------------------------------------------
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
	}
}
