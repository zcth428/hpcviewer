/**
 * 
 */
package edu.rice.cs.hpc.viewer.scope;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.SWTException;

import org.eclipse.jface.dialogs.MessageDialog;
/**
 * @author la5
 *
 */
public class DerivedMetricsDlg extends TitleAreaDialog {

	final private static String NONE = "none";
	
	private Text txtCoef1;
	private Text txtCoef2;
	private Combo cbMetric1;
	private Combo cbMetric2;
	private String []metricsName;
	private Combo cbOperation;
	private Shell shell;
	private Text txtName;
	private Button btnPercent;
	private Button btnDisplay;
	
	//------------------------------------------------------//
	//================= PUBLIC FIELDS ======================//
	//------------------------------------------------------//
	/**
	 * Scale coefficient for the first operand
	 */
	public Float fCoefficient1;
	/**
	 * Scale coefficient for the second operand
	 */
	public Float fCoefficient2;
	/**
	 * Metric index of the first operand
	 */
	public int iChosenMetric1;
	/**
	 * Metric index of the second operand
	 */
	public int iChosenMetric2;
	/**
	 * Operation code between the two operands. See DErivedMetric.java for the constant
	 */
	public int iOperation;
	//============================ OPTIONS
	/**
	 * The name of the new derived metric
	 */
	public String sMetricName;
	/**
	 * flag is the metric should be displayed or not
	 */
	public boolean bDisplay;
	/**
	 * flag if the percentage should be computed or not
	 */
	public boolean bPercent;
	//------------------------------------------------------//
	//=================== CONSTRUCTOR ======================//
	//------------------------------------------------------//

	public DerivedMetricsDlg(Shell parentShell, String []metrics) {
		super(parentShell);
		// TODO Auto-generated constructor stub
		this.metricsName = metrics;
		this.shell = parentShell;
	}

	//------------------------------------------------------//
	//======================= METHODS ======================//
	//------------------------------------------------------//
	  /**
	   * Creates the dialog's contents
	   * 
	   * @param parent the parent composite
	   * @return Control
	   */
	  protected Control createContents(Composite parent) {
	    Control contents = super.createContents(parent);

	    // Set the title
	    setTitle("Creating a derived metric");

	    // Set the message
	    setMessage("A derived metric is based on a simple arithmetic operation of one or two metric columns.");

	    return contents;
	  }

	  /**
	   * Creates the gray area
	   * 
	   * @param parent the parent composite
	   * @return Control
	   */
	  protected Control createDialogArea(Composite parent) {
	    Composite composite = (Composite) super.createDialogArea(parent);
	    Group grpBase = new Group(composite, SWT.BORDER);
	    grpBase.setText("Derived metric definition");	    
	    Composite metricsArea = new Composite(grpBase, SWT.NONE);
	    {
	    	Label lblTitle = new Label(metricsArea, SWT.CENTER);
	    	lblTitle.setText("");
	    	lblTitle = new Label(metricsArea, SWT.CENTER);
	    	lblTitle.setText("Coefficient");
	    	
	    	lblTitle = new Label(metricsArea, SWT.CENTER);
	    	lblTitle.setText("Metric Operand");
	    	
	    	Label lbl1 = new Label(metricsArea, SWT.NONE);
	    	lbl1.setText("Left:");
	    	txtCoef1 = new Text(metricsArea, SWT.NONE);
	    	txtCoef1.setText("1.0");
	    	txtCoef1.setToolTipText("The scale coefficient for the first metric");
	     
	    	cbMetric1 = new Combo(metricsArea, SWT.READ_ONLY);
	    	cbMetric1.setItems(this.metricsName);
	    	cbMetric1.setText(this.metricsName[0]);
	    	cbMetric1.setToolTipText("Select the first metric");
	   	 
	    	Label lbl2 = new Label(metricsArea, SWT.NONE);
	    	lbl2.setText("Right (optional):");

	    	txtCoef2 = new Text(metricsArea, SWT.NONE);
	    	txtCoef2.setText("");
	    	txtCoef2.setToolTipText("Optional scale coefficient for the second operand");
	   	 
	    	cbMetric2 = new Combo(metricsArea, SWT.READ_ONLY);
	    	cbMetric2.setItems(this.metricsName);
	    	cbMetric2.setItem(0, this.metricsName[0]);
	    	cbMetric2.setText(this.metricsName[0]);
	    	cbMetric2.setToolTipText("Select the second metric (optional)");
	    	GridLayoutFactory.fillDefaults().numColumns(3).margins(
				LayoutConstants.getMargins()).generateLayout(metricsArea);
	    }

		new Label(grpBase, SWT.SEPARATOR | SWT.HORIZONTAL);
		
		Composite opArea = new Composite(grpBase, SWT.NONE);
		{
			Label opLabel = new Label(opArea, SWT.NONE);
			opLabel.setText("Arithmetic operation for the two metrics:");
			this.cbOperation = new Combo(opArea, SWT.READ_ONLY);
			String []sOperations = new String[]{"Add (+)","Substract (-)","Multipy (*)","Divide (/)"};
			this.cbOperation.setItems(sOperations);
			this.cbOperation.setText(sOperations[0]);
			this.cbOperation.setEnabled(false);
		}
		txtCoef2.addVerifyListener(new CoefficientVerifyListener(this.txtCoef2, this.cbOperation));

		GridLayoutFactory.fillDefaults().numColumns(2).margins(
				LayoutConstants.getMargins()).generateLayout(opArea);
		
		GridLayoutFactory.fillDefaults().generateLayout(grpBase);
		
		Group grpOptions = new Group(composite,SWT.BORDER);
		{
			Composite nameArea = new Composite(grpOptions, SWT.NONE);
			Label lblName = new Label(nameArea, SWT.LEFT);
			lblName.setText("New name for the derived metric:");
			this.txtName = new Text(nameArea, SWT.NONE);
			this.txtName.setToolTipText("Enter the new name of the derived metric");
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(nameArea);
			//nameArea.setLayout(new FillLayout(SWT.HORIZONTAL));
			
			this.btnPercent = new Button(grpOptions, SWT.CHECK);
			this.btnPercent.setText("Display the metric percentage");
			this.btnPercent.setToolTipText("Also compute the percent in the metric");
			this.btnDisplay = new Button(grpOptions, SWT.CHECK);
			this.btnDisplay.setText("Display the derived metric in the metric table");
			this.btnDisplay.setToolTipText("Display the metric in the table");	
			this.btnDisplay.setSelection(true);
			grpOptions.setText("Options");
		}
		GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(grpOptions);

		Point ptMargin = LayoutConstants.getMargins(); 
		ptMargin.x = 5;
		ptMargin.y = 5;
		GridLayoutFactory.fillDefaults().numColumns(1).margins(ptMargin).generateLayout(
				composite);
		
	    return composite;
	  }	

