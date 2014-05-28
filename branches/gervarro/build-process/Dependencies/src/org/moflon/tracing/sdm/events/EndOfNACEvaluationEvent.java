package org.moflon.tracing.sdm.events;

public class EndOfNACEvaluationEvent extends NACEvent implements
		PatternMatchingTraceEvent {

	private final static String OPERATION_NAME = EndOfNACEvaluationEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public EndOfNACEvaluationEvent(String patternName) {
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
		return "EndOfNACEvaluationEvent [patternName=" + getPatternName()
				+ "]";
	}
	
}
