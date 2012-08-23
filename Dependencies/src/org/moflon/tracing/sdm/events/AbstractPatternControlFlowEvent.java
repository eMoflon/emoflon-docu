package org.moflon.tracing.sdm.events;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;


public abstract class AbstractPatternControlFlowEvent extends AbstractTraceEvent {

	private final String storyPatternName;
	private final EOperation operation;
	
	protected AbstractPatternControlFlowEvent(String storyPatternName, EOperation operation) {
		this.storyPatternName = storyPatternName;
		this.operation = operation;
	}

	public String getStoryPatternName() {
		return storyPatternName;
	}
	
	public EOperation getOperation() {
		return operation;
	}

	@Override
	public Object getTraceData() {
		return storyPatternName;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{storyPatternName, operation};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append("[Pattern: \"");
		sb.append(storyPatternName);
		sb.append('[');
		sb.append(operation.getEContainingClass().getName());
		sb.append('.');
		sb.append(operation.getName());
		sb.append('(');
		EList<EParameter> params = operation.getEParameters();
		if (params != null && params.size() > 0)
			sb.append("...");
		sb.append(") :");
		sb.append(operation.getEType().getName());
		sb.append("]]");
		return sb.toString();
	}
	
}
