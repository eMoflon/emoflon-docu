package org.moflon.tracing.sdm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Stack;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import static org.moflon.util.EAInterfaceUriHelper.*;

public class EAExportFileTraceStrategy extends SDMTraceStrategy {

	public static final String SDM_EA_EXPORT_FILE_NAME_SYS_PROP = "org.moflon.tracing.sdm.EAExportFileTraceStrategy.fileName";
	
	private static final char DELIM = '/';
	private static final String LINE_SEPARATOR = System.lineSeparator();
	private static final String DEFAULT_FILE_NAME = "ea_trace_export.log";
	
	private BufferedWriter out = null;
	private Stack<EOperation> opStack = new Stack<EOperation>();
	private String storyPattern = null;

	public EAExportFileTraceStrategy() {
		String fileName = System.getProperty(SDM_EA_EXPORT_FILE_NAME_SYS_PROP);
		if (fileName == null || "".equals(fileName));
			fileName = DEFAULT_FILE_NAME;
		
		File f = new File(fileName);
		if (!f.exists()) {
			try {
				f.createNewFile();				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f, false), "UTF-8");
			out = new BufferedWriter(osw);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void logOperationEnter(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object[] parameterValues) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageString(op));
		sb.append(DELIM);
		sb.append(getClassString(op));
		sb.append(DELIM);
		sb.append(getEOperationString(op.getName()));
		sb.append(DELIM);
		sb.append(getActivityString());
		sb.append(LINE_SEPARATOR);
		
		try {
			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		opStack.push(op);
	}
	


	@Override
	protected void logOperationExit(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object result) {
		opStack.pop();
		return;
	}

	@Override
	protected void logPatternEnter(SDMTraceContext c, StackTraceWrapper stw,
			String storyPatternName, EOperation op) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageString(op));
		sb.append(DELIM);
		sb.append(getClassString(op));
		sb.append(DELIM);
		sb.append(getEOperationString(op.getName()));
		sb.append(DELIM);
		sb.append(getActivityString());
		sb.append(DELIM);
		sb.append(getStoryNodeString(storyPatternName));
		sb.append(LINE_SEPARATOR);
		
		try {
			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		this.storyPattern = storyPatternName;
	}

	@Override
	protected void logPatternExit(SDMTraceContext c, StackTraceWrapper stw,
			String storyPatternName, EOperation op) {
		if (this.storyPattern != null && this.storyPattern.equals(storyPatternName))
			this.storyPattern = null;
			
		return;
	}

	@Override
	protected void logBindObjVar(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageString(opStack.peek()));
		sb.append(DELIM);
		sb.append(getClassString(opStack.peek()));
		sb.append(DELIM);
		sb.append(getEOperationString(opStack.peek().getName()));
		sb.append(DELIM);
		sb.append(getActivityString());
		sb.append(DELIM);
		sb.append(getStoryNodeString(this.storyPattern));
		sb.append(DELIM);
		sb.append(getObjVarString(objVarName));
		sb.append(LINE_SEPARATOR);
		
		try {
			out.write(sb.toString());
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void logUnbindObjVar(SDMTraceContext c, StackTraceWrapper stw,
			String objVarName, Class<?> objVarType, Object oldValue,
			Object newValue) {
		// implement maybe...
		return;
	}

	@Override
	protected void logMatchFound(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object... paramValues) {
		// implement maybe...
		return;
	}

	@Override
	protected void logNoMatchFound(SDMTraceContext c, StackTraceWrapper stw,
			EOperation op, Object... paramValues) {
		// implement maybe...
		return;
	}
	
	private static String getPackageString(EOperation op) {
		EClass eContainingClass = op.getEContainingClass();
		return getPackageString(eContainingClass.getEPackage());
	}
	
	private static String getPackageString(EPackage p) {
		if (p == null)
			return null;
		StringBuilder sb = new StringBuilder();
		String packageString = getPackageString(p.getESuperPackage());
		if (packageString != null) {
			sb.append(packageString);
			sb.append(DELIM);
		}
		sb.append(getEPackageString(p.getName()));
		return sb.toString();
	}
	
	private static String getClassString(EOperation op) {
		EClass eContainingClass = op.getEContainingClass();
		StringBuilder sb = new StringBuilder();
		sb.append(getEClassString(eContainingClass.getName()));
		return sb.toString(); 
	}
	
}
