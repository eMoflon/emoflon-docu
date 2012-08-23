package org.moflon.tracing.sdm;

import org.eclipse.emf.ecore.EOperation;

public abstract class SDMTraceStrategy {

	protected abstract void logOperationEnter(SDMTraceContext c, StackTraceElement ste, EOperation op, Object[] parameterValues);
	protected abstract void logOperationExit(SDMTraceContext c, StackTraceElement ste, EOperation op, Object result);
	protected abstract void logPatternEnter(SDMTraceContext c, StackTraceElement ste, String storyPatternName, EOperation op);
	protected abstract void logPatternExit(SDMTraceContext c, StackTraceElement ste, String storyPatternName, EOperation op);
	protected abstract void logBindObjVar(SDMTraceContext c, StackTraceElement ste, String objVarName, Class<?> objVarType, Object oldValue, Object newValue);
	protected abstract void logUnbindObjVar(SDMTraceContext c, StackTraceElement ste, String objVarName, Class<?> objVarType, Object oldValue, Object newValue);
	protected abstract void logMatchFound(SDMTraceContext c, StackTraceElement ste, EOperation op, Object... paramValues);
	protected abstract void logNoMatchFound(SDMTraceContext c, StackTraceElement ste, EOperation op, Object... paramValues);
}
