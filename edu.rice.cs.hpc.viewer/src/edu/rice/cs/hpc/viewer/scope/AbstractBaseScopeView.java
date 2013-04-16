package edu.rice.cs.hpc.viewer.scope;

import java.io.FileNotFoundException;
//User interface
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
//SWT
import org.eclipse.swt.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

//Jface
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.window.ToolTip;

//HPC
import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.util.OSValidator;
import edu.rice.cs.hpc.viewer.actions.DebugShowCCT;
import edu.rice.cs.hpc.viewer.actions.DebugShowFlatID;
import edu.rice.cs.hpc.viewer.editor.EditorManager;
import edu.rice.cs.hpc.viewer.util.Utilities;
import edu.rice.cs.hpc.viewer.window.Database;

/**
 * 
 * Abstract class of view-part for different types of views:
 * - calling context view (top down)
 * - callers view (bottom-down)
 * - flat view (static)
 * - thread scope view (not implemented yet, but it will shows metric of a thread)
 *
 */
abstract public class AbstractBaseScopeView  extends ViewPart {

	protected ScopeTreeViewer 	treeViewer;		  	// tree for the caller and callees
    
	protected Database 	database;		// experiment data	
	protected RootScope 		myRootScope;		// the root scope of this view
	protected ColumnViewerSorter sorterTreeColumn;	// sorter for the tree
    protected ScopeViewActions objViewActions;	// actions for this scope view
	
    private EditorManager editorSourceCode;	// manager to display the source code
	private Clipboard cb = null;
	private GC gc = null;
	
	/**
	 * bar composite for placing toolbar and tool items
	 */
	protected CoolBar objCoolbar;
	
	
    //======================================================
    // ................ HELPER ............................
    //======================================================
    
	abstract protected CellLabelProvider getLabelProvider(); 
    
    /**
     * Display the source code of the node in the editor area
     * @param node the current OR selected node
     */
    private void displayFileEditor(Scope scope) {
    	if(editorSourceCode == null) {
    		this.editorSourceCode = new EditorManager(this.getSite());
    	}
    	try {
    		this.editorSourceCode.displayFileEditor(scope);
    	} catch (FileNotFoundException e) {
    		this.objViewActions.showErrorMessage("No source available for binary file "+e.getMessage());
    	}
    }

    //======================================================
    // ................ ACTIONS ............................
    //======================================================

    /**
     * Menu action to zoom-in a node
     */
    private Action acZoomin = new Action("Zoom-in"){
    	public void run() {
    		objViewActions.zoomIn();
    	}
    };
    
    /**
     * Menu action to zoom a node
     */
    private Action acZoomout = new Action("Zoom-out"){
    	public void run() {
    		objViewActions.zoomOut();
    	}
    };

    /**
     * Helper method to know if an item has been selected
     * @return true if an item is selected, false otherwise
     */
    private boolean isItemSelected() {
    	return (this.treeViewer.getTree().getSelectionCount() > 0);
    }
    
    /**
     * Helper method to retrieve the selected item
     * @return
     */
    private Scope getSelectedItem() {
        TreeItem[] selection = this.treeViewer.getTree().getSelection();
        if(selection != null) {
        	Object o = selection[0].getData();
        	/**
        	 * Fix bug which appears when the user wants to see the context menu of
        	 * the top row of the table (the aggregate metrics)
        	 */
        	if(o instanceof Scope)
        		return (Scope)o;
        }
        return null;
    }
    
