package org.moflon.tracing.sdm.events;


public interface TraceEvent {
	public String getOperationName();
	public String getOperationDesctiption();
	public Object getTraceData();
	public Object[] getFullTraceData();
}
