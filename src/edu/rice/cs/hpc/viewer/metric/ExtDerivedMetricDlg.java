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
 *
 */
public class ExtDerivedMetricDlg extends TitleAreaDialog {
	private Label lblExpression;
	private Text txtName;
	private Button btnPercent;

	private Metric []arrMetrics;
	private String []arrStrMetrics;
	private Text txtExpression;
	final FuncMap fctMap = new FuncMap();

	private Scope scope;
	private Expression expFormula;

	private String sMetricName;
	private boolean bPercent;
	  //==========================================================
	  // ---- Constructor
	  //==========================================================
	/**
	 * Constructor to accept Metrics
	 * @param parentShell
	 * @param listOfMetrics
	 */
	public ExtDerivedMetricDlg(Shell parentShell, Metric []listOfMetrics) {
		super(parentShell);
		this.setMetrics(listOfMetrics);
	}
	
	public ExtDerivedMetricDlg(Shell parent, Metric []listOfMetrics, Scope s) {
		super(parent);
		this.setMetrics(listOfMetrics);
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

	  /**
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
	    	lbl.setText("Type a formula for the derived metric. Example: avg($1,$2,$3)/sum($1,$2,$3)");
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
	    	final String []arrFunctions = new String[arrFct.length];
	    	final String []arrFuncNames = fctMap.getFunctionNames();
	    	for(int i=0;i<arrFct.length;i++) {
	    		arrFunctions[i] = arrFct[i].toString();
	    	}
	    	if(arrFunctions != null && arrFunctions.length>0) {
	    		cbFunc.setItems(arrFunctions);
	    		cbFunc.setText(arrFunctions[0]);
	    	}
	    	final Button btnFunc = new Button(grpInsertion, SWT.PUSH);
	    	btnFunc.setText("Insert function");
	    	btnFunc.addSelectionListener(new SelectionListener() {
	   			public void widgetSelected(SelectionEvent e) {
	   				int iPos = txtExpression.getCaretPosition();
	   				String sFunc = arrFuncNames[cbFunc.getSelectionIndex()];
	   				txtExpression.insert( sFunc + "()");
	   				txtExpression.setSelection(iPos+sFunc.length()+1);
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
	    	});

	    	// do not expand the group
	    	GridDataFactory.fillDefaults().grab(false, false).applyTo(grpInsertion);
	    	GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(grpInsertion);
	    	
	    	//---------------- preview
	    	Group grpPreview = new Group(expressionArea, SWT.NONE);
	    	grpPreview.setText("Help: Formula preview");
	    	Button btnExpression = new Button(grpPreview, SWT.PUSH);
	    	btnExpression.setText("Preview");	    	
	    	lblExpression = new Label(grpPreview, SWT.NONE);
	    	lblExpression.setText("");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(lblExpression);
	    	btnExpression.addSelectionListener(new SelectionListener() {
	   			public void widgetSelected(SelectionEvent e) {
	   				if(checkExpression()) {
	   					Double val = applyFormula(scope);
	   					if(val != null) {
	   						lblExpression.setText("Scope:"+scope.getName()+", result:" + val);
	   						return;
	   					}
	   				}
	   				// there is something wrong with the expression
	   				lblExpression.setText("Expression is invalid");
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
	    	});
	    	//GridDataFactory.fillDefaults().grab(false, false).applyTo(grpPreview);
	    	GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(grpPreview);
	    	
	    	GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(expressionArea);
	    }
		GridLayoutFactory.fillDefaults().margins(5, 5).generateLayout(grpBase);
		
		//-------
		// options
		Group grpOptions = new Group(composite,SWT.NONE);
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
	  /**
	   * Attempt to apply the metrics in the scope into the expression 
	   * @param scope
	   * @return
	   */
	  private Double applyFormula(Scope scope) {
		  Double result = null;
		  MetricVarMap varMap;
		  if(scope != null) {
			  varMap = new MetricVarMap(scope);
		  } else {
			  System.out.println("Warning: scope of the node is not set !");
			  varMap = new MetricVarMap();
		  }
		  varMap.setMetrics(arrMetrics);
		  try {
			  result = expFormula.eval(varMap, fctMap);
		  } catch (java.lang.Exception objException) {
			  MessageDialog.openError(getShell(),"Invalid expression", "Error:"+objException.toString());
		  }	   					
		  return result;
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
		
	  public void setMetrics(Metric []listOfMetrics) {
		  int nbMetrics = listOfMetrics.length;
		  this.arrMetrics = listOfMetrics;
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
	   * Call back method when the OK button is pressed
	   */
	  public void okPressed() {
		if(this.checkExpression()) {
			// save the options for further usage (required by the caller)
			this.bPercent = this.btnPercent.getSelection();
			this.sMetricName = this.txtName.getText();
			super.okPressed();
		}
	  }
	
	  /**
	   * Set the scope for verifying the formula
	   * @param s scope
	   */
	  public void setScope(Scope s) {
		this.scope = s;
	  }
	  
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
		dlg.setScope(scope);
		dlg.open();
	}

}
