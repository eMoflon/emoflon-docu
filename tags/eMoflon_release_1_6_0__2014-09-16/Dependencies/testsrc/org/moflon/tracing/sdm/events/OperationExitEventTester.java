package org.moflon.tracing.sdm.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.junit.Before;
import org.junit.Test;

public class OperationExitEventTester {

	private OperationExitEvent e;
	
	private final String res = "done";
	private EOperation op;
	
	@Before
	public void initTests() {
		op = mock(EOperation.class);
		EClass returnType = mock(EClass.class);
		when(returnType.getName()).thenReturn("EString");
		when(op.getEType()).thenReturn(returnType);
		
		EList<EParameter> list = new BasicEList<EParameter>();
		EParameter mockParam = mock(EParameter.class);
		when(mockParam.getName()).thenReturn("param1");		
		list.add(mockParam);
		mockParam = mock(EParameter.class);
		when(mockParam.getName()).thenReturn("param2");
		list.add(mockParam);
		mockParam = mock(EParameter.class);
		when(mockParam.getName()).thenReturn("param3");
		list.add(mockParam);
		when(op.getEParameters()).thenReturn(list);		
		
		e = new OperationExitEvent(op, res);
	}
	
	@Test
	public void test_getTraceData() {
		EObject traceData = e.getTraceData();
		assertNotNull(traceData);
		assertSame(op, traceData);
	}
	
	@Test
	public void test_getFullTraceData() {
		Object[] fullTraceData = e.getFullTraceData();
		assertNotNull(fullTraceData);
		assertTrue(fullTraceData.length == 2);
		assertSame(op, fullTraceData[0]);
		assertSame(res, fullTraceData[1]);
	}
	
	@Test
	public void test_toString() {
		String result = e.toString();
		assertNotNull(result);
		assertTrue(result.length() > 0);
		assertTrue(result.startsWith("OperationExitEvent"));
		assertTrue(result.contains(res));
	}
	
	@Test
	public void test_getOperationName() {
		assertEquals("OperationExitEvent", e.getOperationName());
	}
	
	@Test
	public void test_getOperationDescription() {
		assertNotNull(e.getOperationDesctiption());
		assertTrue(e.getOperationDesctiption().length() > 0);
	}
}
