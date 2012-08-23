package org.moflon.tracing.sdm;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.moflon.tracing.sdm.events.BindObjectVarEvent;
import org.moflon.tracing.sdm.events.MatchFoundEvent;
import org.moflon.tracing.sdm.events.NoMatchFoundEvent;
import org.moflon.tracing.sdm.events.OperationEnterEvent;
import org.moflon.tracing.sdm.events.OperationExitEvent;
import org.moflon.tracing.sdm.events.PatternEnterEvent;
import org.moflon.tracing.sdm.events.PatternExitEvent;
import org.moflon.tracing.sdm.events.UnbindObjectVarEvent;

public class SDMTraceUtil {	
	
	private final static SDMTraceStrategy STRATEGY = new DefaultSDMTraceStrategy();   
			
	private final static String ALL = "global";
	private final static HashMap<String, SDMTraceContext> CONTEXTS = new HashMap<String, SDMTraceContext>(4, 0.75f);
	private final static SDMTraceContext GLOBAL_CONTEXT = new SDMTraceContext();
	
	{
		CONTEXTS.put(ALL, GLOBAL_CONTEXT);
	}
	
	public static SDMTraceContext getTraceContext(String uniqueIdentifier) {
		if (uniqueIdentifier == null)
			throw new IllegalArgumentException("Argument may not be null");
		SDMTraceContext result = CONTEXTS.get(uniqueIdentifier);
		if (result == null) {
			result = new SDMTraceContext();
			CONTEXTS.put(uniqueIdentifier, result);
		}
		return result;
	}
	
	public static void logOperationEnter(SDMTraceContext c, StackTraceElement ste, EObject eThis, Method method, Object[] parameterValues) {
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logOperationEnter(c, ste, selectedOp, parameterValues);
	}

	private static EOperation findEOperation(EObject eThis, Method method) {
		EOperation selectedOp = null;
		String opName = method.getName();
		
		EList<EOperation> eAllOperations = eThis.eClass().getEAllOperations();
		assert(eAllOperations != null && eAllOperations.size() > 0);
		
		List<EOperation> operationCandiates = new LinkedList<EOperation>();
		for (EOperation tmp : eAllOperations) {
			if (tmp.getName().equals(opName)) {
				operationCandiates.add(tmp);
			}
		}
		
		if (operationCandiates.size() == 0) {
			throw new IllegalStateException("No operation with the given name could be found");
		} else if (operationCandiates.size() == 1) {
			selectedOp = operationCandiates.get(0);
		} else {
			selectedOp = filterOperationCandidates(operationCandiates, method);
			if (selectedOp == null)
				throw new IllegalStateException("Could not determine correct EOperation from given input");
		}
		return selectedOp;
	}
	
	private static EOperation filterOperationCandidates(List<EOperation> opCands, Method m) {
		Class<?>[] parameterTypes = m.getParameterTypes();
		
		for (EOperation nOp : opCands) {
			EList<EParameter> eParameters = nOp.getEParameters();
			Iterator<Class<?>> mParamsIterator = Arrays.asList(parameterTypes).iterator();
			boolean flag = (parameterTypes.length == eParameters.size());
			if (flag) {
				for (EParameter nPa : eParameters) {				
					Class<?> methodPa = null;
					try {
						methodPa = mParamsIterator.next();
					} catch (NoSuchElementException e) {
						continue;
					}
					if (nPa.getEType() instanceof EDataType) {
						if (nPa.getEType() instanceof EEnum) {
							// TODO add support for special case
							throw new UnsupportedOperationException("This case still needs to be implemented");
						}
						EDataType dataType = (EDataType) nPa.getEType();
						String javaTypeAsString = dataType.getInstanceClassName();
						if (!methodPa.getCanonicalName().equals(javaTypeAsString)) {
							flag = false;
							continue;
						}
					} else if (nPa.getEType() instanceof EClass) {
						EClass clazz = (EClass) nPa.getEType();
						Class<? extends EClass> methodPaFromEOperation = clazz.getClass();
						if (!isCompatible(methodPaFromEOperation, methodPa)) {
							flag = false;
							continue;
						}
					} else {
						throw new IllegalStateException("Encountered unknown parameter type in EOperation \"" + nOp.getName() + "\"");
					}
				}
				if (flag)
					return nOp; 
			}
		}
		return null;
	}
	
	private static boolean isCompatible(Class<? extends EClass> c1, Class<?> c2) {
		return c1.getCanonicalName().equals(c2.getCanonicalName());		
	}
	
	protected static void logOperationEnter(SDMTraceContext c, StackTraceElement ste, EOperation op, Object[] parameterValues) {
		if (c == null || ste == null || op == null || parameterValues == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logOperationEnter(GLOBAL_CONTEXT, ste, op, parameterValues);
			STRATEGY.logOperationEnter(c, ste, op, parameterValues);
		} else {
			c.traceEvent(ste, new OperationEnterEvent(op, parameterValues));
		}
	}	
	
	public static void logOperationExit(SDMTraceContext c, StackTraceElement ste, EObject eThis, Method method, Object result) {
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logOperationExit(c, ste, selectedOp, result);
	}
	
