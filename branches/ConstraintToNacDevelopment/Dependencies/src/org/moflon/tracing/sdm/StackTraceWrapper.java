package org.moflon.tracing.sdm;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.eclipse.emf.ecore.EOperation;

public class StackTraceWrapper {

	private final StackTraceElement[] callingTrace;
	private final Method method;
	private final EOperation operation;
	
	StackTraceWrapper(Method method, EOperation operation, StackTraceElement[] callingTrace) {
		if (method == null || operation == null || callingTrace == null || callingTrace.length == 0)
			throw new IllegalArgumentException();
		this.callingTrace = callingTrace;
		this.method = method;
		this.operation = operation;
	}

	public StackTraceElement[] getcallingTrace() {
		return callingTrace;
	}

	public Method getMethod() {
		return method;
	}
	
	protected EOperation getOperation() {
		return operation;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof StackTraceWrapper) {
			StackTraceWrapper temp = (StackTraceWrapper) obj;
			if (method.equals(temp.getMethod())) {
				return (Arrays.equals(getcallingTrace(), temp.getcallingTrace()));
			} else {
				return false;
			}
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("[Method: \"");
		sb.append(getMethod());
		sb.append("\" called by: ");		
		sb.append(Arrays.toString(getcallingTrace()));
		sb.append(']');
		return sb.toString();
	}
	
	public boolean isValidStackTrace(StackTraceElement[] stackTrace) {
		boolean flag = false;
		for (int i = 0; i < stackTrace.length; i++) {
			StackTraceElement[] subTrace = Arrays.copyOfRange(stackTrace, i, stackTrace.length);
			flag = Arrays.equals(callingTrace, subTrace);
			if (flag)
				break;
		}
		
		return flag;
	}
}
