package org.moflon.tracing.sdm.states;

import org.moflon.tracing.sdm.msgs.CloseEapFileMsg;
import org.moflon.tracing.sdm.msgs.ProtocolMsg;
import org.moflon.tracing.sdm.msgs.TeardownDebuggingSessionMsg;


public class Sending extends AbstractProtocolState {

	protected static Sending instance = null;
	
	public static Sending getInstance() {
		if (instance == null) {
			instance = new Sending();
		}
		return instance;
	}
	
	private Sending() {
	}

	@Override
	public ProtocolState nextState(ProtocolMsg msg) {
		if (msg instanceof CloseEapFileMsg) {
			return Connected.getInstance();
		} else if (msg instanceof TeardownDebuggingSessionMsg) {
			return Init.getInstance();
		}
		return super.nextState(msg);
	}
	
}
