package org.moflon.tracing.sdm.events;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;

public abstract class AbstractLightweightPatternControlFlowEvent extends AbstractPatternControlFlowEvent {

	private final String uniqueId; 
	
	protected AbstractLightweightPatternControlFlowEvent(String storyPatternName, EOperation operation, String uniqueId) {
		super(storyPatternName, operation);
		this.uniqueId = uniqueId;
	}
	
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{getStoryPatternName(), getOperation(), getUniqueId()};
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append("[Pattern: \"");
		sb.append(getStoryPatternName());
		sb.append('[');
		sb.append(getOperation().getEContainingClass().getName());
		sb.append('.');
		sb.append(getOperation().getName());
		sb.append('(');
		EList<EParameter> params = getOperation().getEParameters();
		if (params != null && params.size() > 0)
			sb.append("...");
		sb.append(") :");
		EClassifier returnType = getOperation().getEType();
		if (returnType == null)
			sb.append("void");
		else 
			sb.append(returnType.getName());
		sb.append("]; ");
		sb.append("UniqueId: ");
		sb.append(getUniqueId());
		sb.append(']');
		return sb.toString();
	}

}
