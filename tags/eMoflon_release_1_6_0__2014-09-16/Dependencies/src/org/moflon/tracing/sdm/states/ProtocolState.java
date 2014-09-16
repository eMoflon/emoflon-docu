package org.moflon.tracing.sdm.states;

import org.moflon.tracing.sdm.msgs.ProtocolMsg;


public interface ProtocolState {
	
	public ProtocolState nextState(ProtocolMsg msg);

}