	  /**
	   * Check the coefficient in the text field. If the text is empty or the value is zero
	   * then return null. Otherwise return the Float value
	   * @param txtCoef
	   * @return
	   */
	  private Float checkCoefficient(Text txtCoef) {
		  String sCoef = txtCoef.getText();
		  Float fCoef = null;
		  if(sCoef.length() > 0) {
			  try {
				  fCoef = new Float(sCoef);
				  if(fCoef.floatValue() == 0)
					  fCoef = null;
			  } catch(java.lang.NumberFormatException e){
				  MessageDialog.openError(this.shell, "Incorrect number", "The field contains incorrect number.");
				  txtCoef.setFocus();
				  txtCoef.setSelection(0, sCoef.length());
			  }
		  } else
			  fCoef = new Float(0);
		  return fCoef;
	  }
	  
		//------------------------------------------------------//
		//======================= EVENTS  ======================//
		//------------------------------------------------------//
	  /**
	   * Event fired when the OK button is pressed
	   */
	  protected void okPressed() {
		  this.fCoefficient1 = this.checkCoefficient(this.txtCoef1);
		  this.fCoefficient2 = this.checkCoefficient(this.txtCoef2);
		  // check if the coefficient is well defined
		  if(this.fCoefficient1 != null && this.fCoefficient2 != null) {
			  this.iChosenMetric1 = this.cbMetric1.getSelectionIndex();
			  this.iChosenMetric2 = this.cbMetric2.getSelectionIndex();
			  this.iOperation = this.cbOperation.getSelectionIndex();
			  // options
			  this.sMetricName = this.txtName.getText();
			  if(this.sMetricName.length() == 0)
				  this.sMetricName = null;
			  else
				  this.sMetricName += "   "; // appended for "sorted direction space"
			  this.bDisplay = this.btnDisplay.getSelection();
			  this.bPercent = this.btnPercent.getSelection();
			  
			  super.okPressed();
		  }
	  }

	  //=====================
	  
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DerivedMetricsDlg der = new DerivedMetricsDlg(null, new String[]{"one", "two", "three"});
		der.open();
	}
	
	//------------------------------------------------------//
	//======================= CLASSES ======================//
	//------------------------------------------------------//
	/**
	 * Listener class to verify if the field of the coefficient is empty or not
	 * If it is not empty, then the next control can be activated
	 * @author la5
	 *
	 */
	class CoefficientVerifyListener implements VerifyListener {
		Control ctrlNext = null;
		Control ctrlCurrent = null;
		
		public CoefficientVerifyListener(Control cCurrent, Control cNext) {
			super();
			this.ctrlNext = cNext;
			this.ctrlCurrent = cCurrent;
		}
		// check if the text is a valid number
		// if the text is valid, then go to the next control (or field)
		// otherwise show an error message
		public void verifyText(VerifyEvent e) {
			String sText = e.text;
			if(sText.length() > 0) {
				try {
					if((ctrlNext != null)) {
						try{
							this.ctrlNext.setEnabled(true);
						} catch (SWTException ex) {
							System.err.println("Unable enabling "+this.ctrlNext.toString() +":"+ex.getMessage());
						}
						
					}
				} catch(java.lang.NumberFormatException eFloat) {
					MessageDialog.openError(shell, "Incorrect coefficient", "The number is not valid.");
					ctrlCurrent.setFocus();
				}
			}
		}
	}
}
