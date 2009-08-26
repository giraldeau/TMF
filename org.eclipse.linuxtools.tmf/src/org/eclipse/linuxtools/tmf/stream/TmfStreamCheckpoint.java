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

package org.eclipse.linuxtools.tmf.stream;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfStreamCheckpoint</u></b>
 * <p>
 * This class maps an event timestamp with a trace location.
 */
public class TmfStreamCheckpoint implements Comparable<TmfStreamCheckpoint> {

    // ========================================================================
    // Attributes
    // ========================================================================
    
    private final TmfTimestamp fTimestamp;
    private final Object fLocation;

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param ts
     * @param location
     */
    public TmfStreamCheckpoint(TmfTimestamp ts, Object location) {
        fTimestamp = ts;
        fLocation = location;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the checkpoint event timestamp
     */
    public TmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /**
     * @return the checkpoint event stream location
     */
    public Object getLocation() {
        return fLocation;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    public int compareTo(TmfStreamCheckpoint other) {
        return fTimestamp.compareTo(other.fTimestamp, false);
    }

}
