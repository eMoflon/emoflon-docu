package org.moflon.tracing.sdm;

import org.eclipse.emf.ecore.EOperation;

public class LoggingSDMTraceStrategy extends SDMTraceStrategy {

	@Override
	protected void logOperationEnter(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object[] parameterValues) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object result) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceElement ste,
			String storyPatternName, EOperation op) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceElement ste,
			String storyPatternName, EOperation op) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceElement ste,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceElement ste,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void logMatchFound(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object... paramValues) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceElement ste,
			EOperation op, Object... paramValues) {
		// TODO Auto-generated method stub
		
	}

}
