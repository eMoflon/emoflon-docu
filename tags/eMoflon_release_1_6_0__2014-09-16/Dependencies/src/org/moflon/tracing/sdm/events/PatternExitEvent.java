package org.moflon.tracing.sdm.events;

import org.eclipse.emf.ecore.EOperation;

public class PatternExitEvent extends AbstractPatternControlFlowEvent {

	private static final String OPERATION_NAME = "PatternExitEvent";
	private static final String OPERATION_DESCRIPTION = "";
	
	public PatternExitEvent(String storyPatternName, EOperation operation) {
		super(storyPatternName, operation);
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
