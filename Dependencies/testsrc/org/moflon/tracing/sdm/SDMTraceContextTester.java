package org.moflon.tracing.sdm;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.ecore.EOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.moflon.tracing.sdm.SDMTraceContext;
import org.moflon.tracing.sdm.SDMTraceUtil;
import org.moflon.tracing.sdm.events.TraceEvent;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;


public class SDMTraceContextTester {

	private SDMTraceContext traceContext;
	private static EOperation op;
	
	@BeforeClass
	public static void initClass() {
		op = mock(EOperation.class); 
	}
	
	@Before
	public void initTest() {
		traceContext = SDMTraceUtil.getTraceContext("foobar");
		traceContext.reset();
	}
	
	private void addEventsInOtherContext() throws NoSuchMethodException, SecurityException {
		Method m = this.getClass().getDeclaredMethod("addEventsInOtherContext", new Class[]{});
		m.setAccessible(true);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(m);
		SDMTraceUtil.logOperationEnter(traceContext, stw, op, new Object[]{});
		SDMTraceUtil.logOperationExit(traceContext, stw, op, null);
	}
	
	@Test
	public void test_getTrace() throws NoSuchMethodException, SecurityException {
		Method m = this.getClass().getDeclaredMethod("test_getTrace", new Class<?>[]{});
		m.setAccessible(true);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(m);
		SDMTraceUtil.logOperationEnter(traceContext, stw, op, new Object[]{});
		addEventsInOtherContext();
		SDMTraceUtil.logOperationExit(traceContext, stw, op, null);
		
		TraceEvent[] trace = traceContext.getTrace(stw);
		assertNotNull(trace);
		assertTrue(trace.length > 0);
		assertTrue(trace.length == 2);
	}
	
	@Test
	public void test_getFlatTrace() throws NoSuchMethodException, SecurityException {
		Method m = this.getClass().getDeclaredMethod("test_getTrace", new Class<?>[]{});
		m.setAccessible(true);
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(m);
		SDMTraceUtil.logOperationEnter(traceContext, stw, op, new Object[]{});
		addEventsInOtherContext();
		SDMTraceUtil.logOperationExit(traceContext, stw, op, null);
		
		TraceEvent[] trace = traceContext.getFlatTrace();
		assertNotNull(trace);
		assertTrue(trace.length > 0);
		assertTrue(trace.length == 4);
	}
	
	@Test
	public void test_getAllTraces() throws NoSuchMethodException, SecurityException {
		Method m = this.getClass().getMethod("test_getTrace", new Class<?>[]{});
		StackTraceWrapper stw = SDMTraceUtil.getStackTraceWrapper(m);
		SDMTraceUtil.logOperationEnter(traceContext, stw, op, new Object[]{});
		addEventsInOtherContext();
		SDMTraceUtil.logOperationExit(traceContext, stw, op, null);
		
		Map<StackTraceWrapper, TraceEvent[]> allTraces = traceContext.getAllTraces();
		assertNotNull(allTraces);
		assertTrue(allTraces.keySet().size() == 2);
		
		Iterator<TraceEvent[]> it = allTraces.values().iterator();		
		TraceEvent[] next = it.next();
		assertNotNull(next);
		assertTrue(next.length == 2);
		
		next = it.next();
		assertNotNull(next);
		assertTrue(next.length == 2);
		assertFalse(it.hasNext());
	}
}
