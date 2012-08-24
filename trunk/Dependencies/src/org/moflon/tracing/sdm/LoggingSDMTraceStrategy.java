package org.moflon.tracing.sdm;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EOperation;
import org.moflon.tracing.sdm.events.BindObjectVarEvent;
import org.moflon.tracing.sdm.events.MatchFoundEvent;
import org.moflon.tracing.sdm.events.NoMatchFoundEvent;
import org.moflon.tracing.sdm.events.OperationEnterEvent;
import org.moflon.tracing.sdm.events.OperationExitEvent;
import org.moflon.tracing.sdm.events.PatternEnterEvent;
import org.moflon.tracing.sdm.events.PatternExitEvent;
import org.moflon.tracing.sdm.events.UnbindObjectVarEvent;

public class LoggingSDMTraceStrategy extends SDMTraceStrategy {

	Logger log = Logger.getLogger(LoggingSDMTraceStrategy.class);
	
	@Override
	protected void logOperationEnter(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object[] parameterValues) {
		log.debug((new OperationEnterEvent(op, parameterValues)).toString());
	}

	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object result) {
		log.debug((new OperationExitEvent(op, result)).toString());
	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceElement ste,
			String storyPatternName, EOperation op) {
		log.debug((new PatternEnterEvent(storyPatternName, op)).toString());
	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceElement ste,
			String storyPatternName, EOperation op) {
		log.debug((new PatternExitEvent(storyPatternName, op)).toString());
	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceElement ste,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		log.debug((new BindObjectVarEvent(objVarName, objVarType, oldValue, newValue)).toString());
	}

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceElement ste,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		log.debug((new UnbindObjectVarEvent(objVarName, objVarType, oldValue, newValue)).toString());		
	}

	@Override
	protected void logMatchFound(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object... paramValues) {
		log.debug((new MatchFoundEvent(op, paramValues)).toString());
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object... paramValues) {
		log.debug((new NoMatchFoundEvent(op, paramValues)).toString());
	}

}
