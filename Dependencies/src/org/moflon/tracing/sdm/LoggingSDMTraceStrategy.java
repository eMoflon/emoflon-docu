package org.moflon.tracing.sdm;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EOperation;
import org.mockito.internal.verification.NoMoreInteractions;
import org.moflon.tracing.sdm.events.BeginNACEvaluationEvent;
import org.moflon.tracing.sdm.events.BindObjectVarEvent;
import org.moflon.tracing.sdm.events.CheckIsomorphicBindingEvent;
import org.moflon.tracing.sdm.events.CommenceOfGraphRewritingEvent;
import org.moflon.tracing.sdm.events.EndOfNACEvaluationEvent;
import org.moflon.tracing.sdm.events.FailedIsomorphicBindingEvent;
import org.moflon.tracing.sdm.events.FailedNACEvent;
import org.moflon.tracing.sdm.events.LightweightPatternEnterEvent;
import org.moflon.tracing.sdm.events.LightweightPatternExitEvent;
import org.moflon.tracing.sdm.events.LinkCreationEvent;
import org.moflon.tracing.sdm.events.LinkDeletionEvent;
import org.moflon.tracing.sdm.events.MatchFoundEvent;
import org.moflon.tracing.sdm.events.NoMatchFoundEvent;
import org.moflon.tracing.sdm.events.NoMoreLinkEndOptionsEvent;
import org.moflon.tracing.sdm.events.ObjectCreationEvent;
import org.moflon.tracing.sdm.events.ObjectDeletionEvent;
import org.moflon.tracing.sdm.events.OperationEnterEvent;
import org.moflon.tracing.sdm.events.OperationExitEvent;
import org.moflon.tracing.sdm.events.PatternEnterEvent;
import org.moflon.tracing.sdm.events.PatternExitEvent;
import org.moflon.tracing.sdm.events.SuccessIsomorphicBindingEvent;
import org.moflon.tracing.sdm.events.SuccessNACEvent;
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

	@Override
	protected void logNoMoreLinkEndOptions(SDMTraceContext c,
			StackTraceWrapper stw, String linkName, String srcObjName,
			String trgtObjName) {
		log.debug((new NoMoreLinkEndOptionsEvent(linkName, srcObjName, trgtObjName)).toString());
	}

	@Override
	protected void logObjectCreation(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object newObjectValue) {
		log.debug((new ObjectCreationEvent(objVarName, objVarType, newObjectValue)).toString());
	}

	@Override
	protected void logObjectDeletion(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldObjectValue) {
		log.debug((new ObjectDeletionEvent(objVarName, objVarType, oldObjectValue)).toString());
	}

	
	@Override
	protected void logLinkCreation(SDMTraceContext c, StackTraceWrapper stw,
			String sourceNodeName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceRoleName,
			String targetNodeName, Class<?> targetNodeType,
			Object targetNodeValue, String targetRoleName) {
		log.debug((new LinkCreationEvent(sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName)).toString());
	}

	@Override
	protected void logLinkDeletion(SDMTraceContext c, StackTraceWrapper stw,
			String sourceNodeName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceRoleName,
			String targetNodeName, Class<?> targetNodeType,
			Object targetNodeValue, String targetRoleName) {
		log.debug((new LinkDeletionEvent(sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName)).toString());
	}

	@Override
	protected void logLightweightPatternEnter(SDMTraceContext c,
			StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId) {
		log.debug((new LightweightPatternEnterEvent(storyPatternName, op, uniqueId)).toString());
	}

	@Override
	protected void logLightweightPatternExit(SDMTraceContext c,
			StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId) {
		log.debug((new LightweightPatternExitEvent(storyPatternName, op, uniqueId)).toString());
	}

	@Override
	protected void logCommenceOfGraphRewriting(SDMTraceContext c,
			StackTraceWrapper stw, String patternName) {
		log.debug((new CommenceOfGraphRewritingEvent(patternName)).toString());
	}

	@Override
	protected void logBeginNACEvaluation(SDMTraceContext c,
			StackTraceWrapper stw, String patternName) {
		log.debug((new BeginNACEvaluationEvent(patternName)).toString());
	}

	@Override
	protected void logEndOfNACEvaluation(SDMTraceContext c,
			StackTraceWrapper stw, String patternName) {
		log.debug((new EndOfNACEvaluationEvent(patternName)).toString());
	}

	@Override
	protected void logNACNotSatisfied(SDMTraceContext c, StackTraceWrapper stw,
			String patternName) {
		log.debug((new FailedNACEvent(patternName)).toString());
	}

	@Override
	protected void logNACSatisfied(SDMTraceContext c, StackTraceWrapper stw,
			String patternName) {
		log.debug((new SuccessNACEvent(patternName)).toString());
	}
	
}
