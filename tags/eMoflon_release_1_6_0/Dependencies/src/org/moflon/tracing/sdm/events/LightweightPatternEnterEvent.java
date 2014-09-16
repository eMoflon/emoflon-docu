package org.moflon.tracing.sdm.events;

import org.eclipse.emf.ecore.EOperation;

public class LightweightPatternEnterEvent extends AbstractLightweightPatternControlFlowEvent {

	private static final String OPERATION_NAME = LightweightPatternEnterEvent.class.getSimpleName();
	private static final String OPERATION_DESCRIPTION = "";
	
	public LightweightPatternEnterEvent(String storyPatternName, EOperation operation, String uniqueId) {
		super(storyPatternName, operation, uniqueId);
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
