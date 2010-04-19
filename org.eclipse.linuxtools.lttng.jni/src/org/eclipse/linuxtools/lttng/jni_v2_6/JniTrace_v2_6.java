package org.eclipse.linuxtools.lttng.jni_v2_6;
/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

/**
 * <b><u>JniTrace_v2_6</u></b>
 * <p>
 * JniTrace version to support Lttng traceformat of version 2.6.<br>
 * This class extend abstract class JniTrace with (possibly) version specific implementation (none yet).<br>
 *  
 * It also make sure the correct library is loaded by liblttvlibraryloader.so 
 * <p>
 */
public class JniTrace_v2_6 extends JniTrace {
	
	// This is the dynamic library name that is passed to the library loader (liblttvlibraryloader.so) to load.
	// It needs to be a complete name, like "libXYZ.so", unlike java that would take "XYZ". It could also take a complete path.
	//	The library need to be accessible, i.e. LD_LIBRARY_PATH need to be set correctly. 
	private static final String LIBRARY_NAME = "liblttvtraceread-2.6.so";
	
	/*
	 * Forbid access to the default constructor
	 */
	protected JniTrace_v2_6() {
		super();
    }
    
	
	public JniTrace_v2_6(String newpath) throws JniException {
		super(newpath);
	}
	
    public JniTrace_v2_6(String newpath, boolean newPrintDebug) throws JniException {
    	super(newpath, newPrintDebug);
    }
    
    public JniTrace_v2_6(JniTrace_v2_6 oldTrace) {
    	super(oldTrace);
    }        
    
    public JniTrace_v2_6(Jni_C_Pointer_And_Library_Id newPtr, boolean newPrintDebug) throws JniException {
    	super(newPtr, newPrintDebug);
    }
    
    
    /**
     * Initialize the C library.<p>
     * 
     * Call the library loader with the .so we wish to load.
     * 
     * @return 	True if the load went successful, false otherwise.
     */
    @Override
	public int initializeLibrary() {
    	return ltt_initializeHandle(LIBRARY_NAME);
    }
    
    
    /**
     * Allocate (call constructor for) a new JniTracefile.<p>
     * 
     * This method is made to bypass limitation related to abstract class, see comment in JniTrace
     * 
     * @return JniTracefile 	a newly allocated JniTracefile
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    @Override
	public JniTracefile allocateNewJniTracefile(Jni_C_Pointer_And_Library_Id newPtr, JniTrace newParentTrace) throws JniException {
    	return new JniTracefile_v2_6(newPtr, newParentTrace);
    }
}
