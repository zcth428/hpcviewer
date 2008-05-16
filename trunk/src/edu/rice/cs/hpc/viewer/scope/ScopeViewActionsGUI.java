/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.layout.*;

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
import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
//import org.eclipse.jface.action.IStatusLineManager;

import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.resources.Icons;
import edu.rice.cs.hpc.viewer.util.ColumnProperties;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.viewer.util.Utilities;
//import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.ExtDerivedMetric;
import edu.rice.cs.hpc.data.experiment.metric.Metric;
/**
 * @author laksono
 *
 */
public class ScopeViewActionsGUI {

    //======================================================
	// ------ DATA ----------------------------------------
    //======================================================
	// GUI STUFFs
    private TreeViewer 	treeViewer;		  	// tree for the caller and callees
    private ScopeViewActions objViewActions;
    private TreeViewerColumn []colMetrics;	// metric columns
    private Shell shell;
    //private IStatusLineManager statusLine;

    // variable declaration uniquely for coolbar
	private ToolItem tiFlatten;		//flatten button
	private ToolItem tiUnFlatten ;	// unflatten button
	private ToolItem tiZoomin;		// zoom-in button
	private ToolItem tiZoomout ;	// zoom-out button
	private ToolItem tiColumns ;	// show/hide button
	private ToolItem tiHotCallPath;
	//private ToolItem tiAddMetric; 	// add a new derived metric
	private ToolItem tiAddExtMetric;
//	private ToolBar tbMessageToolbar;
//	private ToolItem tiMessage;
	private Label lblMessage;
	
	//------------------------------------DATA
	private Scope.Node nodeTopParent; // the current node which is on the top of the table (used as the aggregate node)
    private Experiment 	myExperiment;		// experiment data	
    private RootScope 		myRootScope;		// the root scope of this view

    // ----------------------------------- CONSTANTS
    private Color clrYELLOW, clrRED, clrNORMAL;
    
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
		//this.statusLine = viewSite.getActionBars().getStatusLineManager();
		
		this.clrNORMAL = this.shell.getBackground();
		this.clrYELLOW = new Color(this.shell.getDisplay(),255,255,0);
		this.clrRED = new Color(this.shell.getDisplay(), 250,128,114);
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
	public void updateContent(Experiment exp, RootScope scope, TreeViewerColumn []columns) {
		// save the new data and properties
		this.myExperiment = exp;
		this.myRootScope = scope;
		this.colMetrics = columns;
		//this.setLevelText(scope.getTreeNode().iLevel);	// @TODO: initialized with root level
		
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
		//this.updateFlattenView(iLevel);
	}
	/**
	 * Update the GUI when a flatten actions are performed
	 * @param iLevel: the level of flatten
	 */
	/*
	public void updateFlattenView(int iLevel) {
		//this.setLevelText(iLevel);		// update the display of the level of flattening
	}*/
    //======================================================
    public void setTreeViewer(TreeViewer tree) {
    	this.treeViewer = tree;
    }

    /**
     * Inserting a "node header" on the top of the table to display
     * either aggregate metrics or "parent" node (due to zoom-in)
     * TODO: we need to shift to the left a little bit
     * @param nodeParent
     */
    public void insertParentNode(Scope.Node nodeParent) {
    	Scope scope = nodeParent.getScope();
    	int nbColumns = this.myExperiment.getMetricCount() + 1;	// coloumns in base metrics
    	String []sText = new String[nbColumns];
    	sText[0] = new String(scope.getName());
    	// --- prepare text for base metrics
    	// get the metrics for all columns
    	for (int i=0; i< nbColumns - 1; i++) {
        	Metric metric = this.myExperiment.getMetric(i);
        	/*if(metric instanceof DerivedMetric) {
        		sText[i+1] = DerivedMetric.getTextValue(scope, (DerivedMetric)metric);
        		//System.out.println("SVAG:"+i+":"+sText[i+1]);
        	} else */
        		if(metric instanceof ExtDerivedMetric) {
        		sText[i+1] = ((ExtDerivedMetric)metric).getTextValue(scope);
        	} else
        		sText[i+1] = scope.getMetricTextValue(metric);
    	}
    	
    	// draw the root node item
    	Utilities.insertTopRow(treeViewer, Utilities.getScopeNavButton(scope), sText);
    	this.nodeTopParent = nodeParent;
    }
    