	protected static void logOperationExit(SDMTraceContext c, StackTraceElement ste, EOperation op, Object result) {
		if (c == null || ste == null || op == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logOperationExit(GLOBAL_CONTEXT, ste, op, result);
			STRATEGY.logOperationExit(c, ste, op, result);
		} else {
			c.traceEvent(ste, new OperationExitEvent(op, result));
		}
	}
	
	public static void logPatternEnter(SDMTraceContext c, StackTraceElement ste, EObject eThis, Method method, String patternName) {
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logPatternEnter(c, ste, patternName, selectedOp);
	}
	
	protected static void logPatternEnter(SDMTraceContext c, StackTraceElement ste, String storyPatternName, EOperation op) {
		if (c == null || ste == null || storyPatternName == null || storyPatternName.length() == 0 || op == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logPatternEnter(GLOBAL_CONTEXT, ste, storyPatternName, op);
			STRATEGY.logPatternEnter(c, ste, storyPatternName, op);
		} else {
			c.traceEvent(ste, new PatternEnterEvent(storyPatternName, op));
		}
	}
	
	public static void logPatternExit(SDMTraceContext c, StackTraceElement ste, EObject eThis, Method method, String patternName) {
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logPatternExit(c, ste, patternName, selectedOp);
	}
	
	protected static void logPatternExit(SDMTraceContext c, StackTraceElement ste, String storyPatternName, EOperation op) {
		if (c == null || ste == null || storyPatternName == null || storyPatternName.length() == 0 || op == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logPatternExit(GLOBAL_CONTEXT, ste, storyPatternName, op);
			STRATEGY.logPatternExit(c, ste, storyPatternName, op);
		} else {
			c.traceEvent(ste, new PatternExitEvent(storyPatternName, op));
		}
	}
	
	public static void logBindObjVar(SDMTraceContext c, StackTraceElement ste, String objVarName, Class<?> objVarType, Object oldValue, Object newValue) {
		if (c == null || ste == null || objVarName == null || objVarName.length() == 0 || objVarType == null || newValue == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logBindObjVar(GLOBAL_CONTEXT, ste, objVarName, objVarType, oldValue, newValue);
			STRATEGY.logBindObjVar(c, ste, objVarName, objVarType, oldValue, newValue);
		} else {
			c.traceEvent(ste, new BindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
		}
	}
	
	public static void logUnbindObjVar(SDMTraceContext c, StackTraceElement ste, String objVarName, Class<?> objVarType, Object oldValue, Object newValue) {
		if (c == null || ste == null || objVarName == null || objVarName.length() == 0 || objVarType == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logUnbindObjVar(GLOBAL_CONTEXT, ste, objVarName, objVarType, oldValue, newValue);
			STRATEGY.logUnbindObjVar(c, ste, objVarName, objVarType, oldValue, newValue);
		} else {
			c.traceEvent(ste, new UnbindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
		}
	}
	
	public static void logCheckIsomorphicBinding(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logCheckIsomorphicBinding(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logSuccessIsomorphicBinding(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logSuccessIsomorphicBinding(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logFailedIsomorphicBinding(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logFailedIsomorphicBinding(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logCheckLinkExistence(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logCheckLinkExistence(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logSuccessLinkExistence(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logSuccessLinkExistence(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logFailedLinkExistence(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logFailedLinkExistence(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logCheckAttributeConstraint(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logCheckAttributeConstraint(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logSuccessAttributeConstraint(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logSuccessAttributeConstraint(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logFailedAttributeConstraint(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logFailedAttributeConstraint(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logMethodCallExpression(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logMethodCallExpression(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logAttributeAssignment(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logAttributeAssignment(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logMatchFound(SDMTraceContext c, StackTraceElement ste, EObject eThis, Method method, Object...paramValues) {
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logMatchFound(c, ste, selectedOp, paramValues);
	}
	
	protected static void logMatchFound(SDMTraceContext c, StackTraceElement ste, EOperation op, Object... paramValues) {
		if (!GLOBAL_CONTEXT.equals(c)) {
			logMatchFound(GLOBAL_CONTEXT, ste, op, paramValues);
			STRATEGY.logMatchFound(c, ste, op, paramValues);
		} else {
			c.traceEvent(ste, new MatchFoundEvent(op, paramValues));
		}
	}
	
	public static void logNoMatchFound(SDMTraceContext c, StackTraceElement ste, EObject eThis, Method method, Object...paramValues) {
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logNoMatchFound(c, ste, selectedOp, paramValues);
	}
	
	protected static void logNoMatchFound(SDMTraceContext c, StackTraceElement ste, EOperation op, Object... paramValues) {
		if (!GLOBAL_CONTEXT.equals(c)) {
			logNoMatchFound(GLOBAL_CONTEXT, ste, op, paramValues);
			STRATEGY.logNoMatchFound(c, ste, op, paramValues);
		} else {
			c.traceEvent(ste, new NoMatchFoundEvent(op, paramValues));
		}
	}
	
	public static void logObjCreation(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logObjCreation(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logObjDeletion(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logObjDeletion(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logLinkCreation(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logLinkCreation(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logLinkDeletion(SDMTraceContext c) {
		if (!GLOBAL_CONTEXT.equals(c))
			logLinkDeletion(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
		Method result = null;
		try {
			result = clazz.getMethod(methodName, params);
		} catch (NoSuchMethodException e) {
			// do nothing
		}
		return result;
	}
}
