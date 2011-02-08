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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
// hpcviewer
import edu.rice.cs.hpc.data.experiment.Experiment;
import edu.rice.cs.hpc.data.experiment.metric.*;
import edu.rice.cs.hpc.data.experiment.scope.Scope;
import edu.rice.cs.hpc.viewer.util.UserInputHistory;
// math expression
import com.graphbuilder.math.*;
import com.graphbuilder.math.func.*;

/**
 * @author la5
 * Dialog box to enter a math formula to define a derived metric
 */
public class ExtDerivedMetricDlg extends TitleAreaDialog {
	//------------- GUI variables
	private Combo cbName;
	private Combo cbExpression;
	private Button btnPercent;

	// ------------ Metric and math variables
	private String []arrStrMetrics;
	private Expression expFormula;
	private final ExtFuncMap fctMap;
	private final MetricVarMap varMap;
	
	// ------------- Others
	static private final String HISTORY_FORMULA = "formula";			//$NON-NLS-1$
	static private final String HISTORY_METRIC_NAME = "metric_name";	//$NON-NLS-1$
	private String sMetricName;
	private boolean bPercent;
	private Experiment experiment;
	private Point expression_position;
	
	// ------------- object for storing history of formula and metric names
	private UserInputHistory objHistoryFormula;
	private UserInputHistory objHistoryName;

	//==========================================================
	  // ---- Constructor
	  //==========================================================
	/**
	 * Constructor to accept Metrics
	 * @param parentShell
	 * @param listOfMetrics
	 */
	public ExtDerivedMetricDlg(Shell parentShell, Experiment exp) {
		this(parentShell, exp, null);
	}
	
