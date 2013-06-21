/**
 * 
 */
package edu.rice.cs.hpc.viewer.metric;
// jface
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.IllegalFormatException;

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
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
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
import edu.rice.cs.hpc.common.util.UserInputHistory;
// math expression
import com.graphbuilder.math.*;
import com.graphbuilder.math.func.*;

/**
 * @author la5
 * Dialog box to enter a math formula to define a derived metric
 */
public class ExtDerivedMetricDlg extends TitleAreaDialog {
	
	static public enum MetricDisplayFormat {
		Default, Percent, Custom
	}
	
	//------------- GUI variables
	private Combo cbName;
	private Combo cbExpression;
	private Button btnPercent;
	private Text txtFormat;
	private Button btnCustomFormat;
	private Button btnPercentFormat;
	private Button btnDefaultFormat;
	
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
	private String sFormat;
	MetricDisplayFormat displayFormat;
	
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
		this.fctMap = new ExtFuncMap(exp.getMetrics());
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
	    setMessage("A derived metric is a spreadsheet-like formula using other metrics (variables), operators, functions,\n"
	    			+ "and numerical constants.\n");

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
	    
		Point ptMargin = LayoutConstants.getMargins(); 
		ptMargin.x = 5;
		ptMargin.y = 5;

	    Composite expressionArea = new Composite(grpBase, SWT.NONE);
	    {
	    	Group grpExpression = new Group(expressionArea, SWT.NONE);
	    	
			//--------------------------------------------
			// name of the metric
			//--------------------------------------------
			final Composite nameArea = new Composite(grpExpression, SWT.NONE);
			final Label lblName = new Label(nameArea, SWT.LEFT);
			lblName.setText("Name:");
			
			this.cbName = new Combo(nameArea, SWT.NONE);
			this.cbName.setToolTipText("Name of the derived metric");
			objHistoryName = new UserInputHistory( ExtDerivedMetricDlg.HISTORY_METRIC_NAME );
			this.cbName.setItems( this.objHistoryName.getHistory() );
			
			//--------------------------------------------
			// formula
			//--------------------------------------------	    	
	    	Label lblFormula = new Label(nameArea, SWT.NONE);
	    	lblFormula.setText("Formula: ");
	    	
	    	this.cbExpression = new Combo(nameArea, SWT.NONE);
	    	objHistoryFormula = new UserInputHistory(HISTORY_FORMULA);
	    	this.cbExpression.setItems( objHistoryFormula.getHistory() );
	    	cbExpression.setToolTipText("A spreadsheet-like formula using other metrics (variables), arithmetic operators, functions, and numerical constants");
	    							
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(nameArea);

	    	Label lbl = new Label(grpExpression, SWT.WRAP);
	    	lbl.setText("There are two kinds of metric variables: point-wise and aggregate.  The former is like a spreadsheet cell, the "
						+ "latter is like a spreadsheet-column sum.  To form a variable, prepend '$' and '@', respectively, to a metric id.  "
						+ "For instance, the formula\n"
						+ "    (($2 - $1) * 100.0) / @1\n"
						+ "divides the scaled difference of the point-wise metrics 2 and 1 by the aggregate value of metric 1.");
	    	
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
	    	grpInsertion.setText("Assistance:");

	    	Label lblMetric = new Label(grpInsertion, SWT.NONE);
	    	lblMetric.setText("Metrics:");
	    	
	    	// combo box that lists the metrics
	    	final Combo cbMetric = new Combo(grpInsertion, SWT.READ_ONLY);
	    	cbMetric.setItems(this.arrStrMetrics);
	    	cbMetric.setText(this.arrStrMetrics[0]);

	    	//---------------------------------------------------------------
	    	// button to insert the metric code into the expression field
	    	//---------------------------------------------------------------

	    	final Composite buttonArea = new Composite(grpInsertion, SWT.NONE);
	    	final Button btnMetric = new Button(buttonArea, SWT.PUSH);
	    	btnMetric.setText("Point-wise");
	    	btnMetric.setToolTipText("Insert the metric as point-wise variable in the formula by prepending with '$' sign");
	    	
	    	btnMetric.addSelectionListener(new SelectionListener() {
	   			public void widgetSelected(SelectionEvent e) {
	   				insertMetricToFormula("$", cbMetric.getSelectionIndex());
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
	    	});
	    	
	    	final Button btnAggregate = new Button(buttonArea, SWT.PUSH);
	    	btnAggregate.setText("Aggregate");
	    	btnAggregate.setToolTipText("Insert the metric as aggregate variable in the formula by prepending with '@' sign");
	    	
	    	btnAggregate.addSelectionListener(new SelectionListener() {
	   			public void widgetSelected(SelectionEvent e) {
	   				insertMetricToFormula("@", cbMetric.getSelectionIndex());
	   			}
	   			public void widgetDefaultSelected(SelectionEvent e) {
	   				
	   			}
	    	});
	    	GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(buttonArea);

	    	//---------------- inserting function
	    	Label lblFunc = new Label(grpInsertion, SWT.NONE);
	    	lblFunc.setText("Functions:");
	    	
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
	    	
	    	final Label lblOperators = new Label(grpInsertion, SWT.NONE);
	    	lblOperators.setText("Operators: ");
	    	final Label lblOpValues  = new Label(grpInsertion, SWT.NONE);
	    	lblOpValues.setText("( ) + - * / ^");


	    	// do not expand the group
	    	GridDataFactory.fillDefaults().grab(false, false).applyTo(grpInsertion);
	    	GridLayoutFactory.fillDefaults().numColumns(3).generateLayout(grpInsertion);
	    	
	    	GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(expressionArea);
	    }
		GridLayoutFactory.fillDefaults().margins(ptMargin).generateLayout(grpBase);
		
		//-------
		// options
		//-------
		ExpandBar barOptions = new ExpandBar(composite,SWT.V_SCROLL);
		{
			Composite cOptions = new Composite (barOptions, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(1).margins(ptMargin).generateLayout(cOptions);
			
			// percent option
			this.btnPercent = new Button(cOptions, SWT.CHECK);
			this.btnPercent.setText("Augment metric value display with a percentage relative to column total");
			this.btnPercent.setToolTipText("For each metric value, display the annotation of percentage relative to aggregate metric value");

			// format option
			//final Composite cFormat = new Composite( cOptions, SWT.NONE );
			final Composite cCustomFormat = new Composite( cOptions, SWT.NONE );
			
			btnDefaultFormat = new Button(cCustomFormat, SWT.RADIO);
			btnDefaultFormat.setText("Default format");
			new Label( cCustomFormat, SWT.NONE );
			
			btnPercentFormat = new Button(cCustomFormat, SWT.RADIO);
			btnPercentFormat.setText("Display metric value as percent");
			new Label( cCustomFormat, SWT.NONE );
			
			btnCustomFormat = new Button(cCustomFormat, SWT.RADIO);
			btnCustomFormat.setText("Custom format");
			
			txtFormat = new Text(cCustomFormat, SWT.BORDER);
			final String txtCustomFormat = "The format is based on java.util.Formatter class which is almost equivalent to C's printf format. "; 
			txtFormat.setToolTipText(txtCustomFormat);
			btnCustomFormat.addSelectionListener(new SelectionListener(){

				public void widgetSelected(SelectionEvent e) {
					txtFormat.setEnabled(true); 
				}

				public void widgetDefaultSelected(SelectionEvent e) {}	
			});
			
			btnPercentFormat.addSelectionListener(new SelectionListener(){

				public void widgetSelected(SelectionEvent e) {
					txtFormat.setEnabled(false);
					txtFormat.setFocus();
				}

				public void widgetDefaultSelected(SelectionEvent e) {}
				
			});
			btnDefaultFormat.setSelection(true);

			// make sure to initialize the state of the text
			txtFormat.setEnabled(false); 
			
			final Label lblCustomFormat = new Label( cOptions, SWT.NONE );
			lblCustomFormat.setText(txtCustomFormat + "\n"
					+ "Example: '%6.2f ' will display 6 digit floating-points with 2 digit precision. ");
			
			GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(cCustomFormat);
			
			//GridLayoutFactory.fillDefaults().numColumns(1).generateLayout(cFormat);
			
			// item for expansion bar
			ExpandItem eiOptions = new ExpandItem(barOptions, SWT.NONE, 0);
			eiOptions.setText("Advanced options");
			eiOptions.setHeight(cOptions.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
			eiOptions.setControl(cOptions);
			eiOptions.setExpanded(true);
			
			barOptions.setToolTipText("Optional settings");
		}

		GridLayoutFactory.fillDefaults().numColumns(1).margins(ptMargin).generateLayout(
				composite);
	    return composite;
	  }
	  
	  
	  /*****
	   * insert the selected metric from the combo box into the formula field
	   * 
	   * @param signToPrepend: either '$' or '@'
	   * @param selection_index
	   */
	  private void insertMetricToFormula(String signToPrepend, int selection_index) {
		  final String sText = cbExpression.getText();
		  final int iSelIndex = expression_position.x; 
		  StringBuffer sBuff = new StringBuffer(sText);

		  // insert the metric variable ( i.e.: $ + metric index)
		  final String sMetricIndex = signToPrepend + experiment.getMetric(selection_index).getShortName() ; 
		  sBuff.insert(iSelIndex, sMetricIndex );
		  cbExpression.setText(sBuff.toString());

		  // put cursor after the metric variable
		  Point p = new Point(iSelIndex + sMetricIndex.length(), iSelIndex + sMetricIndex.length());
		  cbExpression.setSelection( p );
		  expression_position = cbExpression.getSelection();

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
	  
	  /***
	   * verify the validity of custom format
	   * @return
	   */
	  private boolean checkFormat() {
		  boolean bResult = true;
		  String sError = null;
		  if (this.btnCustomFormat.getSelection()) {
			  this.sFormat = this.txtFormat.getText();
			  
			  // custom format if selected, cannot be null
			  if (sFormat.length()==0) {
				  bResult = false;
				  sError = "Custom format cannot be empty.";
			  }
			  try {
				  Formatter format = new Formatter();
				  format.format(sFormat, 1.0);
				  
			  } catch (IllegalFormatException e) {
				  sError = "Format is incorrect.";
				  bResult = false;
			  } catch (FormatterClosedException e) {
				  bResult = false;
				  sError = "Illegal format.";
			  }
		  }
		  if (!bResult)
			  MessageDialog.openError(getShell(), "Format syntax error", sError);
		  return bResult;
	  }
	  
	  /**
	   * Run the evaluation 
	   * @param objExpression
	   * @return
	   */
	  private boolean evaluateExpression ( Expression objExpression ) {
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
		  this.arrStrMetrics = new String[nbMetrics];
		  for(int i=0;i<nbMetrics;i++) {
			  BaseMetric metric = listOfMetrics[i];
			  // laksono 2009.12.15: we need to use the shortname instead of the index
			  this.arrStrMetrics[i] = metric.getShortName() + ": "+ metric.getDisplayName();
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
	  
	  /***
	   * get the customized format (if defined)
	   * @return
	   */
	  public String getFormat() {
		  return this.sFormat;
	  }

	  /**
	   * Call back method when the OK button is pressed
	   */
	  public void okPressed() {
		if(this.checkExpression() && this.checkFormat()) {
			// save the options for further usage (required by the caller)
			bPercent = btnPercent.getSelection();
			sMetricName = cbName.getText();
			
			// save user history
			objHistoryFormula.addLine( cbExpression.getText() );
			objHistoryName.addLine( cbName.getText() );

			sFormat = null;
			displayFormat = MetricDisplayFormat.Default;
			
			if (this.btnCustomFormat.getSelection()) {
				sFormat = txtFormat.getText();
				displayFormat = MetricDisplayFormat.Custom;
			} else if (btnPercentFormat.getSelection()) {
				displayFormat = MetricDisplayFormat.Percent;
				sFormat = "%.2f %%";
			}
			
			super.okPressed();
		}
	  }
	
}
