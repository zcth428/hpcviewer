/**
 * 
 */
package edu.rice.cs.hpc.viewer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import edu.rice.cs.hpc.data.experiment.source.FileSystemSourceFile;
import edu.rice.cs.hpc.data.experiment.source.SourceFile;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScope;
import edu.rice.cs.hpc.data.experiment.scope.CallSiteScopeType;
import edu.rice.cs.hpc.data.experiment.scope.LineScope;
import edu.rice.cs.hpc.data.experiment.scope.LoopScope;
import edu.rice.cs.hpc.data.experiment.scope.ProcedureScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
import edu.rice.cs.hpc.data.experiment.scope.RootScopeType;
import edu.rice.cs.hpc.data.experiment.scope.Scope;

import edu.rice.cs.hpc.viewer.experiment.ExperimentData;
import edu.rice.cs.hpc.viewer.experiment.ExperimentManager;
import edu.rice.cs.hpc.viewer.experiment.ExperimentView;
import edu.rice.cs.hpc.viewer.framework.Activator;
import edu.rice.cs.hpc.viewer.resources.Icons;
import edu.rice.cs.hpc.viewer.scope.BaseScopeView;
import edu.rice.cs.hpc.viewer.scope.ColumnViewerSorter;

/**
 * Class providing auxiliary utilities methods.
 * Remark: it is useless to instantiate this class since all its methods are static !
 * @author laksono
 *
 */
public class Utilities {
	//special font for the metric columns. It supposed to be fixed font
	static public Font fontMetric;
	/* generic font for view and editor */
	static public Font fontGeneral;
	
	// special color for the top row
	static public Color COLOR_TOP;
	
	static public String NEW_LINE = System.getProperty("line.separator");
	static private Display objDisplay;	
	static private int iFontHeight = 0;
	
	/**
	 * Set the font for the metric columns (it may be different to other columns)
	 * This method has to be called first before others
	 * @param display
	 */
	static public void setFontMetric(Display display) {
		COLOR_TOP = new Color(display, 255,255,204);
		
		ScopedPreferenceStore objPref = (ScopedPreferenceStore)Activator.getDefault().getPreferenceStore();
		FontData []objFontMetric = display.getSystemFont().getFontData();
		FontData []objFontGeneric = display.getSystemFont().getFontData();
		objFontMetric[0].setName("Courier"); // default font for metrics

		// get the font for metrics columns based on user preferences
		if (objPref != null) {
			// bug fix: for unknown reason, the second instance of hpcviewer cannot find the key
			//	solution: check if the key exist or not IPreferenceStore.STRING_DEFAULT_DEFAULT.equals
			String sValue = objPref.getString (PreferenceConstants.P_FONT_METRIC); 
			if ( !IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(sValue) )
				objFontMetric = PreferenceConverter.getFontDataArray(objPref, PreferenceConstants.P_FONT_METRIC);
			else 
				// bug fix: if user hasn't set the preference, we set it for him/her
				PreferenceConverter.setValue(objPref, PreferenceConstants.P_FONT_METRIC, objFontMetric);
			
			sValue = objPref.getString (PreferenceConstants.P_FONT_GENERIC);
			if ( !IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(sValue) )
				objFontGeneric = PreferenceConverter.getFontDataArray(objPref, PreferenceConstants.P_FONT_GENERIC);
			else 
				// bug fix: if user hasn't set the preference, we set it for him/her
				PreferenceConverter.setValue(objPref, PreferenceConstants.P_FONT_METRIC, objFontMetric);
		}
		// create font for general purpose (view, editor, ...)
		Utilities.fontGeneral = new Font (display, objFontGeneric);
		iFontHeight = objFontGeneric[0].getHeight();
		
		Utilities.fontMetric = new Font(display, objFontMetric);
		// save the display
		Utilities.objDisplay = display;
	}

	/**
	 * Set a new font for metric and generic view
	 * @param window
	 * @param objFontMetric
	 * @param objFontGeneric
	 */
	static public void setFontMetric(IWorkbenchWindow window, FontData objFontMetric[], FontData objFontGeneric[]) {
		boolean isFontChanged = false;
		FontData []myFontMetric = Utilities.fontMetric.getFontData();
		FontData []myFontGeneric = Utilities.fontGeneral.getFontData();
		if ( !myFontMetric[0].equals( objFontMetric[0] ) ) {
			Utilities.fontMetric = new Font( Utilities.objDisplay, objFontMetric);
			Utilities.iFontHeight = objFontMetric[0].getHeight();
			isFontChanged = true; 
		}		
		if ( !myFontGeneric[0].equals( objFontGeneric[0] ) ) {
			Utilities.fontGeneral = new Font( Utilities.objDisplay, objFontGeneric);
			Utilities.iFontHeight = objFontGeneric[0].getHeight();
			isFontChanged = true; 
		}
		if (isFontChanged) {
			// a font has been changed. we need to refresh the view
			resetAllViews (window);
		}
	}
	
