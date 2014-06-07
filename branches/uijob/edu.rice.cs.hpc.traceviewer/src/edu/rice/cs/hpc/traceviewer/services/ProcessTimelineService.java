package edu.rice.cs.hpc.traceviewer.services;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;

import edu.rice.cs.hpc.traceviewer.data.timeline.ProcessTimeline;

public class ProcessTimelineService extends AbstractSourceProvider {

	final static public String PROCESS_TIMELINE_PROVIDER = "edu.rice.cs.hpc.traceviewer.services.ProcessTimelineService.data";
	private ProcessTimeline []traces;


	@Override
	public void dispose() {	}

	@Override
	public Map getCurrentState() {
		Map<String, Object> map = new HashMap<String, Object>(1);
		map.put(PROCESS_TIMELINE_PROVIDER, traces);
		
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] {PROCESS_TIMELINE_PROVIDER};
	}

	public void setProcessTimeline(ProcessTimeline[] traces) {
		this.traces = traces;
	}
	
	
	public void setProcessTimeline(int index, ProcessTimeline trace) {
		traces[index] = trace;
	}
 	
	
	public ProcessTimeline getProcessTimeline(int proc) {
		if (traces == null)
			return null;
		
		return traces[proc];
	}
	
	public int getNumProcessTimeline() {
		if (traces == null)
			return 0;
		return traces.length;
	}
}
