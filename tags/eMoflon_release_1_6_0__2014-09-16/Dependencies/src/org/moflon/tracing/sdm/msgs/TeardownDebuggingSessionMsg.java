package org.moflon.tracing.sdm.msgs;


public class TeardownDebuggingSessionMsg extends AbstractDebuggingMsg {

	public static final String COMMAND = "TEARDOWN_DEBUG_SESSION";
	
	public TeardownDebuggingSessionMsg() {
	}
	
	@Override
	public String toString() {
		return COMMAND;
	}
}
