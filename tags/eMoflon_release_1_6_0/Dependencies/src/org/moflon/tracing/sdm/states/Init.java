package org.moflon.tracing.sdm.states;

import org.moflon.tracing.sdm.msgs.ProtocolMsg;
import org.moflon.tracing.sdm.msgs.SetupDebuggingSessionMsg;


public class Init extends AbstractProtocolState
{

	protected static Init instance = null;

	public static Init getInstance()
	{
		if (instance == null)
		{
			instance = new Init();
		}
		return instance;
	}

	private Init()
	{
	}

	@Override
	public ProtocolState nextState(ProtocolMsg msg)
	{
		if (msg instanceof SetupDebuggingSessionMsg)
		{
			return Connected.getInstance();
		}
		return super.nextState(msg);
	}

}
