/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.jface.action.IStatusLineManager;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.resources.Icons;
import edu.rice.cs.hpc.viewer.util.ColumnProperties;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.viewer.util.Utilities;
/**
 * @author laksono
 *
 */
public class ScopeViewActionsGUI {

    //======================================================
	// ------ DATA ----------------------------------------
    //======================================================
    private TreeViewer 	treeViewer;		  	// tree for the caller and callees
    private ScopeViewActions objViewActions;
    private TreeViewerColumn []colMetrics;	// metric columns
    private Shell shell;
    private IStatusLineManager statusLine;

	//------------------------------------DATA
    public int iFlatLevel = 1;			// for unknown reason, the level starts with 1
    private Experiment 	myExperiment;		// experiment data	
    private Scope 		myRootScope;		// the root scope of this view

    // variable declaration uniquely for coolbar
	private ToolItem tiFlatten;		//flatten button
	private ToolItem tiUnFlatten ;	// unflatten button
	private ToolItem tiZoomin;		// zoom-in button
	private ToolItem tiZoomout ;	// zoom-out button
	private ToolItem tiResize ;		// resize column button
	private ToolItem tiColumns ;	// show/hide button
	private ToolItem tiHotCallPath;
	    

    /**
     * Constructor initializing the data
     * @param shellGUI
     * @param objViewer
     * @param fontMetricColumn
     * @param objActions
     */
	public ScopeViewActionsGUI(IViewSite viewSite, Composite parent, 
			ScopeViewActions objActions) {

		this.objViewActions = objActions;
		this.shell = viewSite.getShell();
		this.statusLine = viewSite.getActionBars().getStatusLineManager();
		
		// ----- coolbar
		this.createCoolBar(parent);
	}

	/**
	 * IMPORTANT: need to call this method once the content of tree is changed !
	 * Warning: call only this method when the tree has been populated !
	 * @param exp
	 * @param scope
	 * @param columns
	 */
	public void updateContent(Experiment exp, Scope scope, TreeViewerColumn []columns) {
		// save the new data and properties
		this.myExperiment = exp;
		this.myRootScope = scope;
		this.colMetrics = columns;
		this.setLevelText(scope.getTreeNode().iLevel+1);	// @TODO: initialized with root level
		
		// actions needed when a new experiment is loaded
		this.resizeTableColumns();	// we assume the data has been populated
        this.enableActions();
        // since we have a new content of experiment, we need to display 
        // the aggregate metrics
        this.displayRootExperiment();
	}
	
	/**
	 * Update the GUI when a flatten actions are performed
	 * @param iLevel
	 * @param showAggregate show in the root node the aggregate metrics
	 */
	public void updateFlattenView(int iLevel, boolean showAggregate) {
		if(showAggregate)
			this.displayRootExperiment();	// display the aggregate metrics
		this.updateFlattenView(iLevel);
	}
	/**
	 * Update the GUI when a flatten actions are performed
	 * @param iLevel: the level of flatten
	 */
	public void updateFlattenView(int iLevel) {
		this.setLevelText(iLevel);		// update the display of the level of flattening
	}
    //======================================================
    public void setTreeViewer(TreeViewer tree) {
    	this.treeViewer = tree;
    }

    public void insertParentNode(Scope.Node nodeParent) {
    	Scope scope = nodeParent.getScope();
    	int nbColumns = this.myExperiment.getMetricCount() + 1;
    	String []sText = new String[nbColumns];
    	sText[0] = new String(scope.getName());
    	// get the metrics for all columns
    	for (int i=0; i< nbColumns - 1; i++) {
        	edu.rice.cs.hpc.data.experiment.metric.Metric metric = this.myExperiment.getMetric(i);
        	sText[i+1] = scope.getMetricTextValue(metric);
    	}
    	// draw the root node item
    	Utilities.insertTopRow(treeViewer, Utilities.getScopeNavButton(scope), sText);
    }
	/**
	 * Add the aggregate metrics item on the top of the tree
	 */
    private void displayRootExperiment() {
    	//Scope.Node node = (Scope.Node)this.myRootScope.getTreeNode().getChildAt(0);
    	Scope.Node  node = (Scope.Node) this.myRootScope.getTreeNode();
    	this.insertParentNode(node);
    }
	
	/**
	 * Resize the columns automatically
	 * ATT: Please call this method once the data has been populated
	 */
	public void resizeTableColumns() {
        // resize the column according to the data size
		int nbCols = this.colMetrics.length;
        for (int i=0; i<nbCols; i++) {
        	TreeColumn column = this.colMetrics[i].getColumn();
        	// do NOT resize if the column is hidden
        	if(column.getWidth()>1)
        		column.pack();
        }
	}

