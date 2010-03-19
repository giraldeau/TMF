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

import org.eclipse.linuxtools.lttng.jni.JniMarkerField;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

/**
 * <b><u>JniMarkerField_v2_6</u></b>
 * <p>
 * JniMarkerField version to support Lttng traceformat of version 2.6<br>
 * This class extend abstract class JniMarkerField with (possibly) version specific implementation.<br>
 * <p>
 */
public class JniMarkerField_v2_6 extends JniMarkerField {
	
	/*
	 * Forbid access to the default constructor
	 */
	protected JniMarkerField_v2_6() {
		super();
    }
	
	
    public JniMarkerField_v2_6(JniMarkerField_v2_6 oldMarkerField) {
    	super(oldMarkerField);
    }
    
    public JniMarkerField_v2_6(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	super(newMarkerFieldPtr);
    }
}
