package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.core.filesystem.EFS;

import edu.rice.cs.hpc.data.experiment.*;
import edu.rice.cs.hpc.data.experiment.source.*;
import edu.rice.cs.hpc.data.experiment.scope.*;
import edu.rice.cs.hpc.data.experiment.metric.*;
//import edu.rice.cs.HPCVision.data.PNode.*;
//import edu.rice.cs.HPCVision.toolkits.HPCViewer.HPCViewerToolkit;
import org.eclipse.core.resources.*;
//laks
import edu.rice.cs.hpc.data.experiment.pnode.*;

public class ScopeView extends ViewPart {
    public static final String ID = "edu.rice.cs.hpc.scope.ScopeView";

    private final String ICONPATH="../../../../../../../icons/";
    private TreeViewer 	treeViewer;
    private Experiment 	myExperiment;
    private int 		sortMetric;
    private PNode[] 	myPNodes;
    private Scope 		myRootScope;
    //TODO private Scope		myFocusScope;
    private java.util.HashMap<String, IFile> mapFilePage = 
    	new java.util.HashMap<String, IFile>();

    /* Listener for selection changes from other views
     * new scope:	nav to scope
     * new metric:	sort by metric / show metric column (shouldn't happen)
     * new nodes:	update data for selected node(s)
     */
    /*
    ISelectionListener otherListener = new ISelectionListener() {
    	public void selectionChanged(IWorkbenchPart srcpart, ISelection sel) {
    		if (srcpart == ScopeView.this)
    			return;
    		// other view selection changed; update setInput
    		if (sel instanceof IStructuredSelection) {
    			IStructuredSelection ss = (IStructuredSelection) sel;
    			if (ss == null || ss.size() < 1)
    				//TODO do nothing? select all? default?
    				return;

    			Object o = ss.getFirstElement();
    			if (o instanceof PNode) {
    				// set new pnodes
    				Object[] oss = ss.toArray();
    				myPNodes = new PNode[oss.length];
    				for(int i=0; i<oss.length; i++)
    					myPNodes[i] = (PNode) oss[i];
    				// TODO only update relevant things
    				updateDisplay();
    			} else if (o instanceof Metric) {
    				// shouldn't happen.
    			} // else ignore
    		}
    		if (sel instanceof Scope.Node) {
        		// TODO new focus scope
        		// change comes from appsig or source
    			System.err.println("ScopeView heard: "+((Scope.Node)sel).getScope().getName());
    			treeViewer.reveal(sel);
    			treeViewer.setSelection(sel, true); //FIXME: doesn't actually select this node
        	}
         }
	};
	*/
	/* Listener for selection changes in THIS view
	 * new scope:	open/close scope; nav to scope; show source
	 * new metric:	update sorting
	 * new nodes:	not possible from here
	 */
	/*ISelectionChangedListener thisListener = new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
        	//TODO anything for local selection changes?
//        	if (!(event.getSelection() instanceof StructuredSelection))
//        		return;
//        	StructuredSelection sel = (StructuredSelection) event.getSelection();
//        	Scope.Node selNode = (Scope.Node) sel.getFirstElement();
//        	SourceFile src = selNode.getScope().getSourceFile();
        	//int startline = selNode.getScope().getFirstLineNumber();
        }		
	};*/
	
	IDoubleClickListener dblListener = new IDoubleClickListener() {
		public void doubleClick(DoubleClickEvent event) {
			if (!(event.getSelection() instanceof StructuredSelection))
				return;
			StructuredSelection sel = (StructuredSelection) event.getSelection();
			Scope.Node node = (Scope.Node) sel.getFirstElement();
			// check if the source code is available
			if (node.getScope().getSourceFile() == SourceFile.NONE
				|| !node.getScope().getSourceFile().isAvailable())
				return;
			// get the complete file name
			//String filename = node.getScope().getSourceFile().getName();
			FileSystemSourceFile newFile = ((FileSystemSourceFile)node.getScope().getSourceFile());
			String sLongName= newFile.getCompleteFilename();
			System.out.println("Loading source file "+ ":"+ sLongName + "("+newFile.getName()+")");
			// laks: try to show the editor
			int iLine = node.getScope().getFirstLineNumber();
			openFileEditor( sLongName, newFile.getName(), iLine );
		}
	};
	
