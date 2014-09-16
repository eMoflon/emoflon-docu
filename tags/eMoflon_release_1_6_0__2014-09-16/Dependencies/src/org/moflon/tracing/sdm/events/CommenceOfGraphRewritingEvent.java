package org.moflon.tracing.sdm.events;

public class CommenceOfGraphRewritingEvent extends AbstractTraceEvent implements
		GraphRewritingTraceEvent {

	private static final String OPERATION_NAME = CommenceOfGraphRewritingEvent.class.getSimpleName();
	private static final String OPERATION_DESCRIPTION = "";
	
	private final String patternName;
	
	public CommenceOfGraphRewritingEvent(String patternName) {
		this.patternName = patternName;
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
	public Object getTraceData() {
		return patternName;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{ patternName };
	}

	@Override
	public String toString() {
		return "CommenceOfGraphRewritingEvent [patternName=" + patternName
				+ "]";
	}

}
