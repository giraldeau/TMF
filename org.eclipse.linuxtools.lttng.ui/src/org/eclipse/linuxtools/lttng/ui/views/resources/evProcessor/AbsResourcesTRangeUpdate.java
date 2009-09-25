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

import java.util.List;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.state.evProcessor.IEventProcessing;
import org.eclipse.linuxtools.lttng.state.model.LttngProcessState;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeComponent;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEvent;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeResourceFactory;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEvent.Type;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource.ResourceTypes;
import org.eclipse.linuxtools.lttng.ui.views.common.AbsTRangeUpdate;
import org.eclipse.linuxtools.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.lttng.ui.views.resources.model.ResourceContainer;
import org.eclipse.linuxtools.lttng.ui.views.resources.model.ResourceModelFactory;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

public abstract class AbsResourcesTRangeUpdate extends AbsTRangeUpdate
		implements IEventProcessing {

	// ========================================================================
	// Data
	// =======================================================================
	protected ResourceContainer resContainer = ResourceModelFactory
			.getResourceContainer();
	protected ParamsUpdater params = ResourceModelFactory.getParamsUpdater();
	protected static final Long ANY_CPU = 0L;

	// ========================================================================
	// Methods
	// =======================================================================
	protected TimeRangeEventResource addLocalResource(long traceStartTime,
			long traceEndTime, String traceId, ResourceTypes type, Long resId) {

		String resourceName = type.toString() + " " + resId.toString();
		// Note : the "traceid" here is assigned to the "groupname" as we group
		// by trace in the UI
		TimeRangeEventResource localRessource = TimeRangeResourceFactory
				.getInstance()
				.createResource(
				resContainer.getUniqueId(), traceStartTime, traceEndTime,
				resourceName, traceId, "", type, resId);
		resContainer.addResource(localRessource);
		return localRessource;
	}

	/**
	 * Used to check if the event is visible within the current visible time
	 * window
	 * 
	 * @return
	 */
	protected boolean withinViewRange(long stime, long etime) {
		long windowStartTime = params.getStartTime();
		long windowEndTime = params.getEndTime();

		// start time is within window
		if (stime >= windowStartTime && stime <= windowEndTime) {
			// The event or part of it shall be displayed.
			return true;
		}

		// end time is within window
		if (etime >= windowStartTime && etime <= windowEndTime) {
			// The event or part of it shall be displayed.
			return true;
		}

		// check that a portion is within the window
		if (stime < windowStartTime && etime > windowEndTime) {
			// The time range is bigger than the selected time window and
			// crosses it
			return true;
		}

		return false;
	}

	public LttngProcessState lttv_state_find_process(
			LttngTraceState traceState, Long cpu, Long pid) {
		// Define the return value
		LttngProcessState returnedProcess = null;

		// Obtain the list of available processes
		List<LttngProcessState> processList = traceState.getProcesses();

		int pos = 0;
		while ((pos < processList.size()) && (returnedProcess == null)) {
			if (processList.get(pos).getPid().equals(pid)) {
				if ((processList.get(pos).getCpu().equals(cpu))
						|| (cpu.longValue() == 0L)) {
					returnedProcess = processList.get(pos);
				}
			}

			pos++;
		}

		return returnedProcess;
	}

	public TimeRangeEventResource resourcelist_obtain_bdev(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.BDEV,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_trap(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.TRAP,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_irq(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.IRQ,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_soft_irq(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.SOFT_IRQ,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_cpu(
			LttngTraceState traceState, Long resourceId) {
		return resourcelist_obtain_generic(resourceId, ResourceTypes.CPU,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_machine(
			LttngTraceState traceState, Long resourceId) {
		// *** VERIFY ***
		// Does "UNKNOWN" make sense for "obtain_machine" ?
		// It seems to be the only choice, thought...
		return resourcelist_obtain_generic(resourceId, ResourceTypes.UNKNOWN,
				traceState.getTraceId());
	}

	public TimeRangeEventResource resourcelist_obtain_generic(Long resourceId,
			ResourceTypes resourceType, String traceId) {
		return resContainer.findResource(resourceId, resourceType, traceId);
	}

	protected boolean globalProcessBeforeExecmode(LttngEvent trcEvent,
			LttngTraceState traceSt) {

		// TODO: Implement the tracking of current resource in order ot speed up
		// searching for the relevant resource similar to current_hash_data in
		// the C implementation
		// e.g.
		// hashed_process_data =
		// process_list->current_hash_data[trace_num][cpu];

		TimeRangeEventResource localResource = resourcelist_obtain_cpu(traceSt,
				trcEvent.getCpuId());
		Long cpu = trcEvent.getCpuId();
		if (localResource == null) {
			TmfTimeRange timeRange = traceSt.getInputDataRef()
					.getTraceTimeWindow();
			localResource = addLocalResource(timeRange.getStartTime()
					.getValue(), timeRange.getEndTime().getValue(), traceSt
					.getTraceId(), ResourceTypes.CPU, cpu);
		}

		// get the start time
		long stime = localResource.getNext_good_time();
		// Get the resource state mode
		String cpuStateMode = traceSt.getCpu_states().get(cpu)
				.peekFromCpuStack().getInName();
		// Call the makeDraw function
		makeDraw(traceSt, stime, trcEvent.getTimestamp().getValue(),
				localResource, params, cpuStateMode);

		return false;
	}

	// *** FIXME ***
	// "stateMode" should NOT be a string, it is very confusing to use (can we
	// use any string? what kind of string? can it be null??)
	// It should be a "ProcessStatus" or "ExecutionMode". However this mean
	// refactoring this part.
	protected boolean makeDraw(LttngTraceState traceSt, long startTime,
			long endTime, TimeRangeEventResource localResource,
			ParamsUpdater params, String stateMode) {

		// Check if the time range is consistent.
		if (endTime < startTime) {
			params.incrementEventsDiscardedWrongOrder();
			return false;
		}

		// Determine start and end times to establish duration
		long stime = startTime;
		long windowEndTime = params.getEndTime();
		long etime = endTime < windowEndTime ? endTime : windowEndTime;

		if (etime < stime || !withinViewRange(stime, etime)) {
			// No use to process the event since it's outside
			// the visible time range of the window
			params.incrementEventsDiscarded();
			return false;
		}

		// Determine if the time range event will fit it the current
		// pixel map
		double duration = etime - stime;
		double k = getPixelsPerNs(traceSt, params);
		double pixels = duration * k;

		// ***VERIFY***
		// Is all this equivalent to this call in C??
		// if(ltt_time_compare(hashed_process_data->next_good_time,evtime) > 0)
		// ***
		// Visibility check
		// Display a "more information" indication by allowing non visible event
		// as long as its previous event is visible.
		boolean visible = true;
		if (pixels < 1) {
			boolean prevEventVisibility = true;
			// Get the visibility indication on previous event for
			// this process
			Vector<TimeRangeComponent> inMemEvents = localResource
					.getTraceEvents();
			if (inMemEvents.size() != 0) {
				TimeRangeComponent prevEvent = inMemEvents.get(inMemEvents
						.size() - 1);
				prevEventVisibility = prevEvent.isVisible();

				// ***VERIFY***
				// This replace all C Call like this one ?
				// #ifdef EXTRA_CHECK if(ltt_time_compare(evtime,
				// time_window.start_time) == -1 || ltt_time_compare(evtime,
				// time_window.end_time) == 1)

				// if previous event visibility is false and the time span
				// between events less than two pixels, there is no need to
				// load it in memory i.e. not visible and a more indicator is
				// within two pixels.
				// return i.e. event discarded to free up memory
				Long eventSpan = stime - prevEvent.getStartTime();
				if (prevEventVisibility == false
						&& ((double) eventSpan * k) < 2) {
					params.incrementEventsDiscarded();
					return false;
				}
			}

			// if previous event is visible, set this one to not
			// visible and continue
			visible = false;
		}

		Type eventType = getEventType(localResource);
		if (eventType != null) {
			// Create the time-range event
			// *** VERIFY ***
			// This should replace this C call, right?
			// TimeWindow time_window =
			// lttvwindow_get_time_window(control_flow_data->tab);
			TimeRangeEvent time_window = new TimeRangeEvent(stime, etime,
					localResource, eventType, stateMode);

			// *** VERIFY ***
			// This is added to replace the multiple draw and gtk/glib command
			// but
			// I'm not sure about it
			time_window.setVisible(visible);
			localResource.addChildren(time_window);
			// Store the next good time to start drawing the event.
			localResource.setNext_good_time(etime);
			// *** VERIFY ***
			// Missing checks like this one?
			// #ifdef EXTRA_CHECK if(ltt_time_compare(evtime,
			// time_window.start_time) == -1 || ltt_time_compare(evtime,
			// time_window.end_time) == 1)
		}

		return false;
	}

	/**
	 * Convert between resource type and timeRange event type
	 * 
	 * @param resource
	 * @return
	 */
	private Type getEventType(TimeRangeEventResource resource) {
		// TODO: Can we merge into one type
		ResourceTypes resType = resource.getType();
		Type eventType = null;

		switch (resType) {
		case CPU:
			eventType = Type.CPU_MODE;
			break;
		case IRQ:
			eventType = Type.IRQ_MODE;
			break;
		case SOFT_IRQ:
			eventType = Type.SOFT_IRQ_MODE;
			break;
		case TRAP:
			eventType = Type.TRAP_MODE;
			break;
		case BDEV:
			eventType = Type.BDEV_MODE;
			break;
		default:
			eventType = Type.PROCESS_MODE;
			break;
		}

		return eventType;
	}

}