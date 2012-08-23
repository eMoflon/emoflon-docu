package org.moflon.tracing.sdm.events;

import org.eclipse.emf.ecore.EOperation;

public abstract class AbstractMatchEvent extends AbstractTraceEvent {

	private final EOperation op;
	private final Object[] params;
	
	protected AbstractMatchEvent(EOperation op, Object... paramValues) {
		this.op = op;
		this.params = paramValues;
	}

	@Override
	public Object getTraceData() {
		return op;
	}

	@Override
	public Object[] getFullTraceData() {
		Object[] result = new Object[params.length + 1];
		result[0] = op;
		for (int i = 0; i < params.length; i++) {
			result[i+1]=params[i];
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append('[');
		sb.append("EOperation: \"");
		sb.append(op.getName());
		sb.append("\"]");
		return super.toString();
	}
	
}
