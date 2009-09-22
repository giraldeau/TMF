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
package org.eclipse.linuxtools.lttng.state.evProcessor.state;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.lttng.state.StateStrings;
import org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;

/**
 * Builds a Map from string event name to a processing handler object, the
 * processors implement the same interface to facilitate transparent methods
 * call,
 * 
 * The map key STring is the entry point of the raw events, using a hash speeds
 * up the resolution of the appropriate processor
 * 
 * @author alvaro
 * 
 */
public class StateUpdateFactory extends AbsEventProcessorFactory {
	// ========================================================================
	// Data
	// =======================================================================
	private final Map<String, IEventProcessing> eventNametoProcessor = new HashMap<String, IEventProcessing>();
	private static StateUpdateFactory instance = new StateUpdateFactory();
	private StateUpdateHandlers instantiateHandler = new StateUpdateHandlers();

	// ========================================================================
	// Constructors
	// =======================================================================
	private StateUpdateFactory() {
		//create one instance of each individual event handler and add the instance to the map
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SYSCALL_ENTRY
				.getInName(), instantiateHandler.getSyscallEntryHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SYSCALL_EXIT
				.getInName(), instantiateHandler.getsySyscallExitHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_ENTRY
				.getInName(), instantiateHandler.getTrapEntryHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_TRAP_EXIT
				.getInName(), instantiateHandler.getTrapExitHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_ENTRY
				.getInName(), instantiateHandler.getTrapEntryHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_EXIT
				.getInName(), instantiateHandler.getTrapExitHandler());

		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY
				.getInName(), instantiateHandler.getTrapEntryHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PAGE_FAULT_NOSEM_EXIT
				.getInName(), instantiateHandler.getTrapExitHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_ENTRY
				.getInName(), instantiateHandler.getIrqEntryHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_IRQ_EXIT
				.getInName(), instantiateHandler.getIrqExitHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_RAISE
				.getInName(), instantiateHandler.getSoftIrqRaiseHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_ENTRY
				.getInName(), instantiateHandler.getSoftIrqEntryHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SOFT_IRQ_EXIT
				.getInName(), instantiateHandler.getSoftIrqExitHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_LIST_INTERRUPT
				.getInName(), instantiateHandler.getEnumInterruptHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_REQUEST_ISSUE
				.getInName(), instantiateHandler.getBdevRequestIssueHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_REQUEST_COMPLETE
				.getInName(), instantiateHandler.getBdevRequestCompleteHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_FUNCTION_ENTRY
				.getInName(), instantiateHandler.getFunctionEntryHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_FUNCTION_EXIT
				.getInName(), instantiateHandler.getFunctionExitHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SYS_CALL_TABLE
				.getInName(), instantiateHandler.getDumpSyscallHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_KPROBE_TABLE
				.getInName(), instantiateHandler.getDumpKprobeHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SOFTIRQ_VEC
				.getInName(), instantiateHandler.getDumpSoftIrqHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_SCHED_SCHEDULE
				.getInName(), instantiateHandler.getSchedChangeHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_FORK
				.getInName(), instantiateHandler.getProcessForkHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_KTHREAD_CREATE
				.getInName(), instantiateHandler.getProcessKernelThreadHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_EXIT
				.getInName(), instantiateHandler.getProcessExitHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_FREE
				.getInName(), instantiateHandler.getProcessFreeHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_EXEC
				.getInName(), instantiateHandler.getProcessExecHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_THREAD_BRAND
				.getInName(), instantiateHandler.GetThreadBrandHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_STATEDUMP_END
				.getInName(), instantiateHandler.getStateDumpEndHandler());
		
		eventNametoProcessor.put(StateStrings.Events.LTT_EVENT_PROCESS_STATE
				.getInName(), instantiateHandler.getEnumProcessStateHandler());


	}

	// ========================================================================
	// Public methods
	// =======================================================================
	/**
	 * The event processors are common to all traces an multiple instances will
	 * use more memory unnecessarily
	 * 
	 * @return
	 */
	public static AbsEventProcessorFactory getInstance() {
		if (instance == null) {
			instance = new StateUpdateFactory();
		}
		return instance;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory#getAfterProcessor(java.lang.String)
	 */
	@Override
	public IEventProcessing getAfterProcessor(String eventType) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.state.evProcessor.AbsEventProcessorFactory#getBeforeProcessor(java.lang.String)
	 */
	@Override
	public IEventProcessing getBeforeProcessor(String eventType) {
		return null;
	}
	
	/**
	 * This is the only event handler to update the State provider
	 * @return 
	 * 
	 */
	public IEventProcessing getStateUpdaterProcessor(String eventType) {
		return eventNametoProcessor.get(eventType);
	}

	@Override
	public IEventProcessing getfinishProcessor() {
		// No finishing processor used
		return null;
	}
}
