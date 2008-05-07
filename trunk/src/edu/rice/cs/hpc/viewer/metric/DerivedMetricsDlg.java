/**
 * 
 */
package edu.rice.cs.hpc.viewer.metric;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.layout.GridDataFactory;

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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;

import edu.rice.cs.hpc.data.experiment.metric.DerivedMetric;
/**
 * @author la5
 *
 */
public class DerivedMetricsDlg extends TitleAreaDialog {

	final private static String NONE = "No metric";
	final private float fValue1 = 2;
	final private float fValue2 = 3;
	
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
	private Label lblCoef2;
	private Label opLabel;
	
	private Label lblExp;
	private Label lblPreview;
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
	 * check the fields, compute, and generate the preview of the expression
	 */
	private void generatePreview() {
		int iMetric1 = this.cbMetric1.getSelectionIndex();
		if(iMetric1 == 0) return;
		
		this.fCoefficient1 = this.checkCoefficient(this.txtCoef1);
		if(this.fCoefficient1 == null) return;
		float fResult = this.fCoefficient1 * this.fValue1;
		String strText = "( metric_1 * " + this.fCoefficient1+" )";
		int iMetric2 = this.cbMetric2.getSelectionIndex();
		if(iMetric2 > 0) {
			 // binary
			this.fCoefficient2 = this.checkCoefficient(this.txtCoef2);
			if(this.fCoefficient2 != null) {
				float fCoef2 = this.fCoefficient2 * this.fValue2;
				int iOp = this.cbOperation.getSelectionIndex();
				String strCoef2 = "( metric_2 * "+this.fCoefficient2 +")";
				switch (iOp) {
				case DerivedMetric.ADD:
					fResult = fResult + (fCoef2);
					strText = strText + " + " + strCoef2;
					break;
				case DerivedMetric.SUB:
					fResult = fResult - (fCoef2);
					strText = strText + " - " + strCoef2;
					break;
				case DerivedMetric.MUL:
					fResult = fResult * (fCoef2);
					strText = strText + " * " + strCoef2;
					break;
				case DerivedMetric.DIV:
					if(fCoef2 != 0) {
						fResult = fResult / (fCoef2);
						strText = strText + " / " + strCoef2;
					} else {
						strText = "Error division by zero: "+strText+" / " + strCoef2;
						fResult = 0;
					}
					break;
				}
			}
		} else {
			// unary
		}
		this.lblExp.setText("Expression: "+strText);
		this.lblPreview.setText("Preview: "+fResult);
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
	    	txtCoef1.setToolTipText("The scale coefficient for the first metric");
	    	txtCoef1.setEnabled(false);
	    	// make the coefficient field small enough for couple of numbers
	    	GridDataFactory.fillDefaults().hint(40, SWT.DEFAULT).grab(false, false).applyTo(txtCoef1);
	     
	    	cbMetric1 = new Combo(metricsArea, SWT.READ_ONLY);
	    	cbMetric1.setItems(this.metricsName);
	    	cbMetric1.add(DerivedMetricsDlg.NONE, 0);
	    	cbMetric1.setText(DerivedMetricsDlg.NONE);
	    	cbMetric1.setToolTipText("Select the first metric");
	   	 	cbMetric1.addSelectionListener(new SelectionListener(){
	   			public void widgetSelected(SelectionEvent e) {
	   				int iSelect = cbMetric1.getSelectionIndex();
	   				if(iSelect > 0) {
	   					txtCoef1.setEnabled(true);
	   					String str = txtCoef1.getText(); 
	   					if(str == null || str.length()==0)
	   						txtCoef1.setText("1.0");
	   					cbMetric2.setEnabled(true);
	   				} else {
	   					txtCoef1.setEnabled(false);
	   					cbMetric2.setEnabled(false);
	   				}
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}

	   	 	});
	   	 	
	    	Label lbl2 = new Label(metricsArea, SWT.NONE);
	    	lbl2.setText("Right (optional):");
	    	txtCoef2 = new Text(metricsArea, SWT.NONE);
	    	txtCoef2.setEnabled(false);
	    	txtCoef2.setToolTipText("Optional scale coefficient for the second operand");
	    	// make the coefficient field small enough for couple of numbers
	    	GridDataFactory.fillDefaults().hint(40, SWT.DEFAULT).grab(false, false).applyTo(txtCoef2);

	    	cbMetric2 = new Combo(metricsArea, SWT.READ_ONLY);
	    	cbMetric2.setItems(this.metricsName);
	    	cbMetric2.add(DerivedMetricsDlg.NONE, 0);
	    	cbMetric2.setText(DerivedMetricsDlg.NONE);
	    	cbMetric2.setToolTipText("Select the second metric (optional)");
	    	cbMetric2.addSelectionListener(new SelectionListener(){
	   			public void widgetSelected(SelectionEvent e) {
	   				int iSelect = cbMetric2.getSelectionIndex();
	   				if(iSelect > 0) {
	   					txtCoef2.setEnabled(true);
	   					String str = txtCoef2.getText(); 
	   					if(str == null || str.length()==0)
	   						txtCoef2.setText("1.0");
	   					cbOperation.setEnabled(true);
	   				} else {
	   					txtCoef2.setEnabled(false);
	   					cbOperation.setEnabled(false);
	   				}
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}

	   	 	});
	    	cbMetric2.setEnabled(false);
	    	GridLayoutFactory.swtDefaults().numColumns(3).generateLayout(metricsArea);
	    	//GridLayoutFactory.fillDefaults().numColumns(3).margins(
			//	LayoutConstants.getMargins()).generateLayout(metricsArea);
	    }

