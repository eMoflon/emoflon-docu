package org.moflon.tracing.sdm.events;

public abstract class AbstractLinkGraphRewritingEvent extends AbstractTraceEvent implements GraphRewritingTraceEvent {

	protected final String sourceNodeName;
	protected final Class<?> sourceNodeType;
	protected final Object sourceNodeValue;
	protected final String sourceRoleName;
	protected final String targetNodeName;
	protected final Class<?> targetNodeType;
	protected final Object targetNodeValue;
	protected final String targetRoleName;
	protected final String linkStr;
	
	public AbstractLinkGraphRewritingEvent(String sourceNodeName, Class<?> sourceNodeType, Object sourceNodeValue, String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue, String targetRoleName) {
		this.sourceNodeName = sourceNodeName;
		this.sourceNodeType = sourceNodeType;
		this.sourceNodeValue = sourceNodeValue;
		this.sourceRoleName = sourceRoleName;
		this.targetNodeName = targetNodeName;
		this.targetNodeType = targetNodeType;
		this.targetNodeValue = targetNodeValue;
		this.targetRoleName = targetRoleName;
		this.linkStr = String.format("%1$s --> %2$s", sourceNodeName, targetNodeName);
	}
	
	@Override
	public Object getTraceData() {
		return linkStr;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{sourceNodeName, sourceNodeType, sourceNodeValue, targetNodeName, targetNodeType, targetNodeValue};
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append("[{");
		sb.append(sourceRoleName);
		sb.append('}');
		sb.append(sourceNodeName);
		sb.append(':');
		sb.append(getSimpleClassName(sourceNodeType));
		sb.append('(');
		sb.append(sourceNodeValue);
		sb.append(") --> {");
		sb.append(sourceRoleName);
		sb.append('}');
		sb.append(targetNodeName);
		sb.append(':');
		sb.append(getSimpleClassName(targetNodeType));
		sb.append('(');
		sb.append(targetNodeValue);
		sb.append(")]");
		return sb.toString();
	}

	private static String getSimpleClassName(Class<?> type) {
		String t = type.getName();
		return t.substring(t.lastIndexOf('.')+1, t.length());
	}

}
