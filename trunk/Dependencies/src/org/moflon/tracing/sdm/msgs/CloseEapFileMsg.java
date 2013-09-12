package org.moflon.tracing.sdm.msgs;



public class CloseEapFileMsg extends AbstractDebuggingMsg {

	public static final String COMMAND = "CLOSE_EAP_FILE";
	
	public CloseEapFileMsg() {
	}
	
	@Override
	public String toString() {
		return COMMAND;
	}
}
