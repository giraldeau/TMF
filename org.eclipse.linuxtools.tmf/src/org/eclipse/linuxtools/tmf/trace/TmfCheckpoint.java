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

package org.eclipse.linuxtools.tmf.trace;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentCheckpoint;

/**
 * <b><u>TmfCheckpoint</u></b>
 * <p>
 * This class maps an event timestamp with a location.
 */
public class TmfCheckpoint implements Comparable<TmfCheckpoint> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    private final TmfTimestamp fTimestamp;
    private final ITmfLocation<?> fLocation;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param ts
     * @param location
     */
    public TmfCheckpoint(TmfTimestamp ts, ITmfLocation<?> location) {
        fTimestamp = ts;
        fLocation = location;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the checkpoint event timestamp
     */
    public TmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /**
     * @return the checkpoint event stream location
     */
    public ITmfLocation<?> getLocation() {
        return fLocation;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
    	int result = 37;
    	result = 17 * result + fTimestamp.hashCode();
    	return result;
    }
 
    @Override
    public boolean equals(Object other) {
    	if (!(other instanceof TmfCheckpoint)) {
    		return false;
    	}
    	TmfCheckpoint o = (TmfCheckpoint) other;
    	return fTimestamp.equals(o.fTimestamp);
    }
 
    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    public int compareTo(TmfCheckpoint other) {
        return fTimestamp.compareTo(other.fTimestamp, false);
    }

}
