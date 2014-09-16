package org.moflon.tracing.sdm.events;

public class BeginNACEvaluationEvent extends NACEvent {

	private final static String OPERATION_NAME = BeginNACEvaluationEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public BeginNACEvaluationEvent(String patternName) {
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
		return "BeginNACEvaluationEvent [patternName=" + getPatternName()
				+ "]";
	}
	
}
