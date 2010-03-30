package edu.rice.cs.hpc.viewer.scope;

import java.io.FileNotFoundException;

//User interface
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

//SWT
import org.eclipse.swt.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

//Jface
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
import org.eclipse.jface.viewers.ViewerCell;

//HPC
import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.experiment.extdata.ThreadLevelDataManager;
import edu.rice.cs.hpc.data.experiment.metric.BaseMetric;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.experiment.ExperimentData;
import edu.rice.cs.hpc.viewer.experiment.ExperimentManager;
import edu.rice.cs.hpc.viewer.util.EditorManager;
import edu.rice.cs.hpc.viewer.util.Utilities;

/**
 * 
 * @author laksonoadhianto
 *
 */
abstract public class BaseScopeView  extends ViewPart {

	private ScopeTreeViewer 	treeViewer;		  	// tree for the caller and callees
    private Experiment 	myExperiment;		// experiment data	
    private RootScope 		myRootScope;		// the root scope of this view
    private ColumnViewerSorter sorterTreeColummn;	// sorter for the tree
    private EditorManager editorSourceCode;	// manager to display the source code
	private ScopeViewActions objViewActions;	// actions for this scope view
	private Clipboard cb = null;
	private TreeViewerColumn []colMetrics;
	/**
	 * bar composite for placing toolbar and tool items
	 */
	protected CoolBar objCoolbar;
	
	protected boolean hasThreadsLevelData = false;
	
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
    private Scope.Node getSelectedItem() {
        TreeItem[] selection = this.treeViewer.getTree().getSelection();
        if(selection != null) {
        	Object o = selection[0].getData();
        	/**
        	 * Fix bug which appears when the user wants to see the context menu of
        	 * the top row of the table (the aggregate metrics)
        	 */
        	if(o instanceof Scope.Node)
        		return (Scope.Node)o;
        }
        return null;
    }
    /**
     * Creating the context submenu for the view
     * TODO Created only the line selected
     * @param mgr
     */
    private void fillContextMenu(IMenuManager mgr) {
    	Scope.Node node = this.getSelectedItem();
        final Action acCopy = new Action("Copy") {
        	public void run() {
        		copyToClipboard();
        	}
        }; 
    	/**
    	 * Fix bug which appears when the user wants to see the context menu of
    	 * the top row of the table (the aggregate metrics)
    	 */
    	if(node == null) {
    		mgr.add(acCopy);
    		return;
    	}
    	// ---- zoomin
        mgr.add(acZoomin);
        acZoomin.setEnabled(this.objViewActions.shouldZoomInBeEnabled(node));
        // ---- zoomout
        mgr.add(acZoomout);
        acZoomout.setEnabled(this.objViewActions.shouldZoomOutBeEnabled());

        // ---- additional feature
        mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

        Scope scope = node.getScope();

        // Laks 2009.06.22: add new feature to copy selected line to the clipboard
        mgr.add(acCopy);

        //--------------------------------------------------------------------------
        // ---------- show the source code
        //--------------------------------------------------------------------------
        
        // show the editor source code
        String sMenuTitle ;
        if(scope instanceof FileScope) {
        	sMenuTitle = "Show " + scope.getSourceFile().getName();
        } else
        	sMenuTitle= "Show "+scope.getToolTip(); // the tooltip contains the info we need: file and the linenum
        ScopeViewTreeAction acShowCode = new ScopeViewTreeAction(sMenuTitle, scope){
        	public void run() {
        		displayFileEditor(this.scope);
        	}
        };
        acShowCode.setEnabled(node.hasSourceCodeFile);
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
        getSite().registerContextMenu(menuMgr, this.treeViewer);
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
    	ScopeTreeContentProvider treeContentProvider;
    	treeContentProvider = new ScopeTreeContentProvider(); 
    	treeViewer.setContentProvider(treeContentProvider);
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);
        //treeViewer.setAutoExpandLevel(2);
        
