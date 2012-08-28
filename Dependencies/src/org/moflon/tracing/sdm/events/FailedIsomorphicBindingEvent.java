package org.moflon.tracing.sdm.events;

public class FailedIsomorphicBindingEvent extends AbstractIsomorphismEvent {

	private static final String OPERATION_NAME = FailedIsomorphicBindingEvent.class.getSimpleName();
	private static final String OPERATION_DESCRIPTION = "";
	
	public FailedIsomorphicBindingEvent(String objVar1Name,	Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value) {
		super(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type,	objVar2Value);
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
