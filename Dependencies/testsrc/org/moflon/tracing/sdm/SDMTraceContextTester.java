package org.moflon.tracing.sdm;

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
	
	private void addEventsInOtherContext() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; // use index of 2 to get the first stable stack trace element 
		SDMTraceUtil.logOperationEnter(traceContext, ste, op, new Object[]{});
		SDMTraceUtil.logOperationExit(traceContext, ste, op, null);
	}
	
	@Test
	public void test_getTrace() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; // use index of 2 to get the first stable stack trace element
		SDMTraceUtil.logOperationEnter(traceContext, ste, op, new Object[]{});
		addEventsInOtherContext();
		SDMTraceUtil.logOperationExit(traceContext, ste, op, null);
		
		TraceEvent[] trace = traceContext.getTrace(ste);
		assertNotNull(trace);
		assertTrue(trace.length > 0);
		assertTrue(trace.length == 2);
	}
	
	@Test
	public void test_getFlatTrace() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; // use index of 2 to get the first stable stack trace element
		SDMTraceUtil.logOperationEnter(traceContext, ste, op, new Object[]{});
		addEventsInOtherContext();
		SDMTraceUtil.logOperationExit(traceContext, ste, op, null);
		
		TraceEvent[] trace = traceContext.getFlatTrace();
		assertNotNull(trace);
		assertTrue(trace.length > 0);
		assertTrue(trace.length == 4);
	}
	
	@Test
	public void test_getAllTraces() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2]; // use index of 2 to get the first stable stack trace element
		SDMTraceUtil.logOperationEnter(traceContext, ste, op, new Object[]{});
		addEventsInOtherContext();
		SDMTraceUtil.logOperationExit(traceContext, ste, op, null);
		
		Map<StackTraceElement, TraceEvent[]> allTraces = traceContext.getAllTraces();
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
