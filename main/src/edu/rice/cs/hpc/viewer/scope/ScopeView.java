package edu.rice.cs.hpc.viewer.scope;

// User interface
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

// SWT
import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

// Jface
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewerColumn;

// HPC
import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.viewer.resources.*;
import edu.rice.cs.hpc.viewer.util.EditorManager;
import edu.rice.cs.hpc.viewer.util.ColumnProperties;
import org.eclipse.swt.graphics.Font;

public class ScopeView extends ViewPart {
    public static final String ID = "edu.rice.cs.hpc.scope.ScopeView";

    private TreeViewer 	treeViewer;		  	// tree for the caller and callees
    private TreeViewerColumn colTree;		// column for the calls tree
    private TreeViewerColumn []colMetrics;	// metric columns
    private Experiment 	myExperiment;		// experiment data	
    private Scope 		myRootScope;		// the root scope of this view
    private ColumnViewerSorter sorterTreeColummn;
    private EditorManager editorSourceCode;
    private Font fontColumn;
	
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
    	this.editorSourceCode.displayFileEditor(node);
    }

    //======================================================
    // ................ ACTIONS ............................
    //======================================================
	
	/**
	 * Go deeper one level
	 */
	private void flattenNode() {
		Object objRoot = this.treeViewer.getTree().getItem(0).getData();
		Scope.Node objNode = (Scope.Node) objRoot;
		if (objRoot instanceof Scope.Node) {
			// original scope node
			objNode = (Scope.Node) objRoot;
		} else if(objRoot instanceof ArrayOfNodes) {
			// this item has been flatten. we will flatten again based on the first item
			ArrayOfNodes objArrays = (ArrayOfNodes) objRoot;
			objNode = objArrays.get(0);
		} else {
			// unknown class. this is almost impossible unless the code is really messy or we have bugs
			System.err.println("ScopeView-Flatten err unknown object:"+objRoot.getClass());
			return;
		}
		// get the reference node
		Scope.Node node = (Scope.Node) objNode;
		Integer objLevel = Integer.valueOf(node.iLevel+1);
		// get the next level
		ArrayOfNodes nodeArray = ((RootScope)this.myRootScope).getTableOfNodes().get(objLevel);
		if(nodeArray != null) {
			this.treeViewer.setInput(nodeArray);
			this.CheckFlattenButtons();
			//treeViewer.refresh();		
		} else {
			// there is something wrong. we return to the original node
			System.err.println("ScopeView-flatten: error cannot flatten further");
		}
	}
	
	/**
	 * go back one level
	 */
	private void unflattenNode() {
		Object objRoot = this.treeViewer.getTree().getItem(0).getData();
		Scope.Node objNode = (Scope.Node) objRoot;
		if (objRoot instanceof Scope.Node) {
			// original scope node
			objNode = (Scope.Node) objRoot;
		} else if(objRoot instanceof ArrayOfNodes) {
			// this item has been flatten. we will flatten again based on the first item
			ArrayOfNodes objArrays = (ArrayOfNodes) objRoot;
			objNode = objArrays.get(0);
		} else {
			// unknown class. this is almost impossible unless the code is really messy or we have bugs
			System.err.println("ScopeView-Flatten err unknown object:"+objRoot.getClass());
			return;
		}
		// get the reference node
		Scope.Node node = (Scope.Node) objNode;
		if(node.iLevel<2) return;
		Integer objLevel = Integer.valueOf(node.iLevel-1);
		ArrayOfNodes nodeArray = ((RootScope)this.myRootScope).getTableOfNodes().get(objLevel);
		if(nodeArray != null) {
			this.treeViewer.setInput(nodeArray);
			this.CheckFlattenButtons();
		} else {
			//treeViewer.setInput(node);
		}
		//treeViewer.refresh();
	}
	
	/**
	 * Zoom-in the children
	 */
	private void zoomIn() {
		ISelection sel = treeViewer.getSelection();
		if (!(sel instanceof StructuredSelection))
			return;
		Object o = ((StructuredSelection)sel).getFirstElement();
		if (!(o instanceof Scope.Node)) {
			System.err.println("ScopeView - zoomin:"+o.getClass());
			return;
		}
		treeViewer.setInput(o);
		//treeViewer.refresh();
	}
	
	/**
	 * Zoom-out the node
	 */
	private void zoomOut() {
		Object o = treeViewer.getInput();
		Scope.Node child;
		if (!(o instanceof Scope.Node)) {
			if(o instanceof ArrayOfNodes) {
				TreeItem []tiObjects = this.treeViewer.getTree().getItems();
				child = (Scope.Node)tiObjects[0].getData();
				// tricky solution when zoom-out the flattened node
				child = (Scope.Node)child.getParent();
			} else {
				System.err.println("ScopeView - zoomout:"+o.getClass());
				return;
			}
		} else
			child = (Scope.Node) o;
		Scope.Node parent = (Scope.Node)child.getParent();
		if (parent == null)
			return;
		treeViewer.setInput( parent );
		//treeViewer.refresh();
	}
	
	/**
	 * Resize the columns automatically
	 * ATT: Please call this method once the data has been populated
	 */
	private void resizeTableColumns() {
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
	
	//------------------------------------DATA
    // variable declaration uniquely for GUI tooltips
	private ToolItem tiFlatten;
	private ToolItem tiUnFlatten ;
	private ToolItem tiZoomin;
	private ToolItem tiZoomout ;
	private ToolItem tiResize ;
	private ToolItem tiColumns ;
	    
    private Action acZoomin = new Action("Zoom-in"){
    	public void run() {
    		zoomIn();
    	}
    };
    
    private Action acZoomout = new Action("Zoom-out"){
    	public void run() {
    		zoomOut();
    	}
    };
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
	}
	
	/**
	 * Enable the some actions (resize and column properties) actions for this view
	 */
	public void enableActions() {
		this.tiResize.setEnabled(true);
		this.tiColumns.setEnabled(true);
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
      	  		flattenNode();
      	  	}
      	});
    	
    	// unflatten
    	tiUnFlatten = new ToolItem(toolbar, SWT.PUSH);
    	tiUnFlatten.setToolTipText("Unflatten nodes one level");
    	tiUnFlatten.setImage(iconsCollection.imgUnFlatten);
    	tiUnFlatten.addSelectionListener(new SelectionAdapter(){
      	  	public void widgetSelected(SelectionEvent e) {
      	  	unflattenNode();
      	  	}    		
    	});
    	
    	// zoom in
    	tiZoomin = new ToolItem(toolbar, SWT.PUSH);
    	tiZoomin.setToolTipText("Zoom-in the selected node");
    	tiZoomin.setImage(iconsCollection.imgZoomIn);
    	tiZoomin.addSelectionListener(new SelectionAdapter() {
      	  	public void widgetSelected(SelectionEvent e) {
      	  	zoomIn();
      	  	}
      	});
    	
    	// zoom out
    	tiZoomout = new ToolItem(toolbar, SWT.PUSH);
    	tiZoomout.setToolTipText("Zoom-out the selected node");
    	tiZoomout.setImage(iconsCollection.imgZoomOut);
    	tiZoomout.addSelectionListener(new SelectionAdapter() {
    	  public void widgetSelected(SelectionEvent e) {
    		  zoomOut();
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
    	
    	this.tiColumns = new ToolItem(toolbar, SWT.MENU);
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
    
    /**
     * Show column properties (hidden, visible ...)
     */
    private void showColumnsProperties() {
    	ColumnProperties objProp = new ColumnProperties(this.getSite().getShell(), this.colMetrics);
    	objProp.open();
    		if(objProp.getReturnCode() == org.eclipse.jface.dialogs.IDialogConstants.OK_ID) {
        		boolean result[] = objProp.getResult();
            	for(int i=0;i<result.length;i++) {
            		if(!result[i]) {
            			int iWidth = this.colMetrics[i].getColumn().getWidth();
            			if(iWidth > 0) {
                			Integer objWidth = Integer.valueOf(iWidth); 
                			this.colMetrics[i].getColumn().setData(objWidth);
                			this.colMetrics[i].getColumn().setWidth(0);
            			}
            		} else {
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
    // ................ CREATION ............................
    //======================================================
    /**
     * Creating an item for the existing coolbar
     * @param coolBar
     * @param toolBar
     */
    private void createCoolItem(CoolBar coolBar, ToolBar toolBar) {
    	CoolItem coolItem = new CoolItem(coolBar, SWT.NULL);
    	coolItem.setControl(toolBar);
    	org.eclipse.swt.graphics.Point size =
    		toolBar.computeSize( SWT.DEFAULT,
    	                           SWT.DEFAULT);
    	org.eclipse.swt.graphics.Point coolSize = coolItem.computeSize (size.x, size.y);
    	coolItem.setSize(coolSize);    	
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

    private boolean shouldZoomInBeEnabled(Scope.Node node) {
    	return (node.getChildCount()>0);
    }
    
    private boolean shouldZoomOutBeEnabled(Scope.Node node) {
    	return (node.iLevel>1);
    }
    
    private boolean shouldFlattenBeEnabled(Scope.Node node) {
    	return (!node.isLeaf());
    }
    
    /**
     * Verify if unflatten can be done
     * @param node root node
     * @return
     */
    private boolean shouldUnflattenBeEnabled(Scope.Node node) {
    	//System.out.println("ScopeView: unflatten:" + node.getScope().getShortName() + " is: " + (node.iLevel>1));
    	return (node.iLevel>1);
    }
    /**
     * Helper method to know if an item has been selected
     * @return true if an item is selected, false otherwise
     */
    public boolean isItemSelected() {
    	return (this.treeViewer.getTree().getSelectionCount() > 0);
    }
    
    /**
     * Helper method to retrieve the selected item
     * @return
     */
    private Scope.Node getSelectedItem() {
        TreeItem[] selection = this.treeViewer.getTree().getSelection();
        return (Scope.Node)selection[0].getData();
    }
    /**
     * Creating the context submenu for the view
     * TODO Created only the line selected
     * @param mgr
     */
    private void fillContextMenu(IMenuManager mgr) {
    	Scope.Node node = this.getSelectedItem();
    	// ---- zoomin
        mgr.add(acZoomin);
        acZoomin.setEnabled(this.shouldZoomInBeEnabled(node));
        // ---- zoomout
        mgr.add(acZoomout);
        acZoomout.setEnabled(this.shouldZoomOutBeEnabled(node));
        // additional feature
        mgr.add(new Separator());
        mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        Scope scope = node.getScope();
        // show the source code
        if(node.hasSourceCodeFile) {
            // show the editor source code
        	String sMenuTitle ;
        	if(scope instanceof FileScope) {
        		sMenuTitle = "Show " + scope.getSourceFile().getName();
        	} else
        		sMenuTitle= "Show "+scope.getToolTip(); // the tooltip contains the info we need: file and the linenum
            mgr.add(new ScopeViewTreeAction(sMenuTitle, node){
                	public void run() {
                		displayFileEditor(this.nodeSelected);
                	}
            });
        }
        // show the call site in case this one exists
        if(scope instanceof CallSiteScope) {
        	CallSiteScope callSiteScope = (CallSiteScope) scope;
        	LineScope lineScope = (LineScope) callSiteScope.getLineScope();
        	// do not show up in the menu context if the callsite does not exist
//        	if(lineScope.getTreeNode().hasSourceCodeFile) {
            	String sMenuTitle = "Callsite "+lineScope.getToolTip();
                mgr.add(new ScopeViewTreeAction(sMenuTitle, lineScope.getTreeNode()){
                	public void run() {
                		displayFileEditor(this.nodeSelected);
                	}
                });
 //       	}
        }
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
    /**
     * Create the content of the view
     */
    public void createPartControl(Composite aParent) {
		// ----- coolbar
    	Composite parent = this.createCoolBar(aParent);

		// -----
    	treeViewer = new TreeViewer(parent,SWT.BORDER|SWT.FULL_SELECTION);
    	// set the attributes
    	treeViewer.setContentProvider(new ScopeTreeContentProvider());
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);

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
        this.createContextMenu();

        //------------------------ LISTENER
		// allow other views to listen for selections in this view (site)
		this.getSite().setSelectionProvider(treeViewer);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event)
		      {
		        IStructuredSelection selection =
		          (IStructuredSelection) event.getSelection();

		        Scope.Node nodeSelected = (Scope.Node) selection.getFirstElement();
		        if(nodeSelected != null) {
		        	// update the state of the toolbar items
		        	CheckZoomButtons(nodeSelected);
		        	CheckFlattenButtons();
					if(nodeSelected.hasSourceCodeFile)
						displayFileEditor(nodeSelected);

		        }
		      }
		});
		
		// prepare the font for metric columns: it is supposed to be fixed font
		Display display = Display.getCurrent();
		int iHeight = display.getSystemFont().getFontData()[0].getHeight();
		this.fontColumn = new Font(display, "Courier New", iHeight, SWT.BOLD); // johnmc - was SWT.NONE
	}
	
    /**
     * Check zoom buttons (zoom out and zoom in)
     * @param node: the current selected node
     */
    private void CheckZoomButtons(Scope.Node node) {
    	tiZoomout.setEnabled(shouldZoomOutBeEnabled(node));
    	tiZoomin.setEnabled(shouldZoomInBeEnabled(node));
    }
    /**
     * Check if flatten/unflatten buttons need to be disable or not.
     */
    private void CheckFlattenButtons() {
    	TreeItem rootItem = treeViewer.getTree().getItem(0);
    	// now we prepare for the flatten
    	Scope.Node nodeRoot = null;
    	Object o = rootItem.getData(); 
    	if(o instanceof Scope.Node) {
    		nodeRoot = (Scope.Node) rootItem.getData();
    	} else if(o instanceof ArrayOfNodes) {
    		ArrayOfNodes objArray = (ArrayOfNodes) rootItem.getData();
    		nodeRoot = (Scope.Node) objArray.get(0);
    	}
    	tiFlatten.setEnabled(shouldFlattenBeEnabled(nodeRoot));
    	tiUnFlatten.setEnabled(shouldUnflattenBeEnabled(nodeRoot));
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
    	myRootScope = scope; //.getParentScope(); // try to get the aggregate value
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
        this.sorterTreeColummn.setMetric(myExperiment.getMetric(0));
        // dirty solution to update titles
        this.colMetrics = new TreeViewerColumn[myExperiment.getMetricCount()];
        {
            // Update metric title labels
            String[] titles = new String[myExperiment.getMetricCount()+1];
            titles[0] = "Scope";	// unused element. Already defined
            // add table column for each metric
        	for (int i=0; i<myExperiment.getMetricCount(); i++)
        	{
        		titles[i+1] = myExperiment.getMetric(i).getDisplayName();	// get the title
        		colMetrics[i] = new TreeViewerColumn(treeViewer,SWT.LEFT);	// add column
        		colMetrics[i].getColumn().setText(titles[i+1]);	// set the title
        		colMetrics[i].getColumn().setWidth(120); //TODO dynamic size
        		colMetrics[i].getColumn().setAlignment(SWT.RIGHT);
        		
        		// laks: addendum for column        		
        		this.colMetrics[i].setLabelProvider(new MetricLabelProvider( 
        				myExperiment.getMetric(i), this.fontColumn));
        		this.colMetrics[i].getColumn().setMoveable(true);
        		//tmp.pack();			// resize as much as possible
        		new ColumnViewerSorter(this.treeViewer, colMetrics[i].getColumn(), myExperiment.getMetric(i),i+1); // sorting mechanism
        		
        	}
            treeViewer.setColumnProperties(titles);
        }
        
        // Update root scope
        treeViewer.setInput(myRootScope.getTreeNode());

        // update the window title
        this.getSite().getShell().setText("HPCViewer: "+myExperiment.getName());
    	resizeTableColumns();
        
        // generate flattening structure 
        ((RootScope)this.myRootScope).createFlattenNode();
        ((RootScope)this.myRootScope).printFlattenNodes();
    
        this.enableActions();
        this.CheckFlattenButtons();
        // no node is selected, so all zoom buttons are disabled
        //this.CheckZoomButtons(myRootScope.getTreeNode());
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
    

}
