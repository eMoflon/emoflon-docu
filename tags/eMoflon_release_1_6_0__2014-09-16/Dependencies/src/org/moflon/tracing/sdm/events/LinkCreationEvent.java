package org.moflon.tracing.sdm.events;

public class LinkCreationEvent extends AbstractLinkGraphRewritingEvent implements
		CreationEvent {

	private final static String OPERATION_NAME = LinkCreationEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public LinkCreationEvent(String sourceNodeName, Class<?> sourceNodeType, Object sourceNodeValue, String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue, String targetRoleName) {
		super(sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName);
	}
	
	@Override
	public String getOperationName() {
		return OPERATION_NAME;
	}

	@Override
	public String getOperationDesctiption() {
		return OPERATION_DESCRIPTION;
	}
	
}