    /**
     * Restoring the "node header" in case of refresh method in the viewer
     */
    private void restoreParentNode() {
    	if(this.nodeTopParent != null) {
    		this.insertParentNode(this.nodeTopParent);
    	}
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
	 * Show a warning message (with yellow background).
	 * The caller has to remove the message and restore it to the original state
	 * by calling restoreMessage() method
	 */
	public void showWarningMessagge(String sMsg) {
		this.lblMessage.setBackground(this.clrYELLOW);
		this.lblMessage.setText(sMsg);
	}
	
	/**
	 * Show an error message on the message bar. It is the caller responsibility to 
	 * remove the message
	 * @param sMsg
	 */
	public void showErrorMessage(String sMsg) {
		this.lblMessage.setBackground(this.clrRED);
		this.lblMessage.setText(" " + sMsg);
	}

	/**
	 * Restore the message bar into the original state
	 */
	public void restoreMessage() {
		if(this.lblMessage != null) {
			this.lblMessage.setBackground(this.clrNORMAL);
			this.lblMessage.setText("");
		}
	}
	/**
	 * Reset the button and actions into disabled state
	 */
	public void resetActions() {
		this.tiFlatten.setEnabled(false);
		this.tiUnFlatten.setEnabled(false);
		this.tiZoomin.setEnabled(false);
		this.tiZoomout.setEnabled(false);
//		this.tiResize.setEnabled(false);
		this.tiColumns.setEnabled(false);
		this.tiHotCallPath.setEnabled(false);
		//this.tiAddMetric.setEnabled(false);
		this.tiAddExtMetric.setEnabled(false);
	}
	
	/**
	 * Enable the some actions (resize and column properties) actions for this view
	 */
	public void enableActions() {
//		this.tiResize.setEnabled(true);
		this.tiColumns.setEnabled(true);
		//this.tiAddMetric.setEnabled(true);
		this.tiAddExtMetric.setEnabled(true);
	}
	
	/**
	 * Display the new level of flattening on the info toolbar
	 * @param iLevel
	 */
	/*
	private void setLevelText(int iLevel) {
		//this.iFlatLevel = iLevel;
		this.statusLine.setMessage("Node level: "+iLevel +
				" / " + this.myRootScope.getMaxLevel());
		// every time we change the level, we need to check the status of the buttons
		this.checkFlattenButtons();
	}*/
    
	/**
	 * Hiding a metric column
	 * @param iColumnPosition: the index of the metric
	 */
	public void hideMetricColumn(int iColumnPosition) {
			int iWidth = this.colMetrics[iColumnPosition].getColumn().getWidth();
   			if(iWidth > 0) {
       			Integer objWidth = Integer.valueOf(iWidth); 
       			this.colMetrics[iColumnPosition].getColumn().setData(objWidth);
       			this.colMetrics[iColumnPosition].getColumn().setWidth(0);
   			}
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
           			this.hideMetricColumn(i);
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
    
    /**
     * Add a new metric column
     * @param colMetric
     */
    public void addMetricColumns(TreeViewerColumn colMetric) {
    	int nbCols = this.colMetrics.length + 1;
    	TreeViewerColumn arrColumns[] = new TreeViewerColumn[nbCols];
    	for(int i=0;i<nbCols-1;i++)
    		arrColumns[i] = this.colMetrics[i];
    	arrColumns[nbCols-1] = colMetric;
    	this.colMetrics = arrColumns;
    	// when adding a new column, we have to refresh the viewer
    	// and this means we have to recompute again the top row of the table
    	this.restoreParentNode();
    }
    //======================================================
    // ................ BUTTON ............................
    //======================================================
    /**
     * Check zoom buttons (zoom out and zoom in)
     * @param node: the current selected node
     */
    public void checkZoomButtons(Scope.Node node) {
    	tiZoomout.setEnabled(this.shouldZoomOutBeEnabled());
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
    	return this.myRootScope.getTreeNode().getDepth()>this.myRootScope.getFlattenLevel() + 1;
    	//return(this.iFlatLevel<((RootScope)this.myRootScope).MAX_LEVELS );
    }
    
    /**
     * Verify if unflatten can be done
     * @param node root node
     * @return
     */
    private boolean shouldUnflattenBeEnabled() {
    	return (this.myRootScope.getFlattenLevel()>0);
    	//return (this.iFlatLevel>1);
    }

    //======================================================
    // ................ ZOOM ............................
    //======================================================
    /**
     * Check if the button Zoom-in should be available given node as 
     * the main node to zoom
     * @param node
     * @return
     */
    static public boolean shouldZoomInBeEnabled(Scope.Node node) {
    	if(node != null)
    		return (node.getChildCount()>0);
    	else
    		return false;
    }
    
    /**
     * Check if the button zoom-out should be enable 
     * @return
     */
    public boolean shouldZoomOutBeEnabled() {
    	return this.objViewActions.shouldZoomOutBeEnabled();
    	
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
    	Composite toolbarArea = new Composite(aParent, SWT.NONE);
    	GridLayout grid = new GridLayout(1,false);
    	aParent.setLayout(grid);
    	CoolBar coolBar = new CoolBar(toolbarArea, SWT.FLAT);
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
      	  		//objViewActions.flattenNode();
      	  		objViewActions.flatten();
      	  	}
      	});
    	
