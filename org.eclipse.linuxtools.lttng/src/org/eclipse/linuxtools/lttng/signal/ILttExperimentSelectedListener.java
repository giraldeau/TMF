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
package org.eclipse.linuxtools.lttng.signal;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal;

/**
 * @author alvaro
 *
 */
public interface ILttExperimentSelectedListener {

	/**
	 * 
	 * @param source
	 * @param experiment
	 */
	public void experimentSelected(Object source,
			TmfExperiment<LttngEvent> experiment);

	/**
	 * @param signal
	 * @param waitForComplete
	 *            if true: wait for the request to complete before returning
	 */
	public void experimentUpdated(TmfExperimentUpdatedSignal signal, boolean waitForComplete);
	
}
