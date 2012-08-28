package org.moflon.tracing.sdm.events;

import org.eclipse.emf.ecore.EOperation;

public abstract class AbstractMatchEvent extends AbstractTraceEvent {

	private final String storyPatternName;
	private final EOperation op;
	private final Object[] params;
	
	protected AbstractMatchEvent(String storyPatternName, EOperation op, Object... paramValues) {
		this.storyPatternName = storyPatternName;
		this.op = op;
		this.params = paramValues;
	}

	@Override
	public Object getTraceData() {
		return storyPatternName;
	}

	@Override
	public Object[] getFullTraceData() {
		Object[] result = new Object[params.length + 2];
		result[0] = storyPatternName;
		result[1] = op;
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
		sb.append("StoryPattern=\"" + storyPatternName + "\", ");
		sb.append("EOperation: \"");
		sb.append(op.getName());
		sb.append("\"]");
		return sb.toString();
	}
	
}
