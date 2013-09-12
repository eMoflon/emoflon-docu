package org.moflon.tracing.sdm;

import org.eclipse.emf.ecore.EOperation;
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
	protected void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName,
			EOperation op, Object... paramValues) {
		c.traceEvent(stw, new MatchFoundEvent(storyPatternName, op, paramValues));
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName,
			EOperation op, Object... paramValues) {
		c.traceEvent(stw, new NoMatchFoundEvent(storyPatternName, op, paramValues));
	}

	@Override
	protected void logCheckIsomorphicBindingEvent(SDMTraceContext c,
			StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type,
			Object objVar2Value) {
		c.traceEvent(stw, new CheckIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value));
	}

	@Override
	protected void logSuccessIsomorphicBindingEvent(SDMTraceContext c,
			StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type,
			Object objVar2Value) {
		c.traceEvent(stw, new SuccessIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value));		
	}

	@Override
	protected void logFailedIsomorphicBinding(SDMTraceContext c,
			StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type,
			Object objVar2Value) {
		c.traceEvent(stw, new FailedIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value));
	}

	@Override
	protected void logNoMoreLinkEndOptions(SDMTraceContext c,
			StackTraceWrapper stw, String linkName, String srcObjName,
			String trgtObjName) {
		c.traceEvent(stw, new NoMoreLinkEndOptionsEvent(linkName, srcObjName, trgtObjName));
	}

	@Override
	protected void logObjectCreation(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object newObjectValue) {
		c.traceEvent(stw, new ObjectCreationEvent(objVarName, objVarType, newObjectValue));
	}

	@Override
	protected void logObjectDeletion(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldObjectValue) {
		c.traceEvent(stw, new ObjectDeletionEvent(objVarName, objVarType, oldObjectValue));
	}

	@Override
	protected void logLinkCreation(SDMTraceContext c, StackTraceWrapper stw,
			String sourceNodeName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceRoleName,
			String targetNodeName, Class<?> targetNodeType,
			Object targetNodeValue, String targetRoleName) {
		c.traceEvent(stw, new LinkCreationEvent(sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName));
	}

	@Override
	protected void logLinkDeletion(SDMTraceContext c, StackTraceWrapper stw,
			String sourceNodeName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceRoleName,
			String targetNodeName, Class<?> targetNodeType,
			Object targetNodeValue, String targetRoleName) {
		c.traceEvent(stw, new LinkDeletionEvent(sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName));
	}

	@Override
	protected void logLightweightPatternEnter(SDMTraceContext c,
			StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId) {
		c.traceEvent(stw, new LightweightPatternEnterEvent(storyPatternName, op, uniqueId));
	}

	@Override
	protected void logLightweightPatternExit(SDMTraceContext c,
			StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId) {
		c.traceEvent(stw, new LightweightPatternExitEvent(storyPatternName, op, uniqueId));
	}

	@Override
	protected void logCommenceOfGraphRewriting(SDMTraceContext c,
			StackTraceWrapper stw, String patternName) {
		c.traceEvent(stw, new CommenceOfGraphRewritingEvent(patternName));
	}

	@Override
	protected void logBeginNACEvaluation(SDMTraceContext c,
			StackTraceWrapper stw, String patternName) {
		c.traceEvent(stw, new BeginNACEvaluationEvent(patternName));
	}

	@Override
	protected void logEndOfNACEvaluation(SDMTraceContext c,
			StackTraceWrapper stw, String patternName) {
		c.traceEvent(stw, new EndOfNACEvaluationEvent(patternName));
	}

	@Override
	protected void logNACNotSatisfied(SDMTraceContext c, StackTraceWrapper stw,
			String patternName) {
		c.traceEvent(stw, new FailedNACEvent(patternName));
	}

	@Override
	protected void logNACSatisfied(SDMTraceContext c, StackTraceWrapper stw,
			String patternName) {
		c.traceEvent(stw, new SuccessNACEvent(patternName));
	}

	@Override
	protected void initializeStrategy()
	{
		// TODO Auto-generated method stub
		
	}
	
}
