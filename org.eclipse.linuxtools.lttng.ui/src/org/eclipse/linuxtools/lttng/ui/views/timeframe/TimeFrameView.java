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

package org.eclipse.linuxtools.lttng.ui.views.timeframe;

import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.trace.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

/**
 * <b><u>TimeFrameView</u></b>
 * <p>
 * The TimeFrameView provides a set of spinners to monitor and set the start
 * time, end time, the current time interval and current time of the trace
 * set at the nanosecond level.
 * <p>
 * It ensures that the following relations are always true:
 * <p>
 * <li>[ startTime >= start time of the trace ]
 * <li>[ endTime <= end time of the trace ]
 * <li>[ startTime <= currentTime <= endTime ]
 * <li>[ interval == (endTime - startTime) ]
 * </li>
 * <p>
 * It provides a slider to rapidly set the current time within the time range
 * (i.e. between startTime and endTime).
 * <p>
 * Finally, it allows modification of the time range and the current time. This
 * triggers notifications to the other LTTng views.
 * <p>
 * FIXME: The slider is very jumpy due to the large number of async updates
 * FIXME: Revisit the control flow between View, Spinners and Slider
 */
public class TimeFrameView extends TmfView {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.timeframe";

    // ========================================================================
    // TimeFrameView
    // ========================================================================

    // The event log timestamp characteristics
    private TmfTimestamp  fTraceStartTime = new TmfTimestamp();
    private TmfTimestamp  fTraceEndTime   = new TmfTimestamp();

    private TmfTimestamp  fCurrentTime   = new TmfTimestamp();
    
    private TmfTimeRange fTraceTimeRange = new TmfTimeRange(fTraceStartTime, fTraceEndTime);
    private TmfTimeRange fTraceSpan      = new TmfTimeRange(fTraceStartTime, fTraceEndTime);
    private byte fScale = 0;

    // Labels
    private static final String START_TIME_LABEL   = "Start Time";
    private static final String END_TIME_LABEL     = "End Time";
    private static final String TIME_RANGE_LABEL   = "Interval";
    private static final String CURRENT_TIME_LABEL = "Current Time";

    private static final int SLIDER_RANGE = 10000;

    private SpinnerGroup fStartGroup;
    private SpinnerGroup fEndGroup;
    private SpinnerGroup fRangeGroup;
    private SpinnerGroup fCurrentGroup;

    // The slider
    private Slider fSlider;

    // The current experiment
    TmfExperiment fExperiment = null;

    /**
     * Constructor
     */
    public TimeFrameView() {
    	super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
	@Override
	public void createPartControl(Composite parent) {

        // Set the view layout
        GridLayout layout = new GridLayout(4, true);
        parent.setLayout(layout);

        fStartGroup   = new SpinnerGroup(this, parent, START_TIME_LABEL,   fTraceTimeRange, fTraceStartTime);
        fEndGroup     = new SpinnerGroup(this, parent, END_TIME_LABEL,     fTraceTimeRange, fTraceEndTime);
        fRangeGroup   = new SpinnerGroup(this, parent, TIME_RANGE_LABEL,   fTraceTimeRange, fTraceEndTime);
        fCurrentGroup = new SpinnerGroup(this, parent, CURRENT_TIME_LABEL, fTraceTimeRange, fTraceStartTime);

        // Create the slider
        createSlider(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
	@Override
	public void setFocus() {
        // TODO Auto-generated method stub
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /**
     * One of the spinners has been updated. Synchronize the other widgets.
     */
    public void synchTimeFrameWidgets(SpinnerGroup trigger) {

        // Collect the data
        TmfTimestamp startTime   = fStartGroup.getCurrentTime();
        TmfTimestamp endTime     = fEndGroup.getCurrentTime();
        TmfTimestamp timeRange   = fRangeGroup.getCurrentTime();
        TmfTimestamp currentTime = fCurrentGroup.getCurrentTime();

        // If startTime was set beyond endTime, adjust endTime and interval
        if (trigger == fStartGroup) {
            if (startTime.compareTo(endTime, false) > 0) {
                endTime = startTime;
            }
        }

        // If endTime was set beyond startTime, adjust startTime and interval
        if (trigger == fEndGroup) {
            if (endTime.compareTo(startTime, false) < 0) {
                startTime = endTime;
            }
        }

        // If timeRange was set, adjust endTime
        if (trigger == fRangeGroup) {
            long start = startTime.getValue();
            long span  = timeRange.getValue();
            TmfTimestamp ts = new TmfTimestamp(start + span, startTime.getScale(), 0);
            if (ts.compareTo(fTraceEndTime, false) > 0) {
                ts = fTraceEndTime.synchronize(fTraceEndTime.getValue(), startTime.getScale());
            }
            endTime = ts;
        }

        // Compute the new time range
        TmfTimeRange subrange = new TmfTimeRange(startTime, endTime);
        TmfTimestamp interval = new TmfTimestamp(startTime.getAdjustment(endTime), startTime.getScale(), 0);

        // Update the spinner groups
        fStartGroup.setContent(fTraceTimeRange, startTime);
        fEndGroup.setContent(fTraceTimeRange, endTime);
        fRangeGroup.setContent(fTraceSpan, interval);
        fCurrentGroup.setContent(subrange, currentTime);

        updateSlider(subrange, currentTime);

        // Notify other views
        if (!fCurrentTime.equals(currentTime)) {
            fCurrentTime = currentTime;
            TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(this, currentTime));
        }
    }

    // ========================================================================
    // Slider Handling
    // ========================================================================

    /**
     * @param parent
     */
    private void createSlider(Composite parent) {
        fSlider = new Slider(parent, SWT.SMOOTH | SWT.FILL);
        fSlider.setMinimum(0);
        fSlider.setMaximum(SLIDER_RANGE + fSlider.getThumb());
        fSlider.setIncrement(SLIDER_RANGE / 100);
        fSlider.setPageIncrement(SLIDER_RANGE / 10);
        fSlider.setSelection(0);

        GridData gridData = new GridData(SWT.LEFT, SWT.TOP, true, false);
        gridData.horizontalAlignment = SWT.FILL;
        gridData.horizontalSpan = 4;
        fSlider.setLayoutData(gridData);

        fSlider.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event event) {
				int ratio = fSlider.getSelection();
				TmfTimestamp span = fCurrentGroup.getSpan();
				long value = span.getValue() * ratio / SLIDER_RANGE;
				TmfTimestamp start = fCurrentGroup.getStartTime();
				TmfTimestamp current = new TmfTimestamp(start.getValue() + value, start.getScale(), 0);
				fCurrentGroup.setValue(current);
			}
        });

    }