	//======================================================
    // ................ GUI and LAYOUT ....................
    //======================================================
	
	/**
	 * Reset the button and actions into disabled state
	 */
	public void resetActions() {
		this.tiFlatten.setEnabled(false);
		this.tiUnFlatten.setEnabled(false);
		this.tiZoomin.setEnabled(false);
		this.tiZoomout.setEnabled(false);
		this.tiResize.setEnabled(false);
		this.tiColumns.setEnabled(false);
		this.tiHotCallPath.setEnabled(false);
	}
	
	/**
	 * Enable the some actions (resize and column properties) actions for this view
	 */
	public void enableActions() {
		this.tiResize.setEnabled(true);
		this.tiColumns.setEnabled(true);
	}
	
	/**
	 * Display the new level of flattening on the info toolbar
	 * @param iLevel
	 */
	private void setLevelText(int iLevel) {
		this.iFlatLevel = iLevel;
		this.statusLine.setMessage("Node level: "+iLevel + " / " + ((RootScope)this.myRootScope).MAX_LEVELS );
		// every time we change the level, we need to check the status of the buttons
		this.checkFlattenButtons();
	}
    
    /**
     * Show column properties (hidden, visible ...)
     */
    private void showColumnsProperties() {
    	ColumnProperties objProp = new ColumnProperties(this.shell, this.colMetrics);
    	objProp.open();
    	if(objProp.getReturnCode() == org.eclipse.jface.dialogs.IDialogConstants.OK_ID) {
        	boolean result[] = objProp.getResult();
           	for(int i=0;i<result.length;i++) {
           		// hide this column
           		if(!result[i]) {
           			int iWidth = this.colMetrics[i].getColumn().getWidth();
           			if(iWidth > 0) {
               			Integer objWidth = Integer.valueOf(iWidth); 
               			this.colMetrics[i].getColumn().setData(objWidth);
               			this.colMetrics[i].getColumn().setWidth(0);
           			}
           		} else {
           			// display the hidden column
            		Object o = this.colMetrics[i].getColumn().getData();
           			int iWidth = 120;
           			if((o != null) && (o instanceof Integer) ) {
           				iWidth = ((Integer)o).intValue();
               			this.colMetrics[i].getColumn().setWidth(iWidth);
           			}
           		}
           	}
   		}
    }
    
    //======================================================
    // ................ BUTTON ............................
    //======================================================
    /**
     * Check zoom buttons (zoom out and zoom in)
     * @param node: the current selected node
     */
    public void checkZoomButtons(Scope.Node node) {
    	tiZoomout.setEnabled(shouldZoomOutBeEnabled(node));
    	boolean b = shouldZoomInBeEnabled(node);
    	tiZoomin.setEnabled(b);
    	this.tiHotCallPath.setEnabled(b);
    }
    /**
     * Check if flatten/unflatten buttons need to be disable or not.
     */
    public void checkFlattenButtons() {
    	tiFlatten.setEnabled(shouldFlattenBeEnabled());
    	tiUnFlatten.setEnabled(shouldUnflattenBeEnabled());
    }

    private boolean shouldFlattenBeEnabled() {
    	return(this.iFlatLevel<((RootScope)this.myRootScope).MAX_LEVELS );
    }
    
    /**
     * Verify if unflatten can be done
     * @param node root node
     * @return
     */
    private boolean shouldUnflattenBeEnabled() {
    	return (this.iFlatLevel>1);
    }

    //======================================================
    // ................ STATIC ............................
    //======================================================
    /**
     * Check if the button Zoom-in should be available given node as 
     * the main node to zoom
     * @param node
     * @return
     */
    static public boolean shouldZoomInBeEnabled(Scope.Node node) {
    	return (node.getChildCount()>0);
    }
    
    /**
     * Check if the button zoom-out should be enable 
     * @param node
     * @return
     */
    static public boolean shouldZoomOutBeEnabled(Scope.Node node) {
    	if(node.getParent() != null) {
    		Scope.Node parent = (Scope.Node) node.getParent(); 
    		return !(parent.getScope() instanceof RootScope );
    	}
    	return (false);
    }

