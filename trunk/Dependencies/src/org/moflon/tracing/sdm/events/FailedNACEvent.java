package org.moflon.tracing.sdm.events;

public class FailedNACEvent extends NACEvent implements
		PatternMatchingTraceEvent {

	private static final String OPERATION_NAME = FailedNACEvent.class.getSimpleName();
	private static final String OPERATION_DESCRIPTION = "";
	
	public FailedNACEvent(String patternName) {
		super(patternName);
	}
	
	@Override
	public String getOperationName() {
		return OPERATION_NAME;
	}

	@Override
	public String getOperationDesctiption() {
		return OPERATION_DESCRIPTION;
	}

	@Override
	public String toString() {
		return "FailedNACEvent [patternName=" + getPatternName() + "]";
	}

}
