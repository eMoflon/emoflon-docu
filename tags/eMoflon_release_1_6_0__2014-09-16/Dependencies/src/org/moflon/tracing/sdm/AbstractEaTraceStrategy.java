package org.moflon.tracing.sdm;

import static org.moflon.util.EAInterfaceUriHelper.getActivityString;
import static org.moflon.util.EAInterfaceUriHelper.getEClassString;
import static org.moflon.util.EAInterfaceUriHelper.getEOperationString;
import static org.moflon.util.EAInterfaceUriHelper.getEPackageString;
import static org.moflon.util.EAInterfaceUriHelper.getEParameterString;
import static org.moflon.util.EAInterfaceUriHelper.getLinkVarString;
import static org.moflon.util.EAInterfaceUriHelper.getObjVarString;
import static org.moflon.util.EAInterfaceUriHelper.getStoryNodeString;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;

public abstract class AbstractEaTraceStrategy extends SDMTraceStrategy
{

	protected static final char DELIM = '/';
	protected static final String LINE_SEPARATOR = System.lineSeparator();

	protected Stack<EOperation> opStack = new Stack<EOperation>();
	protected Stack<String> storyPatternStack = new Stack<String>();

	
	protected Writer out;
	protected String storyPattern;

	protected void init(Writer out)
	{
		this.out = out;
	}

	@Override
	protected void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object[] parameterValues)
	{
		logOperation(op);
		opStack.push(op);
	}

	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object result)
	{
		logOperation(op);
		opStack.pop();
	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op)
	{
		logStoryPattern(storyPatternName, op);
		this.storyPattern = storyPatternName;
		storyPatternStack.push(storyPatternName);
	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op)
	{
		//logStoryPattern(storyPatternName, op);

		if (this.storyPattern != null && this.storyPattern.equals(storyPatternName))
			this.storyPattern = null;
		if(!storyPatternStack.isEmpty() && storyPatternStack.peek().equals(storyPatternName))
			storyPatternStack.pop();
	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue)
	{
		logObjectVariable(objVarName, storyPatternStack.peek(), opStack.peek());
	}

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue)
	{
		//logObjectVariable(objVarName, storyPatternStack.peek(), opStack.peek());
		return;
	}

	@Override
	protected void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues)
	{
		// implement maybe...
		return;
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues)
	{
		// implement maybe...
		return;
	}

	

	@Override
	protected void logCheckIsomorphicBindingEvent(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value)
	{
		return;
	}

	@Override
	protected void logSuccessIsomorphicBindingEvent(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value)
	{
		return;
	}

	@Override
	protected void logFailedIsomorphicBinding(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type,
			Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value)
	{
		return;
	}

	@Override
	protected void logNoMoreLinkEndOptions(SDMTraceContext c, StackTraceWrapper stw, String linkName, String srcObjName, String trgtObjName)
	{
		// implement maybe...
		return;
	}

	@Override
	protected void logObjectCreation(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object newObjectValue)
	{
		logObjectVariable(objVarName, storyPatternStack.peek(), opStack.peek());
	}

	@Override
	protected void logObjectDeletion(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldObjectValue)
	{
		logObjectVariable(objVarName, storyPatternStack.peek(), opStack.peek());
	}

	@Override
	protected void logLinkCreation(SDMTraceContext c, StackTraceWrapper stw, String sourceNodeName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue,
			String targetRoleName)
	{
		logLinkVariable(sourceNodeName, targetNodeName);
	}

	

	@Override
	protected void logLinkDeletion(SDMTraceContext c, StackTraceWrapper stw, String sourceRoleName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceNodeName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue,
			String targetRoleName)
	{
		logLinkVariable(sourceNodeName, targetNodeName);
	}

	@Override
	protected void logLightweightPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId)
	{
	}

	@Override
	protected void logLightweightPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId)
	{
	}

	@Override
	protected void logCommenceOfGraphRewriting(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
	}

	@Override
	protected void logBeginNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
	}

	@Override
	protected void logEndOfNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
	}

	@Override
	protected void logNACNotSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
	}

	@Override
	protected void logNACSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
	}

	private static String getPackageString(EPackage p)
	{
		if (p == null)
			return null;
		StringBuilder sb = new StringBuilder();
		String packageString = getPackageString(p.getESuperPackage());
		if (packageString != null)
		{
			sb.append(packageString);
			sb.append(DELIM);
		}
		sb.append(getEPackageString(p.getName()));
		return sb.toString();
	}

	private static String getClassString(EOperation op)
	{
		EClass eContainingClass = op.getEContainingClass();
		StringBuilder sb = new StringBuilder();
		sb.append(getEClassString(eContainingClass.getName()));
		return sb.toString();
	}

	private String getFullClassString(EOperation op)
	{
		return getPackageString(op) + DELIM + getClassString(op);
	}

	private static String getPackageString(EOperation op)
	{
		EClass eContainingClass = op.getEContainingClass();
		return getPackageString(eContainingClass.getEPackage());
	}
	
	private String getFullEOperationString(EOperation op)
	{
		return getFullClassString(op) + DELIM + getEOperationString(op.getName()) + getEParameterString(op.getEParameters()) + ":EOperation";
	}

	private String getFullActivityString(EOperation op)
	{
		return getFullEOperationString(op) + DELIM + getActivityString();
	}

	private String getFullStoryPatternString(EOperation op, String storyPatternName)
	{
		return getFullActivityString(op) + DELIM + getStoryNodeString(storyPatternName);
	}

	private String getFullObjectVariableString(EOperation op, String storyPatternName, String objectVariableName)
	{
		return getFullStoryPatternString(op, storyPatternName) + DELIM + getObjVarString(objectVariableName);
	}
	
	private String getFullLinkVariableString(EOperation op, String storyPatternName, String lvSrc, String lvTrg)
	{
		return getFullStoryPatternString(op, storyPatternName) + DELIM + getLinkVarString(lvTrg, lvSrc);
	}
	
	private void logLinkVariable(String sourceNodeName, String targetNodeName)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getFullLinkVariableString(opStack.peek(), storyPatternStack.peek(), sourceNodeName, targetNodeName));
		sb.append(LINE_SEPARATOR);

		try
		{
			out.write(sb.toString());
			out.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	
	
	private void logOperation(EOperation op)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getFullActivityString(op));		
		sb.append(LINE_SEPARATOR);

		try
		{
			out.write(sb.toString());
			out.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void logObjectVariable(String objVarName, String storyPatternName, EOperation op)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getFullObjectVariableString(op, storyPatternName, objVarName));
		sb.append(LINE_SEPARATOR);

		try
		{
			out.write(sb.toString());
			out.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void logStoryPattern(String storyPatternName, EOperation op)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getFullStoryPatternString(op, storyPatternName));
		sb.append(LINE_SEPARATOR);

		try
		{
			out.write(sb.toString());
			out.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
