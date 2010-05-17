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
package org.eclipse.linuxtools.lttng.state.experiment;

import org.eclipse.linuxtools.lttng.LttngTestPreparation;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNode;
import org.eclipse.linuxtools.lttng.state.trace.StateTraceManager;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;

/**
 * @author alvaro
 *
 */
public class StateExperimentManagerTextTest extends LttngTestPreparation {

	/**
	 * Test method for {@link org.eclipse.linuxtools.lttng.state.experiment.StateExperimentManager#experimentSelected_prep(org.eclipse.linuxtools.tmf.experiment.TmfExperiment)}.
	 */
	public void testExperimentSelected_text() {
		// make sure a TmfExperiment instance is registered as provider and
		// selected as current
		TmfExperiment<LttngEvent> experiment = prepareTextExperimentToTest();

		// Create a new Experiment manager
		IStateExperimentManager expManager = StateManagerFactory
				.getExperimentManager();
		expManager.waitForCompletion(true);
		// Configure the interval to create check points so this can be tested
		// with medium size files i.e. default is 50000 events
		StateManagerFactory.setTraceCheckPointInterval(1000L);

		// preparation
		expManager.experimentSelected_prep(experiment);
		// Action trigger
		expManager.experimentSelected(this, experiment);

		// Access context tree for Validation
		// access to the context tree
		LTTngTreeNode experimentNode = expManager.getSelectedExperiment();
		StateTraceManager traceManager = (StateTraceManager) experimentNode
				.getChildById(0L);

		// validate
		int numProcesses = traceManager.getStateModel().getProcesses().length;
		assertEquals("Total number of processes created", 276, numProcesses);

	}

	/**
	 * Test method for
	 * {@link org.eclipse.linuxtools.lttng.state.experiment.StateExperimentManager#experimentSelected_prep(org.eclipse.linuxtools.tmf.experiment.TmfExperiment)}
	 * .
	 */
	public void testExperimentSelected_real() {
		// Create a new Experiment manager context
		IStateExperimentManager expManager = prepareExperimentContext(true);
		expManager.waitForCompletion(true);

		// make sure a TmfExperiment instance is registered as provider and
		// selected as current
		TmfExperiment<LttngEvent> experiment = prepareExperimentToTest();

		// preparation
		expManager.experimentSelected_prep(experiment);
		// Action trigger
		expManager.experimentSelected(this, experiment);

		// Access context tree for Validation
		// access to the context tree
		LTTngTreeNode experimentNode = expManager.getSelectedExperiment();
		StateTraceManager traceManager = (StateTraceManager) experimentNode
				.getChildById(0L);

		// validate
		int numProcesses = traceManager.getStateModel().getProcesses().length;
		assertEquals("Total number of processes created", 276, numProcesses);

	}

}
