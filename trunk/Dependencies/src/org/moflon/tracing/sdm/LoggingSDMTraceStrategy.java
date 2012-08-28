package org.moflon.tracing.sdm;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EOperation;
import org.moflon.tracing.sdm.events.BindObjectVarEvent;
import org.moflon.tracing.sdm.events.CheckIsomorphicBindingEvent;
import org.moflon.tracing.sdm.events.FailedIsomorphicBindingEvent;
import org.moflon.tracing.sdm.events.MatchFoundEvent;
import org.moflon.tracing.sdm.events.NoMatchFoundEvent;
import org.moflon.tracing.sdm.events.OperationEnterEvent;
import org.moflon.tracing.sdm.events.OperationExitEvent;
import org.moflon.tracing.sdm.events.PatternEnterEvent;
import org.moflon.tracing.sdm.events.PatternExitEvent;
import org.moflon.tracing.sdm.events.SuccessIsomorphicBindingEvent;
import org.moflon.tracing.sdm.events.UnbindObjectVarEvent;

public class LoggingSDMTraceStrategy extends SDMTraceStrategy {

	Logger log = Logger.getLogger(LoggingSDMTraceStrategy.class);
	
	@Override
	protected void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object[] parameterValues) {
		log.debug((new OperationEnterEvent(op, parameterValues)).toString());
	}

	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object result) {
		log.debug((new OperationExitEvent(op, result)).toString());
	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw,
			String storyPatternName, EOperation op) {
		log.debug((new PatternEnterEvent(storyPatternName, op)).toString());
	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceWrapper stw,
			String storyPatternName, EOperation op) {
		log.debug((new PatternExitEvent(storyPatternName, op)).toString());
	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		log.debug((new BindObjectVarEvent(objVarName, objVarType, oldValue, newValue)).toString());
	}

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		log.debug((new UnbindObjectVarEvent(objVarName, objVarType, oldValue, newValue)).toString());		
	}

	@Override
	protected void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName,
			EOperation op, Object... paramValues) {
		log.debug((new MatchFoundEvent(storyPatternName, op, paramValues)).toString());
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName,
			EOperation op, Object... paramValues) {
		log.debug((new NoMatchFoundEvent(storyPatternName, op, paramValues)).toString());
	}

	@Override
	protected void logCheckIsomorphicBindingEvent(SDMTraceContext c,
			StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type,
			Object objVar2Value) {
		log.debug((new CheckIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value)).toString());
	}

	@Override
	protected void logSuccessIsomorphicBindingEvent(SDMTraceContext c,
			StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type,
			Object objVar2Value) {
		log.debug((new SuccessIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value)).toString());
	}

	@Override
	protected void logFailedIsomorphicBinding(SDMTraceContext c,
			StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type,
			Object objVar2Value) {
		log.debug((new FailedIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value)).toString());
	}



}
