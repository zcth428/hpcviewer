package edu.rice.cs.hpc.traceviewer.ui;

import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import edu.rice.cs.hpc.traceviewer.spaceTimeData.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
/**************************************************
 * A viewer for CallStackSamples.
 *************************************************/
public class CallStackViewer extends TableViewer
{
	/**The SpaceTimeData associated with this CallStackViewer.*/
	private SpaceTimeData stData;
	
	/**********************************************************************************
	 * The built-in viewer for lists of things (the actual graphical representation of
	 * the list of function names.
	 ********************************************************************************/
	private Table stack;

	
	/**The View in which this stack has been created.*/
    public HPCCallStackView csview;
    
    /**The list of function names to be displayed.*/
    private Vector<String> sampleVector;
    
	
    /**Creates a CallStackViewer with Composite parent, SpaceTimeData _stData, and HPCTraceView _view.*/
	public CallStackViewer(Composite parent, HPCCallStackView _csview)
	{
		super(parent, SWT.SINGLE | SWT.V_SCROLL);
		
		csview = _csview;
        stack = this.getTable();
        
        GridData data = new GridData(GridData.FILL_BOTH);
        stack.setLayoutData(data);
        
        //------------------------------------------------
        // add label provider
        //------------------------------------------------
        this.setLabelProvider(new LabelProvider() {
        	public Image getImage(Object element) {
        		if (element instanceof String) {
        			Image img = null;
        			if (stData != null)
        				img = stData.getColorTable().getImage((String)element);
        			return img;
        		}
        		
				return null;        		
        	}
        	
        	public String getText(Object element) {
        		if (element instanceof String)
        			return (String) element;
        		return null;
        	}
        });

        //------------------------------------------------
        // add content provider
        //------------------------------------------------
        this.setContentProvider( new IStructuredContentProvider(){

			public void dispose() {}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) { }

			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof ArrayList<?>) {
					Object o[] = ((ArrayList<?>) inputElement).toArray();
					return o;
				}
				return null;
			}
        	
        });
        
        this.stack.setVisible(false);
        
		stack.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event)
			{
				int depth = stack.getSelectionIndex(); 
				if(depth !=-1 && depth != stData.getDepth()) {
					//csview.traceview.setDepth(stack.getSelectionIndex(), true);
					stData.updateDepth(depth);
				}
			}
		});
	}
	
	
	/***
	 * set new database
	 * @param _stData
	 */
	public void updateData(SpaceTimeData _stData) {
		this.stData = _stData;

		this.resetStack();
		this.stack.setVisible(true);
	}
	
	/**********************************************************************
	 * Sets the sample displayed on the callstack viewer to be the one
	 * that most closely corresponds to (closeTime, process). Additionally,
	 * sets the depth to _depth.
	 *********************************************************************/
	public void setSample(double closeTime, int process, int _depth)
	{
		if (closeTime == -20)
			return;
		
		ProcessTimeline ptl;
		if (process == -1337)
			ptl = stData.getDepthTrace();
		else
			ptl = stData.getProcess(process);
		
		int sample = ptl.findMidpointBefore(closeTime);

		sampleVector = ptl.getSample(sample).getNames();

		int numOverDepth = 0;
		if (sampleVector.size()<=_depth)
		{
			numOverDepth = _depth-sampleVector.size()+1;
			for(int l = 0; l<numOverDepth; l++)
				sampleVector.add("--------------");
		}
		this.setInput(new ArrayList<String>(sampleVector));
	
		stack.select(_depth);
		stack.redraw();
	}
	
	/**Removes unnecessary over depth "--------------"s from the stack.*/
	public void fixSample()
	{
		while((stack.getItemCount() - 1 > stData.getDepth()) && 
			  stack.getItem(stack.getItemCount()-1).equals("--------------"))
		{
			stack.remove(stack.getItemCount() - 1);
		}
		stack.select(stData.getDepth());
	}
	
	/**Sets the viewer's depth to _depth.*/
	public void setDepth(int _depth)
	{
		stack.redraw();
	}
	
	
	/***
	 * reset the content of the stack
	 */
	private void resetStack() {
		final ArrayList<String> callstackname = new ArrayList<String>();

        callstackname.add("Select a sample");
        callstackname.add("from the Trace View");

        this.setInput(callstackname);
	}

}
