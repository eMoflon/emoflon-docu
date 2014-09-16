package org.moflon.tracing.sdm.events;

public class CheckIsomorphicBindingEvent extends AbstractIsomorphismEvent {

	private final static String OPERATION_NAME = CheckIsomorphicBindingEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public CheckIsomorphicBindingEvent(String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value) {
		super(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value);
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
