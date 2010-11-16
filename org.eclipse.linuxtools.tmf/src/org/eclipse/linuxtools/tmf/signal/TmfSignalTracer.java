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
 * <b><u>TmfSignalTracer</u></b>
 * <p>
 * This object (singleton) traces all TmfSignals in the application.
 */
public class TmfSignalTracer {

	static TmfSignalTracer fInstance;

	static public TmfSignalTracer getInstance() {
		if (fInstance == null) {
			fInstance = new TmfSignalTracer();
		}
		return fInstance;
	}

	private TmfSignalTracer() {
	}

	@TmfSignalHandler
	public void traceSignal(TmfSignal signal) {
		System.out.println(signal.getSource().toString() + ": " + signal.toString()); //$NON-NLS-1$
	}
}
