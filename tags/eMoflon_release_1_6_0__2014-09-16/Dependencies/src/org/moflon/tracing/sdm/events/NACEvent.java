package org.moflon.tracing.sdm.events;

public abstract class NACEvent extends AbstractTraceEvent implements
		PatternMatchingTraceEvent {

	private final String patternName;
	
	public NACEvent(String patternName) {
		this.patternName = patternName;
	}
	
	@Override
	public Object getTraceData() {
		return patternName;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{ patternName };
	}

	protected String getPatternName() {
		return patternName;
	}
	
}
