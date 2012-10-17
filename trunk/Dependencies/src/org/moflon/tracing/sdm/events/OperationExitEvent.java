package org.moflon.tracing.sdm.events;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;


public class OperationExitEvent extends AbstractTraceEvent implements ControlFlowTraceEvent {

	private final static String OPERATION_NAME = "OperationExitEvent";
	private final static String OPERATION_DESCTIPTION = "Issued when control flow leaves an EOperation which was specified via an SDM";
	
	private final EOperation op;
	private final Object result; 
	
	public OperationExitEvent(EOperation op, Object result) {
		this.op = op;
		this.result = result;
	}
	
	@Override
	public EObject getTraceData() {
		return op;
	}

	@Override
	public Object[] getFullTraceData() {
		return new Object[]{getTraceData(), result};
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
			sb.append(parametersToString((new ArrayList<EParameter>(opParams))));
		}
		sb.append(") :");
		EClassifier returnType = op.getEType();
		if (returnType == null)
			sb.append("void");
		else 
			sb.append(returnType.getName());
		sb.append('=');
		sb.append(result);
		sb.append(']');
		return sb.toString();
	}
	
	private String parametersToString(List<EParameter> params) {
		if (params.size() == 0)
			return null;
		EParameter eParameter = params.get(0);
		params.remove(0);
		String result = eParameter.getName();
		String suffix = parametersToString(params);
		if (suffix != null)
			result = result + ", " + suffix;
		return result;
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