	/**
	 * Refresh all the views 
	 * @param window: the target window
	 */
	static private void resetAllViews(IWorkbenchWindow window) {
		ExperimentManager objManager = ExperimentData.getInstance(window).getExperimentManager();
		if(objManager != null) {
			ExperimentView objView = objManager.getExperimentView();
			final BaseScopeView arrViews[] = objView.getViews();
			final TreeItemManager objItemManager = new TreeItemManager();
			IWorkbenchPart part = window.getPartService().getActivePart();

			if (part instanceof BaseScopeView) {
				// let refresh the active view first, then we refresh other views via helper thread
				final BaseScopeView objActiveView = (BaseScopeView) part;
				resetView  (objItemManager, objActiveView.getTreeViewer() );
				
				// using helper thread to refresh other views
				window.getShell().getDisplay().asyncExec( new Runnable() {
					public void run() {
						// refresh all the views
						for(int i=0;i<arrViews.length;i++) {
							if (objActiveView != arrViews[i]) {
								TreeViewer tree = arrViews[i].getTreeViewer();
								// reset the view
								Utilities.resetView(objItemManager, tree);
							} else {
								//System.out.println("view "+objActiveView.getTitle()+" has been reset ");
							}
						}
					}
				});
			} else {
				System.err.println("Error while changing font: the active view is not an instance of BaseScopeView ! " + part);
			}
		}

	}
	
	/**
	 * refresh a particular view
	 * To save memory allocation, we ask an instance of TreeItemManager
	 * @param objItemManager
	 * @param tree
	 */
	static private void resetView ( TreeItemManager objItemManager, TreeViewer tree) {
		// save the context first
		objItemManager.saveContext(tree);
		// refresh
		tree.refresh();
		// restore the context
		objItemManager.restoreContext(tree);
	}
	
	/**
	 * Update the font for metric pane with one single font (just take the size)
	 * @param objFontData
	 */
	static private void setFontMetric(IWorkbenchWindow window, int iFontSize) {
		if (iFontSize != Utilities.iFontHeight) {
			Utilities.iFontHeight = iFontSize;
			FontData []myFontGeneric = Utilities.fontGeneral.getFontData();
			myFontGeneric[0].setHeight(iFontSize);
			FontData []myFontMetric = Utilities.fontMetric.getFontData();
			myFontMetric[0].setHeight(iFontSize);
			
			setFontMetric(window, myFontMetric, myFontGeneric);
			
			ExperimentManager objManager = ExperimentData.getInstance(window).getExperimentManager();
			if(objManager != null) {
				ExperimentView objView = objManager.getExperimentView();
				final BaseScopeView arrViews[] = objView.getViews();
				for (int i=0; i<arrViews.length; i++) {
					//arrViews[i].getTreeViewer().getTree()
				}
			}
		}
		
	}
	
	/**
	 * Increment font size
	 * @param window
	 */
	static public void increaseFont(IWorkbenchWindow window) {
		Utilities.setFontMetric(window, Utilities.iFontHeight+1);
		FontData []objFontData = JFaceResources.getHeaderFontDescriptor().increaseHeight(+1).getFontData();
		JFaceResources.getFontRegistry().put(JFaceResources.HEADER_FONT, objFontData);

	}

	/**
	 * Decrement font size
	 * @param window
	 */
	static public void DecreaseFont(IWorkbenchWindow window) {
		Utilities.setFontMetric(window, Utilities.iFontHeight-1);
		FontData []objFontData = JFaceResources.getHeaderFontDescriptor().increaseHeight(-1).getFontData();
		JFaceResources.getFontRegistry().put(JFaceResources.HEADER_FONT, objFontData);
	}

