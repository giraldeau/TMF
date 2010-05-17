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
package org.eclipse.linuxtools.lttng.control;

import org.eclipse.linuxtools.lttng.LttngTestPreparation;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;

public class LTTngSyntheticEventProviderTest extends LttngTestPreparation {
	// ========================================================================
	// Tests
	// ========================================================================

	public void testPlainDataRequest() {
		// prepare
		init();
		TmfExperiment<LttngEvent> experiment = prepareExperimentToTest();
		TmfEventRequest<LttngEvent> request = prepareEventRequest(
				LttngEvent.class, 0, 31);

		// execute
		experiment.sendRequest(request);
		try {
			request.waitForCompletion();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// finish
		assertEquals("Unexpected eventCount", 15316, feventCount);
		boolean expected = true;
		assertEquals("Events received out of expected order", expected,
				validSequence);
	}

	/**
	 * 
	 */
	public void testSyntheticEventRequest() {
		init();
		// Create a new Experiment manager context
		IStateExperimentManager expManager = prepareExperimentContext(false);

		// make sure a TmfExperiment instance is registered as provider and
		// selected as current
		TmfExperiment<LttngEvent> experiment = prepareExperimentToTest();

		// experiment selected, build experiment selection context and trigger
		// check point creation
		expManager.experimentSelected_prep(experiment);
		// Action trigger
		expManager.experimentSelected(this, experiment);

		// Obtain the singleton event provider
		LttngSyntheticEventProvider synProvider = LttngCoreProviderFactory
				.getEventProvider();

		// prepare synthetic event requests
		TmfEventRequest<LttngSyntheticEvent> request1 = prepareEventRequest(
				LttngSyntheticEvent.class, 5, 9); /* 2001 events*/
		TmfEventRequest<LttngSyntheticEvent> request2 = prepareEventRequest(
				LttngSyntheticEvent.class, 11, 13); /* 1001 events */

		// execute
		synProvider.sendRequest(request1);
		synProvider.sendRequest(request2);
		try {
			request2.waitForCompletion();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		// finish
		assertEquals("Unexpected eventCount", 3002, feventCount);
	}

}