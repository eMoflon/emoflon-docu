package org.moflon.tracing.sdm;

import static org.moflon.util.EAInterfaceUriHelper.getActivityString;
import static org.moflon.util.EAInterfaceUriHelper.getEClassString;
import static org.moflon.util.EAInterfaceUriHelper.getEOperationString;
import static org.moflon.util.EAInterfaceUriHelper.getEPackageString;
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

	private void logOperation(EOperation op)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageString(op));
		sb.append(DELIM);
		sb.append(getClassString(op));
		sb.append(DELIM);
		sb.append(getEOperationString(op.getName()));
		sb.append(DELIM);
		sb.append(getActivityString());
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

	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object result)
	{
		logOperation(op);
		opStack.pop();
	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageString(op));
		sb.append(DELIM);
		sb.append(getClassString(op));
		sb.append(DELIM);
		sb.append(getEOperationString(op.getName()));
		sb.append(DELIM);
		sb.append(getActivityString());
		sb.append(DELIM);
		sb.append(getStoryNodeString(storyPatternName));
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

		this.storyPattern = storyPatternName;
	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageString(op));
		sb.append(DELIM);
		sb.append(getClassString(op));
		sb.append(DELIM);
		sb.append(getEOperationString(op.getName()));
		sb.append(DELIM);
		sb.append(getActivityString());
		sb.append(DELIM);
		sb.append(getStoryNodeString(storyPatternName));
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

		if (this.storyPattern != null && this.storyPattern.equals(storyPatternName))
			this.storyPattern = null;
	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageString(opStack.peek()));
		sb.append(DELIM);
		sb.append(getClassString(opStack.peek()));
		sb.append(DELIM);
		sb.append(getEOperationString(opStack.peek().getName()));
		sb.append(DELIM);
		sb.append(getActivityString());
		sb.append(DELIM);
		sb.append(getStoryNodeString(this.storyPattern));
		sb.append(DELIM);
		sb.append(getObjVarString(objVarName));
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

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue)
	{
		// implement maybe...
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

	private static String getPackageString(EOperation op)
	{
		EClass eContainingClass = op.getEContainingClass();
		return getPackageString(eContainingClass.getEPackage());
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

	@Override
	protected void logNoMoreLinkEndOptions(SDMTraceContext c, StackTraceWrapper stw, String linkName, String srcObjName, String trgtObjName)
	{
		// implement maybe...
		return;
	}

	@Override
	protected void logObjectCreation(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object newObjectValue)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logObjectDeletion(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldObjectValue)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logLinkCreation(SDMTraceContext c, StackTraceWrapper stw, String sourceNodeName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue,
			String targetRoleName)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logLinkDeletion(SDMTraceContext c, StackTraceWrapper stw, String sourceRoleName, Class<?> sourceNodeType,
			Object sourceNodeValue, String sourceNodeName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue,
			String targetRoleName)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logLightweightPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logLightweightPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op,
			String uniqueId)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logCommenceOfGraphRewriting(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logBeginNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logEndOfNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logNACNotSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
		// TODO Auto-generated method stub
	}

	@Override
	protected void logNACSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName)
	{
		// TODO Auto-generated method stub
	}
}
