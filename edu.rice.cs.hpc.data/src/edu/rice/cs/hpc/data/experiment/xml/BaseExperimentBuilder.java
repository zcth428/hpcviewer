package edu.rice.cs.hpc.data.experiment.xml;


import java.io.File;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import edu.rice.cs.hpc.data.experiment.BaseExperiment;
import edu.rice.cs.hpc.data.experiment.ExperimentConfiguration;
import edu.rice.cs.hpc.data.experiment.extdata.TraceAttribute;
import edu.rice.cs.hpc.data.experiment.scope.AlienScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpc.data.experiment.scope.FileScope;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoadModuleScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.StatementRangeScope;
import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.experiment.xml.Token2.TokenXML;
import edu.rice.cs.hpc.data.util.Dialogs;
import edu.rice.cs.hpc.data.util.IUserData;


/****
 * 
 * Base class to build experiment class without metrics associated
 * The base class is ideal for using CCT only like hpctraceviewer
 * 
 * @see ExperimentBuilder2 for building experiment with metrics
 */
public class BaseExperimentBuilder extends Builder {

	
	protected final static String LINE_ATTRIBUTE			= "l";
	protected final static String NAME_ATTRIBUTE 			= "n";
	protected final static String FILENAME_ATTRIBUTE 		= "f";
	protected final static String VALUE_ATTRIBUTE 		= "v";
	protected final static String ID_ATTRIBUTE 		= "i";
	
	private final static String PROCEDURE_UNKNOWN = "unknown procedure";

	/** The default name for the experiment, in case none is found by the parser. */
	protected String defaultName;

	/** The parsed configuration. */
	protected ExperimentConfiguration configuration;

	protected Scope rootScope;
	protected Scope viewRootScope[];

	/** The experiment to own parsed objects. */
	protected BaseExperiment experiment;

	/** A stack to keep track of scope nesting while parsing. */
	protected Stack<Scope> scopeStack;

	/** The current source file while parsing. */
	protected Stack<SourceFile> srcFileStack;

	private boolean csviewer;
	
	final private IUserData<String, String> userData;

	protected Token2.TokenXML previousToken = TokenXML.T_INVALID_ELEMENT_NAME;
	protected Token2.TokenXML elemInfoState = TokenXML.T_INVALID_ELEMENT_NAME;

	//--------------------------------------------------------------------------------------
	private HashMap<Integer, String> hashProcedureTable;
	private HashMap<Integer, LoadModuleScope> hashLoadModuleTable;
	private HashMap <Integer, SourceFile> hashSourceFileTable;
	private HashMap<Integer, Scope> hashCallSiteTable;


	private int current_cs_id = Integer.MAX_VALUE - 1;

	//=============================================================
	
	/*************************************************************************
	 *	Creates a ExperimentBuilder.
	 *
	 *	Parsed objects are added to the experiment as soon as possible, but
	 *	all the parsed objects of a given kind must be added at once. The
	 *	builder keeps parsed objects on lists until a whole set can be added
	 *	to the experiment.
	 *	<p>
	 *	Because of the way <code>Metric</code>s are implemented, the metric
	 *	objects must be added to the experiment before any scope objects are
	 *	constructed. Scopes need to know how many metrics the experiment has,
	 *	and find out by asking the experiment.
	 *
	 *  @param experiment: experiment class
	 *  @param defaultName: the default name of the experiment
	 *
	 ************************************************************************/
	public BaseExperimentBuilder(BaseExperiment experiment, String defaultName, IUserData<String, String> userData) {
		super();
		this.csviewer = false;
		viewRootScope = new RootScope[3];
		
		hashProcedureTable = new HashMap<Integer, String>();
		hashLoadModuleTable = new HashMap<Integer, LoadModuleScope>();
		new HashMap<Integer, Scope>();
		hashSourceFileTable = new HashMap<Integer, SourceFile>();
		hashCallSiteTable = new HashMap<Integer, Scope>();

		// parse action data structures
		this.scopeStack = new Stack<Scope>();
		this.srcFileStack = new Stack<SourceFile>();
		this.srcFileStack.push(null); // mimic old behavior

		// creation arguments
		this.experiment = experiment;
		this.defaultName = defaultName;
		
		this.userData = userData;
	}
	
	
	protected boolean isCallingContextTree() {
		return this.csviewer;
	}


	/*************************************************************************
	 *	Takes notice of the beginning of an element.
	 * @throws OldXMLFormatException 
	 ************************************************************************/

