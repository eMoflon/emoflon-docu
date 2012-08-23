package org.moflon.tracing.sdm.events;

public class UnbindObjectVarEvent extends AbstractBindingEvent {

	private final static String OPERATION_NAME = UnbindObjectVarEvent.class.getName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public UnbindObjectVarEvent(String objVarName, Class<?> objVarType, Object oldValue, Object newValue) {
		super(objVarName, objVarType, oldValue, newValue);
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
