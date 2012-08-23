package org.moflon.tracing.sdm.events;

import org.eclipse.emf.ecore.EOperation;

public class NoMatchFoundEvent extends AbstractMatchEvent {
	
	private final static String OPERATION_NAME = NoMatchFoundEvent.class.getName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public NoMatchFoundEvent(EOperation op, Object... paramValues) {
		super(op, paramValues);
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
