package org.eclipse.linuxtools.lttng.jni_v2_5;

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniTrace_v2_5 extends JniTrace {
	
	private static final String LIBRARY_NAME = "liblttvtraceread-2.5.so";
	
	protected JniTrace_v2_5() {
		super();
    }
    
	public JniTrace_v2_5(String newpath) throws JniException {
		super(newpath);
	}
	
    public JniTrace_v2_5(String newpath, boolean newPrintDebug) throws JniException {
    	super(newpath, newPrintDebug);
    }
    
    
    public JniTrace_v2_5(JniTrace_v2_5 oldTrace) {
    	super(oldTrace);
    }        
    
    public JniTrace_v2_5(Jni_C_Pointer newPtr, boolean newPrintDebug) throws JniException {
    	super(newPtr, newPrintDebug);
    }
    
    
    @Override
	public boolean initializeLibrary() {
    	return ltt_initializeHandle(LIBRARY_NAME);
    }
    
    @Override
	public JniTracefile allocateNewJniTracefile(Jni_C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
    	return new JniTracefile_v2_5(newPtr, newParentTrace);
    }
    
    
    
}