	/**	
	 * Insert an item on the top on the tree/table with additional image if not null
	 * @param treeViewer : the tree viewer
	 * @param imgScope : the icon for the tree node
	 * @param arrText : the label of the items (started from  col 0..n-1)
	 */
	static public void insertTopRow(TreeViewer treeViewer, Image imgScope, String []arrText) {
		if(arrText == null)
			return;
    	TreeItem item = new TreeItem(treeViewer.getTree(), SWT.BOLD, 0);
    	if(imgScope != null)
    		item.setImage(0,imgScope);

    	// Laksono 2009.03.09: add background for the top row to distinguish with other scopes
    	item.setBackground(Utilities.COLOR_TOP);
    	// make monospace font for all metric columns
    	item.setFont(Utilities.fontMetric);
    	item.setFont(0, Utilities.fontGeneral); // The tree has the original font
    	// put the text on the table
    	item.setText(arrText);
    	// set the array of text as the item data 
    	// we will use this information when the table is sorted (to restore the original top row)
    	item.setData(arrText);
	}

	/**
	 * Retrieve the top row items into a list of string
	 * @param treeViewer
	 * @return
	 */
	public static String[] getTopRowItems( TreeViewer treeViewer ) {
		TreeItem item = treeViewer.getTree().getItem(0);
		String []sText= null; // have to do this to avoid error in compilation;
		if(item.getData() instanceof Scope) {
			// the table has been zoomed-out
		} else {
			// the table is in original form or flattened or zoom-in
			Object o = item.getData();
			if(o != null) {
				Object []arrObj = (Object []) o;
				if(arrObj[0] instanceof String) {
					sText = (String[]) item.getData(); 
				}
			}
		}
		return sText;
	}
	/**
	 * Return an image depending on the scope of the node.
	 * The criteria is based on ScopeTreeCellRenderer.getScopeNavButton()
	 * @param scope
	 * @return
	 */
	static public Image getScopeNavButton(Scope scope) {
		if (scope instanceof CallSiteScope) {
			CallSiteScope scopeCall = (CallSiteScope) scope;
        	LineScope lineScope = (LineScope) (scopeCall).getLineScope();
			if (((CallSiteScope) scope).getType() == CallSiteScopeType.CALL_TO_PROCEDURE) {
				if(Utilities.isFileReadable(lineScope))
					return Icons.getInstance().imgCallTo;
				else
					return Icons.getInstance().imgCallToDisabled;
			} else {
				if(Utilities.isFileReadable(lineScope))
					return Icons.getInstance().imgCallFrom;
				else
					return Icons.getInstance().imgCallFromDisabled;
			}
		} else if (scope instanceof RootScope) {
			RootScope rs = (RootScope) scope;
			if (rs.getType() == RootScopeType.CallingContextTree)	{ 
				return null;
			}
		} else if (scope instanceof ProcedureScope) {
			if (scope.getParentScope() instanceof RootScope) {
				return null;
			} 
		} else if (scope instanceof LineScope) {
			if (scope.getParentScope() instanceof CallSiteScope) {
				return null;
			}
		}
		else if (scope instanceof LoopScope) {
			if (scope.getParentScope() instanceof CallSiteScope) {
				return null;
			}
		}
		return null;
	}

    /**
     * Verify if the file exist or not.
     * Remark: we will update the flag that indicates the availability of the source code
     * in the scope level. The reason is that it is less time consuming (apparently) to
     * access to the scope level instead of converting and checking into FileSystemSourceFile
     * level.
     * @param scope
     * @return true if the source is available. false otherwise
     */
    static public boolean isFileReadable(Scope scope) {
    	// check if the source code availability is already computed
    	if(scope.iSourceCodeAvailability == Scope.SOURCE_CODE_UNKNOWN) {
    		SourceFile newFile = ((SourceFile)scope.getSourceFile());
    		if (newFile != null) {
        		if( (newFile != SourceFile.NONE)
            			|| ( newFile.isAvailable() )  ) {
            			if (newFile instanceof FileSystemSourceFile) {
            				FileSystemSourceFile objFile = (FileSystemSourceFile) newFile;
            				if(objFile != null) {
            					// find the availability of the source code
            					if (objFile.isAvailable()) {
            						scope.iSourceCodeAvailability = Scope.SOURCE_CODE_AVAILABLE;
            						return true;
            					} 
            				}
            			}
            		}
    		}
    	} else
    		// the source code availability is already computed, we just reuse it
    		return (scope.iSourceCodeAvailability == Scope.SOURCE_CODE_AVAILABLE);
    	// in this level, we don't think the source code is available
		scope.iSourceCodeAvailability = Scope.SOURCE_CODE_NOT_AVAILABLE;
		return false;
    }
}