	public void beginElement(String element, String[] attributes, String[] values) 
	{
		TokenXML current = Token2.map(element);

		switch(current)
		{
		case T_HPCTOOLKIT_EXPERIMENT:
			this.do_HPCTOOLKIT(attributes, values);
			break;
		case T_HEADER:
			this.do_Header(attributes,values);	
			break;
		case T_INFO:
			this.do_Info();
			break;

		case T_SEC_CALLPATH_PROFILE:
			// we need to retrieve the profile name and the ID
			this.csviewer = true;
			this.do_TITLE(attributes, values);
			break;

		case T_SEC_FLAT_PROFILE:
			this.csviewer = false;
			this.do_TITLE(attributes, values);
			break;

			// PGM elements
		case T_SEC_FLAT_PROFILE_DATA:
		case T_CALLPATH_PROFILE_DATA:	// semi old format. some data has this kind of tag
		case T_SEC_CALLPATH_PROFILE_DATA:
			this.begin_SecData(attributes, values);	break;

			// load module dictionary
		case T_LOAD_MODULE:
			this.do_LoadModule(attributes, values);
			break;
			// file dictionary
		case T_FILE:
			this.do_File(attributes, values); break;
			
			// flat profiles
		case T_LM:
			this.begin_LM (attributes, values);	break;
		case T_F:
			this.begin_F  (attributes, values);	break;
		case T_P:
			
		case T_PR:
		case T_PF:
			this.begin_PF  (attributes, values);	break;
		case T_A:
			this.begin_A  (attributes, values);	break;
		case T_L:
			this.begin_L  (attributes, values);	break;
		case T_S:
			this.begin_S  (attributes, values);	break;

			// callstack elements
		case T_C:
			this.begin_CALLSITE(attributes,values); 
			break;
			
		case T_PROCEDURE:
			this.do_Procedure(attributes, values); break;


			// trace database
		case T_TRACE_DB_TABLE:
			this.begin_TraceDBTable(attributes, values);
			break;
			
		case T_TRACE_DB:
			this.do_TraceDB(attributes, values); break;
			
			// old token from old XML
		case T_CSPROFILE:
		case T_HPCVIEWER:
			throw new java.lang.RuntimeException(new OldXMLFormatException());
			// unknown elements
			
		// ---------------------
		// Tokens to be ignored 
		// ---------------------
			
		case T_PROCEDURE_TABLE:
		case T_FILE_TABLE:
		case T_LOAD_MODULE_TABLE:
		case T_SEC_HEADER:
			break;
		
		default:
			break;
		} 
		saveTokenContext(current);
	}


	/****
	 * all children requires to register the current context
	 * 
	 * @param current
	 */
	protected void saveTokenContext(TokenXML current) 
	{
		// laks: preserve the state of the current token for the next parsing state
		this.previousToken = current;
	}
	
	/*************************************************************************
	 *	Takes notice of the ending of an element.
	 ************************************************************************/
	public void endElement(String element)
	{
		TokenXML current = Token2.map(element);
		switch(current)
		{
		case T_SEC_FLAT_PROFILE:
		case T_SEC_CALLPATH_PROFILE:
			break;

		// Data elements
		case T_CALLPATH_PROFILE_DATA:	// @deprecated: semi old format. some data has this kind of tag
		case T_SEC_FLAT_PROFILE_DATA:
		case T_SEC_CALLPATH_PROFILE_DATA:
			this.end_PGM();
			break;
		case T_LM:
			this.end_LM();
			break;
			
		case T_P:
		case T_PR:
		case T_PF:
			this.end_PF();
			break;
		case T_A:
			this.end_A();
			break;
		case T_L:
			this.end_L();
			break;
		case T_S:
			this.end_S();
			break;
		case T_C: 		
			this.end_CALLSITE();
			break;
		case T_F:
			this.end_F();
			break;
			
		case T_TRACE_DB_TABLE:
			this.end_TraceDBTable();
			break;
			

			// ignored elements
			// trace database
		case T_TRACE_DB:
		case T_METRIC_RAW:
		case T_M:
		case T_HPCTOOLKIT_EXPERIMENT:
		case T_NAME_VALUE:
		case T_HEADER:
		case T_INFO:
		case T_METRIC_FORMULA:
		case T_SEC_HEADER:
		case T_METRIC:
		case T_PROCEDURE_TABLE:
		case T_PROCEDURE:
		case T_FILE_TABLE:
		case T_FILE:
		case T_LOAD_MODULE_TABLE:
		case T_LOAD_MODULE:
			break;
		} 
	}


//	------------------------------- BUILDING		---------------------------//

