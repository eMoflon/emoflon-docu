package org.moflon.tracing.sdm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.moflon.tracing.sdm.events.TraceEvent;

public class SDMTraceContext {

	private HashMap<StackTraceWrapper, List<TraceEvent>> data = new HashMap<StackTraceWrapper, List<TraceEvent>>();
	
	private List<TraceEvent> allData = new LinkedList<TraceEvent>(); 
	
	protected SDMTraceContext() {
	}
	
	protected void traceEvent(StackTraceWrapper stackTraceWrapper, TraceEvent e) {
		if (stackTraceWrapper == null)
			throw new IllegalArgumentException("Parameter may not be null");
		
		 if (!isValidStackTraceWrapper(stackTraceWrapper))
			throw new IllegalArgumentException("Invalid StackTraceElement provided");
		
		List<TraceEvent> list = data.get(stackTraceWrapper);
		if (list == null) {
			list = new LinkedList<TraceEvent>();
			data.put(stackTraceWrapper, list);
		}
		list.add(e);
		
		allData.add(e);
	}

	private boolean isValidStackTraceWrapper(StackTraceWrapper stackTraceWrapper) {
		StackTraceElement[] stackTrace = (new Throwable()).getStackTrace();
		return stackTraceWrapper.isValidStackTrace(stackTrace);
	}
	
	public TraceEvent[] getFlatTrace() {
		return allData.toArray(new TraceEvent[]{});
	}
	
	public Map<StackTraceWrapper, TraceEvent[]> getAllTraces() {
		Map<StackTraceWrapper, TraceEvent[]> result = new HashMap<StackTraceWrapper, TraceEvent[]>();
		for (StackTraceWrapper stw : data.keySet()) {
			result.put(stw, getTrace(stw));
		}
		return result;
	}
	
	public TraceEvent[] getTrace(StackTraceWrapper stw) {
		if (stw == null)
			throw new IllegalArgumentException("Parameter may not be null");
		List<TraceEvent> list = data.get(stw);
		if (list != null)
			return list.toArray(new TraceEvent[]{});
		return null;
	}
	
	public void reset() {
		data.clear();
		allData.clear();
	}
}