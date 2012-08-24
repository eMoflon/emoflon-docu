package org.moflon.tracing.sdm;

import org.eclipse.emf.ecore.EOperation;

public abstract class SDMTraceStrategy {

	protected abstract void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object[] parameterValues);
	protected abstract void logOperationExit(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object result);
	protected abstract void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op);
	protected abstract void logPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op);
	protected abstract void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue);
	protected abstract void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue);
	protected abstract void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object... paramValues);
	protected abstract void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object... paramValues);
}
