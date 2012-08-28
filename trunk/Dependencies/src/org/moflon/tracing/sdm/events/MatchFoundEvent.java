package org.moflon.tracing.sdm.events;

import org.eclipse.emf.ecore.EOperation;

public class MatchFoundEvent extends AbstractMatchEvent {

	private final static String OPERATION_NAME = MatchFoundEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public MatchFoundEvent(String storyPatternName, EOperation op, Object... paramValues) {
		super(storyPatternName, op, paramValues);
	}
	
	@Override
	public String getOperationName() {
		return OPERATION_NAME;
	}

	@Override
	public String getOperationDesctiption() {
		return OPERATION_DESCRIPTION;
	}

}
