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
package org.eclipse.linuxtools.lttng.ui.views.resources.evProcessor;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.StateStrings.Events;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;

/**
 * Creates instances of specific after state update handlers, per corresponding
 * event.
 * 
 * @author alvaro
 * 
 */
public class ResourcesTRangeAfterUpdateHandlers {

	/**
	 * <p>
	 * Handles: LTT_EVENT_SCHED_SCHEDULE
	 * </p>
	 * Replace C function named "after_schedchange_hook" in eventhooks.c
	 * <p>
	 * Fields: LTT_FIELD_PREV_PID, LTT_FIELD_NEXT_PID, LTT_FIELD_PREV_STATE
	 * </p>
	 * 
	 * @return
	 */
	final IEventProcessing getAfterSchedChangeHandler() {
		AbsResourcesTRangeUpdate handler = new AbsResourcesTRangeUpdate() {

			private Events eventType = Events.LTT_EVENT_SCHED_SCHEDULE;

			public boolean process(LttngEvent trcEvent, LttngTraceState traceSt) {

				// TODO: After sched scheduler handler should implement an
				// update to the current resource data, similar to
				// current_hash_data in C
				// We don't keep track of current hashed resource, we look in
				// the hash table every time. keeping track of current hash may
				// improve performance, although needs to be bench marked to
				// verify
				// if there's is a real gain.

				// process_list->current_hash_data[trace_num][process_in->cpu] =
				// hashed_process_data_in;

				return false;
			}

			// @Override
			public Events getEventHandleType() {
				return eventType;
			}
		};

		return handler;
	}

	/**
	 * Drawing stuff ?
	 */
	// int after_request(void *hook_data, void *call_data)
	// int after_chunk(void *hook_data, void *call_data)
	// int before_statedump_end(void *hook_data, void *call_data)
}