	/**
	 * Open Eclipse IDE editor for a given filename. 
	 * Beware: for Eclipse 3.2, we need to create a "hidden" project of the file
	 * 			this project should be cleaned in the future !
	 * @param sFilename the complete path of the file to display in IDE
	 */
	private void openFileEditor(String sLongFilename, String sFilename, int iLineNumber) {
		// get the complete path of the file
		org.eclipse.core.filesystem.IFileStore objFile = 
			org.eclipse.core.filesystem.EFS.getLocalFileSystem().getStore(new 
					org.eclipse.core.runtime.Path(sLongFilename).removeLastSegments(1));
		// get the active page for the editor
		org.eclipse.ui.IWorkbenchPage wbPage = this.getSite().getWorkbenchWindow().getActivePage();
		if(wbPage != null ){
			objFile=objFile.getChild(sFilename);
	    	if(!objFile.fetchInfo().exists()) {
	    		 MessageDialog.openInformation(this.getSite().getShell(), "File not found", 
	    		 	sFilename+": File cannot be opened in " + objFile.getName());
	    		 return; // do we need this ?
	    	}
	    	try {
	    		System.out.println("openEditorOnFileStore:"+objFile.toString());
	    		org.eclipse.ui.ide.IDE.openEditorOnFileStore(wbPage, objFile);
	    	} catch (PartInitException e) {
	    		e.printStackTrace();
	    		MessageDialog.openError(this.getSite().getShell(), "Error opening the file", e.getMessage());
	       /* some code */
	     }
		}
	}

	private void setEditorMarker() {
	       /*IFile file;
	       try{
			   IMarker marker = file.createMarker("HPCViewer");
			   marker.setAttribute(IMarker.LINE_NUMBER, iLineNumber+1);
			   marker.setAttribute(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
			   org.eclipse.ui.ide.IDE.gotoMarker(wbPage.getActiveEditor(), marker);
	    	   
	       } catch (org.eclipse.core.runtime.CoreException e) {
	    	   e.printStackTrace();
	       }*/

	}
	ViewerSorter metricVS = new ViewerSorter() {
		public int compare(Viewer viewer, Object e1, Object e2) {
			Scope.Node s1 = (Scope.Node)e1;
			Scope.Node s2 = (Scope.Node)e2;
			MetricValue mv1, mv2;
			
			int col = sortMetric;
			if (sortMetric < 0)
				col = -1 * sortMetric;
			mv1 = s1.getScope().getMetricValue(0);
			/*
			if (myPNodes.length == 1) {
				mv1 = s1.getScope().getMetricValue(
						myExperiment.getMetric(col-1), myPNodes[0]);
				mv2 = s2.getScope().getMetricValue(
						myExperiment.getMetric(col-1), myPNodes[0]);
			} else { //TODO multiple, average
				mv1 = s1.getScope().getMetricValue(
						myExperiment.getMetric(col-1), myPNodes[0]);
				mv2 = s2.getScope().getMetricValue(
						myExperiment.getMetric(col-1), myPNodes[0]);
			}
			if (sortMetric < 0)
				return mv1.compareTo(mv2);
			else
				return mv2.compareTo(mv1);
				*/
			return col;
		}   
	};
	
	public void setViewTitle(String sName) {
		super.setPartName(sName);
	}
    public void setFocus() {
            treeViewer.getTree().setFocus();
    }
    
    // laks: we need experiment and rootscope
    /**
     * Update the data input for Scope View, depending also on the scope
     */
    public void setInput(Experiment ex, RootScope scope) {
    	myExperiment = ex;
    	if (ex != null) {
    		myRootScope = scope;
    		//TODO myFocusScope = ex.getRootScope();
    	}
    	sortMetric = 1;
    	updateDisplay();
    	
    }
    
