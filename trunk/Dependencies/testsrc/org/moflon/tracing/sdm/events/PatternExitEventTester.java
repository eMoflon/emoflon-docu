package org.moflon.tracing.sdm.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.junit.Before;
import org.junit.Test;

public class PatternExitEventTester {
	
	private PatternExitEvent e;
	
	@Before
	public void initTests() {
		EOperation op = mock(EOperation.class);
		EClass owningClass = mock(EClass.class);
		EList<EParameter> params = new BasicEList<EParameter>();
		params.add(mock(EParameter.class));
		EClassifier type = mock(EClassifier.class);
		when(op.getEContainingClass()).thenReturn(owningClass);
		when(owningClass.getName()).thenReturn("MyEClass");
		when(op.getName()).thenReturn("doSomething");
		when(op.getEParameters()).thenReturn(params);
		when(op.getEType()).thenReturn(type);
		when(type.getName()).thenReturn("MyEReturnType");
		e = new PatternExitEvent("some pattern name", op);
	}
	
	@Test
	public void test_getOperationName() {
		assertEquals("PatternExitEvent", e.getOperationName());
	}
	
	@Test
	public void test_getOperationDescription() {
		String operationDesctiption = e.getOperationDesctiption();
		assertNotNull(operationDesctiption);
		assertTrue(operationDesctiption.length() == 0);
	}
	
}
