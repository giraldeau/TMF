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

package org.eclipse.linuxtools.lttng.state.experiment;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNode;
import org.eclipse.linuxtools.lttng.state.LttngStateException;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.state.model.StateModelFactory;
import org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.lttng.state.trace.StateTraceManager;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

/**
 * @author alvaro
 * 
 */
public class StateManagerFactory {
	// ========================================================================
	// Data
	// =======================================================================

	private static IStateExperimentManager experimentManager = null;
	/**
	 * Allows to modify the check point interval for every new instance of trace manager
	 */
	private static Long ftraceCheckPointInterval = null;

	static {
		initCheck();
	}
	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * @param traceUniqueId
	 * @param experiment
	 * @return
	 */
	public static LTTngTreeNode getManager(ITmfTrace rtrace,
			LTTngTreeNode experiment) {

		// Validate
		if (rtrace == null) {
			return null;
		}

		String traceUniqueId = rtrace.getName();
		if (traceUniqueId == null) {
			return null;
		}


		LTTngTreeNode managerNode = null;
		managerNode = experiment.getChildByName(traceUniqueId);

		if (managerNode != null && managerNode instanceof IStateTraceManager) {
			return managerNode;
		}

		LttngTraceState traceModel = StateModelFactory.getStateEntryInstance();
		StateTraceManager manager = null;

		// catch potential construction problems
		try {
			manager = new StateTraceManager(experiment.getNextUniqueId(),
					experiment, traceUniqueId, rtrace, traceModel,
					LttngCoreProviderFactory.getEventProvider());

			// Allow the possibility to configure the trace state check point
			// interval at creation time
			if (ftraceCheckPointInterval != null) {
				manager.setCheckPointInterval(ftraceCheckPointInterval);
			}

		} catch (LttngStateException e) {
			e.printStackTrace();
		}

		experiment.addChild(manager);
		return manager;
	}

	/**
	 * Provide the State trace set manager
	 * 
	 * @return
	 */
	public static IStateExperimentManager getExperimentManager() {
		return experimentManager;
	}

	/**
	 * Remove previously registered managers
	 * 
	 * @param traceUniqueId
	 */
	public static void removeManager(ITmfTrace rtrace, LTTngTreeNode rexperiment) {
		Object experimentObj = rexperiment.getValue();
		if (rtrace != null && rexperiment != null
				&& experimentObj instanceof TmfExperiment<?>) {
			LTTngTreeNode childToremove = rexperiment.getChildByName(rtrace
					.getName());
			if (childToremove != null) {
				rexperiment.removeChild(childToremove);
			}
		} else {
			TraceDebug.debug("Invalid arguments to remove manager for trace: "
					+ rtrace.getName());
		}
	}

	/**
	 * initialization of factory
	 */
	private static void initCheck() {
		if (experimentManager == null) {
			Long id = 0L; // unique id
			String name = "StateExperimentManager"; // name
			experimentManager = new StateExperimentManager(id, name);
		}
	}

	/**
	 * Clea up resources
	 */
	public static void dispose() {
		if (experimentManager != null) {
			experimentManager = null;
		}
	}

	/**
	 * @return the traceCheckPointInterval
	 */
	public static Long getTraceCheckPointInterval() {
		return ftraceCheckPointInterval;
	}

	/**
	 * @param traceCheckPointInterval
	 *            the traceCheckPointInterval to set
	 */
	public static void setTraceCheckPointInterval(Long traceCheckPointInterval) {
		StateManagerFactory.ftraceCheckPointInterval = traceCheckPointInterval;
	}
}