    /**
     * Update the data view based on the XML experiment data
     * @param ex
     */
    public void setInput(Experiment ex) {
    	myExperiment = ex;
    	if (ex != null) {
    		//myPNodes = ex.getPNodes();
    		myRootScope = ex.getRootScope();
    		//TODO myFocusScope = ex.getRootScope();
    	}
    	sortMetric = 1;
    	updateDisplay();
    }

    public TreeViewer getTreeViewer()
    {
            return treeViewer;
    }

	public void createPartControl(Composite parent) {
        treeViewer = new TreeViewer(parent, SWT.SINGLE|SWT.FULL_SELECTION);
        treeViewer.setContentProvider(new ScopeTreeContentProvider());
        treeViewer.setLabelProvider(new ScopeTreeLabelProvider());
        treeViewer.setSorter(new MetricSorter());
//        treeViewer.setSorter(metricVS);
        
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);
/*
        TreeViewerColumn colViewer = new TreeViewerColumn(treeViewer, SWT.LEFT);
        colViewer.getColumn().setText("Scope");
        colViewer.getColumn().setWidth(200); 
 */       
        TreeColumn tmp = new TreeColumn(treeViewer.getTree(),SWT.LEFT, 0);
        tmp.setText("Scope");
        tmp.setWidth(200); //TODO dynamic size
        
        //treeViewer.setUseHashlookup(true); faster lookup, more mem
        //treeViewer.getControl().setLayoutData(layoutData);
        //treeViewer.expandToLevel(1); //TODO expand how much?
        treeViewer.setInput(null);
        
		//TODO selections
		// allow other views to listen for selections in this view (site)
		this.getSite().setSelectionProvider(treeViewer);
		
		// listen for other selection changes in this page 
/*		this.getSite().getWorkbenchWindow().getSelectionService()
			.addPostSelectionListener(otherListener);
*/
		// listen for selection changes in this view(er)
		//treeViewer.addSelectionChangedListener(thisListener);
		
        //TODO treeViewer.addDoubleClickListener(null); maybe zoom?
		treeViewer.addDoubleClickListener(dblListener);

//		this.getSite().getPage().addSelectionListener(otherListener);
		
