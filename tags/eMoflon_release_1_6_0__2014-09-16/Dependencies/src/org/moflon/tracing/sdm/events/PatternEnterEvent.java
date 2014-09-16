package org.moflon.tracing.sdm.events;

import org.eclipse.emf.ecore.EOperation;

public class PatternEnterEvent extends AbstractPatternControlFlowEvent {

	private static final String OPERATION_NAME = "PatternEnterEvent";
	private static final String OPERATION_DESCRIPTION = "";
	
	public PatternEnterEvent(String storyPatternName, EOperation operation) {
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
