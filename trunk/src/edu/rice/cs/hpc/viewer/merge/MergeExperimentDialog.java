package edu.rice.cs.hpc.viewer.merge;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;

/**
 * Dialog box for the merging experiment. Be caution: this is just a dialog box,
 * not a processing class. To process the merging, please use Merging class.
 * @author laksono
 *
 */
public class MergeExperimentDialog extends TitleAreaDialog {
	private Shell objShell;
	private Text text1;
	private Text text2;
	private String sFile1;
	private String sFile2;
	
	/**
	 * Dialog box for retrieving two experiment files
	 * @param shell
	 */
	public  MergeExperimentDialog(Shell shell) {
		super(shell);
		this.objShell = shell;
	}
	
	public String getFirstFilename() {
		return this.sFile1;
	}
	
	public String getSecondFilename() {
		return this.sFile2;
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
	    setTitle("Merging Experiments");

	    // Set the message
	    setMessage("Merging two different experiment files. Please select two experiment files.", IMessageProvider.INFORMATION);

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
	    grid.numColumns=3;
	    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	    composite.setLayout(grid);

	    // Create experiment 1
	    Label label = new Label(composite, SWT.NONE);
	    label.setText ("Experiment 1:");
	     text1=new Text(composite, SWT.BORDER);
	    Button button1 = new Button(composite, SWT.PUSH);
	    button1.setText("Browse ...");
	    label.setLayoutData(new GridData());
	    text1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    button1.setLayoutData(new GridData());
	    
	 // Create experiment 2
	    Label label2 = new Label(composite, SWT.NONE);
	    label2.setText ("Experiment 2:");
	    text2=new Text(composite, SWT.BORDER);	    
	    Button button2 = new Button(composite, SWT.PUSH);
	    button2.setText("Browse ...");
	    label2.setLayoutData(new GridData());
	    text2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    button2.setLayoutData(new GridData());

	    // listener
	    button1.addListener(SWT.Selection, new BrowseExperimentAction(this.objShell, text1));
	    button2.addListener(SWT.Selection, new BrowseExperimentAction(this.objShell, text2));
	    return composite;
	  }

	  /**
	   * action when the button OK is pressed
	   */
	  protected void okPressed() {
			  sFile1 = this.text1.getText();
			  sFile2 = this.text2.getText();
			  if(sFile1.compareTo(sFile2)==0) {
				  // do not merge the same file !
				  org.eclipse.jface.dialogs.MessageDialog.openError(this.objShell,
						  "Error: Experiment files are the same", 
				  			"You have to select two different files to merge !");
			  } else {
				  // files are different. 
				  super.okPressed();
			  }
	  }
	  /**
	   * Class to display a file dialog and get the experiment file
	   * @author laksono
	   *
	   */
	  class BrowseExperimentAction implements Listener {
		  Shell objShell;
		  Text txtFile;
		  public BrowseExperimentAction(Shell shell, Text text) {
			  super();
			  this.objShell = shell;
			  this.txtFile = text;
		  }
	      public void handleEvent(Event e){
	        org.eclipse.swt.widgets.FileDialog fileDialog=new org.eclipse.swt.widgets.FileDialog(
	        			objShell,
	        			org.eclipse.swt.SWT.OPEN);
	        	fileDialog.setText("Load an XML experiment file");
	        	String sFile = fileDialog.open();
	        	this.txtFile.setText(sFile);
	      }	
	  }
}
