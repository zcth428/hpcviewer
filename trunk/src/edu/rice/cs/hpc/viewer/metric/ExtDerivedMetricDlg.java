/**
 * 
 */
package edu.rice.cs.hpc.viewer.metric;
// jface
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
// swt
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
// hpcviewer
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.data.experiment.scope.RootScope;
// math expression
import com.graphbuilder.math.*;
import com.graphbuilder.math.func.*;

/**
 * @author la5
 * Dialog box to enter a math formula to define a derived metric
 */
public class ExtDerivedMetricDlg extends TitleAreaDialog {
	//------------- GUI variables
	//private Label lblExpression;
	private Text txtName;
	private Button btnPercent;
	private Button btnExclusive;
	private Button btnInclusive;

	// ------------ Metric and math variables
	//private Metric []arrMetrics;
	private String []arrStrMetrics;
	private Text txtExpression;
	private Expression expFormula;
	final ExtFuncMap fctMap;

	// ------------- Others
	//private Scope scope;

	private String sMetricName;
	private boolean bPercent;
	private boolean bExclusive = false;

	//==========================================================
	  // ---- Constructor
	  //==========================================================
	/**
	 * Constructor to accept Metrics
	 * @param parentShell
	 * @param listOfMetrics
	 */
	public ExtDerivedMetricDlg(Shell parentShell, BaseMetric []listOfMetrics) {
		super(parentShell);
		this.setMetrics(listOfMetrics);
		this.fctMap = new ExtFuncMap(listOfMetrics, null);
	}
	
	public ExtDerivedMetricDlg(Shell parent, BaseMetric []listOfMetrics, Scope s) {
		super(parent);
		this.setMetrics(listOfMetrics);
		this.fctMap = new ExtFuncMap(listOfMetrics, null);
	}
	
	  //==========================================================
	  // ---- GUI CREATION
	  //==========================================================

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
	    setMessage("A derived metric is based on a simple arithmetic expression of base metrics");