		new Label(grpBase, SWT.SEPARATOR | SWT.VERTICAL);
		
		Composite opArea = new Composite(grpBase, SWT.NONE);
		{
			opLabel = new Label(opArea, SWT.NONE);
			opLabel.setText("Arithmetic operation:");
			this.cbOperation = new Combo(opArea, SWT.READ_ONLY);
			String []sOperations = new String[]{"Add (+)","Substract (-)","Multipy (*)","Divide (/)"};
			this.cbOperation.setItems(sOperations);
			this.cbOperation.setText(sOperations[0]);
			this.cbOperation.setEnabled(false);
			GridLayoutFactory.fillDefaults().numColumns(1) .generateLayout(opArea);
		}		
		GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(grpBase);
		
		//-------
		// preview
		Group grpPreview = new Group(composite, SWT.BORDER);
		{
			grpPreview.setText("Evaluation and preview");
			Button btnPreview = new Button(grpPreview, SWT.NONE);
			btnPreview.setText("Generate preview");
			btnPreview.addSelectionListener(new SelectionListener() {
	   			public void widgetSelected(SelectionEvent e) {
	   				generatePreview();
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
			});
			Composite compoLabels = new Composite(grpPreview, SWT.NONE);
			Label lblValues = new Label(compoLabels, SWT.NONE);
			lblValues.setText("Example values: metric_1 = 2.0 and metric_2 = 3.0");
			lblExp = new Label(compoLabels, SWT.NONE);
			lblExp.setText("Expression:");
			lblPreview = new Label(compoLabels, SWT.NONE);
			lblPreview.setText("Preview:");
			GridLayoutFactory.swtDefaults().numColumns(1).generateLayout(compoLabels);
			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(grpPreview);
		}
		
		//-------
		// options
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
	   * Event fired when the OK button is pressed.
	   * We will check that the filled form is valid
	   */
	  protected void okPressed() {
		  this.fCoefficient1 = this.checkCoefficient(this.txtCoef1);
		  if(this.fCoefficient1 == null) return;
		  // check if the coefficient is well defined
		  this.iChosenMetric1 = this.cbMetric1.getSelectionIndex()-1;
		  // if the left coefficient is valid, then the metric has to be also valid
		  if(this.iChosenMetric1 >= 0){
			  // check for the second coefficient
			  this.iChosenMetric2 = this.cbMetric2.getSelectionIndex()-1;
			  if(this.iChosenMetric2>=0) {
				  this.fCoefficient2 = this.checkCoefficient(this.txtCoef2);
				  if(this.fCoefficient2 == null) return;
				  // now get the operator
				  this.iOperation = this.cbOperation.getSelectionIndex();
			  }
			  // options
			  this.sMetricName = this.txtName.getText();
			  if(this.sMetricName.length() == 0)
				  this.sMetricName = null;
			  else
				  this.sMetricName += "   "; // appended for "sorted direction space"
			  this.bDisplay = this.btnDisplay.getSelection();
			  this.bPercent = this.btnPercent.getSelection();
			  
			  super.okPressed();
		  } else {
			  MessageDialog.openError(this.shell, "Incorrect metric", "Please choose at least a metric to derive.");
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
		System.out.println("output:"+der.iChosenMetric1+" "+der.iChosenMetric2+" "+der.iOperation
				+" "+der.sMetricName+" "+ der.bDisplay+" "+der.bPercent+" "+der.fCoefficient1+" "+der.fCoefficient2);
	}
	
	//------------------------------------------------------//
	//======================= CLASSES ======================//
	//------------------------------------------------------//
	/*class ComboMetric extends Combo {
		public ComboMetric(Composite parent, int style) {
			super(parent, style);
		}
	}*/
	class ComboMetricSelectionListener implements SelectionListener {
		private Combo combo;
		private Text text;
		private Control next;
		public ComboMetricSelectionListener(Combo cb, Text txt, Control nextCtrl) {
			this.combo = cb;
			this.text = txt;
			this.next = nextCtrl;
		}
		public void widgetSelected(SelectionEvent e) {
			int iSelect = combo.getSelectionIndex();
			if(iSelect > 0) {
				text.setEnabled(true);
				String str = text.getText(); 
				if(str == null || str.length()==0)
					text.setText("1.0");
			} else {
				text.setEnabled(false);
			}
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			
		}

	}
}
