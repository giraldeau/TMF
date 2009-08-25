/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfEventSource</u></b>
 * <p>
 * The event source.
 */
public class TmfEventSource {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final Object fSourceId;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * 
     */
    public TmfEventSource() {
        this(null);
    }

	/**
	 * @param sourceId
	 */
	public TmfEventSource(Object sourceId) {
		fSourceId = sourceId;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public Object getSourceId() {
		return fSourceId;
	}

	// ========================================================================
    // Operators
    // ========================================================================

    @Override
    public String toString() {
        return fSourceId.toString();
    }

}
