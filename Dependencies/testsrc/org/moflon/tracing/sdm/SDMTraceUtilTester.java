package org.moflon.tracing.sdm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.junit.Test;
import org.moflon.tracing.sdm.events.BindObjectVarEvent;
import org.moflon.tracing.sdm.events.MatchFoundEvent;
import org.moflon.tracing.sdm.events.NoMatchFoundEvent;
import org.moflon.tracing.sdm.events.PatternEnterEvent;
import org.moflon.tracing.sdm.events.PatternExitEvent;
import org.moflon.tracing.sdm.events.TraceEvent;
import org.moflon.tracing.sdm.events.UnbindObjectVarEvent;
import org.moflon.util.eMoflonEMFUtil;

public class SDMTraceUtilTester {

	private static Method getMethod(Object o, String methodName, Class... paramTypes) {
		Method m;
		try {
			m = o.getClass().getDeclaredMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		m.setAccessible(true);
		return m;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_getTraceContext1() {
		SDMTraceUtil.getTraceContext(null);
	}
	
	@Test
	public void test_getTraceContext2() {
		assertNotNull(SDMTraceUtil.getTraceContext(""));
	}
	
	@Test
	public void test_getTraceContext3() {
		assertNotNull(SDMTraceUtil.getTraceContext("foo"));
	}
	
	@Test
	public void test_getTraceContext4() {
		assertNotSame(SDMTraceUtil.getTraceContext("foo"), 
					  SDMTraceUtil.getTraceContext("bar"));
	}
	
	@Test
	public void test_getTraceContext5() {
		assertSame(SDMTraceUtil.getTraceContext("foo"), 
				   SDMTraceUtil.getTraceContext("foo"));
	}
	
	@Test
	public void test_getTraceContext6() {
		assertNotSame(SDMTraceUtil.getTraceContext("foo"), 
					  SDMTraceUtil.getTraceContext("foo.bar"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnter1() {
		SDMTraceUtil.logOperationEnter(null, null, null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnter2() throws NoSuchMethodException, SecurityException {
		EOperation mockedOperation = mock(EOperation.class);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationEnter2", new Class[]{}));
		SDMTraceUtil.logOperationEnter(null, stw, mockedOperation, new Object[]{true});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnter3() {
		EOperation mockedOperation = mock(EOperation.class);
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), null, mockedOperation, new Object[]{true});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnter4() {
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationEnter4", new Class[]{}));
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, null, new Object[]{true});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnter5() {
		EOperation mockedOperation = mock(EOperation.class);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationEnter5", new Class[]{}));
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, mockedOperation, null);
	}
	
	@Test
	public void test_logOperationEnter6() {
		EOperation mockedOperation = mock(EOperation.class);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationEnter6", new Class[]{}));
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, mockedOperation, new Object[]{true});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnterB1_nullParams() {
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationEnterB1_nullParams", new Class[]{}));
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, null, null, new Object[]{true});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnterB2_nullParam1() {
		EObject mockedEObj = mock(EObject.class);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationEnterB2_nullParam1", new Class[]{}));
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, mockedEObj, null, new Object[]{true});
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationEnterB3_nullParam2() throws NoSuchMethodException, SecurityException, NullPointerException  {
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		Method currentMethod = getMethod(this, currentMethodName, new Class[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, null, currentMethod, new Object[]{true});
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_logOperationEnterB4_NoMatchingEOperation() throws NoSuchMethodException, SecurityException, NullPointerException  {
		EObject mockedEObj = mock(EObject.class);
		EClass mockedEClass = mock(EClass.class);
		EOperation mockedOperation = mock(EOperation.class);
		when(mockedEObj.eClass()).thenReturn(mockedEClass);
		EList<EOperation> ops = new BasicEList<EOperation>();
		ops.add(mockedOperation);		
		when(mockedEClass.getEAllOperations()).thenReturn(ops);
		when(mockedOperation.getName()).thenReturn("foo");
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		Method currentMethod = getMethod(this, currentMethodName, new Class[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, mockedEObj, currentMethod, new Object[]{true});
	}
	
	@Test
	public void test_logOperationEnterB5_SeveralEOperationsButOnlyOneMatch() throws NoSuchMethodException, SecurityException, NullPointerException  {
		EObject mockedEObj = mock(EObject.class);
		EClass mockedEClass = mock(EClass.class);
		EOperation mockedOperation = mock(EOperation.class);
		
		EList<EParameter> params = new BasicEList<EParameter>();
		EParameter param1 = mock(EParameter.class);
		EParameter param2 = mock(EParameter.class);
		params.add(param1);
		params.add(param2);
		when(mockedOperation.getEParameters()).thenReturn(params);
		when(mockedOperation.getEParameters()).thenReturn(new BasicEList<EParameter>());
		
		EOperation mockedOperation2 = mock(EOperation.class);
		when(mockedEObj.eClass()).thenReturn(mockedEClass);
		EList<EOperation> ops = new BasicEList<EOperation>();
		ops.add(mockedOperation);
		ops.add(mockedOperation2);
		when(mockedEClass.getEAllOperations()).thenReturn(ops);
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		when(mockedOperation.getName()).thenReturn(currentMethodName);
		when(mockedOperation2.getName()).thenReturn(currentMethodName);
		Method currentMethod = getMethod(this, currentMethodName, new Class<?>[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, mockedEObj, currentMethod, new Object[]{true});
	}
	
	@Test
	public void test_logOperationEnterB6_SimpleMatch() throws NoSuchMethodException, SecurityException, NullPointerException  {
		EObject mockedEObj = mock(EObject.class);
		EClass mockedEClass = mock(EClass.class);
		EOperation mockedOperation = mock(EOperation.class);
		when(mockedEObj.eClass()).thenReturn(mockedEClass);
		EList<EOperation> ops = new BasicEList<EOperation>();
		ops.add(mockedOperation);		
		when(mockedEClass.getEAllOperations()).thenReturn(ops);
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		when(mockedOperation.getName()).thenReturn(currentMethodName);
		Method currentMethod = getMethod(this, currentMethodName, new Class<?>[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceUtil.logOperationEnter(SDMTraceUtil.getTraceContext("foo"), stw, mockedEObj, currentMethod, new Object[]{true});
	}
	
//	private StackTraceElement getStackTraceElement() {
//		return (new Throwable()).getStackTrace()[2]; // use index of 3 to get the first stable stack trace element
//	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationExit1() {
		SDMTraceUtil.logOperationExit(null, null, null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationExit2() {
		EOperation mockedOperation = mock(EOperation.class);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationExit2", new Class[]{}));
		SDMTraceUtil.logOperationExit(null, stw, mockedOperation, true);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationExit3() {
		EOperation mockedOperation = mock(EOperation.class);
		SDMTraceUtil.logOperationExit(SDMTraceUtil.getTraceContext("foo"), null, mockedOperation, true);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void test_logOperationExit4() {
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationExit4", new Class[]{}));
		SDMTraceUtil.logOperationExit(SDMTraceUtil.getTraceContext("foo"), stw, null, true);
	}
	
	@Test
	public void test_logOperationExit5() {
		EOperation mockedOperation = mock(EOperation.class);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationExit5", new Class[]{}));
		SDMTraceUtil.logOperationExit(SDMTraceUtil.getTraceContext("foo"), stw, mockedOperation, null);
	}
	
	@Test
	public void test_logOperationExit6() {
		EOperation mockedOperation = mock(EOperation.class);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logOperationExit6", new Class[]{}));
		SDMTraceUtil.logOperationExit(SDMTraceUtil.getTraceContext("foo"), stw, mockedOperation, true);
	}
	
	@Test
	public void test_logPatternEnter1() throws NoSuchMethodException, SecurityException, NullPointerException {
		EObject mockedEObj = mock(EObject.class);
		EClass mockedEClass = mock(EClass.class);
		EOperation mockedOperation = mock(EOperation.class);
		when(mockedEObj.eClass()).thenReturn(mockedEClass);
		EList<EOperation> ops = new BasicEList<EOperation>();
		ops.add(mockedOperation);		
		when(mockedEClass.getEAllOperations()).thenReturn(ops);
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		when(mockedOperation.getName()).thenReturn(currentMethodName);
		when(mockedOperation.getEAnnotations()).thenReturn(null);
		Method currentMethod = getMethod(this, currentMethodName, new Class<?>[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceUtil.getTraceContext("foo").reset();
		// test
		SDMTraceUtil.logPatternEnter(SDMTraceUtil.getTraceContext("foo"), stw, mockedEObj,  currentMethod, "pattern1");
		// check
		Map<StackTraceWrapper, TraceEvent[]> allTraces = SDMTraceUtil.getTraceContext("foo").getAllTraces();
		assertTrue(!allTraces.values().isEmpty());
		assertTrue(allTraces.values().size() == 1);
		assertTrue(allTraces.values().iterator().next()[0] instanceof PatternEnterEvent);		
	}
	
	@Test
	public void test_logPatternExit1() throws NoSuchMethodException, SecurityException, NullPointerException {
		// setup
		EObject mockedEObj = mock(EObject.class);
		EClass mockedEClass = mock(EClass.class);
		EOperation mockedOperation = mock(EOperation.class);
		when(mockedEObj.eClass()).thenReturn(mockedEClass);
		EList<EOperation> ops = new BasicEList<EOperation>();
		ops.add(mockedOperation);		
		when(mockedEClass.getEAllOperations()).thenReturn(ops);
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		when(mockedOperation.getName()).thenReturn(currentMethodName);
		when(mockedOperation.getEAnnotations()).thenReturn(null);
		Method currentMethod = getMethod(this, currentMethodName, new Class<?>[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceUtil.getTraceContext("foo").reset();
		// test
		SDMTraceUtil.logPatternExit(SDMTraceUtil.getTraceContext("foo"), stw, mockedEObj,  currentMethod, "pattern1");
		// check
		Map<StackTraceWrapper, TraceEvent[]> allTraces = SDMTraceUtil.getTraceContext("foo").getAllTraces();
		assertTrue(!allTraces.values().isEmpty());
		assertTrue(allTraces.values().size() == 1);
		assertTrue(allTraces.values().iterator().next()[0] instanceof PatternExitEvent);	
	}
	
	@Test
	public void test_logBindingObjVar() {
		//setup
		SDMTraceContext traceContext = SDMTraceUtil.getTraceContext("foo");
		traceContext.reset();
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logBindingObjVar", new Class[]{}));
		// test
		SDMTraceUtil.logBindObjVar(traceContext, stw, "someObjVar", EObject.class, null, mock(EObject.class));
		// check
		Map<StackTraceWrapper, TraceEvent[]> allTraces = SDMTraceUtil.getTraceContext("foo").getAllTraces();
		assertTrue(!allTraces.values().isEmpty());
		assertTrue(allTraces.values().size() == 1);
		assertTrue(allTraces.values().iterator().next()[0] instanceof BindObjectVarEvent);
	}
	
	@Test
	public void test_logUnbindingObjVar() {
		//setup
		SDMTraceContext traceContext = SDMTraceUtil.getTraceContext("foo");
		traceContext.reset();
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(getMethod(this, "test_logUnbindingObjVar", new Class[]{}));
		// test
		SDMTraceUtil.logUnbindObjVar(traceContext, stw, "someObjVar", EObject.class, mock(EObject.class), null);
		// check
		Map<StackTraceWrapper, TraceEvent[]> allTraces = SDMTraceUtil.getTraceContext("foo").getAllTraces();
		assertTrue(!allTraces.values().isEmpty());
		assertTrue(allTraces.values().size() == 1);
		assertTrue(allTraces.values().iterator().next()[0] instanceof UnbindObjectVarEvent);
	}
	
	@Test
	public void test_logMatchFound() throws NoSuchMethodException, SecurityException, NullPointerException {
		// setup
		EObject mockedEObj = mock(EObject.class);
		EClass mockedEClass = mock(EClass.class);
		EOperation mockedOperation = mock(EOperation.class);
		when(mockedEObj.eClass()).thenReturn(mockedEClass);
		EList<EOperation> ops = new BasicEList<EOperation>();
		ops.add(mockedOperation);		
		when(mockedEClass.getEAllOperations()).thenReturn(ops);
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		when(mockedOperation.getName()).thenReturn(currentMethodName);
		when(mockedOperation.getEAnnotations()).thenReturn(null);
		Method currentMethod = getMethod(this, currentMethodName, new Class<?>[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceContext traceContext = SDMTraceUtil.getTraceContext("foo");
		traceContext.reset();
		// test
		SDMTraceUtil.logMatchFound(traceContext, stw, mockedEObj, currentMethod, new Object[]{});
		// check
		Map<StackTraceWrapper, TraceEvent[]> allTraces = SDMTraceUtil.getTraceContext("foo").getAllTraces();
		assertTrue(!allTraces.values().isEmpty());
		assertTrue(allTraces.values().size() == 1);
		assertTrue(allTraces.values().iterator().next()[0] instanceof MatchFoundEvent);
	}
	
	@Test
	public void test_logNoMatchFound() throws NoSuchMethodException, SecurityException, NullPointerException {
		// setup
		EObject mockedEObj = mock(EObject.class);
		EClass mockedEClass = mock(EClass.class);
		EOperation mockedOperation = mock(EOperation.class);
		when(mockedEObj.eClass()).thenReturn(mockedEClass);
		EList<EOperation> ops = new BasicEList<EOperation>();
		ops.add(mockedOperation);		
		when(mockedEClass.getEAllOperations()).thenReturn(ops);
		StackTraceElement stackTraceElement = (new Throwable()).getStackTrace()[0];		
		String currentMethodName = stackTraceElement.getMethodName();
		when(mockedOperation.getName()).thenReturn(currentMethodName);
		when(mockedOperation.getEAnnotations()).thenReturn(null);
		Method currentMethod = getMethod(this, currentMethodName, new Class<?>[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(currentMethod);
		SDMTraceContext traceContext = SDMTraceUtil.getTraceContext("foo");
		traceContext.reset();
		// test
		SDMTraceUtil.logNoMatchFound(traceContext, stw, mockedEObj, currentMethod, new Object[]{});
		// check
		Map<StackTraceWrapper, TraceEvent[]> allTraces = SDMTraceUtil.getTraceContext("foo").getAllTraces();
		assertTrue(!allTraces.values().isEmpty());
		assertTrue(allTraces.values().size() == 1);
		assertTrue(allTraces.values().iterator().next()[0] instanceof NoMatchFoundEvent);
	}
	
	@Test
	public void test_getStackTraceWrapper() throws NoSuchMethodException, SecurityException {
		SDMTraceContext traceContext = SDMTraceUtil.getTraceContext("foo");
		traceContext.reset();
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(this.getClass().getMethod("test_getStackTraceWrapper", new Class[]{}));
		assertTrue(stw.toString().contains("test_getStackTraceWrapper"));
		assertTrue(stw.toString().contains("called by"));
	}
}