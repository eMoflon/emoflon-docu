package org.moflon.tracing.sdm.events;

public class ObjectDeletionEvent extends AbstractTraceEvent implements
		DeletionEvent {

	private final static String OPERATION_NAME = ObjectDeletionEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";

	private final String objVarName;
	private final Class<?> objVarType;
	private final Object oldObjectValue;
	
	public ObjectDeletionEvent(String objVarName, Class<?> objVarType, Object oldObjectValue) {
		this.objVarName = objVarName;
		this.objVarType = objVarType;
		this.oldObjectValue = oldObjectValue;
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
		return oldObjectValue;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{ oldObjectValue, objVarName, objVarType };
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append('[');
		sb.append(oldObjectValue);
		sb.append("; ");
		sb.append(objVarName);
		sb.append(':');
		sb.append(getSimpleClassName(objVarType));
		sb.append(']');
		return sb.toString();
	}
	
	private static String getSimpleClassName(Class<?> type) {
		String t = type.getName();
		return t.substring(t.lastIndexOf('.')+1, t.length());
	}

}
