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
	protected void logOperationEnter(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object[] parameterValues) {
		c.traceEvent(ste, new OperationEnterEvent(op, parameterValues));
	}

	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object result) {
		c.traceEvent(ste, new OperationExitEvent(op, result));
	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceElement ste,
			String storyPatternName, EOperation op) {
		c.traceEvent(ste, new PatternEnterEvent(storyPatternName, op));
	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceElement ste,
			String storyPatternName, EOperation op) {
		c.traceEvent(ste, new PatternExitEvent(storyPatternName, op));
	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceElement ste,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		c.traceEvent(ste, new BindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
	}

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceElement ste,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		c.traceEvent(ste, new UnbindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
	}

	@Override
	protected void logMatchFound(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object... paramValues) {
		c.traceEvent(ste, new MatchFoundEvent(op, paramValues));
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object... paramValues) {
		c.traceEvent(ste, new NoMatchFoundEvent(op, paramValues));
	}
	
}
