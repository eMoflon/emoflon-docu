package org.moflon.tracing.sdm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.moflon.tracing.sdm.events.TraceEvent;

public class SDMTraceContext {

	private HashMap<StackTraceElement, List<TraceEvent>> data = new HashMap<StackTraceElement, List<TraceEvent>>();
	
	private List<TraceEvent> allData = new LinkedList<TraceEvent>(); 
	
	protected SDMTraceContext() {
	}
	
	protected void traceEvent(StackTraceElement stackElem, TraceEvent e) {
		if (stackElem == null)
			throw new IllegalArgumentException("Parameter may not be null");
		
		if (!isValidStackTraceElem(stackElem))
			throw new IllegalArgumentException("Invalid StackTraceElement provided");
		
		List<TraceEvent> list = data.get(stackElem);
		if (list == null) {
			list = new LinkedList<TraceEvent>();
			data.put(stackElem, list);
		}
		list.add(e);
		
		allData.add(e);
	}

	private boolean isValidStackTraceElem(StackTraceElement stackElem) {
		Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();		
		boolean isValidStackElem = false;
		for (StackTraceElement[] traces : allStackTraces.values()) {
			for (StackTraceElement curStackElem : traces) {
				if (stackElem.equals(curStackElem)) {
					isValidStackElem = true;
					break;
				}				
			}
			if (isValidStackElem)
				break;
		}
		return isValidStackElem;
	}
	
	public TraceEvent[] getFlatTrace() {
		return allData.toArray(new TraceEvent[]{});
	}
	
	public Map<StackTraceElement, TraceEvent[]> getAllTraces() {
		Map<StackTraceElement, TraceEvent[]> result = new HashMap<StackTraceElement, TraceEvent[]>();
		for (StackTraceElement ste : data.keySet()) {
			result.put(ste, getTrace(ste));
		}
		return result;
	}
	
	public TraceEvent[] getTrace(StackTraceElement ste) {
		if (ste == null)
			throw new IllegalArgumentException("Parameter may not be null");
		List<TraceEvent> list = data.get(ste);
		if (list != null)
			return list.toArray(new TraceEvent[]{});
		return null;
	}
	
	public void reset() {
		data.clear();
		allData.clear();
	}
}
