package org.eclipse.linuxtools.lttng.jni.exception;
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

/**
 * <b><u>JniTracefileException</u></b>
 * <p>
 * Basic exception class for the JniTracefile class
 */
public class JniTracefileException extends JniException {
    private static final long serialVersionUID = 5081317864491800084L;

    public JniTracefileException(String errMsg) {
        super(errMsg);
    }
}
