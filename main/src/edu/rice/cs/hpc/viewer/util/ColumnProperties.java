package edu.rice.cs.hpc.viewer.util;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;

import java.util.ArrayList;

public class ColumnProperties extends TitleAreaDialog {
	private CheckboxTableViewer objCheckBoxTable ;
	private TreeViewerColumn []objColumns;
	private boolean []results;
	
	//--------------------------------------------------
	/**
	 * Data model for the column properties
	 * Containing two items: the state and the title
	 * @author laksono
	 *
	 */
	private class PropertiesModel {
		public boolean isVisible;
		public String sTitle;
		public int iIndex;
		
		public PropertiesModel(boolean b, String s, int i) {
			this.isVisible = b;
			this.sTitle = s;
			this.iIndex = i;
		}
	}
	
	//--------------------------------------------------
	/**
	 * Label provider for the table
	 * @author laksono
	 *
	 */
	class CheckLabelProvider implements ILabelProvider {
		public void addListener(ILabelProviderListener arg0) {
		    // Throw it away
		  }
		public void dispose() {
		    // Nothing to dispose
		  }
		public boolean isLabelProperty(Object arg0, String arg1) {
		    return false;
		  }
		public void removeListener(ILabelProviderListener arg0) {
		    // Ignore
		  }
		public Image getImage(Object arg0) {
		    return null;
		  }
		public String getText(Object arg0) {
			PropertiesModel prop = (PropertiesModel) arg0;
		    return prop.sTitle;
		  }
	}
	
	/**
	 * Content provider for the table
	 * @author laksono
	 *
	 */
	private class PropertiesContentProvider implements IStructuredContentProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return (PropertiesModel[])inputElement;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
		}
		
	}

	 /**
	   * Creates the dialog's contents
	   * 
	   * @param parent the parent composite
	   * @return Control
	   */
	  protected Control createContents(Composite parent) {
	    Control contents = super.createContents(parent);

	    // Set the title
	    setTitle("Column Properties");

	    // Set the message
	    setMessage("Please check columns to be shown and uncheck columns to be hidden", IMessageProvider.INFORMATION);

	    return contents;
	  }

	  /**
	   * Creates the gray area
	   * 
	   * @param parent the parent composite
	   * @return Control
	   */
	  protected Control createDialogArea(Composite aParent) {
	    Composite composite = new Composite(aParent, SWT.BORDER);//(Composite) super.createDialogArea(aParent);

	    org.eclipse.swt.layout.GridLayout grid = new org.eclipse.swt.layout.GridLayout();
	    grid.numColumns=2;
	    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	    composite.setLayout(grid);

	    Table table = new Table(composite, SWT.CHECK | SWT.BORDER);
	    table.setLayoutData(new GridData(GridData.FILL_BOTH));
	    this.objCheckBoxTable = new CheckboxTableViewer(table) ;
		objCheckBoxTable.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		// setup the content provider
		objCheckBoxTable.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object parent) {
				return (PropertiesModel[])parent;
			}
			public void dispose() {
				// nope
			}
			public void inputChanged(Viewer v, Object oldInput, Object newInput) {
				//throw it away
			}
		}); 
		this.objCheckBoxTable.setLabelProvider(new CheckLabelProvider());
		this.updateContent(); // fill the table
	    return composite;
	  }

		//--------------------------------------------------
	  /**
	   * get the information about columns
	   * Need to call this if the table changes its columns !!
	   * @param objModels
	   */
	  public void setData(TreeViewerColumn []columns) {
		  this.objColumns = columns;
	  }

	  /**
	   * Populate the content of the table with the new information
	   */
	  public void updateContent() {
		  if(this.objColumns == null)
			  return; // caller of this object need to set up the column first !
		  int nbColumns = this.objColumns.length;
		  PropertiesModel objProps[] = new PropertiesModel[nbColumns];
		  ArrayList<PropertiesModel> arrColumns = new ArrayList<PropertiesModel>();
		  this.results = new boolean[nbColumns];
		  for(int i=0;i<nbColumns;i++) {
			  TreeColumn column = this.objColumns[i].getColumn();
			  boolean isVisible = column.getWidth() > 1;
			  String sTitle = column.getText();
			  objProps[i] = new PropertiesModel(isVisible, sTitle, i);
			  // we need to find which columns are visible
			  if(isVisible) {
				  arrColumns.add(objProps[i]);
			  }
			  this.results[i] = false; // initialize with false value
		  }
		  this.objCheckBoxTable.setInput(objProps);
		  this.objCheckBoxTable.setCheckedElements(arrColumns.toArray());
	  }

	  //--------------------------------------------------
	  /**
	   * action when the button OK is pressed
	   */
	  protected void okPressed() {
		  Object oElems[] = this.objCheckBoxTable.getCheckedElements();
		  if(oElems.length > 0){
			  if(oElems[0] instanceof PropertiesModel) {
				  for(int i=0;i<oElems.length;i++) {
					  PropertiesModel element = (PropertiesModel) oElems[i];
					  try {
						  this.results[element.iIndex] = true;
					  } catch(java.lang.Exception e) {
						  e.printStackTrace();
					  }
				  }
			  }
		  }
		  super.okPressed();
	  }
	  
	  /**
	   * Get the list of checked and unchecked items
	   * @return the array of true/false
	   */
	  public boolean[] getResult() {
		  return this.results;
	  }

		//--------------------------------------------------
	  /**
	   * Example for the test unit
	   */
	  public void setExample() {
		PropertiesModel []objModels = new PropertiesModel[4];
		objModels[0] = new PropertiesModel(true, "one", 0);
		objModels[1] = new PropertiesModel(false, "two", 1);
		objModels[2] = new PropertiesModel(true, "three", 2);
		objModels[3] = new PropertiesModel(true, "four", 3);
		this.objCheckBoxTable.setInput(objModels);
		PropertiesModel ch[] = new PropertiesModel[3];
		ch[0] = objModels[0];
		ch[1] = objModels[2];
		ch[2] = objModels[3];
		this.objCheckBoxTable.setCheckedElements(ch);
	  }
		//--------------------------------------------------
		/**
		 * Constructor column properties dialog
		 * ATT: need to call setData to setup the column
		 * @param parentShell
		 */
		public ColumnProperties(Shell parentShell) {
			super(parentShell);
			// TODO Auto-generated constructor stub
		}

	
		/**
		 * Constructor with column
		 * @param shell
		 * @param columns
		 */
		public ColumnProperties(Shell shell, TreeViewerColumn []columns) {
			super(shell);
			this.objColumns = columns;
		}
	  //==========================================
	  /**
	   * test unit
	   */
	  public static void main(String []args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		ColumnProperties objProp = new ColumnProperties(shell);
		//objProp.setExample();
		objProp.open();
	  }
}