    //======================================================
    // ................ CREATION ............................
    //======================================================
    /**
     * Creating an item for the existing coolbar
     * @param coolBar
     * @param toolBar
     */
    private void createCoolItem(CoolBar coolBar, Control toolBar) {
    	CoolItem coolItem = new CoolItem(coolBar, SWT.NULL);
    	coolItem.setControl(toolBar);
    	org.eclipse.swt.graphics.Point size =
    		toolBar.computeSize( SWT.DEFAULT,
    	                           SWT.DEFAULT);
    	org.eclipse.swt.graphics.Point coolSize = coolItem.computeSize (size.x, size.y);
    	coolItem.setSize(coolSize);    	
    }
    
    
	/**
     * Create a toolbar region on the top of the view. This toolbar will be used to host some buttons
     * to make actions on the treeview.
     * @param aParent
     * @return Composite of the view. The tree should be based on this composite.
     */
    private Composite createCoolBar(Composite aParent) {
    	// make the parent with grid layout
    	GridLayout grid = new GridLayout(1,false);
    	aParent.setLayout(grid);
    	CoolBar coolBar = new CoolBar(aParent, SWT.FLAT);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
    	coolBar.setLayoutData(data);

    	// prepare the toolbar
    	ToolBar toolbar = new ToolBar(coolBar, SWT.FLAT);
    	Icons iconsCollection = Icons.getInstance();
    	
    	// ------------- prepare the items
    	// flatten
    	tiFlatten = new ToolItem(toolbar, SWT.PUSH);
    	tiFlatten.setToolTipText("Flatten nodes one level");
    	tiFlatten.setImage(iconsCollection.imgFlatten);
    	tiFlatten.addSelectionListener(new SelectionAdapter() {
      	  	public void widgetSelected(SelectionEvent e) {
      	  		objViewActions.flattenNode();
      	  	}
      	});
    	
    	// unflatten
    	tiUnFlatten = new ToolItem(toolbar, SWT.PUSH);
    	tiUnFlatten.setToolTipText("Unflatten nodes one level");
    	tiUnFlatten.setImage(iconsCollection.imgUnFlatten);
    	tiUnFlatten.addSelectionListener(new SelectionAdapter(){
      	  	public void widgetSelected(SelectionEvent e) {
      	  		objViewActions.unflattenNode();
      	  	}    		
    	});
    	
    	// zoom in
    	tiZoomin = new ToolItem(toolbar, SWT.PUSH);
    	tiZoomin.setToolTipText("Zoom-in the selected node");
    	tiZoomin.setImage(iconsCollection.imgZoomIn);
    	tiZoomin.addSelectionListener(new SelectionAdapter() {
      	  	public void widgetSelected(SelectionEvent e) {
      	  	objViewActions.zoomIn();
      	  	}
      	});
    	
    	// zoom out
    	tiZoomout = new ToolItem(toolbar, SWT.PUSH);
    	tiZoomout.setToolTipText("Zoom-out the selected node");
    	tiZoomout.setImage(iconsCollection.imgZoomOut);
    	tiZoomout.addSelectionListener(new SelectionAdapter() {
    	  public void widgetSelected(SelectionEvent e) {
    		  objViewActions.zoomOut();
    	  }
    	});
    	
    	new ToolItem(toolbar, SWT.SEPARATOR);
    	// hot call path
    	this.tiHotCallPath= new ToolItem(toolbar, SWT.PUSH);
    	tiHotCallPath.setToolTipText("Expand the hot path below the selected node");
    	tiHotCallPath.setImage(iconsCollection.imgFlame);
    	tiHotCallPath.addSelectionListener(new SelectionAdapter() {
    	  public void widgetSelected(SelectionEvent e) {
    		  objViewActions.showHotCallPath();
    	  }
    	});
    	
    	new ToolItem(toolbar, SWT.SEPARATOR);
    	
    	tiResize = new ToolItem(toolbar, SWT.PUSH);
    	tiResize.setToolTipText("Resize columns width");
    	tiResize.setImage(iconsCollection.imgResize);
    	tiResize.addSelectionListener(new SelectionAdapter() {
      	  public void widgetSelected(SelectionEvent e) {
          	resizeTableColumns();
      	  }
      	});
    	
    	this.tiColumns = new ToolItem(toolbar, SWT.PUSH);
    	tiColumns.setImage(iconsCollection.imgColumns);
    	tiColumns.setToolTipText("Hide/show columns");
    	tiColumns.addSelectionListener(new SelectionAdapter() {
        	  public void widgetSelected(SelectionEvent e) {
        		  showColumnsProperties();
        	  }
        	});
    	
        // set the coolitem
    	this.createCoolItem(coolBar, toolbar);
    	this.resetActions();

    	return aParent;
    }
}
