package org.moflon.tracing.sdm.events;

public class LinkDeletionEvent extends AbstractLinkGraphRewritingEvent implements
		DeletionEvent {

	private final static String OPERATION_NAME = LinkDeletionEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	public LinkDeletionEvent(String sourceNodeName, Class<?> sourceNodeType, Object sourceNodeValue, String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue, String targetRoleName) {
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
