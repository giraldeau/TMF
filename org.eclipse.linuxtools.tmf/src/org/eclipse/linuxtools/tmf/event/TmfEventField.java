/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfEventField</u></b>
 * <p>
 * A basic event field.
 */
public class TmfEventField {

    // ========================================================================
    // Attributes
    // ========================================================================

    private final Object fValue;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param value
     */
    public TmfEventField(Object value) {
        fValue = value;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return
     */
    public Object getValue() {
        return fValue;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return fValue.toString();
    }
}