    /**
     * @param range
     * @param current
     */
    private void updateSlider(TmfTimeRange range, TmfTimestamp current) {

        // Determine the new relative position
        long total    = range.getStartTime().getAdjustment(range.getEndTime());
        long relative = range.getStartTime().getAdjustment(current);

        // Set the slider value
        final long position = (total > 0) ? (relative * SLIDER_RANGE / total) : 0;

        // Update the slider on the UI thread
        fSlider.getDisplay().asyncExec(new Runnable() {
			public void run() {
		        fSlider.setSelection((int) position);
			}
        });
    }

    // ========================================================================
    // TMF Signal Handling
    // ========================================================================

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {

        // Update the trace reference
        fExperiment = signal.getExperiment();

        // Update the time frame
        fTraceTimeRange = fExperiment.getTimeRange();
        fTraceStartTime = fTraceTimeRange.getStartTime();
        fTraceEndTime   = fTraceTimeRange.getEndTime();
        fScale          = fTraceStartTime.getScale();

        // Update the widgets
        fStartGroup.setContent(fTraceTimeRange, fTraceStartTime);
        fEndGroup.setContent(fTraceTimeRange, fTraceEndTime);
        fCurrentGroup.setContent(fTraceTimeRange, fTraceStartTime);

        fCurrentTime = fTraceStartTime;

        TmfTimestamp delta = new TmfTimestamp(fTraceStartTime.getAdjustment(fTraceEndTime), fScale, 0);
        fTraceSpan = new TmfTimeRange(new TmfTimestamp(0, fScale, 0), delta);
        fRangeGroup.setContent(fTraceSpan, delta);
    }

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {

        // Update the time frame
       	fTraceTimeRange = signal.getTrace().getTimeRange();
        fTraceStartTime = fTraceTimeRange.getStartTime();
        fTraceEndTime   = fTraceTimeRange.getEndTime();
        fScale          = fTraceStartTime.getScale();

        // Update the widgets
        fStartGroup.setContent(fTraceTimeRange, fStartGroup.getCurrentTime());
        fEndGroup.setContent(fTraceTimeRange, fTraceEndTime);
        fCurrentGroup.setContent(fTraceTimeRange, fCurrentGroup.getCurrentTime());

        TmfTimestamp delta = new TmfTimestamp(fTraceStartTime.getAdjustment(fTraceEndTime), fScale, 0);
        fTraceSpan = new TmfTimeRange(new TmfTimestamp(0, fScale, 0), delta);
        fRangeGroup.setContent(fTraceSpan, delta);
    }

    /**
     * @param signal
     */
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
    	if (signal.getSource() != this) {
            fCurrentTime = signal.getCurrentTime();
            if (fStartGroup.getCurrentTime().compareTo(fCurrentTime, false) > 0) {
            	fStartGroup.setContent(new TmfTimeRange(fCurrentTime, fEndGroup.getCurrentTime()), fCurrentTime);
            }
            if (fEndGroup.getCurrentTime().compareTo(fCurrentTime, false) < 0) {
            	fEndGroup.setContent(new TmfTimeRange(fStartGroup.getCurrentTime(), fCurrentTime), fCurrentTime);
            }
            fCurrentGroup.setContent(null, fCurrentTime);
            updateSlider(fCurrentGroup.getTimeRange(), fCurrentTime);
    	}
    }

}