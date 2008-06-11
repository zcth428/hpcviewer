package edu.rice.cs.hpc.viewer.scope;

import java.io.FileNotFoundException;

// User interface
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
// SWT
import org.eclipse.swt.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.Rectangle;

// Jface
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ITreeViewerListener;

// HPC
import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.util.EditorManager;
import edu.rice.cs.hpc.viewer.util.Utilities;

import org.eclipse.swt.events.MouseEvent;


public class ScopeView extends ViewPart {
    public static final String ID = "edu.rice.cs.hpc.scope.ScopeView";

    private ScopeTreeViewer 	treeViewer;		  	// tree for the caller and callees
    private TreeViewerColumn colTree;		// column for the calls tree
    private Experiment 	myExperiment;		// experiment data	
    private Scope 		myRootScope;		// the root scope of this view
    private ColumnViewerSorter sorterTreeColummn;	// sorter for the tree
    private EditorManager editorSourceCode;	// manager to display the source code
    private ScopeTreeContentProvider treeContentProvider;
	private ScopeViewActions objViewActions;	// actions for this scope view
    
    //======================================================
    // ................ HELPER ............................
    //======================================================
    
    
    /**
     * Display the source code of the node in the editor area
     * @param node the current OR selected node
     */
    private void displayFileEditor(Scope.Node node) {
    	if(editorSourceCode == null) {
    		this.editorSourceCode = new EditorManager(this.getSite());
    	}
    	try {
    		this.editorSourceCode.displayFileEditor(node);
    	} catch (FileNotFoundException e) {
    		this.objViewActions.showErrorMessage(e.getMessage());
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
    	/**
    	 * Fix bug which appears when the user wants to see the context menu of
    	 * the top row of the table (the aggregate metrics)
    	 */
    	if(node == null) return;
    	// ---- zoomin
        mgr.add(acZoomin);
        acZoomin.setEnabled(this.objViewActions.shouldZoomInBeEnabled(node));
        // ---- zoomout
        mgr.add(acZoomout);
        acZoomout.setEnabled(this.objViewActions.shouldZoomOutBeEnabled());
        // additional feature
        mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        // Laks: we don't need additional marker
        //mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        Scope scope = node.getScope();
        
        // ---------- show the source code
        
        // show the editor source code
        	String sMenuTitle ;
        	if(scope instanceof FileScope) {
        		sMenuTitle = "Show " + scope.getSourceFile().getName();
        	} else
        		sMenuTitle= "Show "+scope.getToolTip(); // the tooltip contains the info we need: file and the linenum
        	ScopeViewTreeAction acShowCode = new ScopeViewTreeAction(sMenuTitle, node){
            	public void run() {
            		displayFileEditor(this.nodeSelected);
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
            	ScopeViewTreeAction acShowCallsite = new ScopeViewTreeAction(sMenuTitle, lineScope.getTreeNode()){
                	public void run() {
                		displayFileEditor(this.nodeSelected);
                	}
                }; 
            	// do not show up in the menu context if the callsite does not exist
                acShowCallsite.setEnabled(Utilities.isFileReadable(lineScope));
                mgr.add(acShowCallsite);
        }
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
    private class ScopeViewTreeAction extends Action {
    	protected Scope.Node nodeSelected;
    	public ScopeViewTreeAction(String sTitle, Scope.Node nodeCurrent) {
    		super(sTitle);
    		this.nodeSelected = nodeCurrent;
    	}
    	public void setScopeNode(Scope.Node node) {
    		this.nodeSelected = node;
    	}
    }
    
    //===================================================================
    // ---------- VIEW CREATION -----------------------------------------
    //===================================================================
    /**
     * Create the content of the view
     */
    public void createPartControl(Composite aParent) {
		// prepare the font for metric columns: it is supposed to be fixed font
		Display display = Display.getCurrent();
		Utilities.setFontMetric(display);
		
		// Create the actions (flatten, unflatten,...) and the tollbar on top of the table
        this.objViewActions = new ScopeViewActions(this.getViewSite(),
        		aParent); //actions of the tree
        
		// ----- 03.21.2008 Laks: add virtual library for better memory consumption
    	treeViewer = new ScopeTreeViewer(aParent,SWT.BORDER|SWT.FULL_SELECTION | SWT.VIRTUAL);
    	// set the attributes
    	this.treeContentProvider = new ScopeTreeContentProvider(); 
    	treeViewer.setContentProvider(this.treeContentProvider);
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);
        //treeViewer.setAutoExpandLevel(2);

        // tell the action class that we have built the tree
        this.objViewActions.setTreeViewer(treeViewer);
        
        //----------------- create the column tree
        this.colTree = new TreeViewerColumn(treeViewer,SWT.LEFT, 0);
        this.colTree.getColumn().setText("Scope");
        this.colTree.getColumn().setWidth(200); //TODO dynamic size
        this.colTree.setLabelProvider(new ScopeLabelProvider(this.getSite().getWorkbenchWindow())); // laks addendum
        sorterTreeColummn = new ColumnViewerSorter(this.treeViewer, this.colTree.getColumn(), null,0); 

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
        		// this doesn't matter on Mac since the OS only one button
        		// but on other OS, this can make differences
        		if(event.button != 1) // yes, we only allow the first button 
        			return;
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
    		            	displayFileEditor(lineScope.getTreeNode());
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
    		        	displayFileEditor(objNode);
    		        }
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

		        if(selection.getFirstElement() instanceof Scope.Node) {
			        Scope.Node nodeSelected = (Scope.Node) selection.getFirstElement();
			        if(nodeSelected != null) {
			        	// update the state of the toolbar items
			        	objViewActions.checkButtons(nodeSelected);
			        }
		        } else {
		        	// selection on wrong node
		        }
		      }
		}); 
		
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
    	updateDisplay();
    }
    
    /**
     * Update the data view based on the XML experiment data
     * @param ex
     */
    public void setInput(Experiment ex) {
    	myExperiment = ex;
    	if (ex != null) {
    		myRootScope = ex.getRootScope();
    	}
    	updateDisplay();
    }

	/**
	 * Update the content of the tree view
	 */
	private void updateDisplay() {
        if (myExperiment == null)
        	return;
        int iColCount = this.treeViewer.getTree().getColumnCount();
        if(iColCount>1) {
        	// remove the columns blindly
        	// TODO we need to have a more elegant solution here
        	for(int i=1;i<iColCount;i++) {
        		this.treeViewer.getTree().getColumn(1).dispose();
        	}
        }
        // prepare the data for the sorter class for tree
        this.sorterTreeColummn.setMetric(myExperiment.getMetric(0));
        // prepare the experiment for the content provider of the tree column
        //this.treeContentProvider.setExperiment(myExperiment);
        // dirty solution to update titles
        TreeViewerColumn []colMetrics = new TreeViewerColumn[myExperiment.getMetricCount()];
        {
            // Update metric title labels
            String[] titles = new String[myExperiment.getMetricCount()+1];
            titles[0] = "Scope";	// unused element. Already defined
            // add table column for each metric
        	for (int i=0; i<myExperiment.getMetricCount(); i++)
        	{
        		titles[i+1] = myExperiment.getMetric(i).getDisplayName();	// get the title
        		colMetrics[i] = Utilities.addTreeColumn(treeViewer, myExperiment.getMetric(i), i+1, (i==0));
        	}
            treeViewer.setColumnProperties(titles); // do need this ??
            //treeViewer.getTree().setSelection(TreeItem);
        }
        
        // Update root scope
        treeViewer.setInput(myRootScope.getTreeNode());
        // update the window title
        this.getSite().getShell().setText("hpcviewer: "+myExperiment.getName());
        
        // update the root scope of the actions !
        this.objViewActions.updateContent(this.myExperiment, (RootScope)this.myRootScope, colMetrics);
        // FIXME: For unknown reason, the updateContent method above does not resize the column automatically,
        // so we need to do it here, manually ... sigh
        this.objViewActions.resizeColumns();	// resize the column to fit all metrics
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
    
    public void showProcessingMessage() {
    	this.objViewActions.showProcessingMessage();
    }
    public void restoreProcessingMessage() {
    	this.objViewActions.restoreProcessingMessage();
    }

}
