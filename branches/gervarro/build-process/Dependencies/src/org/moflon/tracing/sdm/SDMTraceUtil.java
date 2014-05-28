package org.moflon.tracing.sdm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
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

public class SDMTraceUtil {	
	
	public final static String DISABLE_TRACING_SYS_PROP = "org.moflon.tracing.sdm.SDMTraceUtil.disableTracing";
	public final static String SELECTED_TACING_STRATEGY_SYS_PROP = "org.moflon.tracing.sdm.SDMTraceUtil.tracingStrategies";
	
	private final static List<SDMTraceStrategy> STRATS = new ArrayList<SDMTraceStrategy>(1);
	private final static SDMTraceStrategy DEFAULT_STRATEGY = EADebuggingSocketTraceStrategy.getInstance();  
			
	private final static String ALL = "global";
	private final static HashMap<String, SDMTraceContext> CONTEXTS = new HashMap<String, SDMTraceContext>(4, 0.75f);
	private final static SDMTraceContext GLOBAL_CONTEXT = new SDMTraceContext();
	
	private static boolean disableTracing = false;
	private static boolean initialized = false;	
	
	public static void reset() {
		CONTEXTS.clear();
		putGlobalContext();
	}

	private static void putGlobalContext() {
		CONTEXTS.put(ALL, GLOBAL_CONTEXT);		
	}
	
	private static void init() {
		if (initialized)
			return;
		CONTEXTS.put(ALL, GLOBAL_CONTEXT);
		if (System.getProperties().containsKey(DISABLE_TRACING_SYS_PROP)) {
			String value = System.getProperty(DISABLE_TRACING_SYS_PROP);
			boolean parsedBoolean = Boolean.parseBoolean(value);
			disableTracing = parsedBoolean;
		}
		if (System.getProperties().containsKey(SELECTED_TACING_STRATEGY_SYS_PROP)) {
			String value = System.getProperty(SELECTED_TACING_STRATEGY_SYS_PROP);
			StringTokenizer tokenizer = new StringTokenizer(value, ",");
			ClassLoader classLoader = SDMTraceUtil.class.getClassLoader();
			while (tokenizer.hasMoreTokens()) {
				String nextToken = tokenizer.nextToken().trim();
				try {
					Class<?> loadClass = classLoader.loadClass(nextToken);
					if (SDMTraceStrategy.class.isAssignableFrom(loadClass)) {
						Object instance = null;
						try {
							Method singletonGetterMethod = loadClass.getMethod("getInstance");
							instance = singletonGetterMethod.invoke((Object) null, (Object[])null);
						} catch (NoSuchMethodException e) {
							instance = loadClass.newInstance();
						} catch (SecurityException | IllegalArgumentException | InvocationTargetException e) {
							throw new RuntimeException(e);
						}
						STRATS.add((SDMTraceStrategy) instance);						
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		if (STRATS.isEmpty())
			STRATS.add(DEFAULT_STRATEGY);
		initialized = true;
	}
	
	public static SDMTraceContext getTraceContext(String uniqueIdentifier) {		
		init();
		
		for(SDMTraceStrategy strategy : STRATS)
		{
			strategy.initializeStrategy();
		}
		
		if (uniqueIdentifier == null)
			throw new IllegalArgumentException("Argument may not be null");
		SDMTraceContext result = CONTEXTS.get(uniqueIdentifier);
		if (result == null) {
			result = new SDMTraceContext();
			CONTEXTS.put(uniqueIdentifier, result);
		}
		return result;
	}
	
	public static void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw, EObject eThis, Method method, Object[] parameterValues) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logOperationEnter(c, stw, selectedOp, parameterValues);
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
						EClassifier eType = nPa.getEType();
						String instanceClassName = eType.getInstanceClassName();
						if (!methodPa.getCanonicalName().equals(instanceClassName)) {
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
	
	protected static void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object[] parameterValues) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || op == null || parameterValues == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logOperationEnter(GLOBAL_CONTEXT, stw, op, parameterValues);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logOperationEnter(c, stw, op, parameterValues);
			}
		} else {
			c.traceEvent(stw, new OperationEnterEvent(op, parameterValues));
		}
	}	
	
