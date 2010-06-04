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
package org.eclipse.linuxtools.lttng.state.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.model.LTTngTreeNode;
import org.eclipse.linuxtools.lttng.request.ILttngSyntEventRequest;
import org.eclipse.linuxtools.lttng.request.IRequestStatusListener;
import org.eclipse.linuxtools.lttng.request.LttngSyntEventRequest;
import org.eclipse.linuxtools.lttng.state.LttngStateException;
import org.eclipse.linuxtools.lttng.state.evProcessor.ITransEventProcessor;
import org.eclipse.linuxtools.lttng.state.evProcessor.state.StateEventToHandlerFactory;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.state.model.StateModelFactory;
import org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext;
import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfLocation;

public class StateTraceManager extends LTTngTreeNode implements IStateTraceManager, ILttngStateContext {

	// constants
	private static final long DEFAULT_OFFSET = 0L;
	private static final int DEFAULT_CHUNK = 1;

	// configurable check point interval
	private static final long LTTNG_CHECK_POINT_INTERVAL = 15000L;
	private long fcheckPointInterval = LTTNG_CHECK_POINT_INTERVAL;

	private TmfExperiment<LttngEvent> fExperiment = null;

	// immutable Objects
	private final ITmfTrace fTrace;
	private int fcpuNumber = -1;
	private final ITransEventProcessor fStateUpdateProcessor;

	// potentially thread shared
	private final HashMap<Long, LttngTraceState> stateCheckpointsList = new HashMap<Long, LttngTraceState>();
	private final Vector<TmfCheckpoint> timestampCheckpointsList = new Vector<TmfCheckpoint>();
	private LttngTraceState fStateModel;
	private LttngTraceState fCheckPointStateModel;

	// locks
	private Object checkPointsLock = new Object();


	
	// =======================================================================
	// Constructor
	// =======================================================================
	/**
	 * @param id
	 * @param parent
	 * @param name
	 * @param trace
	 * @throws LttngStateException
	 */
	public StateTraceManager(Long id, LTTngTreeNode parent, String name, ITmfTrace trace) throws LttngStateException {
		super(id, parent, name, trace);

		if (trace == null) {
			throw new LttngStateException("No TmfTrace object available!");
		}

		fTrace = trace;
		fStateUpdateProcessor = StateEventToHandlerFactory.getInstance();

		init();

		fStateModel = StateModelFactory.getStateEntryInstance(this);
		fStateModel.init(this);

		fCheckPointStateModel = StateModelFactory.getStateEntryInstance(this);
		fCheckPointStateModel.init(this);
	}

	// =======================================================================
	// Methods
	// =======================================================================
	@SuppressWarnings("unchecked")
	private void init() {
		// resolve the experiment
		Object obj = getParent().getValue();
		if (obj != null && obj instanceof TmfExperiment<?>) {
			fExperiment = (TmfExperiment<LttngEvent>) obj;
		}

		// initialize the number of cpus
		if (fTrace instanceof LTTngTrace) {
			fcpuNumber = ((LTTngTrace) fTrace).getCpuNumber();
		} else if (fTrace instanceof LTTngTextTrace) {
			fcpuNumber = ((LTTngTextTrace) fTrace).getCpuNumber();
		}
	}
	




	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.IStateManager#getEventLog()
	 */
	public ITmfTrace getTrace() {
		return fTrace;
	}

	/**
	 * Save a checkpoint if it is needed at that point
	 * <p>
	 * The function will use "eventCount" internally to determine if a save was
	 * needed
	 * 
	 * @param eventCounter
	 *            The event "count" or event "id" so far
	 * @param eventTime
	 *            The timestamp of this event
	 * 
	 * @return boolean True if a checkpoint was saved, false otherwise
	 */
	private void saveCheckPointIfNeeded(Long eventCounter, TmfTimestamp eventTime) {
		// Save a checkpoint every LTTNG_STATE_SAVE_INTERVAL event
		if ((eventCounter.longValue() % fcheckPointInterval) == 0) {
			LttngTraceState stateCheckPoint;
			synchronized (fCheckPointStateModel) {
				stateCheckPoint = fCheckPointStateModel.clone();
			}

			TraceDebug.debug("Check point created here: " + eventCounter
					+ " -> " + eventTime.toString() + "************"
					+ getTrace().getName() + "   >>>>> Thread: "
					+ Thread.currentThread().getId());

			synchronized (checkPointsLock) {
				// Save the checkpoint
				stateCheckpointsList.put(eventCounter, stateCheckPoint);
				// Save correlation between timestamp and checkpoint index

				timestampCheckpointsList.add(new TmfCheckpoint(new TmfTimestamp(eventTime), new TmfLocation<Long>(
						eventCounter)));
			}
		}
	}

