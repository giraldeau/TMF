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

package org.eclipse.linuxtools.tmf.experiment;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.signal.TmfSignal;

/**
 * <b><u>TmfExperimentSelectedSignal</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentSelectedSignal<T extends TmfEvent> extends TmfSignal {

	private final TmfExperiment<T> fExperiment;
	
	public TmfExperimentSelectedSignal(Object source, TmfExperiment<T> experiment) {
		super(source);
		fExperiment = experiment;
	}

	public TmfExperiment<? extends TmfEvent> getExperiment() {
		return fExperiment;
	}

	@Override
	public String toString() {
		return "[TmfExperimentSelectedSignal (" + fExperiment.getName() + ")]";
	}
}