	public static void logOperationExit(SDMTraceContext c, StackTraceWrapper stw, EObject eThis, Method method, Object result) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logOperationExit(c, stw, selectedOp, result);
	}
	
	protected static void logOperationExit(SDMTraceContext c, StackTraceWrapper stw, EOperation op, Object result) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || op == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logOperationExit(GLOBAL_CONTEXT, stw, op, result);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logOperationExit(c, stw, op, result);
			}
		} else {
			c.traceEvent(stw, new OperationExitEvent(op, result));
		}
	}
	
	public static void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw, EObject eThis, Method method, String patternName) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logPatternEnter(c, stw, patternName, selectedOp);
	}
	
	protected static void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || storyPatternName == null || storyPatternName.length() == 0 || op == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logPatternEnter(GLOBAL_CONTEXT, stw, storyPatternName, op);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logPatternEnter(c, stw, storyPatternName, op);
			}
		} else {
			c.traceEvent(stw, new PatternEnterEvent(storyPatternName, op));
		}
	}
	
	public static void logLightweightPatternEnter(SDMTraceContext c, StackTraceWrapper stw, EObject eThis, Method method, String patternName, String uniqueId) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logLightweightPatternEnter(c, stw, patternName, selectedOp, uniqueId);
	}
	
	protected static void logLightweightPatternEnter(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, String uniqueId) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || storyPatternName == null || storyPatternName.length() == 0 || op == null || uniqueId == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logLightweightPatternEnter(GLOBAL_CONTEXT, stw, storyPatternName, op, uniqueId);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logLightweightPatternEnter(c, stw, storyPatternName, op, uniqueId);
			}
		} else {
			c.traceEvent(stw, new LightweightPatternEnterEvent(storyPatternName, op, uniqueId));
		}
	}
	
	public static void logPatternExit(SDMTraceContext c, StackTraceWrapper stw, EObject eThis, Method method, String patternName) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logPatternExit(c, stw, patternName, selectedOp);
	}
	
	protected static void logPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || storyPatternName == null || storyPatternName.length() == 0 || op == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logPatternExit(GLOBAL_CONTEXT, stw, storyPatternName, op);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logPatternExit(c, stw, storyPatternName, op);
			}
		} else {
			c.traceEvent(stw, new PatternExitEvent(storyPatternName, op));
		}
	}
	
	public static void logLightweightPatternExit(SDMTraceContext c, StackTraceWrapper stw, EObject eThis, Method method, String patternName, String uniqueId) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logLightweightPatternExit(c, stw, patternName, selectedOp, uniqueId);
	}
	
	protected static void logLightweightPatternExit(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, String uniqueId) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || storyPatternName == null || storyPatternName.length() == 0 || op == null || uniqueId == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logLightweightPatternExit(GLOBAL_CONTEXT, stw, storyPatternName, op, uniqueId);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logLightweightPatternExit(c, stw, storyPatternName, op, uniqueId);
			}
		} else {
			c.traceEvent(stw, new LightweightPatternExitEvent(storyPatternName, op, uniqueId));
		}
	}
	
	public static void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || objVarName == null || objVarName.length() == 0 || objVarType == null || newValue == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logBindObjVar(GLOBAL_CONTEXT, stw, objVarName, objVarType, oldValue, newValue);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logBindObjVar(c, stw, objVarName, objVarType, oldValue, newValue);
			}
		} else {
			c.traceEvent(stw, new BindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
		}
	}
	
	public static void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldValue, Object newValue) {
		init();
		if (disableTracing)
			return;
		
		if (c == null || stw == null || objVarName == null || objVarName.length() == 0 || objVarType == null)
			throw new IllegalArgumentException();
		if (!GLOBAL_CONTEXT.equals(c)) {
			logUnbindObjVar(GLOBAL_CONTEXT, stw, objVarName, objVarType, oldValue, newValue);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logUnbindObjVar(c, stw, objVarName, objVarType, oldValue, newValue);
			}
		} else {
			c.traceEvent(stw, new UnbindObjectVarEvent(objVarName, objVarType, oldValue, newValue));
		}
	}
	
	public static void logCheckIsomorphicBinding(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logCheckIsomorphicBinding(GLOBAL_CONTEXT, stw, objVar1Name, objVar1Type, objVar2Value, objVar2Name, objVar2Type, objVar2Value);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logCheckIsomorphicBindingEvent(c, stw, objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value);
			}
		} else {
			c.traceEvent(stw, new CheckIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value));
		}
	}
	
	public static void logSuccessIsomorphicBinding(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logSuccessIsomorphicBinding(GLOBAL_CONTEXT, stw, objVar1Name, objVar1Type, objVar2Value, objVar2Name, objVar2Type, objVar2Value);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logSuccessIsomorphicBindingEvent(c, stw, objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value);
			}
		} else {
			c.traceEvent(stw, new SuccessIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value));
		}
	}
	
	public static void logFailedIsomorphicBinding(SDMTraceContext c, StackTraceWrapper stw, String objVar1Name, Class<?> objVar1Type, Object objVar1Value, String objVar2Name, Class<?> objVar2Type, Object objVar2Value) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logFailedIsomorphicBinding(GLOBAL_CONTEXT, stw, objVar1Name, objVar1Type, objVar2Value, objVar2Name, objVar2Type, objVar2Value);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logFailedIsomorphicBinding(c, stw, objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value);
			}
		} else {
			c.traceEvent(stw, new FailedIsomorphicBindingEvent(objVar1Name, objVar1Type, objVar1Value, objVar2Name, objVar2Type, objVar2Value));
		}
	}
	
	public static void logCheckLinkExistence(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logCheckLinkExistence(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logSuccessLinkExistence(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logSuccessLinkExistence(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logFailedLinkExistence(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logFailedLinkExistence(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logCheckAttributeConstraint(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logCheckAttributeConstraint(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logSuccessAttributeConstraint(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logSuccessAttributeConstraint(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logFailedAttributeConstraint(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logFailedAttributeConstraint(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logMethodCallExpression(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logMethodCallExpression(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logAttributeAssignment(SDMTraceContext c) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c))
			logAttributeAssignment(GLOBAL_CONTEXT);
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public static void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EObject eThis, Method method, Object...paramValues) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logMatchFound(c, stw, storyPatternName, selectedOp, paramValues);
	}
	
	protected static void logMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logMatchFound(GLOBAL_CONTEXT, stw, storyPatternName, op, paramValues);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logMatchFound(c, stw, storyPatternName, op, paramValues);
			}
		} else {
			c.traceEvent(stw, new MatchFoundEvent(storyPatternName, op, paramValues));
		}
	}
	
	public static void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EObject eThis, Method method, Object...paramValues) {
		init();
		if (disableTracing)
			return;
		
		if (eThis == null)
			throw new IllegalArgumentException("Please provide the correct EObject instance that holds the entered method");
		
		if (method == null)
			throw new IllegalArgumentException();
		
		EOperation selectedOp = findEOperation(eThis, method);
		logNoMatchFound(c, stw, storyPatternName, selectedOp, paramValues);
	}
	
	protected static void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw, String storyPatternName, EOperation op, Object... paramValues) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logNoMatchFound(GLOBAL_CONTEXT, stw, storyPatternName, op, paramValues);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logNoMatchFound(c, stw, storyPatternName, op, paramValues);
			}
		} else {
			c.traceEvent(stw, new NoMatchFoundEvent(storyPatternName, op, paramValues));
		}
	}
	
	public static void logObjCreation(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object newObjectValue) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logObjCreation(GLOBAL_CONTEXT, stw, objVarName, objVarType, newObjectValue);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logObjectCreation(c, stw, objVarName, objVarType, newObjectValue);
			}
		} else {
			c.traceEvent(stw, new ObjectCreationEvent(objVarName, objVarType, newObjectValue));
		}
	}
	
	public static void logObjDeletion(SDMTraceContext c, StackTraceWrapper stw, String objVarName, Class<?> objVarType, Object oldObjectValue) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logObjDeletion(GLOBAL_CONTEXT, stw, objVarName, objVarType, oldObjectValue);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logObjectDeletion(c, stw, objVarName, objVarType, oldObjectValue);
			}
		} else {
			c.traceEvent(stw, new ObjectDeletionEvent(objVarName, objVarType, oldObjectValue));
		}
	}
	
	public static void logLinkCreation(SDMTraceContext c, StackTraceWrapper stw, String sourceNodeName, Class<?> sourceNodeType, Object sourceNodeValue, String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue, String targetRoleName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logLinkCreation(GLOBAL_CONTEXT, stw, sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logLinkCreation(c, stw, sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName);
			}
		} else {
			c.traceEvent(stw, new LinkCreationEvent(sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName));
		}
	}
	
	public static void logLinkDeletion(SDMTraceContext c, StackTraceWrapper stw, String sourceNodeName, Class<?> sourceNodeType, Object sourceNodeValue, String sourceRoleName, String targetNodeName, Class<?> targetNodeType, Object targetNodeValue, String targetRoleName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logLinkDeletion(GLOBAL_CONTEXT, stw, sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logLinkDeletion(c, stw, sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName);
			}
		} else {
			c.traceEvent(stw, new LinkDeletionEvent(sourceNodeName, sourceNodeType, sourceNodeValue, sourceRoleName, targetNodeName, targetNodeType, targetNodeValue, targetRoleName));
		}
	}
	
	public static void logNoMoreLinkEndOptions(SDMTraceContext c, StackTraceWrapper stw, String linkName, String srcObjName, String trgtObjName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logNoMoreLinkEndOptions(GLOBAL_CONTEXT, stw, linkName, srcObjName, trgtObjName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logNoMoreLinkEndOptions(c, stw, linkName, srcObjName, trgtObjName);
			}
		} else {
			c.traceEvent(stw, new NoMoreLinkEndOptionsEvent(linkName, srcObjName, trgtObjName));
		}
	}
	
	public static void logCommenceOfGraphRewriting(SDMTraceContext c, StackTraceWrapper stw, String patternName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logCommenceOfGraphRewriting(GLOBAL_CONTEXT, stw, patternName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logCommenceOfGraphRewriting(c, stw, patternName);
			}
		} else {
			c.traceEvent(stw, new CommenceOfGraphRewritingEvent(patternName));
		}
	}
	
	public static void logBeginNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logBeginNACEvaluation(GLOBAL_CONTEXT, stw, patternName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logBeginNACEvaluation(c, stw, patternName);
			}
		} else {
			c.traceEvent(stw, new BeginNACEvaluationEvent(patternName));
		}
	}
	
	public static void logEndOfNACEvaluation(SDMTraceContext c, StackTraceWrapper stw, String patternName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logEndOfNACEvaluation(GLOBAL_CONTEXT, stw, patternName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logEndOfNACEvaluation(c, stw, patternName);
			}
		} else {
			c.traceEvent(stw, new EndOfNACEvaluationEvent(patternName));
		}
	}
	
	public static void logNACNotSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logNACNotSatisfied(GLOBAL_CONTEXT, stw, patternName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logNACNotSatisfied(c, stw, patternName);
			}
		} else {
			c.traceEvent(stw, new FailedNACEvent(patternName));
		}
	}
	
	public static void logNACSatisfied(SDMTraceContext c, StackTraceWrapper stw, String patternName) {
		init();
		if (disableTracing)
			return;
		
		if (!GLOBAL_CONTEXT.equals(c)) {
			logNACSatisfied(GLOBAL_CONTEXT, stw, patternName);
			for (SDMTraceStrategy strategy : STRATS) {
				strategy.logNACSatisfied(c, stw, patternName);
			}
		} else {
			c.traceEvent(stw, new SuccessNACEvent(patternName));
		}
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
	
	public static StackTraceWrapper getStackTraceWrapper(Method m, EOperation op) {
		StackTraceElement[] fullTrace = (new Throwable()).getStackTrace();
		return new StackTraceWrapper(m, op, Arrays.copyOfRange(fullTrace, 2, fullTrace.length));
	}
}
