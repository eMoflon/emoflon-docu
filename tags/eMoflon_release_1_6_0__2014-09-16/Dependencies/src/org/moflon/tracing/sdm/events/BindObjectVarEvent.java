package org.moflon.tracing.sdm.events;

public class BindObjectVarEvent extends AbstractBindingEvent {

	private final static String OPERATION_NAME = BindObjectVarEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public BindObjectVarEvent(String objVarName, Class<?> objVarType, Object oldValue, Object newValue) {
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

	@Override
	public String toString() {
		return super.toString();
	}	
	
}
