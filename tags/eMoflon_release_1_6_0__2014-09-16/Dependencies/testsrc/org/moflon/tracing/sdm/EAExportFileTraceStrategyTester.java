package org.moflon.tracing.sdm;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class EAExportFileTraceStrategyTester {
	
	private final static String STORY_PATTERN_NAME = "somePattern";

	private static EAExportFileTraceStrategy strat;
	private static EOperation op;
	
	@BeforeClass
	public static void init(){		
		op = mock(EOperation.class);
		EClass containigClass = mock(EClass.class); 
		EPackage parentPackage = mock(EPackage.class);
		EPackage grandParentPackage = mock(EPackage.class);
		
		when(op.getName()).thenReturn("someMethod");
		when(op.getEContainingClass()).thenReturn(containigClass);
		
		when(containigClass.getName()).thenReturn("MyClass");
		when(containigClass.getEPackage()).thenReturn(parentPackage);
		
		when(parentPackage.getName()).thenReturn("subPackage");
		when(parentPackage.getESuperPackage()).thenReturn(grandParentPackage);
		
		when(grandParentPackage.getName()).thenReturn("rootPackage");
		when(grandParentPackage.getESuperPackage()).thenReturn(null);
	}
	
	@Before
	public void initTests() {
		strat = new EAExportFileTraceStrategy();
	}
	
	@Test
	public void test_logOperationEnter() {
		strat.logOperationEnter(null, null, op, null);
	}
	
	@Test
	public void test_logOperationExit() {
		strat.logOperationEnter(null, null, op, null);
		strat.logOperationExit(null, null, op, null);
	}
	
	@Test
	public void test_logPatternEnter() {
		strat.logPatternEnter(null, null, STORY_PATTERN_NAME, op);
	}
	
	@Test
	public void test_logPatternExit() {
		strat.logPatternExit(null, null, STORY_PATTERN_NAME, op);
	}
	
	@Test
	public void test_completeTrace() {
		strat.logOperationEnter(null, null, op, null);
		strat.logPatternEnter(null, null, "patter1", op);
		strat.logPatternEnter(null, null, "patter2", op);
		strat.logPatternEnter(null, null, "patter3", op);
		strat.logOperationExit(null, null, op, null);
	}
}
