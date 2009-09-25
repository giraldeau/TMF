/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Alvaro Sanchez-Leon - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.resources;

import java.util.Arrays;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.state.IStateDataRequestListener;
import org.eclipse.linuxtools.lttng.state.RequestCompletedSignal;
import org.eclipse.linuxtools.lttng.state.RequestStartedSignal;
import org.eclipse.linuxtools.lttng.state.StateDataRequest;
import org.eclipse.linuxtools.lttng.state.StateManager;
import org.eclipse.linuxtools.lttng.state.evProcessor.EventProcessorProxy;
import org.eclipse.linuxtools.lttng.state.experiment.StateExperimentManager;
import org.eclipse.linuxtools.lttng.state.experiment.StateManagerFactory;
import org.eclipse.linuxtools.lttng.ui.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeComponent;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeViewerProvider;
import org.eclipse.linuxtools.lttng.ui.views.common.ParamsUpdater;
import org.eclipse.linuxtools.lttng.ui.views.resources.evProcessor.ResourcesTRangeUpdateFactory;
import org.eclipse.linuxtools.lttng.ui.views.resources.model.ResourceModelFactory;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewerFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITimeAnalysisViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeScaleSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.ITmfTimeSelectionListener;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeScaleSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author alvaro
 * 
 */
