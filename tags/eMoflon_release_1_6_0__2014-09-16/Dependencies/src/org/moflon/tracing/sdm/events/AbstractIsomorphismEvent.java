package org.moflon.tracing.sdm.events;

public abstract class AbstractIsomorphismEvent extends AbstractTraceEvent implements PatternMatchingTraceEvent {

	protected final String objVar1Name;
	protected final String objVar2Name;
	protected final Class<?> objVar1Type;
	protected final Class<?> objVar2Type;
	protected final Object objVar1Value;
	protected final Object objVar2Value;
	private final String traceData;
	
	
	protected AbstractIsomorphismEvent(String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value) {
		this.objVar1Name = objVar1Name;
		this.objVar2Name = objVar2Name;
		this.objVar1Type = objVar1Type;
		this.objVar2Type = objVar2Type;
		this.objVar1Value = objVar1Value;
		this.objVar2Value = objVar2Value;
		traceData = String.format("JavaSDM.ensure(!%1$s.equals(%2$s))", objVar1Name, objVar2Name);
	}

	@Override
	public Object getTraceData() {		
		return traceData;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{traceData, objVar1Type, objVar1Value, objVar2Type, objVar2Value};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append('[');
		sb.append(objVar1Name + ':' + objVar1Type.getSimpleName());
		sb.append("=\"");
		sb.append(objVar1Value);
		sb.append("\" =?= ");
		sb.append(objVar2Name + ':' + objVar2Type.getSimpleName());
		sb.append("=\"");
		sb.append(objVar2Value);
		sb.append("\"]");
		return sb.toString();
	}
	
}
