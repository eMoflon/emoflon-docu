package org.moflon.tracing.sdm.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;

public class OperationEnterEvent extends AbstractTraceEvent implements ControlFlowTraceEvent {

	private final static String OPERATION_NAME = "OperationEnterEvent";
	private final static String OPERATION_DESCTIPTION = "Issued when control flow enters an EOperation which was specified via an SDM";
	
	private final EOperation op;
	private final Object[] params;
	
	public OperationEnterEvent(EOperation op, Object... paramValues) {
		this.op = op;
		this.params = paramValues;
	}
	
	@Override
	public EObject getTraceData() {
		return op;
	}

	@Override
	public Object[] getFullTraceData() {
		Object[] result = new Object[params.length + 1];
		result[0] = op;
		for (int i = 0; i < params.length; i++) {
			result[i+1]=params[i];
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getOperationName());
		sb.append('[');
		sb.append("EOperation: \"");
		sb.append(op.getName());
		sb.append('(');		
		EList<EParameter> opParams = op.getEParameters();
		if (opParams != null && opParams.size() > 0) {
			sb.append(parametersAndValuesToString(new ArrayList<EParameter>(opParams), new ArrayList(Arrays.asList(params))));
		}
		sb.append(") :");
		EClassifier returnType = op.getEType();
		if (returnType == null)
			sb.append("void");
		else 
			sb.append(returnType.getName());
		sb.append("\"]");
		return sb.toString();
	}
	
	private String parametersAndValuesToString(List<EParameter> params, List<Object> values) {
		if (params.size() == 0 && values.size() == 0) {
			return null;
		} else if (params.size() > 0 && values.size() > 0) {		
			EParameter eParameter = params.get(0);
			params.remove(0);
			Object value = values.get(0);
			values.remove(0);
			String result = eParameter.getName() + "=" + value;
			String suffix = parametersAndValuesToString(params, values);
			if (suffix != null)
				result = result + ", " + suffix;
			return result;
		}
		throw new IllegalStateException("Parameter count differs from values count");
	}

	@Override
	public String getOperationName() {
		return OPERATION_NAME;
	}

	@Override
	public String getOperationDesctiption() {
		return OPERATION_DESCTIPTION;
	}
	
}