    /**
     * Creating the context submenu for the view
     * TODO Created only the line selected
     * @param mgr
     */
    private void fillContextMenu(IMenuManager mgr) {
    	Scope scope = this.getSelectedItem();
        final Action acCopy = new Action("Copy") {
        	public void run() {
        		copyToClipboard();
        	}
        }; 
    	/**
    	 * Fix bug which appears when the user wants to see the context menu of
    	 * the top row of the table (the aggregate metrics)
    	 */
    	if(scope == null) {
    		mgr.add(acCopy);
    		return;
    	}
    	// ---- zoomin
        mgr.add(acZoomin);
        acZoomin.setEnabled(this.objViewActions.shouldZoomInBeEnabled(scope));
        // ---- zoomout
        mgr.add(acZoomout);
        acZoomout.setEnabled(this.objViewActions.shouldZoomOutBeEnabled());

        // ---- additional feature
        mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));


        // Laks 2009.06.22: add new feature to copy selected line to the clipboard
        mgr.add(acCopy);

        //--------------------------------------------------------------------------
        // ---------- show the source code
        //--------------------------------------------------------------------------
        
        // show the editor source code
        final String SHOW_MENU = "Show ";
        
        String sMenuTitle ;
        if(scope instanceof FileScope) {
        	sMenuTitle = SHOW_MENU + scope.getSourceFile().getName();
        } else
        	sMenuTitle= SHOW_MENU +scope.getToolTip(); // the tooltip contains the info we need: file and the linenum
        
        ScopeViewTreeAction acShowCode = new ScopeViewTreeAction(sMenuTitle, scope){
        	public void run() {
        		displayFileEditor(this.scope);
        	}
        };
        acShowCode.setEnabled(Utilities.isFileReadable(scope));
        mgr.add(acShowCode);

        // show the call site in case this one exists
        if(scope instanceof CallSiteScope) {
        	// get the call site scope
        	CallSiteScope callSiteScope = (CallSiteScope) scope;
        	LineScope lineScope = (LineScope) callSiteScope.getLineScope();
        	// setup the menu
        	sMenuTitle = "Callsite "+lineScope.getToolTip();
        	ScopeViewTreeAction acShowCallsite = new ScopeViewTreeAction(sMenuTitle, lineScope){
        		public void run() {
        			displayFileEditor(this.scope);
        		}
        	}; 
        	// do not show up in the menu context if the callsite does not exist
        	acShowCallsite.setEnabled(Utilities.isFileReadable(lineScope));
        	mgr.add(acShowCallsite);
        }
        

        //--------------------------------------------------------------------------
        // ---------- additional context menu
        //--------------------------------------------------------------------------
        mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        this.createAdditionalContextMenu(mgr, scope);
    }
    
    
    /**
     * Procedure to copy the selected items into string clipboard
     */
    private void copyToClipboard() {
    	// only selected items that are copied into clipboard
    	TreeItem []itemsSelected = this.treeViewer.getTree().getSelection();
    	// convert the table into a string
    	String sText = this.objViewActions.getContent(itemsSelected, "\t");
    	// send the string into clipboard
    	TextTransfer textTransfer = TextTransfer.getInstance();
    	if (this.cb == null)
    		this.cb = new Clipboard(this.getSite().getShell().getDisplay());
		cb.setContents(new Object[]{sText}, new Transfer[]{textTransfer});
    }
    
    /**
     * Creating context menu manager
     */
    private void createContextMenu() {
        // Create menu manager.
    	MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager mgr) {
                    if(isItemSelected())
                    	fillContextMenu(mgr);
                }
        });
        
        // Create menu.
        Menu menu = menuMgr.createContextMenu(this.treeViewer.getControl());
        this.treeViewer.getControl().setMenu(menu);
        
        // Register menu for extension.    
        // Using an id allows code that extends this class to add entries to this context menu.
        getSite().registerContextMenu("edu.rice.cs.hpc.viewer.scope.ScopeView", menuMgr, this.treeViewer);
    }
    
    /**
     * Actions/menus for Scope view tree.
     * @author laksono
     *
     */
    protected class ScopeViewTreeAction extends Action {
    	protected Scope scope;
    	public ScopeViewTreeAction(String sTitle, Scope scopeCurrent) {
    		super(sTitle);
    		this.scope = scopeCurrent;
    	}
    }
    
    
    //===================================================================
    // ---------- VIEW CREATION -----------------------------------------
    //===================================================================
    
    /**
     * Create the content of the view
     */
    public void createPartControl(Composite aParent) {
    	Composite objCompositeParent;
    	objCompositeParent = this.createToolBarArea(aParent);
    	this.objCoolbar = this.initToolbar(objCompositeParent);
		this.objViewActions =  createActions(objCompositeParent, this.objCoolbar); //actions of the tree
		
		// ----- 03.21.2008 Laks: add virtual library for better memory consumption
		// Laks 2009.06.22: add multi-selection for enabling copying into clipboard 
    	treeViewer = new ScopeTreeViewer(aParent,SWT.BORDER|SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
    	// set the attributes
    	ScopeTreeContentProvider treeContentProvider = getScopeContentProvider(); 
    	treeViewer.setContentProvider(treeContentProvider);
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);
        
        // --------------------------------------------------------------------
        // tricky code for linux: by default on linux the tooltip doesn't show up
        //  we should manually tell eclipse/jface that we want a tooltip for the table
        // --------------------------------------------------------------------
        if (OSValidator.isUnix())
        	ColumnViewerToolTipSupport.enableFor(treeViewer, ToolTip.NO_RECREATE);

        //treeViewer.setAutoExpandLevel(2);
        
        //----------------- create the column tree
        TreeViewerColumn colTree;		// column for the scope tree
        colTree = new TreeViewerColumn(treeViewer,SWT.LEFT, 0);
        colTree.getColumn().setText("Scope");
        colTree.getColumn().setWidth(200); //TODO dynamic size
        colTree.setLabelProvider( getLabelProvider() ); // laks addendum
        sorterTreeColumn = new ColumnViewerSorter(this.treeViewer, colTree.getColumn(), null,0); 
        
        //-----------------
        // Laks 11.11.07: need this to expand the tree for all view
        GridData data = new GridData(GridData.FILL_BOTH);
        treeViewer.getTree().setLayoutData(data);
        //-----------------
        // create the context menus
        this.createContextMenu();

        //------------------------ LISTENER --------------
        /**
         * add listener when left button mouse is clicked 
         * On MAC it doesn't matter which button, but on Windows, we need to make sure !
         */
        gc = new GC(this.treeViewer.getTree().getDisplay());
        treeViewer.getTree().addListener(SWT.MouseDown, new ScopeMouseListener(gc, this.getSite().getPage())); 
        
        // bug #132: https://outreach.scidac.gov/tracker/index.php?func=detail&aid=132&group_id=22&atid=169
        // need to capture event of "collapse" tree then check if the button state should be updated or not.
        treeViewer.addTreeListener(new ITreeViewerListener(){
        	public void treeCollapsed(TreeExpansionEvent event) {
        		objViewActions.checkNodeButtons();
        	}
        	public void treeExpanded(TreeExpansionEvent event){
        		
        	}
        });
        
		Utilities.listenerToResetRowHeight( treeViewer );

		// allow other views to listen for selections in this view (site)
		this.getSite().setSelectionProvider(treeViewer);
		
		/**
		 * Add Listener for change of selection so that every change will update
		 * the status of the toolbar buttons (able or disabled) 
		 */
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event)
		      {
		        TreeSelection selection =
		          (TreeSelection) event.getSelection();

		        Object objElement = selection.getFirstElement();
		        if(objElement instanceof Scope) {
			        Scope nodeSelected = (Scope) objElement;
			        if(nodeSelected != null) {
			        	// update the state of the toolbar items
			        	objViewActions.checkStates(nodeSelected);
			        }
		        } else {
		        	// selection on wrong node
		        	objViewActions.disableButtons();
		        }
		      }
		}); 
		
		// ---------------------------------------------------------------
		// register listener to capture debugging mode
		// ---------------------------------------------------------------
		final ICommandService commandService = (ICommandService) this.getSite().getService(ICommandService.class);
		commandService.addExecutionListener( new IExecutionListener(){

			public void notHandled(String commandId, NotHandledException exception) {}
			public void postExecuteFailure(String commandId, ExecutionException exception) {}
			public void preExecute(String commandId, ExecutionEvent event) {}

			/*
			 * (non-Javadoc)
			 * @see org.eclipse.core.commands.IExecutionListener#postExecuteSuccess(java.lang.String, java.lang.Object)
			 */
			public void postExecuteSuccess(String commandId, Object returnValue) 
			{
				if (commandId.equals(DebugShowCCT.commandId) || commandId.equals(DebugShowFlatID.commandId))
				{
					// refresh the table to take into account the turn on/off debugging mode
					Utilities.resetView(treeViewer);
				}
			}
		});
	}
    
    /**
     * Create the toolbar layout
     * @param parent
     * @return
     */
    protected Composite createToolBarArea(Composite parent) {
    	// make the parent with grid layout
    	Composite toolbarArea = new Composite(parent, SWT.NONE);
    	GridLayout grid = new GridLayout(1,false);
    	parent.setLayout(grid);
    	return toolbarArea;
    }

    /**
     * Create and Initialize coolbar, set the layout and return the coolbar 
     * @param toolbarArea
     * @return
     */
    protected CoolBar initToolbar(Composite toolbarArea) {
    	CoolBar coolBar = new CoolBar(toolbarArea, SWT.FLAT);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	coolBar.setLayoutData(data);

    	return coolBar;
    }
    

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose() {
    	if (gc != null)
    		gc.dispose();
    }
    
    //======================================================
    // ................ UPDATE ............................
    //======================================================
    // laks: we need experiment and rootscope
    /**
     * Update the data input for Scope View, depending also on the scope
     */
    public void setInput(Database db, RootScope scope, boolean keepColumnStatus) {
    	initDatabase(db, scope, keepColumnStatus);
    	updateDisplay();
    }
    
    /***
     * initialize view without creating the tree and the metric columns
     * 
     * @param db
     * @param scope
     */
    public void initDatabase(Database db, RootScope scope, boolean keepColumnStatus) {
    	database = db;
    	myRootScope = scope;// try to get the aggregate value

        // tell the action class that we have built the tree
        this.objViewActions.setTreeViewer(treeViewer);
        
        initTableColumns(keepColumnStatus);
    }
    
    
    
    //======================================================
    // ................ MISC ............................
    //======================================================
	/**
	 * Modify the title of the view
	 * @param sName
	 */
	public void setViewTitle(String sName) {
		super.setPartName(sName);
	}
    public void setFocus() {
            treeViewer.getTree().setFocus();
    }
    
    public ScopeViewActions getViewActions() {
    	return this.objViewActions;
    }
    /**
     * return the tree of this viewer (even though there's no experiment active)
     * @return
     */
    public ScopeTreeViewer getTreeViewer() {
    	return this.treeViewer;
    }

    /****
     * get the experiment of this view
     * @return
     */
    public Experiment getExperiment() {
    	if (database != null)
    		return database.getExperiment();
    	return null;
    }

    /****
     * get the root scope (either cct, caller tree or flat tree)
     * @return
     */
    public RootScope getRootScope() {
    	return this.myRootScope;
    }
    
    public Database getDatabase() {
    	return database;
    }
    //======================================================
    // ................ ABSTRACT...........................
    //======================================================

	/**
     * Tell children to update the content with the new database
	 * Update the content of the tree view when a new experiment is loaded
	 */
	abstract public void updateDisplay();

	abstract protected void initTableColumns(boolean keepColumnStatus);
	
    /**
     * The derived class has to implement this method to create its own actions
     * For instance, caller view and cct view has the same actions but flat view
     * 	may have additional actions (flattening ...)
     * @param parent
     * @param coolbar
     * @return
     */
    abstract protected ScopeViewActions createActions(Composite parent, CoolBar coolbar);
    
    /***
     * event when a user starts to click
     * @param event
     */
    protected abstract void mouseDownEvent(Event event);

    abstract protected void createAdditionalContextMenu(IMenuManager mgr, Scope scope);
    
    abstract protected ScopeTreeContentProvider getScopeContentProvider();

    //======================================================
    // ................ CLASSES...........................
    //======================================================

    
    /***
     * 
     * class to handle mouse up and down event in scope tree
     *
     */
    public class ScopeMouseListener implements Listener {

    	final private GC gc;
    	final private IWorkbenchPage page;
    	
    	/**
    	 * initialization with the gc of the tree
    	 * @param gc of the tree
    	 */
    	public ScopeMouseListener(final GC gc, IWorkbenchPage page) {
    		this.gc = gc;
    		this.page = page;
    	}
    	
    	/*
    	 * (non-Javadoc)
    	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
    	 */
    	public void handleEvent(Event event) {

    		// tell the children to handle the mouse click
    		mouseDownEvent(event);

    		if(event.button != 1) {
    			// yes, we only allow the first button
    			return;
    		}
    		
    		// get the item
    		TreeItem []itemsSelected = treeViewer.getTree().getSelection();
    		if(itemsSelected == null || itemsSelected.length==0)
    			return; // no selected. it will hard to for us to go further
    		TreeItem item = itemsSelected[0];
    		Rectangle recImage = item.getImageBounds(0);	// get the image location (if exist)
    		Rectangle recText = item.getTextBounds(0);
    		
    		// verify if the user click on the icon
    		if(recImage.intersects(event.x, event.y, event.width, event.height)) {
    			// Check the object of the click/select item
    	        TreeSelection selection = (TreeSelection) treeViewer.getSelection();
    	        Object o = selection.getFirstElement();
    	        
    	        // we will treat this click if the object is Scope
    	        if(o instanceof Scope) {
    	        	Scope scope = (Scope) o;
    	            // show the call site in case this one exists
    	            if(scope instanceof CallSiteScope) {
    	            	// get the call site scope
    	            	CallSiteScope callSiteScope = (CallSiteScope) scope;
    	            	LineScope lineScope = (LineScope) callSiteScope.getLineScope();
    	            	displaySourceCode(lineScope);
    	            } else {
    	            }
    	        }
    		} else if(recText.intersects(event.x, event.y, 1, 1)){
    			// Check the object of the click/select item
    	        TreeSelection selection = (TreeSelection) treeViewer.getSelection();
    	        Object o = selection.getFirstElement();
    	        
    	        // we will treat this click if the object is Scope.Node
    	        if(o instanceof Scope) {
    	        	if (o instanceof CallSiteScope) {
    	        		CallSiteScope cs = (CallSiteScope) o;
    	        		// the line number in xml is started from zero, while the source
    	        		//	code starts from 1
    	        		int line = 1 + cs.getLineScope().getFirstLineNumber();
    	        		
    	        		if (gc != null && line>0) {
        		        	Point p = gc.textExtent(":" + line);
        		        	if (p.x+recText.x >= event.x && p.y+recText.y>= event.y) {
        		        		displaySourceCode( cs.getLineScope() );
        		        		return;
        		        	}
    	        		}
    	        	}
    	        	displaySourceCode( (Scope)o );
    	        }
    		} else {
    			// User click a region other than tree
    		}		
    	}
    	
    	/**
    	 * special source code display when user click a tree node
    	 * @param scope
    	 */
    	private void displaySourceCode( Scope scope ) {
			// display the source code if the view is not maximized
    		int state = page.getPartState( page.getActivePartReference() );
    		if (state != IWorkbenchPage.STATE_MAXIMIZED) {
    			displayFileEditor( scope );
    		}
    	}
    }

}