    	// unflatten
    	tiUnFlatten = new ToolItem(toolbar, SWT.PUSH);
    	tiUnFlatten.setToolTipText("Unflatten nodes one level");
    	tiUnFlatten.setImage(iconsCollection.imgUnFlatten);
    	tiUnFlatten.addSelectionListener(new SelectionAdapter(){
      	  	public void widgetSelected(SelectionEvent e) {
      	  		//objViewActions.unflattenNode();
      	  		objViewActions.unflatten();
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
    	/*this.tiAddMetric = new ToolItem(toolbar, SWT.PUSH);
    	this.tiAddMetric.setToolTipText("Add a new derived metric");
    	this.tiAddMetric.setImage(iconsCollection.imgAddMetric);
    	this.tiAddMetric.addSelectionListener(new SelectionAdapter(){
      	  public void widgetSelected(SelectionEvent e) {
    		  objViewActions.addNewMetric();
    	  }
    	}); */
    	
    	this.tiAddExtMetric = new ToolItem(toolbar, SWT.PUSH);
    	tiAddExtMetric.setImage(iconsCollection.imgExtAddMetric);
    	tiAddExtMetric.setToolTipText("Add a new derived metric");
    	tiAddExtMetric.addSelectionListener(new SelectionAdapter(){
    		public void widgetSelected(SelectionEvent e) {
    			objViewActions.addExtNewMetric();
    		}
    	});
    /*	
    	tiResize = new ToolItem(toolbar, SWT.PUSH);
    	tiResize.setToolTipText("Resize columns width");
    	tiResize.setImage(iconsCollection.imgResize);
    	tiResize.addSelectionListener(new SelectionAdapter() {
      	  public void widgetSelected(SelectionEvent e) {
          	resizeTableColumns();
      	  }
      	});
  */  	
    	this.tiColumns = new ToolItem(toolbar, SWT.PUSH);
    	tiColumns.setImage(iconsCollection.imgColumns);
    	tiColumns.setToolTipText("Hide/show columns");
    	tiColumns.addSelectionListener(new SelectionAdapter() {
        	  public void widgetSelected(SelectionEvent e) {
        		  showColumnsProperties();
        	  }
        	});
    	
    	new ToolItem(toolbar, SWT.SEPARATOR);
        // set the coolitem
    	this.createCoolItem(coolBar, toolbar);
    	
    	// message text
    	// tbMessageToolbar = new ToolBar(coolBar, SWT.FLAT);
    	// tiMessage = new ToolItem(tbMessageToolbar, SWT.NONE);
    	// this.createCoolItem(coolBar, tbMessageToolbar);
    	lblMessage = new Label(toolbarArea, SWT.NONE);
    	lblMessage.setText("");
    	//lblMessage.setSize(1000, SWT.DefaultSelection);
    	GridDataFactory.fillDefaults().grab(false, false).applyTo(coolBar);
    	GridDataFactory.fillDefaults().grab(true, false).applyTo(lblMessage);
    	GridDataFactory.fillDefaults().grab(true, false).applyTo(toolbarArea);
    	GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(toolbarArea);

    	this.resetActions();
    	return aParent;
    }
    
}
