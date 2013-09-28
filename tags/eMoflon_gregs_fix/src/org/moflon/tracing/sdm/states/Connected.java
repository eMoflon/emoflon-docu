package org.moflon.tracing.sdm.states;

import org.moflon.tracing.sdm.msgs.ProtocolMsg;
import org.moflon.tracing.sdm.msgs.TeardownDebuggingSessionMsg;


public class Connected extends AbstractProtocolState {

	protected static Connected instance = null;
	
	public static Connected getInstance() {
		if (instance == null) {
			instance = new Connected();
		}
		return instance;
	}
	
	private Connected() {
	}

	@Override
	public ProtocolState nextState(ProtocolMsg msg) {
		if (msg instanceof TeardownDebuggingSessionMsg) {
			return Init.getInstance();
		} 
		else if(msg == null)
		{
			return Sending.getInstance();
		}
		return super.nextState(msg);
	}
	
}
