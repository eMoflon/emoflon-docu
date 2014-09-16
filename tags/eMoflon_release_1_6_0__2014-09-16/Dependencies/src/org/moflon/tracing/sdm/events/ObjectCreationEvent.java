package org.moflon.tracing.sdm.events;

public class ObjectCreationEvent extends AbstractTraceEvent implements
		CreationEvent {
	
	private final static String OPERATION_NAME = ObjectCreationEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";

	private final String objVarName;
	private final Class<?> objVarType;
	private final Object newObjectValue;
	
	public ObjectCreationEvent(String objVarName, Class<?> objVarType, Object newObjectValue) {
		this.objVarName = objVarName;
		this.objVarType = objVarType;
		this.newObjectValue = newObjectValue;
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
	public Object getTraceData() {
		return newObjectValue;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{ newObjectValue, objVarName, objVarType };
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append('[');
		sb.append(objVarName);
		sb.append(':');
		sb.append(getSimpleClassName(objVarType));
		sb.append(" <- ");
		sb.append(newObjectValue);
		sb.append(']');
		return sb.toString();
	}
	
	private static String getSimpleClassName(Class<?> type) {
		String t = type.getName();
		return t.substring(t.lastIndexOf('.')+1, t.length());
	}
	
}
