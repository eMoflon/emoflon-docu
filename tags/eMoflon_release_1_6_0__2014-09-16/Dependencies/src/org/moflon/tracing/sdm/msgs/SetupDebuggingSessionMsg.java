package org.moflon.tracing.sdm.msgs;


public class SetupDebuggingSessionMsg extends AbstractDebuggingMsg {

	public static final String COMMAND = "SETUP_DEBUG_SESSION";
	
	public SetupDebuggingSessionMsg() {
	}
	
	@Override
	public String toString() {
		return COMMAND;
	}
}
