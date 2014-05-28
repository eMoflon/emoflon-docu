package org.moflon.tracing.sdm.events;

public class SuccessNACEvent extends NACEvent implements
		PatternMatchingTraceEvent {

	private static final String OPERATION_NAME = SuccessNACEvent.class.getSimpleName();
	private static final String OPERATION_DESCRIPTION = "";
	
	public SuccessNACEvent(String patternName) {
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
		return "SuccessNACEvent [patternName=" + getPatternName() + "]";
	}
	
}
