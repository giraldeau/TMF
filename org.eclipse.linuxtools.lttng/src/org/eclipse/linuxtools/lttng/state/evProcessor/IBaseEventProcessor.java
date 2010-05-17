/*******************************************************************************
 * Copyright (c) 2010 Ericsson
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

import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfEvent;

public interface IBaseEventProcessor {

	/**
	 * Base event handler, either dispatcher or actual handler
	 * 
	 * @param tmfEvent
	 * @param traceSt
	 */
	public abstract void process(TmfEvent tmfEvent, LttngTraceState traceSt);

}