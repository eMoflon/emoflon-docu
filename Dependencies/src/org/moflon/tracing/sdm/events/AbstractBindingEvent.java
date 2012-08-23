package org.moflon.tracing.sdm.events;

public abstract class AbstractBindingEvent extends AbstractTraceEvent {

	private final String objVarName;
	private final Class<?> objVarType;
	private final Object oldValue;
	private final Object newValue;
	
	protected AbstractBindingEvent(String objVarName, Class<?> objVarType, Object oldValue, Object newValue) {
		this.objVarName = objVarName;
		this.objVarType = objVarType;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	@Override
	public Object getTraceData() {
		return objVarName;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{ objVarName, objVarType, oldValue, newValue };
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append('[');
		sb.append("ObjVar=\")");
		sb.append(objVarName);
		sb.append(':');
		sb.append(getSimpleClassName(objVarType));
		sb.append('(');
		sb.append(oldValue);
		sb.append("-->");
		sb.append(newValue);
		sb.append(")]");
		return super.toString();
	}
	
	private static String getSimpleClassName(Class<?> type) {
		String t = type.getName();
		return t.substring(t.lastIndexOf('.')+1, t.length());
	}

}
