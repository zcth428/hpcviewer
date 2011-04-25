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

import edu.rice.cs.hpc.traceviewer.painter.Position;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.ProcessTimeline;
import edu.rice.cs.hpc.traceviewer.spaceTimeData.SpaceTimeData;
/**************************************************
 * A viewer for CallStackSamples.
 *************************************************/
public class CallStackViewer extends TableViewer
{
	/**The SpaceTimeData associated with this CallStackViewer.*/
	private SpaceTimeData stData;
	
    /**Creates a CallStackViewer with Composite parent, SpaceTimeData _stData, and HPCTraceView _view.*/
	public CallStackViewer(Composite parent, final HPCCallStackView _csview)
	{
		super(parent, SWT.SINGLE | SWT.V_SCROLL);
		
        final Table stack = this.getTable();
        
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
        
        stack.setVisible(false);
        final CallStackViewer csviewer = this;
		stack.addListener(SWT.Selection, new Listener(){
			public void handleEvent(Event event)
			{
				int depth = stack.getSelectionIndex(); 
				if(depth !=-1 && depth != stData.getDepth()) {
					// ask the depth editor to update the depth and launch the updateDepth event
					_csview.depthEditor.setSelection(depth);
					stData.updateDepth(depth, csviewer);
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
		this.getTable().setVisible(true);
	}
	
	/**********************************************************************
	 * Sets the sample displayed on the callstack viewer to be the one
	 * that most closely corresponds to (closeTime, process). Additionally,
	 * sets the depth to _depth.
	 *********************************************************************/
	public void setSample(Position position, int _depth)
	{
		if (position.time == -20)
			return;
		
		//-------------------------------------------------------------------------------------------
		// dirty hack: the call stack viewer requires relative index of process, not the absolute !
		// so if the region is zoomed, then the relative index is based on the displayed processes
		//
		// however, if the selected process is less than the start of displayed process, 
		// 	then we keep the selected process
		//-------------------------------------------------------------------------------------------
		int adjustedPosition = position.processInCS; /*( process < stData.getBegProcess() ? 
				process : process-stData.getBegProcess() );
		
		if (process >= stData.getNumberOfDisplayedProcesses() ) {
			double scale = stData.getHeight() / stData.getNumberOfDisplayedProcesses();
			adjustedPosition = (int) (process /  scale);
		}*/
		ProcessTimeline ptl;
		ptl = stData.getProcess(adjustedPosition);
		
		int sample = ptl.findMidpointBefore(position.time);

		final Vector<String> sampleVector = ptl.getSample(sample).getNames();

		int numOverDepth = 0;
		if (sampleVector.size()<=_depth)
		{
			numOverDepth = _depth-sampleVector.size()+1;
			for(int l = 0; l<numOverDepth; l++)
				sampleVector.add("--------------");
		}
		this.setInput(new ArrayList<String>(sampleVector));
	
		this.setDepth(_depth);
	}
	

	
	/**Sets the viewer's depth to _depth.*/
	public void setDepth(int _depth)
	{
		this.getTable().select(_depth);
		this.getTable().redraw();
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