	    return contents;
	  }

	  /*
	   * {@docRoot org.eclipse.jface.dialogs.TitleAreaDialog}
	   * @see {@link org.eclipse.jface.dialogs.TitleAreaDialog} 
	   */
	  protected Control createDialogArea(Composite parent) {
	    Composite composite = (Composite) super.createDialogArea(parent);
	    Group grpBase = new Group(composite, SWT.NONE);
	    grpBase.setText("Derived metric definition");
	    Composite expressionArea = new Composite(grpBase, SWT.NONE);
	    {
	    	Group grpExpression = new Group(expressionArea, SWT.NONE);
	    	Label lbl = new Label(grpExpression, SWT.NONE);
	    	lbl.setText("Type the formula for the derived metric. Example: $0+(avg($1,$2,$3)/max($1,$2,$3))");
	    	this.txtExpression = new Text(grpExpression, SWT.NONE);
	    	txtExpression.setToolTipText("Write a simple arithmetic expression");
	    	GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(grpExpression);
	    	
	    	//--------------- inserting metric
	    	Group grpInsertion = new Group(expressionArea, SWT.NONE);
	    	grpInsertion.setText("Help: Inserting metrics/functions");

	    	Label lblMetric = new Label(grpInsertion, SWT.NONE);
	    	lblMetric.setText("Metric:");
	    	// combo box that lists the metrics
	    	final Combo cbMetric = new Combo(grpInsertion, SWT.READ_ONLY);
	    	cbMetric.setItems(this.arrStrMetrics);
	    	cbMetric.setText(this.arrStrMetrics[0]);
	    	// button to insert the metric code into the expression field
	    	Button btnMetric = new Button(grpInsertion, SWT.PUSH);
	    	btnMetric.setText("Insert metric");
	    	btnMetric.addSelectionListener(new SelectionListener() {
	   			public void widgetSelected(SelectionEvent e) {
	   				txtExpression.insert("$"+cbMetric.getSelectionIndex());
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
	    	});
	    	
	    	//---------------- inserting function
	    	Label lblFunc = new Label(grpInsertion, SWT.NONE);
	    	lblFunc.setText("Function:");
	    	final Combo cbFunc = new Combo(grpInsertion, SWT.READ_ONLY);
	    	fctMap.loadDefaultFunctions();
	    	Function arrFct[] = fctMap.getFunctions();
	    	// create the list of the name of the function
	    	// list of the name  of the function and its arguments
	    	final String []arrFunctions = new String[arrFct.length];
	    	// the list of the name of the function to be inserted
	    	final String []arrFuncNames = fctMap.getFunctionNames();
	    	for(int i=0;i<arrFct.length;i++) {
	    		arrFunctions[i] = arrFct[i].toString();
	    	}
	    	// insert the name of the function into the combo box
	    	if(arrFunctions != null && arrFunctions.length>0) {
	    		cbFunc.setItems(arrFunctions);
	    		// by default insert the toplist function
	    		cbFunc.setText(arrFunctions[0]);
	    	}
	    	final Button btnFunc = new Button(grpInsertion, SWT.PUSH);
	    	btnFunc.setText("Insert function");
	    	btnFunc.addSelectionListener(new SelectionListener() {
	    		 // action to insert the name of the function into the formula text
	   			public void widgetSelected(SelectionEvent e) {
	   				int iPos = txtExpression.getCaretPosition();
	   				String sFunc = arrFuncNames[cbFunc.getSelectionIndex()];
	   				txtExpression.insert( sFunc + "()");
	   				// put the caret inside the parentheses
	   				txtExpression.setSelection(iPos+sFunc.length()+1);
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
	    	});

	    	// do not expand the group
	    	GridDataFactory.fillDefaults().grab(false, false).applyTo(grpInsertion);
	    	GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(grpInsertion);
	    	
	    	GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(expressionArea);
	    }
		GridLayoutFactory.fillDefaults().margins(5, 5).generateLayout(grpBase);
		
		//-------
		// options
		Group grpOptions = new Group(composite,SWT.NONE);
		{
			// exclusive or inclusive ?
			Composite typeArea = new Composite(grpOptions, SWT.NONE);
			Label lblType = new Label(typeArea, SWT.LEFT);
			lblType.setText("Type of metric: ");
			btnExclusive = new Button(typeArea, SWT.RADIO);
			btnExclusive.setText("Exclusive");
			btnExclusive.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) { 
					appendMetricType();
				}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
			});
			btnExclusive.setSelection(true);
			btnInclusive = new Button(typeArea, SWT.RADIO);
			btnInclusive.setText("Inclusive");
			btnInclusive.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) { 
					appendMetricType();
				}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
			});
			GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(typeArea);

			// name of the metric
			Composite nameArea = new Composite(grpOptions, SWT.NONE);
			Label lblName = new Label(nameArea, SWT.LEFT);
			lblName.setText("New name for the derived metric:");
			this.txtName = new Text(nameArea, SWT.NONE);
			this.txtName.setToolTipText("Enter the new name of the derived metric");
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(nameArea);
			
			// percent option
			this.btnPercent = new Button(grpOptions, SWT.CHECK);
			this.btnPercent.setText("Display the metric percentage");
			this.btnPercent.setToolTipText("Also compute the percent in the metric");

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
	  
	  //==========================================================
	  // ---- PRIVATE METHODS
	  //==========================================================
	  private boolean appendMetricType() {
		  int iAppendType = 0; // 0=no append, 1=append inc, 2=append exc
		  String sName = this.txtName.getText();
		  boolean bEndsWithInc = sName.endsWith("(I)");
		  boolean bEndsWithExc = sName.endsWith("(E)");
		  boolean bIsInclusive = this.btnInclusive.getSelection();
		  if(bEndsWithInc) {
			  if(bIsInclusive)
				  return false;
			  else {
				  iAppendType = 2;
				  sName = sName.substring(0, sName.length()-3);
			  }
		  } else if(bEndsWithExc) {
			  if(!bIsInclusive)
				  return false;
			  else {
				  sName = sName.substring(0, sName.length()-3);
				  iAppendType = 1;
			  }
		  } else {
			  // the name has no suffix for inclusive/exclusive, we just append it
			  if(bIsInclusive)
				  iAppendType = 1;
			  else
				  iAppendType = 2;
		  }
		  if(iAppendType == 1) {
			  this.txtName.setText(sName + "(I)");
		  } else if(iAppendType == 2) {
			  this.txtName.setText(sName + "(E)");
		  }
		  return (iAppendType==0);
	  }

	  /**
	   * check if the expression is correct
	   * @return
	   */
	  private boolean checkExpression() {
		  boolean bResult = false;
			String sExpression = this.txtExpression.getText();
			if(sExpression.length() > 0) {
				try {
					this.expFormula = ExpressionTree.parse(sExpression);
					bResult = true;
				} catch (ExpressionParseException e) {
					MessageDialog.openError(this.getShell(), "Invalid expression", e.getDescription());
				}
			} else {
				MessageDialog.openError(this.getShell(), "Error: empty expression", 
					"An expression can not be empty.");
			}
		  return bResult;
	  }
	  
	  //==========================================================
	  // ---- PUBLIC METHODS
	  //==========================================================
		
	  public void setMetrics(BaseMetric []listOfMetrics) {
		  int nbMetrics = listOfMetrics.length;
		  //this.arrMetrics = listOfMetrics;
		  this.arrStrMetrics = new String[nbMetrics];
		  for(int i=0;i<nbMetrics;i++) {
			  this.arrStrMetrics[i]="$"+i + ": "+ listOfMetrics[i].getDisplayName();
		  }
	  }
	  /**
	   * get the expression of the derived metric.
	   * This method should be called once the user click the OK button
	   */
	  public Expression getExpression() {
		  return this.expFormula;
	  }
	  
	  /**
	   * Return the new name of the metric
	   * @return the name 
	   */
	  public String getName() {
		  return this.sMetricName;
	  }
	  
	  /**
	   * return if the percent has to be displayed or not.
	   * @return true if the percent has to be displayed
	   */
	  public boolean getPercentDisplay() {
		  return this.bPercent;
	  }
	  
	  /**
	   * return true if the metrics' type is exclusive
	   * @return
	   */
	  public boolean isExclusive() {
		  return this.bExclusive;
	  }
	  /**
	   * Call back method when the OK button is pressed
	   */
	  public void okPressed() {
		if(this.checkExpression()) {
			// save the options for further usage (required by the caller)
			this.bPercent = this.btnPercent.getSelection();
			this.sMetricName = this.txtName.getText();
			this.bExclusive = this.btnExclusive.getSelection();
			super.okPressed();
		}
	  }
	
	  /**
	   * Set the scope for verifying the formula
	   * @param s scope
	   */
	  /*
	  public void setScope(Scope s) {
		this.scope = s;
	  } */
	  
	//-----------------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Metric []metrics = new Metric[4];
		Scope scope = new RootScope(null, "Toto", "Tata", null);
		for(int i=0;i<4;i++) {
			metrics[i] = new Metric(null, "s"+i,"native-"+i,"display-"+i,false,true,
					"%",null,0);
			//MetricValue val = new MetricValue(i, i*10.0);
			//scope.setMetricValue(i, val);
		}
		ExtDerivedMetricDlg dlg =  new ExtDerivedMetricDlg(null, metrics);
		//dlg.setScope(scope);
		dlg.open();
	}

}
