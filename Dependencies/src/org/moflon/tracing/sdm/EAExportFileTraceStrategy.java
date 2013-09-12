package org.moflon.tracing.sdm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class EAExportFileTraceStrategy extends AbstractEaTraceStrategy {

	public static final String SDM_EA_EXPORT_FILE_NAME_SYS_PROP = "org.moflon.tracing.sdm.EAExportFileTraceStrategy.fileName";
	private static final String DEFAULT_FILE_NAME = "ea_trace_export.log";

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
			init(out);			
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void initializeStrategy()
	{
		// TODO Auto-generated method stub
		
	}
	
}
