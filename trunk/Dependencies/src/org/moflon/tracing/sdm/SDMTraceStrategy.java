package org.moflon.tracing.sdm;

import org.eclipse.emf.ecore.EOperation;

public abstract class SDMTraceStrategy {

	protected abstract void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object[] parameterValues);
	protected abstract void logOperationExit(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object result);
	protected abstract void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op);
	protected abstract void logPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op);
	protected abstract void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue);
	protected abstract void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue);
	protected abstract void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues);
	protected abstract void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues);
	protected abstract void logCheckIsomorphicBindingEvent(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value);
	protected abstract void logSuccessIsomorphicBindingEvent(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value);
	protected abstract void logFailedIsomorphicBinding(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value);
}