public class ResourcesView extends TmfView implements
		ITmfTimeSelectionListener, ITmfTimeScaleSelectionListener,
		IStateDataRequestListener {

	// ========================================================================
	// Data
	// ========================================================================
	public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.resources";
	private Vector<StateDataRequest> pendingDataRequests = new Vector<StateDataRequest>();

	// private int totalNumItems = 0;
	// Actions
	private Action resetScale;
	private Action nextEvent;
	private Action prevEvent;
	private Action nextTrace;
	private Action prevTrace;
	private Action showLegend;
	private Action filterTraces;
	private Action zoomIn;
	private Action zoomOut;
	private Action synch;

	private ITimeAnalysisViewer tsfviewer;
	private Composite top;

	// private static SimpleDateFormat stimeformat = new SimpleDateFormat(
	// "yy/MM/dd HH:mm:ss");

	// private TraceModelImplFactory fact;

	// ========================================================================
	// Methods
	// ========================================================================

	/**
	 * The constructor.
	 */
	public ResourcesView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		top = new Composite(parent, SWT.BORDER);

		top.setLayout(new FillLayout());
		tsfviewer = TmfViewerFactory.createViewer(top,
				new TimeRangeViewerProvider());

		tsfviewer.addWidgetSelectionListner(this);
		tsfviewer.addWidgetTimeScaleSelectionListner(this);

		// Traces shall not be grouped to allow synchronization
		tsfviewer.groupTraces(true);
		tsfviewer.setAcceptSelectionAPIcalls(true);

		// Viewer to notify selection to this class
		// This class will synchronize selections with table.
		tsfviewer.addWidgetSelectionListner(this);
		tsfviewer.addWidgetTimeScaleSelectionListner(this);

		// Create the help context id for the viewer's control
		// TODO: Associate with help system
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				tsfviewer.getControl(),
				"org.eclipse.linuxtools.lttng.ui.views.resource.view"); //$NON-NLS-1$

		makeActions();
		hookContextMenu();
		contributeToActionBars();

		// Register the updater in charge to refresh elements as we update the
		// time ranges
		// FlowParamsUpdater listener = FlowModelFactory.getParamsUpdater();
		// tsfviewer.addWidgetTimeScaleSelectionListner(listener);

		// TODO: refactor regitration / notificatio process
		// Register this view to receive updates when the model is updated with
		// fresh info
		// ModelListenFactory.getRegister().addFlowModelUpdatesListener(this);

		// Register the event processor factory in charge of event handling
		EventProcessorProxy.getInstance().addEventProcessorFactory(
				ResourcesTRangeUpdateFactory.getInstance());

		// set the initial view parameter values
		// Experiment start and end time
		// as well as time space width in pixels, used by the time analysis
		// widget
		ParamsUpdater paramUpdater = ResourceModelFactory.getParamsUpdater();
		StateExperimentManager experimentManger = StateManagerFactory
				.getExperimentManager();
		// Read relevant values
		int timeSpaceWidth = tsfviewer.getTimeSpace();
		TmfTimeRange timeRange = experimentManger.getExperimentTimeRange();
		if (timeRange != null) {
			long time0 = timeRange.getStartTime().getValue();
			long time1 = timeRange.getEndTime().getValue();
			paramUpdater.update(time0, time1, timeSpaceWidth);
		}

		// Read current data if any available
		StateManagerFactory.getExperimentManager().readExperiment(
				"resourceView", this);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ResourcesView.this.fillContextMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(tsfviewer.getControl());
		tsfviewer.getControl().setMenu(menu);
		getSite()
				.registerContextMenu(menuMgr, tsfviewer.getSelectionProvider());
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		// manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(synch);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		// manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(showLegend);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(synch);
		manager.add(new Separator());
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		// manager.add(showLegend);
		manager.add(new Separator());
		manager.add(resetScale);
		manager.add(nextEvent);
		manager.add(prevEvent);
		manager.add(nextTrace);
		manager.add(prevTrace);
		// manager.add(filterTraces);
		manager.add(zoomIn);
		manager.add(zoomOut);
		manager.add(synch);
		manager.add(new Separator());
	}

	private void makeActions() {
		// action4
		resetScale = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.resetStartFinishTime();
				}

			}
		};
		resetScale.setText(Messages.getString("ResourcesView.Action.Reset")); //$NON-NLS-1$
		resetScale.setToolTipText(Messages
				.getString("ResourcesView.Action.Reset.ToolTip")); //$NON-NLS-1$
		resetScale.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ResourcesView.tmf.UI"),
						"icons/home_nav.gif"));

		// action5
		nextEvent = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectNextEvent();
				}
			}
		};
		nextEvent.setText(Messages.getString("ResourcesView.Action.NextEvent")); //$NON-NLS-1$
		nextEvent.setToolTipText(Messages
				.getString("ResourcesView.Action.NextEvent.Tooltip")); //$NON-NLS-1$
		nextEvent.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ResourcesView.tmf.UI"),
						"icons/next_event.gif"));

		// action6
		prevEvent = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectPrevEvent();
				}
			}
		};
		prevEvent.setText(Messages.getString("ResourcesView.Action.PrevEvent")); //$NON-NLS-1$
		prevEvent.setToolTipText(Messages
				.getString("ResourcesView.Action.PrevEvent.Tooltip")); //$NON-NLS-1$
		prevEvent.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ResourcesView.tmf.UI"),
						"icons/prev_event.gif"));

		// action7
		nextTrace = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectNextTrace();
				}
			}
		};
		nextTrace.setText(Messages
				.getString("ResourcesView.Action.NextResource")); //$NON-NLS-1$
		nextTrace.setToolTipText(Messages
				.getString("ResourcesView.Action.NextResource.ToolTip")); //$NON-NLS-1$
		nextTrace.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ResourcesView.tmf.UI"),
						"icons/next_item.gif"));

		// action8
		prevTrace = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.selectPrevTrace();
				}
			}
		};
		prevTrace.setText(Messages
				.getString("ResourcesView.Action.PreviousResource")); //$NON-NLS-1$
		prevTrace.setToolTipText(Messages
				.getString("ResourcesView.Action.PreviousResource.Tooltip")); //$NON-NLS-1$
		prevTrace.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ResourcesView.tmf.UI"),
						"icons/prev_item.gif"));

		// action9
		showLegend = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.showLegend();
				}
			}
		};
		showLegend.setText(Messages.getString("ResourcesView.Action.Legend")); //$NON-NLS-1$
		showLegend.setToolTipText(Messages
				.getString("ResourcesView.Action.Legend.ToolTip")); //$NON-NLS-1$

		// action10
		filterTraces = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.filterTraces();
				}
			}
		};
		filterTraces.setText(Messages.getString("ResourcesView.Action.Filter")); //$NON-NLS-1$
		filterTraces.setToolTipText(Messages
				.getString("ResourcesView.Action.Filter.ToolTip")); //$NON-NLS-1$
		filterTraces.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Messages
						.getString("ResourcesView.tmf.UI"),
						"icons/filter_items.gif"));

		// action10
		zoomIn = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.zoomIn();
				}
			}
		};
		zoomIn.setText(Messages.getString("ResourcesView.Action.ZoomIn")); //$NON-NLS-1$
		zoomIn.setToolTipText(Messages
				.getString("ResourcesView.Action.ZoomIn.Tooltip")); //$NON-NLS-1$
		zoomIn.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),
				"icons/zoomin_nav.gif"));

		// action10
		zoomOut = new Action() {
			@Override
			public void run() {
				if (tsfviewer != null) {
					tsfviewer.zoomOut();
				}
			}
		};
		zoomOut.setText(Messages.getString("ResourcesView.Action.ZoomOut")); //$NON-NLS-1$
		zoomOut.setToolTipText(Messages
				.getString("ResourcesView.Action.ZoomOut.tooltip")); //$NON-NLS-1$
		zoomOut.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),
				"icons/zoomout_nav.gif"));

		// action11
		synch = new Action() {
			@Override
			public void run() {
				// Note: No action since the synch flag is used by Control flow
				// view
				// the actual viewer is set to accept api selections in
				// createpartcontrol.

				// if (synch.isChecked()) {
				// tsfviewer.setAcceptSelectionAPIcalls(true);
				// } else {
				// tsfviewer.setAcceptSelectionAPIcalls(false);
				// }
			}
		};
		synch.setText(Messages.getString("ResourcesView.Action.Synchronize")); //$NON-NLS-1$
		synch.setToolTipText(Messages
				.getString("ResourcesView.Action.Synchronize.ToolTip")); //$NON-NLS-1$
		synch.setChecked(false);
		synch.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				Messages.getString("ResourcesView.tmf.UI"),
						"icons/synced.gif"));
		// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_SYNCED);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		tsfviewer.getControl().setFocus();
	}

	public void tsfTmProcessSelEvent(TmfTimeSelectionEvent event) {
		Object source = event.getSource();
		if (source == null) {
			return;
		}

		// TmfTimeAnalysisViewer rViewer = (TmfTimeAnalysisViewer)
		// event.getSource();
		// TmfTimeAnalysisViewer synchViewer = null;
		// Synchronize viewer selections if Enabled,
		// make sure the selection does not go in loops
		// if (tsfviewer == rViewer) {
		// synchViewer = tsfviewer2;
		// } else {
		// synchViewer = tsfviewer;
		// }
		// Notify listener views.

		ParamsUpdater paramUpdater = ResourceModelFactory.getParamsUpdater();
		Long savedSelTime = paramUpdater.getSelectedTime();

		long selTimens = event.getSelectedTime();

		// make sure the new selected time is different than saved before
		// executing update
		if (savedSelTime == null || savedSelTime != selTimens) {
			// Notify listener views.
			synchTimeNotification(selTimens);

			// Update the parameter updater to save the selected time
			paramUpdater.setSelectedTime(selTimens);

			if (TraceDebug.isDEBUG()) {
				// Object selection = event.getSelection();
				TraceDebug.debug("Selected Time in Resource View: "
						+ new LttngTimestamp(selTimens));
			}
		}
	}

	public void tsfTmProcessTimeScaleEvent(TmfTimeScaleSelectionEvent event) {
		// source needed to keep track of source values
		Object source = event.getSource();

		if (source != null) {
			// Update the parameter updater before carrying out a read request
			ParamsUpdater paramUpdater = ResourceModelFactory
					.getParamsUpdater();
			boolean newParams = paramUpdater.processTimeScaleEvent(event);

			if (newParams) {
				// Read the updated time window
				TmfTimeRange trange = paramUpdater.getTrange();
				if (trange != null) {
					StateManagerFactory.getExperimentManager()
							.readExperimentTimeWindow(trange, "resourceView",
									this);
				}
			}
		}
	}

	/**
	 * Obtains the remainder fraction on unit Seconds of the entered value in
	 * nanoseconds. e.g. input: 1241207054171080214 ns The number of seconds can
	 * be obtain by removing the last 9 digits: 1241207054 the fractional
	 * portion of seconds, expressed in ns is: 171080214
	 * 
	 * @param v
	 * @return
	 */
	public String formatNs(long v) {
		StringBuffer str = new StringBuffer();
		boolean neg = v < 0;
		if (neg) {
			v = -v;
			str.append('-');
		}

		String strVal = String.valueOf(v);
		if (v < 1000000000) {
			return strVal;
		}

		// Extract the last nine digits (e.g. fraction of a S expressed in ns
		return strVal.substring(strVal.length() - 9);
	}

	// // @Override
	// public void resourceModelUpdates(ModelUpdatesEvent event) {
	// ITmfTimeAnalysisEntry[] items = event.getItems();
	// resourceModelUpdates(items, event.getStartTime(), event.getEndTime());
	// }

	public void resourceModelUpdates(final ITmfTimeAnalysisEntry[] items,
			final long startTime, final long endTime) {
		tsfviewer.getControl().getDisplay().asyncExec(new Runnable() {

			public void run() {
				tsfviewer.display(items, startTime, endTime);
				tsfviewer.resizeControls();
			}
		});
	}

	@Override
	public void dispose() {
		// dispose parent resources
		super.dispose();
		// Remove the event processor factory
		EventProcessorProxy.getInstance().removeEventProcessorFactory(
				ResourcesTRangeUpdateFactory.getInstance());

		tsfviewer.removeWidgetSelectionListner(this);
		tsfviewer.removeWidgetTimeScaleSelectionListner(this);
		tsfviewer = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.IStateDataRequestListener#
	 * processingStarted(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@TmfSignalHandler
	public void processingStarted(RequestStartedSignal startSignal) {
		StateDataRequest request = startSignal.getRequest();
		cancelPendingRequests();
		if (request != null) {
			// make sure there are no duplicates
			if (!pendingDataRequests.contains(request)) {
				pendingDataRequests.add(request);
			}

			StateManager smanager = request.getStateManager();
			// Clear the children on the Processes related to this manager.
			// Leave the GUI in charge of the updated data.
			String traceId = smanager.getEventLog().getName();
			ResourceModelFactory.getResourceContainer().clearChildren(traceId);
			// Start over
			ResourceModelFactory.getParamsUpdater().setEventsDiscarded(0);
		}
	}

	/**
	 * Orders cancellation of any pending data requests
	 */
	private void cancelPendingRequests() {
		for (StateDataRequest request : pendingDataRequests) {
			request.cancel();
		}
		pendingDataRequests.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.lttng.state.IStateDataRequestListener#
	 * processingCompleted(org.eclipse.linuxtools.lttng.state.StateDataRequest)
	 */
	@TmfSignalHandler
	public void processingCompleted(
			RequestCompletedSignal completedSignal) {
		StateDataRequest request = completedSignal.getRequest();

		if (request == null) {
			return;
		} else {
			// Remove from the pending requests record
			pendingDataRequests.remove(request);
		}

		// No data refresh actions for cancelled requests.
		if (request.isCancelled() || request.isFailed()) {
			if (TraceDebug.isDEBUG()) {
				TmfTimeRange range = request.getRange();
				TraceDebug.debug("Request cancelled: "
						+ range.getStartTime().toString() + " - "
						+ range.getEndTime().toString());
			}
			return;
		}

		StateManager smanager = request.getStateManager();
		long experimentStartTime = -1;
		long experimentEndTime = -1;
		TmfTimeRange experimentTimeRange = smanager.getExperimentTimeWindow();
		if (experimentTimeRange != null) {
			experimentStartTime = experimentTimeRange.getStartTime().getValue();
			experimentEndTime = experimentTimeRange.getEndTime().getValue();
		}

		// Obtain the current resource list
		TimeRangeEventResource[] resourceArr = ResourceModelFactory
				.getResourceContainer().readResources();

		// Sort the array by pid
		Arrays.sort(resourceArr);

		// Update the view part
		resourceModelUpdates(resourceArr, experimentStartTime,
				experimentEndTime);

		// reselect to original time
		ParamsUpdater paramUpdater = ResourceModelFactory.getParamsUpdater();
		final Long selTime = paramUpdater.getSelectedTime();
		if (selTime != null) {
			Display display = tsfviewer.getControl().getDisplay();
			display.asyncExec(new Runnable() {
				public void run() {
					tsfviewer.setSelectedTime(selTime, false, this);
				}
			});
		}

		if (TraceDebug.isDEBUG()) {
			Long count = smanager.getEventCount();
			int eventCount = 0;
			for (TimeRangeEventResource resource : resourceArr) {
				eventCount += resource.getTraceEvents().size();
			}

			int discarded = ResourceModelFactory.getParamsUpdater()
					.getEventsDiscarded();
			int discardedOutofOrder = ResourceModelFactory.getParamsUpdater()
					.getEventsDiscardedWrongOrder();
			TraceDebug
					.debug("Events handled: "
							+ count
							+ " Events loaded in Resource view: "
							+ eventCount
							+ " Number of events discarded: "
							+ discarded
							+ "\n\tNumber of events discarded with start time earlier than next good time: "
							+ discardedOutofOrder);
		}

	}

	public void newTimeRange(TimeRangeComponent trange) {
		// TODO Auto-generated method stub

	}

	/**
	 * Trigger time synchronisation to other views this method shall be called
	 * when a check has been performed to note that an actual change of time has
	 * been performed vs a pure re-selection of the same time
	 * 
	 * @param time
	 */
	private void synchTimeNotification(long time) {
		// if synchronisation selected
		if (synch.isChecked()) {
			// Notify other views
			TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(this,
					new LttngTimestamp(time)));
		}
	}

	/**
	 * Registers as listener of time selection from other tmf views
	 * 
	 * @param signal
	 */
	@TmfSignalHandler
	public void synchToTime(TmfTimeSynchSignal signal) {
		if (synch.isChecked()) {
			Object source = signal.getSource();
			if (signal != null && source != null && source != this) {
				// Internal value is expected in nano seconds.
				long selectedTime = signal.getCurrentTime().getValue();
				if (tsfviewer != null) {
					tsfviewer.setSelectedTime(selectedTime, true, source);
				}
			}
		}
	}
}