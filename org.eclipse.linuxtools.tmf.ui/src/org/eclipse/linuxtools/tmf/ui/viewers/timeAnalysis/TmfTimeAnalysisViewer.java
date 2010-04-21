/*****************************************************************************
 * Copyright (c) 2007, 2008, 2009 Intel Corporation, Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    Ruslan A. Scherbakov, Intel - Initial API and implementation
 *    Alexander N. Alexeev, Intel - Add monitors statistics support
 *    Alvaro Sanchez-Leon - Adapted for TMF
 *
 * $Id: ThreadStatesView.java,v 1.7 2008/05/19 15:07:21 jkubasta Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.dialogs.TmfTimeFilterDialog;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.dialogs.TmfTimeLegend;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.TimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.ITimeDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.TimeScaleCtrl;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.TmfTimeStatesCtrl;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.TmfTimeTipHandler;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.TraceColorScheme;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.Utils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TmfTimeAnalysisViewer implements ITimeAnalysisViewer, ITimeDataProvider, SelectionListener {

	/** vars */
	private long _minTimeInterval;
	private long _selectedTime;
	private long _beginTime;
	private long _endTime;
	private long _time0;
	private long _time1;
	private long _time0_;
	private long _time1_;
	private long _time0_InLastEvent = 0;
	private long _time1_InLastEvent = 0;
	private boolean _timeRangeFixed;
	private int _nameWidthPref = 200;
	private int _minNameWidth = 6;
	private int _nameWidth;
	private Composite _dataViewer;

	private TmfTimeStatesCtrl _stateCtrl;
	private TimeScaleCtrl _timeScaleCtrl;
	private TmfTimeTipHandler _threadTip;
	private TraceColorScheme _colors;
	private TmfTimeAnalysisProvider _utilImplm;

	private boolean _acceptSetSelAPICalls = false;
	Vector<ITmfTimeSelectionListener> widgetSelectionListners = new Vector<ITmfTimeSelectionListener>();
	Vector<ITmfTimeScaleSelectionListener> widgetTimeScaleSelectionListners = new Vector<ITmfTimeScaleSelectionListener>();
	Vector<ITmfTimeFilterSelectionListener> widgetFilterSelectionListeners = new Vector<ITmfTimeFilterSelectionListener>();

	// Calender Time format, using Epoch reference or Relative time
	// format(default
	private boolean calendarTimeFormat = false;
	private int timeScaleBoderWidth = 4;
	private int timeScaleHeight = 22;

	/** ctor */
	public TmfTimeAnalysisViewer(Composite parent, TmfTimeAnalysisProvider provider) {
        createDataViewer(parent, provider);
	}

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.api.ITimeAnalysisWidget#display(org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.model.TmfTaTrace[])
     */
    public void display(ITmfTimeAnalysisEntry[] traceArr) {
        modelUpdate(traceArr);
    }

	public void display(ITmfTimeAnalysisEntry[] traceArr, long start, long end,
			boolean updateTimeBounds) {
		modelUpdate(traceArr, start, end, updateTimeBounds);
    }

    public void controlMoved(ControlEvent e) {
	}

	public void controlResized(ControlEvent e) {
		resizeControls();
	}

	// called from the display order in the API
	public void modelUpdate(ITmfTimeAnalysisEntry[] traces) {
		if (null != _stateCtrl) {
			loadOptions();
			updateInternalData(traces);
			_stateCtrl.redraw();
			_timeScaleCtrl.redraw();
		}
	}

	// called from the display order in the API
	public void modelUpdate(ITmfTimeAnalysisEntry[] traces, long start,
			long end, boolean updateTimeBounds) {
		if (null != _stateCtrl) {
			loadOptions();
			updateInternalData(traces, start, end);
			if (updateTimeBounds) {
				_timeRangeFixed = true;
				// set window to match limits
				setStartFinishTime(_time0_, _time1_);
			} else {
				_stateCtrl.redraw();
				_timeScaleCtrl.redraw();
			}
		}
	}

	public void itemUpdate(ITmfTimeAnalysisEntry parent, TimeEvent item) {
		if (null != parent && null != item) {
			_stateCtrl.refreshPartial(parent, item);
			_stateCtrl.redraw();
			_timeScaleCtrl.redraw();
		}
	}

	public void selectionChanged() {
	}

	protected String getViewTypeStr() {
		return "viewoption.threads";
	}

	int getMarginWidth(int idx) {
		return 0;
	}

	int getMarginHeight(int idx) {
		return 0;
	}

	void loadOptions() {
		_minTimeInterval = 500;
		_selectedTime = -1;
		_nameWidth = Utils.loadIntOption(getPreferenceString("namewidth"),
				_nameWidthPref, _minNameWidth, 1000);
	}

	void saveOptions() {
		Utils.saveIntOption(getPreferenceString("namewidth"), _nameWidth);
	}

	protected Control createDataViewer(Composite parent,
			TmfTimeAnalysisProvider utilImplm) {
		loadOptions();
		_utilImplm = utilImplm;
		_colors = new TraceColorScheme();
		_dataViewer = new Composite(parent, SWT.NULL);
		_dataViewer.setLayoutData(GridUtil.createFill());

		_timeScaleCtrl = new TimeScaleCtrl(_dataViewer, _colors);
		_timeScaleCtrl.setTimeProvider(this);
		_timeScaleCtrl.setLayoutData(GridUtil.createFill());

		_stateCtrl = new TmfTimeStatesCtrl(_dataViewer, _colors, _utilImplm);

		_stateCtrl.setTimeProvider(this);
		_stateCtrl.addSelectionListener(this);
		_stateCtrl.setLayoutData(GridUtil.createFill());
		_dataViewer.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent event) {
				resizeControls();
			}
		});
		resizeControls();
		_dataViewer.update();
		_threadTip = new TmfTimeTipHandler(parent.getShell(), _utilImplm, this);
		_threadTip.activateHoverHelp(_stateCtrl);
		return _dataViewer;
	}

	public void dispose() {
		saveOptions();
		_stateCtrl.dispose();
		_dataViewer.dispose();
		_colors.dispose();
	}

	public void resizeControls() {
		Rectangle r = _dataViewer.getClientArea();
		if (r.isEmpty())
			return;

		// System.out.println("Client Area:" + r);
		// timeScaleBoderWidth = 8;
		// timeScaleHeight = 22;
		_timeScaleCtrl.setBounds(r.x, r.y + timeScaleBoderWidth, r.width,
				timeScaleHeight);
		_stateCtrl.setBounds(r.x, r.y + timeScaleBoderWidth + timeScaleHeight,
				r.width, r.height - timeScaleBoderWidth - timeScaleHeight);
		int width = r.width;
		if (_nameWidth > width - _minNameWidth)
			_nameWidth = width - _minNameWidth;
		if (_nameWidth < _minNameWidth)
			_nameWidth = _minNameWidth;
	}

	/** Tries to set most convinient time range for display. */
	void setTimeRange(Object traces[]) {
		_endTime = 0;
		_beginTime = -1;
		ITimeEvent event;
		for (int i = 0; i < traces.length; i++) {
			ITmfTimeAnalysisEntry thread = (ITmfTimeAnalysisEntry) traces[i];
			long lastEventTime = thread.getStopTime();
			if (lastEventTime > thread.getStartTime()) {
				if (lastEventTime > _endTime)
					_endTime = lastEventTime;
			}
			List<TimeEvent> list = thread.getTraceEvents();
			int len = list.size();
			if (len > 0) {
				event = (ITimeEvent) list.get(list.size() - 1);
				lastEventTime = event.getTime();
				if (lastEventTime > _endTime) {
					_endTime = lastEventTime;
					long duration = event.getDuration();
					_endTime += duration > 0 ? duration : 1000000;
				}
				event = (ITimeEvent) list.get(0);
				if (_beginTime < 0 || _beginTime > event.getTime())
					_beginTime = event.getTime();
			}
		}

		if (_beginTime < 0)
			_beginTime = 0;
	}

	void setTimeBounds() {
		_time0_ = _beginTime - (long) ((_endTime - _beginTime) * 0.02);
		if (_time0_ < 0)
			_time0_ = 0;
		// _time1_ = _time0_ + (_endTime - _time0_) * 1.05;
		_time1_ = _endTime;
		// _time0_ = Math.floor(_time0_);
		// _time1_ = Math.ceil(_time1_);
		if (!_timeRangeFixed) {
			_time0 = _time0_;
			_time1 = _time1_;
		}
	}

	/**
	 * @param traces
	 */
	void updateInternalData(ITmfTimeAnalysisEntry[] traces) {
		if (null == traces)
			traces = new ITmfTimeAnalysisEntry[0];
		setTimeRange(traces);
		refreshAllData(traces);
	}

	/**
	 * @param traces
	 * @param start
	 * @param end
	 */
	void updateInternalData(ITmfTimeAnalysisEntry[] traces, long start, long end) {
		if (null == traces)
			traces = new ITmfTimeAnalysisEntry[0];
		if (end < 1 || start < 1) {
			// End or start time are unspecified and need to be determined from
			// individual processes
			setTimeRange(traces);
		} else {
			_beginTime = start;
			_endTime = end;
		}

		refreshAllData(traces);
	}

	/**
	 * @param traces
	 */
	private void refreshAllData(ITmfTimeAnalysisEntry[] traces) {
		setTimeBounds();
		if (_selectedTime < 0 || _selectedTime > _endTime)
			_selectedTime = _endTime;
		_stateCtrl.refreshData(traces);
		filterOutNotification();
	}

	public void setFocus() {
		if (null != _stateCtrl)
			_stateCtrl.setFocus();
	}

	public boolean isInFocus() {
		return _stateCtrl.isInFocus();
	}

	public ITmfTimeAnalysisEntry getSelectedTrace() {
		return _stateCtrl.getSelectedTrace();
	}

	public ISelection getSelection() {
		return _stateCtrl.getSelection();
	}

	public ISelection getSelectionTrace() {
		return _stateCtrl.getSelectionTrace();
	}

	public long getTime0() {
		return _time0;
	}

	public long getTime1() {
		return _time1;
	}

	public long getMinTimeInterval() {
		return _minTimeInterval;
	}

	public int getNameSpace() {
		return _nameWidth;
	}

	public void setNameSpace(int width) {
		_nameWidth = width;
		width = _stateCtrl.getClientArea().width;
		if (_nameWidth > width - 6)
			_nameWidth = width - 6;
		if (_nameWidth < 6)
			_nameWidth = 6;
		_stateCtrl.adjustScrolls();
		_stateCtrl.redraw();
		_timeScaleCtrl.redraw();
	}

	public int getTimeSpace() {
		int w = _stateCtrl.getClientArea().width;
		return w - _nameWidth;
	}

	public long getSelectedTime() {
		return _selectedTime;
	}

	public long getBeginTime() {
		return _beginTime;
	}

	public long getEndTime() {
		return _endTime;
	}

	public long getMaxTime() {
		return _time1_;
	}

	public long getMinTime() {
		return _time0_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.ITimeDataProvider
	 * #mouseUp()
	 */
	public void mouseUp() {
		// refresh listeners with the new selected time if changed
		notifyStartFinishTimeSelectionListeners(_time0, _time1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.ITimeDataProvider
	 * #setStartFinishTimeNotify(long, long)
	 */
	public void setStartFinishTimeNotify(long time0, long time1) {
		setStartFinishTime(time0, time1);
		notifyStartFinishTimeSelectionListeners(time0, time1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets.ITimeDataProvider
	 * #setStartFinishTime(long, long)
	 */
	public void setStartFinishTime(long time0, long time1) {
		_time0 = time0;
		if (_time0 < _time0_)
			_time0 = _time0_;
		_time1 = time1;
		if (_time1 - _time0 < _minTimeInterval)
			_time1 = _time0 + _minTimeInterval;
		if (_time1 > _time1_)
			_time1 = _time1_;
		_timeRangeFixed = true;
		_stateCtrl.adjustScrolls();
		_stateCtrl.redraw();
		_timeScaleCtrl.redraw();
	}

	public void resetStartFinishTime() {
		setStartFinishTimeNotify(_time0_, _time1_);
		_timeRangeFixed = false;
	}

	public void setSelectedTimeInt(long time, boolean ensureVisible) {
		// Trace.debug("currentTime:" + _selectedTime + " new time:" + time);
		_selectedTime = time;
		if (_selectedTime > _endTime)
			_selectedTime = _endTime;
		if (_selectedTime < _beginTime)
			_selectedTime = _beginTime;
		if (ensureVisible) {
			double timeSpace = (_time1 - _time0) * .02;
			double timeMid = (_time1 - _time0) * .1;
			if (_selectedTime < _time0 + timeSpace) {
				double dt = _time0 - _selectedTime + timeMid;
				_time0 -= dt;
				_time1 -= dt;
			} else if (_selectedTime > _time1 - timeSpace) {
				double dt = _selectedTime - _time1 + timeMid;
				_time0 += dt;
				_time1 += dt;
			}
			if (_time0 < 0) {
				_time1 -= _time0;
				_time0 = 0;
			} else if (_time1 > _time1_) {
				_time0 -= _time1 - _time1_;
				_time1 = _time1_;
			}
		}
		_stateCtrl.adjustScrolls();
		_stateCtrl.redraw();
		_timeScaleCtrl.redraw();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO: Opening call stack shall be replaced to a configurable view
		// new OpenCallStackViewAction().openView(false);
		// Replaced by event notification
		// updateModelSelection();
		notifySelectionListeners(TmfTimeSelectionEvent.type.WIDGET_DEF_SEL);
	}

	public void widgetSelected(SelectionEvent e) {
		// Replace by event notification
		// updateModelSelection();
		notifySelectionListeners(TmfTimeSelectionEvent.type.WIDGET_SEL);
	}

	public void selectNextEvent() {
		_stateCtrl.selectNextEvent();
	}

	public void selectPrevEvent() {
		_stateCtrl.selectPrevEvent();
	}

	public void selectNextTrace() {
		_stateCtrl.selectNextTrace();
	}

	public void selectPrevTrace() {
		_stateCtrl.selectPrevTrace();
	}

	public void groupTraces(boolean on) {
		_stateCtrl.groupTraces(on);
	}

	public void filterTraces() {
		if (_dataViewer == null || _dataViewer.isDisposed())
			return;

		if (TmfTimeFilterDialog.getTraceFilter(_dataViewer.getShell(), _stateCtrl
				.getTraces(), _stateCtrl.getTraceFilter())) {
			_stateCtrl.refreshData();
			filterOutNotification();
		}
	}

	public void showLegend() {
		if (_dataViewer == null || _dataViewer.isDisposed())
			return;

		TmfTimeLegend.open(_dataViewer.getShell(), _utilImplm);
	}

	public void toggleThreadsInteractionDrawing() {
		_stateCtrl.toggleTraceInteractionDrawing();
	}

	public void setThreadJoinDrawing(boolean on) {
		_stateCtrl.setTraceJoinDrawing(on);
	}

	public void setThreadWaitDrawing(boolean on) {
		_stateCtrl.setTraceWaitDrawing(on);
	}

	public void setThreadReleaseDrawing(boolean on) {
		_stateCtrl.setTraceReleaseDrawing(on);
	}

	public boolean getThreadInteractionDrawing() {
		return _stateCtrl.getTracesInteractionDrawing();
	}

	public boolean getThreadJoinDrawing() {
		return _stateCtrl.getTraceJoinDrawing();
	}

	public boolean getThreadWaitDrawing() {
		return _stateCtrl.getTraceWaitDrawing();
	}

	public boolean getThreadReleaseDrawing() {
		return _stateCtrl.getTraceReleaseDrawing();
	}

	protected void select(Object obj) {
		if (obj == null)
			return;
		// TODO: ThreadDetails Adaption removed, might need replacement
		// if (obj instanceof ThreadDetails) {
		// obj = ((ThreadDetails) obj).getThread();
		// }
		if (obj instanceof ITmfTimeAnalysisEntry) {
			// _stateCtrl.selectThread((TsfTmTrace) obj);
		}
	}

	public void zoomIn() {
		_stateCtrl.zoomIn();
	}

	public void zoomOut() {
		_stateCtrl.zoomOut();
	}

	private String getPreferenceString(String string) {
		return getViewTypeStr() + "." + string;
	}

	public void addWidgetSelectionListner(ITmfTimeSelectionListener listener) {
		widgetSelectionListners.add(listener);
	}

	public void removeWidgetSelectionListner(ITmfTimeSelectionListener listener) {
		widgetSelectionListners.removeElement(listener);
	}

	public void addWidgetTimeScaleSelectionListner(
			ITmfTimeScaleSelectionListener listener) {
		widgetTimeScaleSelectionListners.add(listener);
	}

	public void removeWidgetTimeScaleSelectionListner(
			ITmfTimeScaleSelectionListener listener) {
		widgetTimeScaleSelectionListners.removeElement(listener);
	}

	public void setSelectedTime(long time, boolean ensureVisible, Object source) {
		if (_acceptSetSelAPICalls == false || this == source) {
			return;
		}

		setSelectedTimeInt(time, ensureVisible);
	}

	public void setSelectedEvent(ITimeEvent event, Object source) {
		if (_acceptSetSelAPICalls == false || event == null || source == this) {
			return;
		}
		ITmfTimeAnalysisEntry trace = event.getEntry();
		if (trace != null) {
			_stateCtrl.selectItem(trace, false);
		}

		setSelectedTimeInt(event.getTime(), true);
	}

	public void setSelectedTraceTime(ITmfTimeAnalysisEntry trace, long time, Object source) {
		if (_acceptSetSelAPICalls == false || trace == null || source == this) {
			return;
		}

		if (trace != null) {
			_stateCtrl.selectItem(trace, false);
		}

		setSelectedTimeInt(time, true);
	}

	public void setSelectedTrace(ITmfTimeAnalysisEntry trace) {
		if (trace == null) {
			return;
		}

		_stateCtrl.selectItem(trace, false);
	}

	public void setSelectVisTimeWindow(long time0, long time1, Object source) {
		if (_acceptSetSelAPICalls == false || source == this) {
			return;
		}

		setStartFinishTime(time0, time1);
	}

	public void setAcceptSelectionAPIcalls(boolean acceptCalls) {
		_acceptSetSelAPICalls = acceptCalls;
	}

	private synchronized void notifySelectionListeners(
			TmfTimeSelectionEvent.type rtype) {
		// Any listeners out there ?
		if (widgetSelectionListners.size() > 0) {
			// Locate the event selected
			ISelection selection = getSelection();
			Object sel = null;
			if (selection != null && !selection.isEmpty()) {
				sel = ((IStructuredSelection) selection).getFirstElement();
			}

			if (sel != null) {
				// Notify Selection Listeners
				TmfTimeSelectionEvent event = new TmfTimeSelectionEvent(this,
						rtype, sel, getSelectedTime());

				for (Iterator<ITmfTimeSelectionListener> iter = widgetSelectionListners
						.iterator(); iter.hasNext();) {
					ITmfTimeSelectionListener listener = (ITmfTimeSelectionListener) iter
							.next();
					listener.tsfTmProcessSelEvent(event);
				}
			}
		}
	}

	public void notifyStartFinishTimeSelectionListeners(long _time0, long _time1) {
		if (widgetTimeScaleSelectionListners.size() > 0) {
			// Check if the time has actually changed from last notification
			if (_time0 != _time0_InLastEvent || _time1 != _time1_InLastEvent) {
				// Notify Time Scale Selection Listeners
				TmfTimeScaleSelectionEvent event = new TmfTimeScaleSelectionEvent(
						this, _time0, _time1, getTimeSpace(), getSelectedTime());

				for (Iterator<ITmfTimeScaleSelectionListener> iter = widgetTimeScaleSelectionListners
						.iterator(); iter.hasNext();) {
					ITmfTimeScaleSelectionListener listener = (ITmfTimeScaleSelectionListener) iter
							.next();
					listener.tsfTmProcessTimeScaleEvent(event);
				}

				// last time notification cache
				_time0_InLastEvent = _time0;
				_time1_InLastEvent = _time1;
			}
		}
	}

	public void setTimeCalendarFormat(boolean toAbsoluteCaltime) {
		calendarTimeFormat = toAbsoluteCaltime;
	}

	public boolean isCalendarFormat() {
		return calendarTimeFormat;
	}

	public int getBorderWidth() {
		return timeScaleBoderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		if (borderWidth > -1) {
			this.timeScaleBoderWidth = borderWidth;
		}
	}

	public int getHeaderHeight() {
		return timeScaleHeight;
	}

	public void setHeaderHeight(int headerHeight) {
		if (headerHeight > -1) {
			this.timeScaleHeight = headerHeight;
		}
	}

	public int getItemHeight() {
		if (_stateCtrl != null) {
			return _stateCtrl.getItemHeight();
		}
		return 0;
	}

	public void setItemHeight(int rowHeight) {
		if (_stateCtrl != null) {
			_stateCtrl.setItemHeight(rowHeight);
		}
	}

	public boolean isVisibleVerticalScroll() {
		if (_stateCtrl != null) {
			_stateCtrl.isVisibleVerticalScroll();
		}
		return false;
	}

	public void setVisibleVerticalScroll(boolean visibleVerticalScroll) {
		if (_stateCtrl != null) {
			_stateCtrl.setVisibleVerticalScroll(visibleVerticalScroll);
		}
	}

	public void setNameWidthPref(int width) {
		_nameWidthPref = width;
		if (width == 0) {
			_minNameWidth = 0;
		}
	}

	public int getNameWidthPref(int width) {
		return _nameWidthPref;
	}

	public void addFilterSelectionListner(ITmfTimeFilterSelectionListener listener) {
		widgetFilterSelectionListeners.add(listener);
	}

	public void removeFilterSelectionListner(
			ITmfTimeFilterSelectionListener listener) {
		widgetFilterSelectionListeners.remove(listener);
	}

	private void filterOutNotification() {
		TmfTimeFilterSelectionEvent event = new TmfTimeFilterSelectionEvent(this);
		event.setFilteredOut(_stateCtrl.getFilteredOut());
		for (ITmfTimeFilterSelectionListener listener : widgetFilterSelectionListeners) {
			listener.tmfTaProcessFilterSelection(event);
		}
	}

	/**
	 * needed in case there's a need to associate a context menu
	 * 
	 * @return
	 */
	public Control getControl() {
		return _stateCtrl;
	}

	/**
	 * Get the selection provider
	 * 
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return _stateCtrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITimeAnalysisViewer
	 * #waitCursor(boolean)
	 */
	public void waitCursor(boolean waitInd) {
		_stateCtrl.waitCursor(waitInd);
	}
}
