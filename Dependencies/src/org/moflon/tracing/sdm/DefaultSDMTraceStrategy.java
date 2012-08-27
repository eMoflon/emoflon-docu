package org.moflon.tracing.sdm;

import org.eclipse.emf.ecore.EOperation;
import org.moflon.tracing.sdm.events.BindObjectVarEvent;
import org.moflon.tracing.sdm.events.MatchFoundEvent;
import org.moflon.tracing.sdm.events.NoMatchFoundEvent;
import org.moflon.tracing.sdm.events.OperationEnterEvent;
import org.moflon.tracing.sdm.events.OperationExitEvent;
import org.moflon.tracing.sdm.events.PatternEnterEvent;
import org.moflon.tracing.sdm.events.PatternExitEvent;
import org.moflon.tracing.sdm.events.UnbindObjectVarEvent;

public class DefaultSDMTraceStrategy extends SDMTraceStrategy {

	@Override
	protected void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object[] parameterValues) {
		c.traceEvent(stw, new OperationEnterEvent(op, parameterValues));
	}

	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object result) {
		c.traceEvent(stw, new OperationExitEvent(op, result));
	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw,
			String storyPatternName, EOperation op) {
		c.traceEvent(stw, new PatternEnterEvent(storyPatternName, op));
	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceWrapper stw,
			String storyPatternName, EOperation op) {
		c.traceEvent(stw, new PatternExitEvent(storyPatternName, op));
	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		c.traceEvent(stw, new BindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
	}

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		c.traceEvent(stw, new UnbindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
	}

	@Override
	protected void logMatchFound(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object... paramValues) {
		c.traceEvent(stw, new MatchFoundEvent(op, paramValues));
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object... paramValues) {
		c.traceEvent(stw, new NoMatchFoundEvent(op, paramValues));
	}
	
}