	public ExtDerivedMetricDlg(Shell parent, Experiment exp, Scope s) {
		super(parent);
		experiment = exp;
		this.setMetrics(exp.getMetrics());
		this.fctMap = new ExtFuncMap(exp.getMetrics(), null);
		this.varMap = new MetricVarMap ( s, exp );
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
	    setMessage("A derived metric is based on a simple arithmetic expression of base metrics\n");

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
	    	
			//--------------------------------------------
			// name of the metric
			//--------------------------------------------
			final Composite nameArea = new Composite(grpExpression, SWT.NONE);
			final Label lblName = new Label(nameArea, SWT.LEFT);
			lblName.setText("New name for the derived metric:");
			
			this.cbName = new Combo(nameArea, SWT.NONE);
			this.cbName.setToolTipText("Enter the new name of the derived metric");
			objHistoryName = new UserInputHistory( ExtDerivedMetricDlg.HISTORY_METRIC_NAME );
			this.cbName.setItems( this.objHistoryName.getHistory() );
			
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(nameArea);
			
			//--------------------------------------------
			// formula
			//--------------------------------------------
	    	Label lbl = new Label(grpExpression, SWT.WRAP);
	    	lbl.setText("The value of a metric is labeled with $x where x is the metric ID which can be found in the Metric help combo box.\n" + 
	    		"For instance, an expression \'$1 / $2 * 100.0\' means that" + 
	    		" the new metric is computed by dividing the value of metric \'1\' with the value of metric \'2\' and multiplied with number 100.0");
	    	
			final Composite formulaArea = new Composite(grpExpression, SWT.NONE);
	    	Label lblFormula = new Label(formulaArea, SWT.NONE);
	    	lblFormula.setText("Formula: ");
	    	
	    	this.cbExpression = new Combo(formulaArea, SWT.NONE);
	    	objHistoryFormula = new UserInputHistory(HISTORY_FORMULA);
	    	this.cbExpression.setItems( objHistoryFormula.getHistory() );
	    	cbExpression.setToolTipText("Write a simple arithmetic expression");
	    				
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(formulaArea);

	    	
	    	expression_position = new Point(0,0);
	    	cbExpression.addKeyListener( new KeyAdapter(){

				public void keyReleased(KeyEvent e) {
					expression_position = cbExpression.getSelection();
				}
				
			});
			
	    	cbExpression.addMouseListener( new MouseAdapter(){
				public void mouseUp(MouseEvent e)  {
					if (cbExpression.getClientArea().contains(e.x, e.y)) {
						expression_position = cbExpression.getSelection();
					}
				}
				
			});
	    	
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
	   				final String sText = cbExpression.getText();
	   				final int iSelIndex = expression_position.x; 
	   				StringBuffer sBuff = new StringBuffer(sText);
	   				
	   				// insert the metric variable ( i.e.: $ + metric index)
	   				final String sMetricIndex = "$" + experiment.getMetric(cbMetric.getSelectionIndex()).getShortName() ; 
	   				sBuff.insert(iSelIndex, sMetricIndex );
	   				cbExpression.setText(sBuff.toString());

	   				// put cursor after the metric variable
	   				Point p = new Point(iSelIndex + sMetricIndex.length(), iSelIndex + sMetricIndex.length());
	   				cbExpression.setSelection( p );
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
	   				Point p = expression_position;
	   				String sFunc = arrFuncNames[cbFunc.getSelectionIndex()];
	   				StringBuffer sb = new StringBuffer( cbExpression.getText() );
	   				int iLen = sFunc.length();
	   				sb.insert( p.x, sFunc );
	   				sb.insert( p.x + iLen, "()" );
	   				p.x = p.x + iLen + 1;
	   				p.y = p.x;
	   				cbExpression.setText( sb.toString() );
	   				cbExpression.setSelection( p );
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
	  

	  /**
	   * check if the expression is correct
	   * @return
	   */
	  private boolean checkExpression() {
		  boolean bResult = false;
			String sExpression = this.cbExpression.getText();
			if(sExpression.length() > 0) {
				try {
					this.expFormula = ExpressionTree.parse(sExpression);
					bResult = evaluateExpression ( this.expFormula );
				} catch (ExpressionParseException e) {
					MessageDialog.openError(this.getShell(), "Invalid expression", e.getDescription());
				}
			} else {
				MessageDialog.openError(this.getShell(), "Error: empty expression", 
					"An expression can not be empty.");
			}
		  return bResult;
	  }
	  
	  /**
	   * Run the evaluation 
	   * @param objExpression
	   * @return
	   */
	  private boolean evaluateExpression ( Expression objExpression ) {
			//MetricVarMap vm = new MetricVarMap(false /* case sensitive */);

			// vm.setScope(null);
			try {
				objExpression.eval( varMap, fctMap);
				// if there is no exception, we assume everything goes fine
				return true;
				
			} catch(java.lang.Exception e) {
				// should throw an exception
				MessageDialog.openError( this.getShell(), "Error: incorrect expression", e.getMessage());
			}

			return false;
		}

	  //==========================================================
	  // ---- PUBLIC METHODS
	  //==========================================================
		
	  public void setMetrics(BaseMetric []listOfMetrics) {
		  int nbMetrics = listOfMetrics.length;
		  //this.arrMetrics = listOfMetrics;
		  this.arrStrMetrics = new String[nbMetrics];
		  for(int i=0;i<nbMetrics;i++) {
			  BaseMetric metric = listOfMetrics[i];
			  // laksono 2009.12.15: we need to use the shortname instead of the index
			  this.arrStrMetrics[i]="$"+metric.getShortName() + ": "+ metric.getDisplayName();
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
	  /*
	  public boolean isExclusive() {
		  return this.bExclusive;
	  }*/
	  /**
	   * Call back method when the OK button is pressed
	   */
	  public void okPressed() {
		if(this.checkExpression()) {
			// save the options for further usage (required by the caller)
			this.bPercent = this.btnPercent.getSelection();
			this.sMetricName = this.cbName.getText();
			
			// save user history
			this.objHistoryFormula.addLine( this.cbExpression.getText() );
			this.objHistoryName.addLine( this.cbName.getText() );
			//this.bExclusive = this.btnExclusive.getSelection();
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
	  /*
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
*/
}