        //----------------- create the column tree
        TreeViewerColumn colTree;		// column for the scope tree
        colTree = new TreeViewerColumn(treeViewer,SWT.LEFT, 0);
        colTree.getColumn().setText("Scope");
        colTree.getColumn().setWidth(200); //TODO dynamic size
        colTree.setLabelProvider( getLabelProvider() ); // laks addendum
        sorterTreeColummn = new ColumnViewerSorter(this.treeViewer, colTree.getColumn(), null,0); 
        
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
        treeViewer.getTree().addListener(SWT.MouseDown, new Listener(){
        	public void handleEvent(Event event) {

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
    		        // we will treat this click if the object is Scope.Node
    		        if(o instanceof Scope.Node) {
    		        	Scope.Node objNode = (Scope.Node) o;
    		        	Scope scope = objNode.getScope();
    		            // show the call site in case this one exists
    		            if(scope instanceof CallSiteScope) {
    		            	// get the call site scope
    		            	CallSiteScope callSiteScope = (CallSiteScope) scope;
    		            	LineScope lineScope = (LineScope) callSiteScope.getLineScope();
    		            	displayFileEditor(lineScope);
    		            } else {
    		            }
    		        }
        		} else if(recText.intersects(event.x, event.y, 1, 1)){
        			// Check the object of the click/select item
    		        TreeSelection selection = (TreeSelection) treeViewer.getSelection();
    		        Object o = selection.getFirstElement();
    		        // we will treat this click if the object is Scope.Node
    		        if(o instanceof Scope.Node) {
    		        	Scope.Node objNode = (Scope.Node) o;
    		        	displayFileEditor(objNode.getScope());
    		        }
        		} else {
        			// User click a region other than tree
        		}
        	}
        }); 
        // bug #132: https://outreach.scidac.gov/tracker/index.php?func=detail&aid=132&group_id=22&atid=169
        // need to capture event of "collapse" tree then check if the button state should be updated or not.
        treeViewer.addTreeListener(new ITreeViewerListener(){
        	public void treeCollapsed(TreeExpansionEvent event) {
        		objViewActions.checkNodeButtons();
        	}
        	public void treeExpanded(TreeExpansionEvent event){
        		
        	}
        });

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
		        if(objElement instanceof Scope.Node) {
			        Scope.Node nodeSelected = (Scope.Node) objElement;
			        if(nodeSelected != null) {
			        	// update the state of the toolbar items
			        	//objViewActions.checkButtons(nodeSelected);
			        	objViewActions.checkStates(nodeSelected);
			        }
		        } else {
		        	// selection on wrong node
		        	objViewActions.disableButtons();
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
    

    //======================================================
    // ................ UPDATE ............................
    //======================================================
    // laks: we need experiment and rootscope
    /**
     * Update the data input for Scope View, depending also on the scope
     */
    public void setInput(Experiment ex, RootScope scope) {
    	myExperiment = ex;
    	myRootScope = scope;// try to get the aggregate value

        // tell the action class that we have built the tree
        this.objViewActions.setTreeViewer(treeViewer);

    	updateDisplay();

    	this.hasThreadsLevelData = (myExperiment.getThreadLevelDataManager() != null);
    }
    
	/**
	 * Update the content of the tree view when a new experiment is loaded
	 */
	private void updateDisplay() {
        if (myExperiment == null)
        	return;
        int iColCount = this.treeViewer.getTree().getColumnCount();
        if(iColCount>1) {
        	// remove the metric columns blindly
        	// TODO we need to have a more elegant solution here
        	for(int i=1;i<iColCount;i++) {
        		this.treeViewer.getTree().getColumn(1).dispose();
        	}
        }
        // prepare the data for the sorter class for tree
        this.sorterTreeColummn.setMetric(myExperiment.getMetric(0));

        // force garbage collector to remove the old data
        this.colMetrics = null;
        int nbMetrics = myExperiment.getMetricCount();
        boolean status[] = new boolean[nbMetrics];
        // dirty solution to update titles
        this.colMetrics = new TreeViewerColumn[nbMetrics];
        {
            // Update metric title labels
            String[] titles = new String[nbMetrics+1];
            titles[0] = "Scope";	// unused element. Already defined
            // add table column for each metric
        	for (int i=0; i<nbMetrics; i++)
        	{
        		titles[i+1] = myExperiment.getMetric(i).getDisplayName();	// get the title
        		colMetrics[i] = this.treeViewer.addTreeColumn(myExperiment.getMetric(i), (i==0));
        		status[i] = myExperiment.getMetric(i).getDisplayed();
        	}
            treeViewer.setColumnProperties(titles); // do we need this ??
        }
        
        // Update root scope
        Scope.Node nodeRootScope = myRootScope.getTreeNode();
        if (nodeRootScope.getChildCount() > 0) {
            treeViewer.setInput(myRootScope.getTreeNode());
            // update the window title
            this.getSite().getShell().setText("hpcviewer: "+myExperiment.getName());
            
            // update the root scope of the actions !
            this.objViewActions.updateContent(this.myExperiment, (RootScope)this.myRootScope, colMetrics);
            // FIXME: For unknown reason, the updateContent method above does not resize the column automatically,
            // so we need to do it here, manually ... sigh
            this.objViewActions.resizeColumns();	// resize the column to fit all metrics
        	this.objViewActions.objActionsGUI.setColumnsStatus(status);
        	
            // Laks 2009.03.17: select the first scope
            TreeItem objItem = this.treeViewer.getTree().getItem(1);
            this.treeViewer.getTree().setSelection(objItem);
            // reset the button
            this.objViewActions.checkNodeButtons();
        } else {
        	// empty experiment data (it should be a warning instead of an error. The error should be on the profile side).
//        	org.eclipse.jface.dialogs.MessageDialog.openWarning(this.getSite().getShell(), "Warning: empty database", 
//        			"The database contains no data.");
        	this.objViewActions.showErrorMessage("Warning: empty database.");
        }
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


    protected Experiment getExperiment() {
    	return this.myExperiment;
    }

    //======================================================
    // ................ ABSTRACT...........................
    //======================================================
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
}