	/**
	 * @return the lttng_check_point_interval
	 */
	public long getCheckPointInterval() {
		return fcheckPointInterval;
	}

	/**
	 * @param check_point_interval
	 *            , the lttng_check_point_interval to set
	 */
	public void setCheckPointInterval(long check_point_interval) {
		this.fcheckPointInterval = check_point_interval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#
	 * restoreCheckPointByTimestamp
	 * (org.eclipse.linuxtools.tmf.event.TmfTimestamp)
	 */
	@SuppressWarnings("unchecked")
	public TmfTimestamp restoreCheckPointByTimestamp(TmfTimestamp eventTime) {
		TmfTimeRange experimentRange = fExperiment.getTimeRange();
		TmfTimestamp nearestTimeStamp = fTrace.getStartTime();

		// The GUI can have time limits higher than this log, since GUI can
		// handle multiple logs
		if ((eventTime.getValue() < 0) || (eventTime.getValue() > experimentRange.getEndTime().getValue())) {
			return null;
		}

		// The GUI can have time limits lower than this trace, since experiment
		// can handle multiple traces
		if ((eventTime.getValue() < fTrace.getStartTime().getValue())) {
			eventTime = fTrace.getStartTime();
		}

		Collections.sort(timestampCheckpointsList);
		// Initiate the compare with a checkpoint containing the target time
		// stamp to find
		int index = Collections.binarySearch(timestampCheckpointsList, new TmfCheckpoint(eventTime,
				new TmfLocation<Long>(0L)));
		// adjust index to round down to earlier checkpoint when exact match
		// not
		// found
		index = getPrevIndex(index);

		LttngTraceState traceState;
		if (index == 0) {
			// No checkpoint restore is needed, start with a brand new
			// TraceState
			traceState = StateModelFactory.getStateEntryInstance(this);
		} else {
			synchronized (checkPointsLock) {
				// Useful CheckPoint found
				TmfCheckpoint checkpoint = timestampCheckpointsList.get(index);
				nearestTimeStamp = checkpoint.getTimestamp();
				// get the location associated with the checkpoint
				TmfLocation<Long> location = (TmfLocation<Long>) checkpoint.getLocation();
				// reference a new copy of the checkpoint template
				traceState = stateCheckpointsList.get(location.getLocation()).clone();
			}
		}

		// Restore the stored traceState
		synchronized (this) {
			fStateModel = traceState;
		}

		return nearestTimeStamp;
	}

	/**
	 * Adjust the result from a binary search to the round down position
	 * 
	 * @param position
	 *            if Negative is: (-(insertion point) -1)
	 * @return position or if no match found, earlier than insertion point
	 */
	private int getPrevIndex(int position) {
		int roundDownPosition = position;
		if (position < 0) {
			roundDownPosition = -(position + 2);
		}

		roundDownPosition = roundDownPosition < 0 ? 0 : roundDownPosition;
		return roundDownPosition;
	}

	// TODO: Remove this request type when the UI handle their own requests
	/**
	 * Request Event data of a specified time range
	 * 
	 * @param timeWindow
	 * @param listener
	 * @param processor
	 * @return ILttngEventRequest The request made
	 */
	ILttngSyntEventRequest getDataRequestByTimeRange(TmfTimeRange timeWindow, IRequestStatusListener listener,
			final ITransEventProcessor processor) {

		ILttngSyntEventRequest request = new StateTraceManagerRequest(timeWindow, DEFAULT_OFFSET,
				TmfDataRequest.ALL_DATA, DEFAULT_CHUNK, listener, getExperimentTimeWindow(), processor) {
		};

		return request;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#getStateModel
	 * ()
	 */
	public LttngTraceState getStateModel() {
		synchronized (fStateModel) {
			return fStateModel;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#
	 * getCheckPointStateModel()
	 */
	public LttngTraceState getCheckPointStateModel() {
		synchronized (fStateModel) {
			return fCheckPointStateModel;
		}
	}

	/**
	 * @return the stateCheckpointsList
	 */
	HashMap<Long, LttngTraceState> getStateCheckpointsList() {
		return stateCheckpointsList;
	}

	/**
	 * @return the timestampCheckpointsList
	 */
	Vector<TmfCheckpoint> getTimestampCheckpointsList() {
		return timestampCheckpointsList;
	}
	// =======================================================================
	// Inner Class
	// =======================================================================
	class StateTraceManagerRequest extends LttngSyntEventRequest {
		// =======================================================================
		// Data
		// =======================================================================
		final TmfEvent[] evt = new TmfEvent[1];
		final ITransEventProcessor fprocessor;
		LttngSyntheticEvent synEvent;
		Long fCount = getSynEventCount();

		// =======================================================================
		// Constructor
		// =======================================================================
		public StateTraceManagerRequest(TmfTimeRange range, long offset, int nbEvents, int maxBlockSize,
				IRequestStatusListener listener, TmfTimeRange experimentTimeRange, ITransEventProcessor processor) {

			super(range, offset, nbEvents, maxBlockSize, listener, experimentTimeRange, processor);
			fprocessor = processor;
			TraceDebug.debug("Instance created for range: " + range.toString());
			fCount = 0L;
		}

		// =======================================================================
		// Methods
		// =======================================================================
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.linuxtools.lttng.request.LttngSyntEventRequest#handleData
		 * ()
		 */
		@Override
		public void handleData() {
			TmfEvent[] result = getData();

			evt[0] = (result.length > 0) ? result[0] : null;
			if (evt[0] != null) {
				synEvent = (LttngSyntheticEvent) evt[0];
				if (synEvent.getSynType() == SequenceInd.AFTER) {
					// Note : We call this function before incrementing
					// eventCount to save a default check point at the "0th"
					// event
					saveCheckPoint(fCount, synEvent.getTimestamp());
					fCount++;

					if (TraceDebug.isDEBUG()) {
						if (fCount % 1000 == 0) {
							TraceDebug.debug("handled: " + fCount + " sequence: " + synEvent.getSynType());
						}
					}
				}
			}
		}

		/**
		 * To be overridden by active save e.g. check points, this no action
		 * default is used for requests which do not require rebuilding of
		 * checkpoints e.g. requiring data of a new time range selection
		 * 
		 * @param count
		 * @param time
		 */
		public void saveCheckPoint(Long count, TmfTimestamp time) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#
	 * getNumberOfCpus()
	 */
	public int getNumberOfCpus() {
		return fcpuNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#
	 * getTraceTimeWindow()
	 */
	public TmfTimeRange getTraceTimeWindow() {
		if (fTrace != null) {
			return fTrace.getTimeRange();

		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#getTraceId
	 * ()
	 */
	public String getTraceId() {
		if (fTrace != null) {
			return fTrace.getName();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#
	 * getExperimentTimeWindow()
	 */
	public TmfTimeRange getExperimentTimeWindow() {
		if (fExperiment != null) {
			return fExperiment.getTimeRange();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#getExperimentName
	 * ()
	 */
	public String getExperimentName() {
		return fExperiment.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.resource.ILttngStateContext#getTraceIdRef
	 * ()
	 */
	public ITmfTrace getTraceIdRef() {
		return fTrace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#clearCheckPoints
	 * ()
	 */
	public void clearCheckPoints() {
		synchronized (checkPointsLock) {
			stateCheckpointsList.clear();
			timestampCheckpointsList.clear();

			fCheckPointStateModel = StateModelFactory.getStateEntryInstance(this);
			try {
				fCheckPointStateModel.init(this);
			} catch (LttngStateException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.state.trace.IStateTraceManager#handleEvent
	 * (org.eclipse.linuxtools.lttng.event.LttngSyntheticEvent, java.lang.Long)
	 */
	public void handleEvent(LttngSyntheticEvent synEvent, Long eventCount) {
		fStateUpdateProcessor.process(synEvent, fCheckPointStateModel);

		// Save checkpoint as needed
		saveCheckPointIfNeeded(eventCount - 1, synEvent.getTimestamp());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("\n\tTotal number of processes in the Shared State model: " + fStateModel.getProcesses().length
				+ "\n\t" + "Total number of processes in the Check point State model: "
				+ fCheckPointStateModel.getProcesses().length);

		TmfTimeRange traceTRange = fTrace.getTimeRange();
		sb.append("\n\tTrace time interval for trace " + fTrace.getName() + "\n\t"
				+ new LttngTimestamp(traceTRange.getStartTime()));
		sb.append(" - " + new LttngTimestamp(traceTRange.getEndTime()));
		sb.append("\n\tCheckPoints available at: ");
		for (TmfCheckpoint cpoint : timestampCheckpointsList) {
			sb.append("\n\t" + "Location: " + cpoint.getLocation() + " - " + cpoint.getTimestamp());
		}

		return sb.toString();
	}

}