		makeActions();
	}
	
	
	private void makeActions() {
		// Flatten
		Action aFlatten = new Action() {
			public void run() {	}
		};
		aFlatten.setText("Flatten");
		aFlatten.setToolTipText("Flatten");
		aFlatten.setImageDescriptor(ImageDescriptor.createFromFile(
				this.getClass(),
				this.ICONPATH+"Flatten.gif"));

		// Unflatten
		Action aUnflatten = new Action() {
			public void run() {}
		};
		aUnflatten.setText("Unflatten");
		aUnflatten.setToolTipText("Unflatten");
		aUnflatten.setImageDescriptor(ImageDescriptor.createFromFile(
				this.getClass(),
				this.ICONPATH+"Unflatten.gif"));

		// Zoom In
		Action aZoomIn = new Action() {
			public void run() {
				ISelection sel = treeViewer.getSelection();
				if (!(sel instanceof StructuredSelection))
					return;
				Object o = ((StructuredSelection)sel).getFirstElement();
				if (!(o instanceof Scope.Node))
					return;
				treeViewer.setInput(o);
				treeViewer.refresh();
			}
		};
		aZoomIn.setText("Zoom In");
		aZoomIn.setToolTipText("Zoom In");
		aZoomIn.setImageDescriptor(ImageDescriptor.createFromFile(
				this.getClass(),
				this.ICONPATH+"Zoom in large.gif"));
				
		// Zoom Out
		Action aZoomOut = new Action() {
			public void run() {
				Object o = treeViewer.getInput();
				if (!(o instanceof Scope.Node))
					return;
				Scope.Node child = (Scope.Node) o;
				Scope.Node parent = (Scope.Node)child.getParent();
				//Scope.Node parent = ((Scope.Node)o).getParent();
				if (parent == null)
					return;
				treeViewer.setInput( parent );
				treeViewer.refresh();
			}
		};
		aZoomOut.setText("Zoom Out");
		aZoomOut.setToolTipText("Zoom Out");
		aZoomOut.setImageDescriptor(ImageDescriptor.createFromFile(
				this.getClass(),
				this.ICONPATH+"Zoom out large.gif"));

		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		manager.add(aFlatten);
		manager.add(aUnflatten);
		manager.add(aZoomIn);
		manager.add(aZoomOut);
	}
	
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
        // dirty solution to update titles
        {
            // Update metric title labels
            String[] titles = new String[myExperiment.getMetricCount()+1];
            titles[0] = "Scope";
            TreeColumn tmp;
        	for (int i=0; i<myExperiment.getMetricCount(); i++)
        	{
        		titles[i+1] = myExperiment.getMetric(i).getDisplayName();
        		tmp = new TreeColumn(treeViewer.getTree(),SWT.LEFT, i+1);
        		tmp.setText(titles[i+1]);
        		tmp.setWidth(120); //TODO dynamic size
        		new ColumnViewerSorter(this.treeViewer, tmp);
        		
        	}
            treeViewer.setColumnProperties(titles);
        }
        
        // Update metric value table
        ((ScopeTreeLabelProvider)treeViewer.getLabelProvider()).
        		setMetrics(myExperiment.getMetrics());
        
        // Update active pnodes
        ((ScopeTreeLabelProvider)treeViewer.getLabelProvider()).
        		setPNodes(myPNodes);

        // TODO Update scope focus
        //treeViewer.getTree().showItem(myFocusScope.getTreeNode());
        
        // Update root scope
        treeViewer.setInput(myRootScope.getTreeNode());
        
        treeViewer.refresh();
	}

    //------------------ sorting stuff
    
    private class MetricSorter extends ViewerSorter {
    	int iDir = 0;
    	/*public int category(Object element) {
    		this.iDir = (1-iDir);
    		return this.iDir;
    	}*/
    }
    
	private static class ColumnViewerSorter extends ViewerComparator {
		public static final int ASC = 1;
		public static final int NONE = 0;
		public static final int DESC = -1;
		private int direction = 0;
		private TreeColumn column;
		private ColumnViewer viewer;
		
		public ColumnViewerSorter(ColumnViewer viewer, TreeColumn column) {
			this.column = column;
			this.viewer = viewer;
			this.column.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					if( ColumnViewerSorter.this.viewer.getComparator() != null ) {
						if( ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this ) {
							int tdirection = ColumnViewerSorter.this.direction;
							
							if( tdirection == ASC ) {
								setSorter(ColumnViewerSorter.this, DESC);
							} else if( tdirection == DESC ) {
								setSorter(ColumnViewerSorter.this, NONE);
							}
						} else {
							setSorter(ColumnViewerSorter.this, ASC);
						}
					} else {
						setSorter(ColumnViewerSorter.this, ASC);
					}
				}
			});
		}
		
		public void setSorter(ColumnViewerSorter sorter, int direction) {
			if( direction == NONE ) {
				column.getParent().setSortColumn(null);
				column.getParent().setSortDirection(SWT.NONE);
				viewer.setComparator(null);
			} else {
				column.getParent().setSortColumn(column);
				sorter.direction = direction;
				
				if( direction == ASC ) {
					column.getParent().setSortDirection(SWT.DOWN);
				} else {
					column.getParent().setSortDirection(SWT.UP);
				}
				
				if( viewer.getComparator() == sorter ) {
					viewer.refresh();
				} else {
					viewer.setComparator(sorter);
				}
				
			}
		}

		public int compare(Viewer viewer, Object e1, Object e2) {
			return direction * doCompare(viewer, e1, e2);
		}
		
		// laks: lazy comparison
		protected int doCompare(Viewer viewer, Object e1, Object e2) {
			return 1;
//			return (e1.toString().compareTo(e2.toString()));
		}
	}


}
