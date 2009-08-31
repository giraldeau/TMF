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

package org.eclipse.linuxtools.tmf.signal;

/**
 * <b><u>TmfSignalTrace</u></b>
 * <p>
 * TODO: Activate the tracing from a preference
 */
public class TmfSignalTrace {

	@TmfSignalHandler
	public void traceSignal(TmfSignal signal) {
		System.out.println(signal.getSource().toString() + ": " + signal.getClass().toString());
	}
}