	/*************************************************************************
	 * Process a HPCToolkitExperiment
	 *************************************************************************/
	private void do_HPCTOOLKIT(String[] attributes, String[] values) {
		String version = null;
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i].charAt(0) == 'v') {
				//version of the database
				version = values[i];
			}
		}
		this.experiment.setVersion(version);
	}


	/*************************************************************************
	 *      Processes a TARGET element as TITLE.
	 ************************************************************************/

	private void do_Header(String[] attributes, String[] values)
	{
		this.configuration.setName(values[0]);
	}

	private void do_Info() {
		this.elemInfoState = this.previousToken;
	}

	/*************************************************************************
	 *	Processes a TITLE element.
	 ************************************************************************/

	private void do_TITLE(String[] attributes, String[] values)
	{
		// <!ATTLIST SecCallPathProfile
		//        i CDATA #REQUIRED
		//        n CDATA #REQUIRED>
		this.Assert(attributes.length == 2);
		String sTitle = "";
		if(values.length == 2) {
			sTitle = values[1];
		}
		this.configuration.setName(sTitle);
	}

	/************************************************************************
	 * <!ELEMENT LoadModule (Info?)>
    <!ATTLIST LoadModule
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 * Example:
	 *   <LoadModule i="43497" n="/lib64/libc-2.7.so"/>
	 * @param attributes
	 * @param values
	 ************************************************************************/
	private void do_LoadModule(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		
		// We assume that the 1st attribute is always the ID and the 2nd attribute is the value
		String sValue = values[1];
		String sID = values[0];
		try {
			Integer objID = new Integer(sID);
			LoadModuleScope lmScope = new LoadModuleScope(this.experiment, sValue, null, objID.intValue());
			this.hashLoadModuleTable.put(objID, lmScope);
		} catch (java.lang.NumberFormatException e) {
			System.err.println("Incorrect load module ID: "+sID);
		} catch (java.lang.NullPointerException e) {
			System.err.println("load module table is empty. Key: "+sID+" value: "+sValue);
		}
	}



	/*************************************************************************
	 *      Processes a FILE.
	 *          <!ELEMENT File (Info?)>
    <!ATTLIST File
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 ************************************************************************/
	private void do_File(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		String sID = values[0];		
		try {
			final Integer objFileID = Integer.parseInt(sID);
			// just in case if there is a duplicate key in the dictionary, we need to make a test
			final SourceFile sourceFile = this.getOrCreateSourceFile(values[1], objFileID.intValue());
			
			this.hashSourceFileTable.put(objFileID, sourceFile);
			
		} catch (Exception e) {
			
		}
	}

	
	/*************************************************************************
	 *	Begins processing an LM (load module) element.
	 *	<!ATTLIST LM
                i CDATA #IMPLIED
                n CDATA #REQUIRED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_LM(String[] attributes, String[] values)
	{
		// LM n="load module name"
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		String sIndex = getAttributeByName(ID_ATTRIBUTE, attributes, values);
		
		try {
			Integer objIndex = Integer.parseInt(sIndex);
			SourceFile sourceFile = this.getOrCreateSourceFile(name, objIndex.intValue());
			Scope lmScope = new LoadModuleScope(this.experiment, name, sourceFile, objIndex.intValue());
			// make a new load module scope object
			this.beginScope(lmScope);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*************************************************************************
	 *	Begins processing an F (file) element.
	 *      <!ATTLIST F
                i CDATA #IMPLIED
                n CDATA #REQUIRED>
	 ************************************************************************/
	private void begin_F(String[] attributes, String[] values)

	{
		// F n="filename"
		String inode = getAttributeByName(ID_ATTRIBUTE, attributes, values);
		try {
			Integer objFileKey = Integer.parseInt(inode);
			// make a new file scope object
			SourceFile sourceFile  = this.getOrCreateSourceFile(getAttributeByName(NAME_ATTRIBUTE, attributes, values), 
					objFileKey.intValue());

			this.srcFileStack.push(sourceFile);
			Scope fileScope = new FileScope(this.experiment, sourceFile, objFileKey.intValue());

			this.beginScope(fileScope);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

	}


	/*************************************************************************
	 *	Begins processing a PF (procedure frame) element.
	 *       <!ATTLIST Pr
                i  CDATA #IMPLIED
                s  CDATA #IMPLIED
                n  CDATA #REQUIRED
                lm CDATA #IMPLIED
                f  CDATA #IMPLIED
                l  CDATA #IMPLIED
                a  (1|0) "0"
                v  CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_PF(String[] attributes, String[] values)
	{
			boolean istext = true; 
			boolean isalien = false; 
			boolean new_cct_format = false;
			int cct_id = 0, flat_id = 0;
			int firstLn = 0, lastLn = 0;
			SourceFile srcFile = null; // file location of this procedure
			
			LoadModuleScope objLoadModule = null;
			String sProcName = PROCEDURE_UNKNOWN;

			for(int i=0; i<attributes.length; i++) {
				if (attributes[i].equals("s")) { 
					// new database format: s is the flat ID of the procedure
					sProcName = this.getProcedureName(values[i]);
					flat_id = Integer.valueOf(values[i]);
					if (!new_cct_format)
						// old format: cct ID = flat ID
						cct_id = flat_id;
					
				} else if (attributes[i].equals(ID_ATTRIBUTE)) {
					// id of the proc frame. needs to cross ref
					cct_id = Integer.parseInt(values[i]); 
					new_cct_format = true;
					
				} else if(attributes[i].equals(FILENAME_ATTRIBUTE)) {
					// file
					istext = true;
					try {
						Integer indexFile = Integer.parseInt(values[i]);
						srcFile = this.hashSourceFileTable.get(indexFile);
					} catch (java.lang.NumberFormatException e) {
						// in this case, either the value of "f" is invalid or it is the name of the file
						// In some old format the attribute f contains the file not in the dictionary. So 
						// 	we need to create it from here
						if (this.srcFileStack.size()==1) {
							// the first stack is null, so let start from number 1
							srcFile = this.getOrCreateSourceFile(values[i], this.srcFileStack.size()+1);
						}
					}
					
				} else if(attributes[i].equals("lm")) { 
					// load module
					try {
						// let see if the value of ln is an ID or a simple load module name
						Integer indexFile = Integer.parseInt(values[i]);
						// look at the dictionary for the name of the load module
						objLoadModule = this.hashLoadModuleTable.get(indexFile);
						if (objLoadModule == null) {
							objLoadModule = new LoadModuleScope(this.experiment, values[i], null, indexFile.intValue());
							this.hashLoadModuleTable.put(indexFile, objLoadModule);
						}
					} catch (java.lang.NumberFormatException e) {
						// this error means that the lm is not based on dictionary
						objLoadModule = new LoadModuleScope(this.experiment, values[i], null, values[i].hashCode());
					}
				} else if (attributes[i].equals("p") ) {
					// obsolete format: p is the name of the procedure
					sProcName = values[i];
					
				} else if(attributes[i].equals("n")) {
					// new database format: n is the flat ID of the procedure
					sProcName = this.getProcedureName(values[i]);
					
				} else if(attributes[i].equals("l")) {
					// line number (or range)
					StatementRange objRange = new StatementRange(values[i]);
					firstLn = objRange.getFirstLine();
					lastLn = objRange.getLastLine();
					
				} else if(attributes[i].equals("a")) { 
					// alien
					if (values[i].equals("1")) {
						isalien = true;
					}
				} else if(attributes[i].equals("v")) {
				}
			}
			
			if (isalien) {
				flat_id = Integer.MAX_VALUE ^ flat_id;

				if (sProcName.isEmpty()) {
					// this is a line scope
					Scope scope;
					if (firstLn == lastLn)
						scope = new LineScope(this.experiment, srcFile, firstLn-1, cct_id, flat_id);
					else
						scope = new StatementRangeScope(this.experiment, srcFile, 
								firstLn-1, lastLn-1, cct_id, flat_id);
					scope.setCpid(0);
					this.beginScope_internal(scope, false);
					srcFile.setIsText(istext);
					this.srcFileStack.add(srcFile);
					return;
				} else {
					// this is a procedure scope uses the handling below
				}
			}

			// FLAT PROFILE: we retrieve the source file from the previous tag
			if(srcFile == null) {
					srcFile = this.srcFileStack.peek();
			} 
			 
			srcFile.setIsText(istext);
			this.srcFileStack.add(srcFile);

			ProcedureScope procScope  = new ProcedureScope(this.experiment, objLoadModule, srcFile, 
					firstLn-1, lastLn-1, 
					sProcName, isalien, cct_id, flat_id, userData);

			if ( (this.scopeStack.size()>1) && ( this.scopeStack.peek() instanceof LineScope)  ) {

				LineScope ls = (LineScope)this.scopeStack.pop();
				
				//-----------------------------------------------------------------------------------------
				// In some database (especially the old ones), they have the same ID for different call sites
				// In order to keep compatibility, we need to generate our own ID hoping it doesn't interfere
				// with the ID generated by the new hpcprof :-(
				//-----------------------------------------------------------------------------------------
				int	callsiteID = this.getCallSiteID(ls, procScope);  
				CallSiteScope csn = new CallSiteScope(ls, procScope, 
						CallSiteScopeType.CALL_TO_PROCEDURE, cct_id, callsiteID);

				// beginScope pushes csn onto the node stack and connects it with its parent
				// this is done while the ls is off the stack so the parent of csn is ls's parent. 
				// afterward, we rearrange the top of stack to tuck ls back underneath csn in case it is 
				// needed for a subsequent procedure frame that is a sibling of csn in the tree.
				this.beginScope(csn);
				CallSiteScope csn2 = (CallSiteScope) this.scopeStack.pop();
				this.scopeStack.push(ls);
				this.scopeStack.push(csn2);

			} else {
				this.beginScope(procScope);
			}
	}

	/*************************************************************************
	 * Retrieve the ID of a call site.
	 * Instead of using hpcprof's flat index, we reconstruct our own ID 
	 *  (not sure if this is hpcprof's bug or not). hpcprof's flat index is not
	 *  suitable if the same function is from the same file from different load modules
	 *  In this case, hpcprof's flat index will be the same (which is incorrect) 
	 *  
	 * In normal case, the ID is the hashcode of its call site (line scope). 
	 * But, in case of there are multiple calls in one line statement, we need
	 * to generate different ID for each call sites.
	 * 
	 * @param ls
	 * @param cs
	 * @return
	 *************************************************************************/
	private int getCallSiteID ( LineScope ls, ProcedureScope cs ) {
		LoadModuleScope module = cs.getLoadModule();
		// add flat index if there are two calls in the same line
		String sName = ls.getName() + "/" + cs.getName() + "/" + ls.getFlatIndex();
		// in case of the same file and the same procedure with different module name
		// this should fix where we have ~unknown-file~ and ~unknown-procedure~ in 
		// different modules
		if (module != null) {
			sName = module.getModuleName()+ "/" + sName;
		}
		
		int scope_id = sName.hashCode();
		Scope s_old = this.hashCallSiteTable.get( Integer.valueOf(scope_id) );
		if (s_old != null) {
			if (s_old.getName().equals(cs.getName())) {
				// the same line, the same ID, the same calls
			} else {
				// the same line, different calls. We need to create a new ID
				scope_id = this.current_cs_id;
				this.hashCallSiteTable.put(Integer.valueOf(scope_id), cs);
				this.current_cs_id--;
			}
		} else {
			this.hashCallSiteTable.put(Integer.valueOf(scope_id), cs);
		}
		return scope_id;
	}
	

	
	/*************************************************************************
	 *	Begins processing a A (alien) element.
      <!ELEMENT A (A|L|S|C|M)*>      <!ATTLIST A
                i CDATA #IMPLIED
                f CDATA #IMPLIED
                n CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/

	private void begin_A(String[] attributes, String[] values)
	{
		String sIndex = null;
		String filenm = null;
		String procnm = null;
		String sLine = null;
		
		// make a new alien scope object
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i].equals(ID_ATTRIBUTE)) {
				sIndex = values[i];
			} else if (attributes[i].equals(FILENAME_ATTRIBUTE)) {
				filenm = values[i];
			} else if (attributes[i].equals(NAME_ATTRIBUTE)) {
				procnm = values[i];
			} else if (attributes[i].equals(LINE_ATTRIBUTE)) {
				sLine = values[i];
			}
		}
		
		try {
			Integer objIndex = Integer.parseInt(sIndex);

			int firstLn, lastLn;
			StatementRange objRange = new StatementRange(sLine);
			firstLn = objRange.getFirstLine();
			lastLn = objRange.getLastLine();

			SourceFile sourceFile = this.getOrCreateSourceFile(filenm, objIndex.intValue());
			this.srcFileStack.push(sourceFile);

			Scope alienScope = new AlienScope(this.experiment, sourceFile, filenm, procnm, firstLn-1, lastLn-1, objIndex.intValue());

			this.beginScope(alienScope);

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
	}

	
	/*************************************************************************
	 * 
	 * @param attributes
	 * @param values
	 * <!ATTLIST Procedure
              i CDATA #REQUIRED
              n CDATA #REQUIRED>
	 *************************************************************************/
	private void do_Procedure(String[] attributes, String[] values) {
		if(values.length < 2)
			return;
		String sID = values[0];
		String sData = values[1];

		try {
			Integer objID = Integer.parseInt(sID);
			this.hashProcedureTable.put(objID, sData);
		} catch (java.lang.NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	


	/*************************************************************************
	 *	Begins processing an L (loop) element.
	 *	<!ELEMENT L (A|L|S|C|M)*>
      <!ATTLIST L
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/

	private void begin_L(String[] attributes, String[] values)
	{
		int cct_id = 0, flat_id = 0;
		int firstLn = 0;
		int lastLn = 0;
		
		for(int i=0; i<attributes.length; i++) {
			if(attributes[i].equals("s")) {
				flat_id = Integer.valueOf(values[i]);
				if (cct_id == 0)
					cct_id = flat_id;
				
			} else if(attributes[i].equals("l")) {
				String sLine = values[i];
				StatementRange objRange = new StatementRange( sLine );
				firstLn = objRange.getFirstLine();
				lastLn = objRange.getLastLine();	
				
			} else if(attributes[i].equals(ID_ATTRIBUTE)) {
				cct_id = Integer.valueOf(values[i]);
			} 
		}

		SourceFile sourceFile = this.srcFileStack.peek();
		if (this.csviewer) {
			// Use the source file of the Procedure Frame
			// NOTE: the current scope (i.e. the parent of this
			// nascent loop scope) should be either a procedure frame
			// or a loop that recursively obtained its file from the
			// procedure frame.
			Scope frameScope = this.getCurrentScope();
			//while ( !(frameScope instanceof ProcedureScope) ) {
			//  frameScope = frameScope.getParentScope();
			//}
			sourceFile = frameScope.getSourceFile();
		}
		Scope loopScope = new LoopScope(this.experiment, sourceFile, firstLn-1, lastLn-1, cct_id, flat_id);

		this.beginScope(loopScope);
	}

	/*************************************************************************
	 *	Finishes processing an L (loop) element.
	 ************************************************************************/

	private void end_L()
	{
		this.endScope();
	}

	/*************************************************************************
	 *	Begins processing an LN (line) element.
	 * <!ATTLIST S
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_S(String[] attributes, String[] values)
	{
		begin_S_internal( attributes,  values, false);
	}
	private void begin_S_internal(String[] attributes, String[] values, boolean isCallSite)
	{
		int cct_id = 0, flat_id = 0;
		// make a new statement-range scope object
		int firstLn = 0;
		int lastLn  = 0;
		int cpid = 0;

		for(int i=0; i<attributes.length; i++) {
			if(attributes[i].equals(LINE_ATTRIBUTE)) {
				firstLn = Integer.valueOf(values[i]);
				lastLn = firstLn;
				
			} else if(attributes[i].equals("s"))  {
				flat_id = Integer.valueOf(values[i]);
				if (cct_id == 0)
					cct_id = flat_id;
				
			} else if(attributes[i].equals(ID_ATTRIBUTE))  {
				cct_id = Integer.valueOf(values[i]);

			} else if(attributes[i].equals("it")) { //the cpid
				cpid = Integer.parseInt(values[i]);
			}

		}

		SourceFile srcFile = this.srcFileStack.peek();


		Scope scope;
		if( firstLn == lastLn )
			scope = new LineScope(this.experiment, srcFile, firstLn-1, cct_id, flat_id);
		else
			scope = new StatementRangeScope(this.experiment, srcFile, 
					firstLn-1, lastLn-1, cct_id, flat_id);

		scope.setCpid(cpid);
		if (isCallSite) {
			this.beginScope_internal(scope, false);
		} else {
			this.beginScope(scope);
		}
	}

	/*************************************************************************
	 *	Finishes processing an S (line) element.
	 ************************************************************************/

	private void end_S()
	{
		this.endScope(); 
	}


	/*************************************************************************
	 * Begin a new CALLSITE
	 * <!ATTLIST C
                i CDATA #IMPLIED
                s CDATA #IMPLIED
                l CDATA #IMPLIED
                v CDATA #IMPLIED>
	 ************************************************************************/
	private void begin_CALLSITE(String[] attributes, String[] values) {  
		this.begin_S_internal(attributes, values, true); // orig: true  
	}



	/*************************************************************************
	 * 	end a callsite.
	 ************************************************************************/
	private void end_CALLSITE() 
	{
		end_S();
	}


	private void end_PGM()
	{
		this.endScope();
	}


	


	/*************************************************************************
	 *	Finishes processing an LM (load module) element.
	 ************************************************************************/
	private void end_LM()
	{
		this.endScope();
	}



	/*************************************************************************
	 *	Finishes processing an F (file) element.
	 ************************************************************************/
	private void end_F()
	{
		this.endScope();
		this.srcFileStack.pop();
		this.Assert(srcFileStack.peek() == null); // mimic old behavior
	}


	
	
	/*************************************************************************
	 *	Finishes processing a P (procedure) element.
	 ************************************************************************/

	private void end_PF()
	{
		this.srcFileStack.pop();
		this.endScope();
	}




	/*************************************************************************
	 *	Finishes processing a A (alien) element.
	 ************************************************************************/

	private void end_A()
	{
		this.srcFileStack.pop();
		this.endScope();
	}

	
	
	//--------------------------------------------------------------------------------
	// trace database
	//--------------------------------------------------------------------------------
	private void begin_TraceDBTable(String[] attributes, String[] values) 
	{
	}

	
	private void end_TraceDBTable() 
	{
	}

	
	
	/*******
	 * handling trace db
	 * @param attributes
	 * @param values
	 */
	private void do_TraceDB(String[] attributes, String[] values)
	{
		TraceAttribute attribute = new TraceAttribute();
		// tallent: Note that the DTD currently only permits one instance of <TraceDB>
		for (int i=0; i<attributes.length; i++) {
			
			if (attributes[i].charAt(0) == 'i') {
			} else if (attributes[i].equals("db-glob")) {
				attribute.dbGlob = values[i];
				
			} else if (attributes[i].equals("db-min-time")) {
				attribute.dbTimeMin = Long.valueOf(values[i]);
				
			} else if (attributes[i].equals("db-max-time")) {
				attribute.dbTimeMax = Long.valueOf(values[i]);

			} else if (attributes[i].equals("db-header-sz")) {
				attribute.dbHeaderSize = Integer.valueOf(values[i]);
			}
		}
		this.experiment.setTraceAttribute(attribute);
	}



	//===============================================
	// Utilities that may be used by children of the class
	//===============================================
	
	/*************************************************************************
	 *	Begins processing a profile data (program) element.
	 ************************************************************************/
	protected void begin_SecData(String[] attributes, String[] values) 
	{
		String name = getAttributeByName(NAME_ATTRIBUTE, attributes, values);
		
		// make the root scope
		this.rootScope = new RootScope(this.experiment, name,"Invisible Outer Root Scope", RootScopeType.Invisible);
		this.scopeStack.push(this.rootScope);	// don't use 'beginScope'

		final String title;
		final RootScopeType rootType;
		
		if (this.csviewer) {
			title = "Calling Context View";
			rootType = RootScopeType.CallingContextTree;
		} else {
			title = "Flat View";
			rootType = RootScopeType.Flat;
		}
		this.viewRootScope[0]  = new RootScope(this.experiment, name, title, rootType);
		beginScope(this.viewRootScope[0]);
	}


	/************************************************************************* 
	 * treat XML attributes like a named property list; this is an alternative to a brittle
	 * position-based approach for recognizing attributes
	 *************************************************************************/
	protected String getAttributeByName(String name, String[] attributes, String[] values)
	{
		for (int i = 0; i < attributes.length; i++) if (name == attributes[i]) return values[i];
		return null;
	}


//	SCOPE TREE BUILDING													//

	/*************************************************************************
	 *	Adds a newly parsed scope to the scope tree.
	 ************************************************************************/
	private void beginScope(Scope scope)
	{
		beginScope_internal(scope, true);
	}

	/****************************************************************
	 * 
	 * @param scope
	 * @param addToTree
	 *****************************************************************/
	protected void beginScope_internal(Scope scope, boolean addToTree)
	{
		Scope top = this.getCurrentScope();
		if (addToTree) {
			top.addSubscope(scope);
			scope.setParentScope(top);
		}
		this.scopeStack.push(scope);
	}

	/*************************************************************************
	 *	Ends a newly parsed scope.
	 ************************************************************************/
	protected void endScope()
	{
		try {
			this.scopeStack.pop();
		} catch (java.util.EmptyStackException e) {
			System.out.println("End of stack:"+this.parser.getLineNumber());
		}
	}

	/*************************************************************************
	 *	Returns the current scope.
	 ************************************************************************/
	protected Scope getCurrentScope()
	{
		return this.scopeStack.peek();
	}

	/*************************************************************************
	 *   Get the File for a callsite 	
	 *   Using Hashtable to store the "FileSystemSourceFile" object 
	 *   for the callsite's file attribute.
	 ************************************************************************/
	protected SourceFile getOrCreateSourceFile(String fileLine, int keyFile)
	{
		SourceFile sourceFile=hashSourceFileTable.get(keyFile);
		if (sourceFile == null) {
			File filename = new File(fileLine);
			sourceFile = new FileSystemSourceFile(experiment, filename, keyFile);
			this.hashSourceFileTable.put(Integer.valueOf(keyFile), sourceFile);
		}  

		return sourceFile;
	}

	

	/*************************************************************************
	 *	Initializes the build process.
	 ************************************************************************/

	public void begin()
	{
		this.configuration = new ExperimentConfiguration();
	}


	/*************************************************************************
	 *	Takes notice of content characters within an element.
	 *
	 *	None of the elements in an hpcviewer experiment XML file have content
	 *	characters, so this method should never be called.
	 *
	 ************************************************************************/
	public void content(String s)
	{
		Dialogs.notCalled("ExperimentBuilder.content");
	}



	/*************************************************************************
	 *	Finalizes the build process.
	 ************************************************************************/

	public void end()
	{
		// bugs no 224: https://outreach.scidac.gov/tracker/index.php?func=detail&aid=224&group_id=22&atid=169
		try {
			// pop out root scope
			this.scopeStack.pop();
		} catch (EmptyStackException e) {
			System.err.println("ExperimentBuilder: no root scope !");
		}
		
		// check that input was properly nested
		if (!this.scopeStack.empty()) {
			Scope topScope = this.scopeStack.peek();
			System.out.println("Stack is not empty; remaining top scope = " + topScope.getName());
			this.error();
		}

		// copy parse results into configuration
		this.experiment.setConfiguration(this.configuration);

		this.experiment.setRootScope(this.rootScope);
		
		// supply defaults for missing info
		if( this.configuration.getName() == null )
			this.configuration.setName(this.defaultName);
		if( this.configuration.getSearchPathCount() == 0 )
		{
			List<File> paths = new ArrayList<File>();
			paths.add(new File(""));
			paths.add(new File("src"));
			paths.add(new File("compile"));
			this.configuration.setSearchPaths(paths);
		}

	}

	

	
	//--------------------------------------------------------------------------------
	// Utilities
	//--------------------------------------------------------------------------------

	
	private String getProcedureName(String sProcIndex) {
		String sProcName = PROCEDURE_UNKNOWN;
		boolean hashtableExist = (this.hashProcedureTable.size()>0);
		if(hashtableExist) {
			try {
				Integer objProcID = Integer.parseInt(sProcIndex); 
				// get the real name of the procedure from the dictionary
				String sProc = this.hashProcedureTable.get(objProcID);
				if(sProc != null) {
					sProcName = sProc;
				}
			} catch (java.lang.NumberFormatException e) {
				System.err.println("Warning: Procedure index doesn't exist: " + sProcIndex);
			}
		} else {
			// the database of procedure doesn't exist. This can be a flat view.
			sProcName = sProcIndex;
		}
		return sProcName;
	}
	
	
	
	/*************************************************************************
	 * Class to treat a string of line or range of lines into two lines: first line and last line 
	 * @author laksonoadhianto
	 *
	 ************************************************************************/
	private class StatementRange {
		private int firstLn;
		private int lastLn;
		
		public StatementRange(String sLine) {
			// find the range separator
			int iSeparator = sLine.indexOf('-');
			if(iSeparator > 0) {
				// separator exist, it should be a range
				this.firstLn = Integer.parseInt( sLine.substring(0,iSeparator) );
				this.lastLn = Integer.parseInt( sLine.substring(iSeparator+1) );
			} else {
				// no separator: no range
				this.firstLn = Integer.parseInt(sLine);
				this.lastLn = this.firstLn;
			}
		}
		
		public int getFirstLine( ) { return this.firstLn; }
		public int getLastLine( ) { return this.lastLn; }
	}

}
