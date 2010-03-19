package org.eclipse.linuxtools.lttng.jni_v2_3;
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
 * <b><u>JniMarkerField_v2_3</u></b>
 * <p>
 * JniMarkerField version to support Lttng traceformat of version 2.3<br>
 * This class extend abstract class JniMarkerField with (possibly) version specific implementation.<br>
 * <p>
 */
public class JniMarkerField_v2_3 extends JniMarkerField {
	
	/*
	 * Forbid access to the default constructor
	 */
	protected JniMarkerField_v2_3() {
		super();
    }
	
	
    public JniMarkerField_v2_3(JniMarkerField_v2_3 oldMarkerField) {
    	super(oldMarkerField);
    }
    
    public JniMarkerField_v2_3(Jni_C_Pointer newMarkerFieldPtr) throws JniException {
    	super(newMarkerFieldPtr);
    }
}
