/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.state.model;



/**
 * <b><u>LttvTrapState</u></b>
 * <p>
 *
 */
public class LttngTrapState implements Cloneable {
    // ========================================================================
	// Data
    // =======================================================================
	private Long running;

	
    // ========================================================================
    // Constructor
    // =======================================================================
	public LttngTrapState(Long running) {
		this.running = running;
	}
	
    public LttngTrapState clone() {
	    LttngTrapState newState = null;
        
        try {
            newState = (LttngTrapState)super.clone();
            
            // *** IMPORTANT ***
            // Basic type in java are immutable! 
            // Thus, using assignation ("=") on basic type is CORRECT, 
            //  but we should ALWAYS use "new" or "clone()" on "non basic" type
            newState.running = this.running;
        }
        catch ( CloneNotSupportedException e ) {
            System.out.println("Cloning failed with : " + e.getMessage() );
        }
        
        return newState;
    }
	
    // ========================================================================
    // Methods
    // =======================================================================
	public Long getRunning() {
		return running;
	}

	public void setRunning(Long running) {
		this.running = running;
	}

	public void incrementRunning() {
		running++;
	}
	
	public void decrementRunning() {
		if (running > 0) {
			running--;			
		}
	}
}