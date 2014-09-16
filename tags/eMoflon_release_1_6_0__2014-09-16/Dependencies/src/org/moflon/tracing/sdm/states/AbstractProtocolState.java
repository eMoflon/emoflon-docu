package org.moflon.tracing.sdm.states;

import org.moflon.tracing.sdm.msgs.ProtocolMsg;


public abstract class AbstractProtocolState implements ProtocolState {

	public ProtocolState nextState(ProtocolMsg msg) {
		return this;
	}
}
