package org.moflon.tracing.sdm;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
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
	
	public Map<StackTraceWrapper, TraceEvent[]> getAllTracesForMethod(Method m) {
		Map<StackTraceWrapper, TraceEvent[]> result = new HashMap<StackTraceWrapper, TraceEvent[]>();
		for (StackTraceWrapper stw : data.keySet()) {
			if (stw.getMethod().equals(m))
				result.put(stw, getTrace(stw));
		}
		return result;
	}
	
	public Map<StackTraceWrapper, TraceEvent[]> getAllTracesForMethod(EOperation op) {
		Map<StackTraceWrapper, TraceEvent[]> result = new HashMap<StackTraceWrapper, TraceEvent[]>();
		for (StackTraceWrapper stw : data.keySet()) {
			if (equalEOperations(stw.getOperation(), op))
				result.put(stw, getTrace(stw));
		}
		return result;
	}
	
	private boolean equalEOperations(EOperation op1, EOperation op2) {
		if (op1.getName().equals(op2.getName())) {
			if (equalEClassifier(op1.getEContainingClass(), op2.getEContainingClass())) {
				EList<EParameter> params1 = op1.getEParameters();
				EList<EParameter> params2 = op2.getEParameters();				
				if (params1.size() == params2.size()) {
					Iterator<EParameter> it1 = params1.iterator();
					Iterator<EParameter> it2 = params2.iterator();
					boolean flag = true;
					while (it1.hasNext() && it2.hasNext()) {
						EParameter next1 = it1.next();
						EParameter next2 = it2.next();
						flag = flag && equalEClassifier(next1.getEType(), next2.getEType());
					}
					if (flag)
						return true;
				}
			}
		}
		return false;
	}
	
	private boolean equalEClassifier(EClassifier c1, EClassifier c2) {
		if (c1.getName().equals(c2.getName())) {
			return (equalEPackage(c1.getEPackage(), c2.getEPackage()));
		}
		
		return false;
	}
	
	private boolean equalEPackage(EPackage p1, EPackage p2) {
		if (p1.getName().equals(p2.getName())) {
			EPackage superP1 = p1.getESuperPackage(); 
			EPackage superP2 = p2.getESuperPackage();
			if (superP1 == null && superP2 == null)
				return true;
			if (superP1 != null && superP2 != null)
				return equalEPackage(superP1, superP2);
		}
		
		return false;
	}
	
	public TraceEvent[] getTrace(StackTraceWrapper stw) {
		if (stw == null)
			throw new IllegalArgumentException("Parameter may not be null");
		List<TraceEvent> list = data.get(stw);
		if (list != null)
			return list.toArray(new TraceEvent[]{});
		return null;
	}
	
	public TraceEvent[] getPseudoFlatTraceForMethod(Method m) {
		List<TraceEvent> result = new LinkedList<TraceEvent>();
		List<StackTraceWrapper> relevantSTWs = new LinkedList<StackTraceWrapper>();
		for (StackTraceWrapper stw : data.keySet()) {
			if (stw.getMethod().equals(m)) {
				relevantSTWs.add(stw);
			}
		}
		if (relevantSTWs.isEmpty())
			return new TraceEvent[]{};
		for (int i = 0; i < allData.size(); i++) {
			TraceEvent traceEvent = allData.get(i);
			for (StackTraceWrapper stw : relevantSTWs) {
				if (data.get(stw).contains(traceEvent)) {
					result.add(traceEvent);
					continue;
				}
			}
		}
		return result.toArray(new TraceEvent[]{});
	}
	

	
	public void reset() {
		data.clear();
		allData.clear();
	}
}
