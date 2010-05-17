/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.state.evProcessor;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;

/**
 * @author alvaro
 *
 */
public interface ILttngEventProcessor {

	// ========================================================================
	// Abstract methods
	// =======================================================================
	/**
	 * Implementors will either dispatch or determine the handler of the event
	 * provided
	 * 
	 * @param trcEvent
	 * @param traceSt
	 * @return
	 */
	public abstract boolean process(LttngEvent trcEvent, LttngTraceState traceSt);
	
}
