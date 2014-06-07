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
 * 
 * REMARK: THIS CODE IS NOT COMPATIBLE WITH OLD DATABASE !!!
 *  
 * @author laksonoadhianto
 *
 *************************************************************************************************/

public class FlatViewScopeVisitor implements IScopeVisitor {
	private Hashtable<Integer, LoadModuleScope> htFlatLoadModuleScope;
	private Hashtable<Integer, FileScope> htFlatFileScope;
	private HashMap<String, FlatScopeInfo> htFlatScope;
	private HashMap<String, Scope[]> htFlatCostAdded;
	
	private Experiment experiment;
	private RootScope root_ft;
	
	private InclusiveOnlyMetricPropagationFilter inclusive_filter;
	private ExclusiveOnlyMetricPropagationFilter exclusive_filter;
	
	//final private boolean debug = false;
	
	/******************************************************************
	 * Constructor
	 * @param exp: experiment
	 * @param root: the root of the tree
	 ******************************************************************/
	public FlatViewScopeVisitor( Experiment exp, RootScope root) {
		this.experiment = exp;
		
		this.htFlatLoadModuleScope = new Hashtable<Integer, LoadModuleScope>();
		this.htFlatFileScope = new Hashtable<Integer, FileScope>();
		this.htFlatScope     = new HashMap<String, FlatScopeInfo>();
		this.htFlatCostAdded = new HashMap<String, Scope[]>();
		
		this.root_ft = root;
		
		this.inclusive_filter = new InclusiveOnlyMetricPropagationFilter( exp );
		this.exclusive_filter = new ExclusiveOnlyMetricPropagationFilter( exp );
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
		add(scope,vt, true, true); 
	}
	public void visit(LoopScope scope, ScopeVisitType vt) 			{
		add(scope,vt, true, false); 
	}
	public void visit(ProcedureScope scope, ScopeVisitType vt) 		{
		add(scope,vt, true, false); 
	}

	
	/******************************************************************
	 * Create or add a flat scope based on the scope from CCT
	 * @param scope
	 * @param vt
	 * @param add_inclusive: flag if an inclusive cost had to be combined in flat/module scope
	 * @param add_exclusive: flag if an exclusive cost had to be combined in flat/module scope
	 ******************************************************************/
	private void add( Scope scope, ScopeVisitType vt, boolean add_inclusive, boolean add_exclusive ) {
		
		String id = this.getID(scope); 

		if (vt == ScopeVisitType.PreVisit ) {
			//--------------------------------------------------------------------------
			// Pre-visit
			//--------------------------------------------------------------------------
			Scope flat_info[] = this.htFlatCostAdded.get( id );
			if (flat_info != null) {
				this.htFlatCostAdded.remove(id);
			}
			
			FlatScopeInfo objFlat = this.getFlatCounterPart(scope, scope, id);
			
			//--------------------------------------------------------------------------
			// Aggregating metrics to load module and flat scope
			// Notes: this is not correct for Derived incremental metrics
			//--------------------------------------------------------------------------
			addCostIfNecessary(id, objFlat.flat_lm, scope, add_inclusive, add_exclusive);
			addCostIfNecessary(id, objFlat.flat_file, scope, add_inclusive, add_exclusive);

			//--------------------------------------------------------------------------
			// For call site, we need also to create its procedure scope
			//--------------------------------------------------------------------------
			if (scope instanceof CallSiteScope) {
				ProcedureScope proc_cct_s = ((CallSiteScope) scope).getProcedureScope();
				this.getFlatCounterPart(proc_cct_s, scope, id);
			}

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
	
	
	/****************************************************************************
	 * decrement scope's counter
	 * @param flat_s
	 ****************************************************************************/
	private void decrementCounter(Scope flat_s) {
		if (flat_s != null) {
			flat_s.decrementCounter();
		}
	}
	

	/****************************************************************************
	 * Get the flat counterpart of the scope cct:
	 * - check if the flat counter part already exist
	 * -- if not, create a new one
	 * - get the flat file counter part exists
	 * 
	 * @param scopeCCT
	 * @return
	 ****************************************************************************/
	private FlatScopeInfo getFlatScope( Scope cct_s ) {
		//-----------------------------------------------------------------------------
		// get the flat scope
		//-----------------------------------------------------------------------------
		String id = this.getID(cct_s);
		
		FlatScopeInfo flat_info_s = this.htFlatScope.get( id );
		
		if (flat_info_s == null) {
			boolean need_to_create_file_scope = true;
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
			} else if (cct_s instanceof ProcedureScope) {
				ProcedureScope cct_proc_s = (ProcedureScope) cct_s;
				Scope cct_enc_s = cct_s;
				//---------------------------------------------------------------------------
				// Old database: if CCT scope is a procedure, and it is an alien, then
				// 	we need to find the enclosing procedure of its parent
				//---------------------------------------------------------------------------
				if (cct_proc_s.isAlien()) {
					//need_to_create_file_scope = false;
				}
				proc_cct_s = this.findEnclosingProcedure(cct_enc_s);
			} else {
				proc_cct_s = this.findEnclosingProcedure(cct_s);
			}

			if (proc_cct_s == null) {
				throw new RuntimeException("Cannot find the enclosing procedure for " + cct_s);
			}
			
			//-----------------------------------------------------------------------------
			// Initialize the flat scope of this cct
			//-----------------------------------------------------------------------------
			flat_info_s.flat_s = cct_s.duplicate();
			
			if (need_to_create_file_scope) {
				//-----------------------------------------------------------------------------
				// Initialize the load module scope
				//-----------------------------------------------------------------------------
				flat_info_s.flat_lm = this.createFlatModuleScope(proc_cct_s);

				//-----------------------------------------------------------------------------
				// Initialize the flat file scope
				//-----------------------------------------------------------------------------
				flat_info_s.flat_file = this.createFlatFileScope(proc_cct_s, flat_info_s.flat_lm);
				
				//-----------------------------------------------------------------------------
				// Attach the scope to the file if it is a procedure
				//-----------------------------------------------------------------------------
				if (flat_info_s.flat_s instanceof ProcedureScope) {
					this.addToTree(flat_info_s.flat_file, flat_info_s.flat_s);
				}
			}

			//-----------------------------------------------------------------------------
			// save the info into hashtable
			//-----------------------------------------------------------------------------
			this.htFlatScope.put( id, flat_info_s);
		}
		
		return flat_info_s;
	}

	
	
	/*****************************************************************
	 * Create the flat view of a load module
	 * @param proc_cct_s
	 * @return
	 *****************************************************************/
	private LoadModuleScope createFlatModuleScope(ProcedureScope proc_cct_s) {
		LoadModuleScope lm = proc_cct_s.getLoadModule();
		LoadModuleScope lm_flat_s = null;
		
		// some old database do not provide load module information
		if (lm != null)  {
			lm_flat_s = this.htFlatLoadModuleScope.get(lm.hashCode());
			if (lm_flat_s == null) {
				// no load module has been created. we allocate a new one
				lm_flat_s = (LoadModuleScope) lm.duplicate();
				// attach the load module to the root scope
				this.addToTree(root_ft, lm_flat_s);
				// store this module into our dictionary
				this.htFlatLoadModuleScope.put(lm.hashCode(), lm_flat_s);
			}
		}
		return lm_flat_s;
	}
	

	/*****************************************************************
	 * Create the flat view of a file scope
	 * @param cct_s
	 * @param flat_lm
	 * @return
	 *****************************************************************/
	private FileScope createFlatFileScope(Scope cct_s, LoadModuleScope flat_lm) {
		SourceFile src_file = cct_s.getSourceFile();
		int fileID   = src_file.getFileID();
		FileScope flat_file = this.htFlatFileScope.get(fileID);
		
		//-----------------------------------------------------------------------------
		// ATTENTION: it is possible that a file can be included into more than one load module
		//-----------------------------------------------------------------------------
		if ( (flat_file == null) ){
			flat_file = this.createFileScope(src_file, flat_lm);
			
		} else {
			
			Scope parent_lm = flat_file.getParentScope();
			if (parent_lm instanceof LoadModuleScope) {
				LoadModuleScope flat_parent_lm = (LoadModuleScope) parent_lm;

				// check if the load module the existing file is the same with the scope's load module
				if (flat_parent_lm.hashCode() != flat_lm.hashCode() ) {
					// the same file in different load module scope !!!
					flat_file = this.createFileScope(src_file, flat_lm);
				}
			}

		}
		return flat_file;
	}
	
	
	/*****************************************************************
	 * Create a new file scope (this procedure will NOT check if the file already exists or not) !
	 * @param src_file
	 * @param lm_s
	 * @return
	 *****************************************************************/
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
	
	
	/*****************************************************************
	 * construct the flat view of a cct scope
	 * @param cct_s
	 * @param proc_cct_s
	 * @return
	 *****************************************************************/
	private FlatScopeInfo getFlatCounterPart( Scope cct_s, Scope cct_s_metrics, String id) {
		// -----------------------------------------------------------------------------
		// Get the flat scope of the parent 	
		// -----------------------------------------------------------------------------
		Scope cct_parent_s = cct_s.getParentScope() ;
		Scope flat_enc_s = null;

		if (cct_parent_s != null) {
			if (cct_parent_s instanceof RootScope) {
				// ----------------------------------------------
				// main procedure
				// ----------------------------------------------
				flat_enc_s = null;
			} else {
				if ( cct_parent_s instanceof CallSiteScope ) {
					// ----------------------------------------------
					// parent is a call site
					// ----------------------------------------------
					ProcedureScope proc_cct_s = ((CallSiteScope)cct_parent_s).getProcedureScope(); 
					FlatScopeInfo flat_enc_info = this.getFlatScope(proc_cct_s);
					flat_enc_s = flat_enc_info.flat_s;

				} else {					
					// ----------------------------------------------
					// parent is a line scope or loop scope or procedure scope
					// ----------------------------------------------
					FlatScopeInfo flat_enc_info = this.getFlatScope(cct_parent_s);
					flat_enc_s = flat_enc_info.flat_s;
				}

			}
		}

		FlatScopeInfo objFlat = this.getFlatScope(cct_s);

		if (flat_enc_s != null)
			this.addToTree(flat_enc_s, objFlat.flat_s);

		this.addCostIfNecessary(id, objFlat.flat_s, cct_s_metrics, true, true);
		return objFlat;
		
	}
	
	
	/***********************************************************
	 * Retrieve the ID given a scope
	 * a flat ID is the name of the class class concatenated by its hashcode
	 * This is to force to have different ID for different classes
	 * @param scope
	 * @return
	 ***********************************************************/
	private String getID( Scope scope ) {
		int id = scope.hashCode();
		String hash_id = scope.getClass().getSimpleName();
		if (hash_id != null) {
			hash_id = hash_id.substring(0, 2) + id;
		} else {
			hash_id = String.valueOf(id);
		}
		return hash_id;
	}
	
	
	/***********************************************************
	 * Add a child as the subscope of a parent
	 * @param parent
	 * @param child
	 ***********************************************************/
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
	

	/***********************************************************
	 * check if two scopes have the same "content"
	 * @param s1
	 * @param s2
	 * @return
	 ***********************************************************/
	private boolean isTheSameScope(Scope s1, Scope s2) {
		
		// are s1 and s2 the same class ?
		if ( s1.getClass() != s2.getClass() )
			return false;
		
		return (s1.hashCode() == s2.hashCode());
	}
	
	
	/***********************************************************
	 * add a child to the parent
	 * @param parent
	 * @param child
	 ***********************************************************/
	private void addChild(Scope parent, Scope child) {
		if (parent.hashCode() == child.hashCode()) 
			System.err.println("ERROR: Same ID "+parent.hashCode()+": "+parent + " - " + child);
		parent.addSubscope(child);
		child.setParentScope(parent);
	}
	
	
	/***********************************************************
	 * Iteratively finding an enclosing procedure of a CCT scope
	 * @param cct_s
	 * @return
	 ***********************************************************/
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

	
	/***********************************************************
	 * check if a scope has been assigned as the outermost instance
	 * @param scope
	 * @return
	 ***********************************************************/
	private boolean isOutermostInstance(Scope scope) {
		return scope.getCounter() == 1;
	}

	
	/***********************************************************
	 * add the cost of the cct into the flat scope if "necessary"
	 * Necessary means: add the inclusive cost if the cct scope if the outermost scope
	 * @param flat_s
	 * @param cct_s
	 ***********************************************************/
	private void addCostIfNecessary( String objCode, Scope flat_s, Scope cct_s, boolean add_inclusive, boolean add_exclusive ) {
		if (flat_s == null)
			return;
		
		flat_s.incrementCounter();
			
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
	//int iline = 0; 

	
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
		Scope flat_s;
	}
}
