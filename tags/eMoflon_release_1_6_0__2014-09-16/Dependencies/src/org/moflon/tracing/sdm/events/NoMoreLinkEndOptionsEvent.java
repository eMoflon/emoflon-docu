package org.moflon.tracing.sdm.events;


public class NoMoreLinkEndOptionsEvent extends AbstractTraceEvent implements
		PatternMatchingTraceEvent {

	private final static String OPERATION_NAME = NoMoreLinkEndOptionsEvent.class.getSimpleName();
	private final static String OPERATION_DESCRIPTION = "";
	
	private final String linkName;
	private final String srcObjName;
	private final String trgtObjName;
	private final String representation;
	
	public NoMoreLinkEndOptionsEvent(String linkName, String srcObjName, String trgtObjName) {
		this.linkName = linkName;
		this.srcObjName = srcObjName;
		this.trgtObjName = trgtObjName;
		this.representation = String.format("%1$s--{%2$s}-->%3$s", srcObjName, linkName, trgtObjName);
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
		return representation;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{ linkName, srcObjName, trgtObjName };
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append("[");
		sb.append(representation);
		sb.append("]");
		return sb.toString();
	}

}
