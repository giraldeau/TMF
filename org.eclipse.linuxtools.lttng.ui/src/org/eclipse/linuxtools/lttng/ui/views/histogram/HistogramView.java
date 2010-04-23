/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * <b><u>HistogramView</u></b>
 * <p>
 * View that contain an visual approach to the window that control the request.
 * This is intended to replace the TimeFrameView
 * <p>
 * This view is composed of 2 canvas, one for the whole experiment and one for the selectionned window in the experiment.
 * It also contain a certain number of controls to print or change informations about the experiment.
 */
public class HistogramView extends TmfView implements ControlListener {
	
	// *** TODO ***
	// Here is what's left to do in this view
	//
	// 1- Make sure all control are thread safe (bug 309348)
	//		Right now, all the basic controls (i.e. Text and Label) are sensible to "Thread Access Exception" if
	//			updated from different threads; we need to carefully decide when/where to redraw them.
	//		This is a real problem since there is a lot of thread going on in this view.
	//		All basic control should be subclassed to offer "Asynchronous" functions.
	
    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.histogram";
    
    // "Minimum" screen width size. On smaller screen, we will apply several space saving technique
    private static final int SCREEN_SMALL_IF_SMALLER_THAN = 1600;
    
    // Size of the "fulll trace" canvas
    private static final int FULL_TRACE_CANVAS_HEIGHT = 25;
    private static final int FULL_TRACE_BAR_WIDTH = 1;
    private static final double FULL_TRACE_DIFFERENCE_TO_AVERAGE = 1.5;
    
    // Size of the "Selected Window" canvas
    private static final int SELECTED_WINDOW_CANVAS_WIDTH = 300;
    private static final int SMALL_SELECTED_WINDOW_CANVAS_WIDTH = 200;
    private static final int SELECTED_WINDOW_CANVAS_HEIGHT = 60;
    private static final int SELECTED_WINDOW_BAR_WIDTH = 1;
    private static final double SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE = 10.0;
    
    // For the two "events" label (Max and min number of events in the selection), we force a width
    // This will prevent the control from moving horizontally if the number of events in the selection varies
    private static final int NB_EVENTS_FIXED_WIDTH = 50;
    
    
    // The "small font" height used to display time will be "default font" minus this constant
    private static final int SMALL_FONT_MODIFIER = 2;
    private static final int VERY_SMALL_FONT_MODIFIER = 4;
    
    // *** TODO ***
    // This need to be changed as soon the framework implement a "window"
    private static long DEFAULT_WINDOW_SIZE = (1L * 1000000000);
    
    // The last experiment received/used by the view
    private TmfExperiment<LttngEvent> lastUsedExperiment = null;
    
    // Parent of the view
    private Composite parent = null;
    
    // Request and canvas for the "full trace" part
    private HistogramRequest dataBackgroundFullRequest = null;
    private ParentHistogramCanvas fullExperimentCanvas = null;
    
    // Request and canvas for the "selected window"
	private HistogramRequest selectedWindowRequest = null;
    private ChildrenHistogramCanvas selectedWindowCanvas = null;
    
    // Content of the timeTextGroup
    //	Since the user can modify them with erroneous value, 
    //	we will keep track of the value internally 
	private Long selectedWindowTime = 0L;
	private Long selectedWindowTimerange = 0L;
	private Long currentEventTime = 0L;
	
    // *** All the UI control below
	//
	// NOTE : All textboxes will be READ_ONLY.
	//			So the user will be able to select/copy the value in them but not to change it
	private Text txtExperimentStartTime = null;
	private Text txtExperimentStopTime = null;
	
	private Text  txtWindowStartTime = null;
	private Text  txtWindowStopTime  = null;
	private Text  txtWindowMaxNbEvents = null;
	private Text  txtWindowMinNbEvents = null;
    
	private static final String WINDOW_TIMERANGE_LABEL_TEXT 	= "Window Timerange   ";
	private static final String WINDOW_CURRENT_TIME_LABEL_TEXT 	= "Cursor Centered on ";
	private static final String EVENT_CURRENT_TIME_LABEL_TEXT 	= "Current Event Time ";
	private TimeTextGroup  ntgTimeRangeWindow = null;
	private TimeTextGroup  ntgCurrentWindowTime = null;
	private TimeTextGroup  ntgCurrentEventTime = null;
	
	/**
	 * Default contructor of the view
	 */
	public HistogramView() {
		super(ID);
	}
	
	/**
	 * Create the UI controls of this view
	 * 
	 * @param  parent	The composite parent of this view
	 */
	@Override
	public void createPartControl(Composite newParent) {
		// Save the parent
		parent = newParent;
		
		// Default font
		Font font = parent.getFont();
		FontData tmpFontData = font.getFontData()[0];
		
		
		Font smallFont = null;
		int  nbEventWidth = -1; 
		int selectedCanvasWidth = -1;
		boolean doesTimeTextGroupNeedAdjustment = false;
		
		// Calculate if we need "small screen" fixes
		if ( parent.getDisplay().getBounds().width < SCREEN_SMALL_IF_SMALLER_THAN ) {
			// A lot smaller font for timstampe
			smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight() - VERY_SMALL_FONT_MODIFIER, tmpFontData.getStyle());
			
			// Smaller selection window canvas
			selectedCanvasWidth = SMALL_SELECTED_WINDOW_CANVAS_WIDTH;
			// Smaller event number text field
			nbEventWidth = NB_EVENTS_FIXED_WIDTH/2;
			
			// Tell the text group to ajust
			doesTimeTextGroupNeedAdjustment = true;
		}
		else {
			// Slightly smaller font for timestamp
			smallFont = new Font(font.getDevice(), tmpFontData.getName(), tmpFontData.getHeight() - SMALL_FONT_MODIFIER, tmpFontData.getStyle());
			// Usual size for selected window and event number text field
			nbEventWidth = NB_EVENTS_FIXED_WIDTH;
			selectedCanvasWidth = SELECTED_WINDOW_CANVAS_WIDTH;
			// No ajustement needed by the text group
			doesTimeTextGroupNeedAdjustment = false;
		}
		
		// Layout for the whole view, other elements will be in a child composite of this one 
		// Contains :
		// 		Composite layoutSelectionWindow
		//		Composite layoutTimesSpinner
		//		Composite layoutExperimentHistogram
		Composite layoutFullView = new Composite(parent, SWT.NONE);
		GridLayout gridFullView = new GridLayout();
		gridFullView.numColumns = 2;
		gridFullView.horizontalSpacing = 0;
		gridFullView.verticalSpacing = 0;
		gridFullView.marginHeight = 0;
		gridFullView.marginWidth = 0;
		layoutFullView.setLayout(gridFullView);
		
		
		// Layout that contain the SelectionWindow
		// Contains : 
		// 		Label lblWindowStartTime
		// 		Label lblWindowStopTime
		// 		Label lblWindowMaxNbEvents
		// 		Label lblWindowMinNbEvents
		// 		ChildrenHistogramCanvas selectedWindowCanvas
		Composite layoutSelectionWindow = new Composite(layoutFullView, SWT.NONE);
		GridLayout gridSelectionWindow = new GridLayout();
		gridSelectionWindow.numColumns = 3;
		gridSelectionWindow.marginHeight = 0;
		gridSelectionWindow.marginWidth = 0;
		gridSelectionWindow.horizontalSpacing = 0;
		gridSelectionWindow.verticalSpacing = 0;
		layoutSelectionWindow.setLayout(gridSelectionWindow);
		GridData gridDataSelectionWindow = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		layoutSelectionWindow.setLayoutData(gridDataSelectionWindow);
		
		
		// Layout that contain the time spinner
		// Contains : 
		// 		NanosecTextGroup  spTimeRangeWindow
		// 		NanosecTextGroup  spCurrentWindowTime
		// 		NanosecTextGroup  spCurrentEventTime
		Composite layoutTimesSpinner = new Composite(layoutFullView, SWT.NONE);
		GridLayout gridTimesSpinner = new GridLayout();
		gridTimesSpinner.numColumns = 3;
		gridTimesSpinner.marginHeight = 0;
		gridTimesSpinner.marginWidth = 0;
		gridTimesSpinner.horizontalSpacing = 0;
		gridTimesSpinner.verticalSpacing = 0;
		layoutTimesSpinner.setLayout(gridTimesSpinner);
		GridData gridDataTimesSpinner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		layoutTimesSpinner.setLayoutData(gridDataTimesSpinner);
		
		
		// Layout that contain the complete experiment histogram and related controls.
		// Contains : 
		//		Label lblExperimentStartTime
		//		Label lblExperimentStopTime
		// 		ParentHistogramCanvas fullTraceCanvas
		Composite layoutExperimentHistogram = new Composite(layoutFullView, SWT.NONE);
		GridLayout gridExperimentHistogram = new GridLayout();
		gridExperimentHistogram.numColumns = 2;
		gridExperimentHistogram.marginHeight = 0;
		gridExperimentHistogram.marginWidth = 0;
		gridExperimentHistogram.horizontalSpacing = 0;
		gridExperimentHistogram.verticalSpacing = 0;
		layoutExperimentHistogram.setLayout(gridExperimentHistogram);
		GridData gridDataExperimentHistogram = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		layoutExperimentHistogram.setLayoutData(gridDataExperimentHistogram);
		
		
		
		// *** Everything related to the selection window is below
		GridData gridDataSelectionWindowCanvas = new GridData(SWT.LEFT, SWT.TOP, true, false, 2, 2);
		gridDataSelectionWindowCanvas.heightHint = SELECTED_WINDOW_CANVAS_HEIGHT;
		gridDataSelectionWindowCanvas.minimumHeight = SELECTED_WINDOW_CANVAS_HEIGHT;
		gridDataSelectionWindowCanvas.widthHint = selectedCanvasWidth;
		gridDataSelectionWindowCanvas.minimumWidth = selectedCanvasWidth;
		selectedWindowCanvas = new ChildrenHistogramCanvas(this, layoutSelectionWindow, SWT.BORDER);
		selectedWindowCanvas.setLayoutData(gridDataSelectionWindowCanvas);
		
		GridData gridDataWindowMaxEvents = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		// Force a width, to avoid the control to enlarge if the number of events change
		gridDataWindowMaxEvents.minimumWidth = nbEventWidth;
		txtWindowMaxNbEvents = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowMaxNbEvents.setFont(smallFont);
		txtWindowMaxNbEvents.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowMaxNbEvents.setEditable(false);
		txtWindowMaxNbEvents.setText("");
		txtWindowMaxNbEvents.setLayoutData(gridDataWindowMaxEvents);
		
		GridData gridDataWindowMinEvents = new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1);
		// Force a width, to avoid the control to enlarge if the number of events change
		gridDataWindowMinEvents.minimumWidth = nbEventWidth;
		txtWindowMinNbEvents = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowMinNbEvents.setFont(smallFont);
		txtWindowMinNbEvents.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowMinNbEvents.setEditable(false);
		txtWindowMinNbEvents.setText("");
		txtWindowMinNbEvents.setLayoutData(gridDataWindowMinEvents);
		
		GridData gridDataWindowStart = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		txtWindowStartTime = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowStartTime.setFont(smallFont);
		txtWindowStartTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowStartTime.setEditable(false);
		txtWindowStartTime.setText("");
		txtWindowStartTime.setLayoutData(gridDataWindowStart);
		
		GridData gridDataWindowStop = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		txtWindowStopTime = new Text(layoutSelectionWindow, SWT.READ_ONLY);
		txtWindowStopTime.setFont(smallFont);
		txtWindowStopTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtWindowStopTime.setEditable(false);
		txtWindowStopTime.setText("");
		txtWindowStopTime.setLayoutData(gridDataWindowStop);
		
		GridData gridDataSpacer = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gridDataSpacer.minimumWidth = nbEventWidth;
		// *** HACK ***
		// To align properly AND to make sure the canvas size is fixed, we NEED to make sure all "section" of the 
		//		gridlayout are taken (and if possible of a fixed size).
		// However, SWT is VERY VERY DUMB and won't consider griddata that contain no control. 
		// Since there will be missing a section, the SelectedWindowCanvas + NbEventsText will take 3 spaces, but
		//		startTimeText + stopTimeText will take only 2 (as if empty the other griddata of 1 will get ignored).
		// StopTime will then take over the missing space; I want to align "stopTime" right on the end of canvas, so 
		// 		the added space to stop time would make it being aligned improperly
		// So I NEED the empty griddata to be considered! 
		// Visually : 
		// |---------------|---------------|-----------|
		// |SelectionCanvas SelectionCanvas|NbEventText|
		// |SelectionCanvas SelectionCanvas|NbEventText|
		// |---------------|---------------|-----------|
		// |StartTime      |       StopTime|    ???    |
		// |---------------|---------------|-----------|
		//
		// So since SWT will only consider griddata with control, 
		//		I need to create a totally useless control in the ??? section.
		// That's ugly, useless and it generally a bad practice.
		//
		// *** SUB-HACK ***
		// Other interesting fact about SWT : the way it draws (Fill/Expand control in grid) will change if 
		//		the control is a Text of a Label. 
		// A Label here will be "pushed" by startTime/stopTime Text and won't fill the full space as NbEventText.
		// A Text  here will NOT be "pushed" and would give a nice visual output.
		// 		(NB : No, I am NOT kidding, try it for yourself!)
		//
		// Soooooo I guess I will use a Text here. Way to go SWT!
		// Downside is that disabled textbox has a slightly different color (even if you change it) so if I want
		//		to make the text "invisible", I have to keep it editable, so it can be selected.
		//
		// Label uselessControlToByPassSWTStupidBug = new Label(layoutSelectionWindow, SWT.BORDER); // WON'T align correctly!!!
		Text uselessControlToByPassSWTStupidBug = new Text(layoutSelectionWindow, SWT.READ_ONLY); // WILL align correctly!!!
		uselessControlToByPassSWTStupidBug.setEditable(false);
		uselessControlToByPassSWTStupidBug.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		uselessControlToByPassSWTStupidBug.setLayoutData(gridDataSpacer);
		
		
		
		// *** Everything related to the time text group is below
		GridData gridDataCurrentEvent = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2);
		ntgCurrentEventTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, EVENT_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ), doesTimeTextGroupNeedAdjustment);
		ntgCurrentEventTime.setLayoutData(gridDataCurrentEvent);
		
		GridData gridDataTimeRange = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2);
		ntgTimeRangeWindow = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_TIMERANGE_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ), doesTimeTextGroupNeedAdjustment);
		ntgTimeRangeWindow.setLayoutData(gridDataTimeRange);
		
		GridData gridDataCurrentWindow = new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 2);
		ntgCurrentWindowTime = new TimeTextGroup(this, layoutTimesSpinner, SWT.BORDER, SWT.BORDER, WINDOW_CURRENT_TIME_LABEL_TEXT, HistogramConstant.formatNanoSecondsTime( 0L ), doesTimeTextGroupNeedAdjustment);
		ntgCurrentWindowTime.setLayoutData(gridDataCurrentWindow);
		
		
		
		// *** Everything related to the experiment canvas is below
		GridData gridDataExperimentCanvas = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		gridDataExperimentCanvas.heightHint = FULL_TRACE_CANVAS_HEIGHT;
		gridDataExperimentCanvas.minimumHeight = FULL_TRACE_CANVAS_HEIGHT;
		fullExperimentCanvas = new ParentHistogramCanvas(this, layoutExperimentHistogram, SWT.BORDER);
		fullExperimentCanvas.setLayoutData(gridDataExperimentCanvas);
		
		GridData gridDataExperimentStart = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		txtExperimentStartTime = new Text(layoutExperimentHistogram, SWT.READ_ONLY);
		txtExperimentStartTime.setFont(smallFont);
		txtExperimentStartTime.setText("");
		txtExperimentStartTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtExperimentStartTime.setEditable(false);
		txtExperimentStartTime.setLayoutData(gridDataExperimentStart);
		
		GridData gridDataExperimentStop = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		txtExperimentStopTime = new Text(layoutExperimentHistogram, SWT.READ_ONLY);
		txtExperimentStopTime.setFont(smallFont);
		txtExperimentStopTime.setText("");
		txtExperimentStopTime.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
		txtExperimentStopTime.setEditable(false);
		txtExperimentStopTime.setLayoutData(gridDataExperimentStop);
	}
	
	// *** FIXME ***
	// This is mainly used because of a because in the "experimentSelected()" notification, we shouldn't need this
	/**
	 * Method called when the view receive the focus.<p>
	 * If ExperimentSelected didn't send us a request yet, get the current Experiment and fire requests
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setFocus() {
		// WARNING : This does not seem to be thread safe
		TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)TmfExperiment.getCurrentExperiment();
		
		if ( (dataBackgroundFullRequest == null) && (tmpExperiment != null) ) {
			createCanvasAndRequests(tmpExperiment);
		}
		
		// Call a redraw for everything
		parent.redraw();
	}
	
	/**
	 * Method called when the user select (double-click on) an experiment.<p>
	 * We will create the needed canvas and fire the requests.
	 * 
	 * @param signal	Signal received from the framework. Contain the experiment.
	 */
    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<LttngEvent> signal) {
    	TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)signal.getExperiment();
    	createCanvasAndRequests(tmpExperiment);
    }
    
    // *** VERIFY ***
	// Not sure what this should do since I don't know when it will be called
	// Let's do the same thing as experimentSelected for now
	//
    /**
	 * Method called when an experiment is updated (??).<p>
	 * ...for now, do nothing, as udating an experiment running in the background might cause crash 
	 * 
	 * @param signal	Signal received from the framework. Contain the experiment.
	 */
//    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
//    	
//    	TmfExperiment<LttngEvent> tmpExperiment = (TmfExperiment<LttngEvent>)signal.getExperiment();
//    	
//    	// Make sure the UI object are sane
//		resetControlsContent();
//		
//		// Redraw the canvas right away to have something "clean" as soon as we can
//		fullExperimentCanvas.redraw();
//		selectedWindowCanvas.redraw();
//		
//		// Recreate the request
//		createCanvasAndRequests(tmpExperiment);
    }
    
    /**
     * Method called when synchonization is active and that the user select an event.<p>
     * We update the current event timeTextGroup and move the selected window if needed.
     * 
     * @param signal	Signal received from the framework. Contain the event.
     */
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
    	// In case we received our own signal
    	if (signal.getSource() != this) {
            TmfTimestamp currentTime = signal.getCurrentTime();
            
            // Update the current event controls
            currentEventTime = currentTime.getValue();
            updateSelectedEventTime();
            
            // If the given event is outside the selection window, recenter the window
            if ( isGivenTimestampInSelectedWindow( currentEventTime ) == false) {
            	fullExperimentCanvas.centerWindow( fullExperimentCanvas.getHistogramContent().getClosestXPositionFromTimestamp(currentEventTime) );
            	// Notify control that the window changed
            	windowChangedNotification();
            }
    	}
    }
    
    /*
     * Create the canvas needed and issue the requests
     * 
     * @param newExperiment	Experiment we will use for the request
     */
    private void createCanvasAndRequests(TmfExperiment<LttngEvent> newExperiment) {
    	lastUsedExperiment = newExperiment;
    	
    	TmfExperiment<LttngEvent> experimentCopy = newExperiment.createTraceCopy();
    	
    	// Create the content for the full experiment. 
    	// This NEED to be created first, as we use it in the selectedWindowCanvas
		fullExperimentCanvas.createNewHistogramContent(fullExperimentCanvas.getSize().x, FULL_TRACE_BAR_WIDTH, FULL_TRACE_CANVAS_HEIGHT, FULL_TRACE_DIFFERENCE_TO_AVERAGE);
		fullExperimentCanvas.createNewSelectedWindow(DEFAULT_WINDOW_SIZE);
    	// Set the window of the fullTrace canvas visible.
		fullExperimentCanvas.getCurrentWindow().setSelectedWindowVisible(true);
		fullExperimentCanvas.getHistogramContent().resetTable(newExperiment.getStartTime().getValue(), newExperiment.getEndTime().getValue());
		
		// Create the content for the selected window. 
		selectedWindowCanvas.createNewHistogramContent(selectedWindowCanvas.getSize().x ,SELECTED_WINDOW_BAR_WIDTH, SELECTED_WINDOW_CANVAS_HEIGHT, SELECTED_WINDOW_DIFFERENCE_TO_AVERAGE);
		selectedWindowCanvas.getHistogramContent().resetTable(fullExperimentCanvas.getCurrentWindow().getTimestampLeft(), fullExperimentCanvas.getCurrentWindow().getTimestampRight());
		
		// Make sure the UI object are sane
		resetControlsContent();
		
		// Redraw the canvas right away to have something "clean" as soon as we can
    	if ( dataBackgroundFullRequest != null ) {
    		fullExperimentCanvas.redraw();
    		selectedWindowCanvas.redraw();
    	}
    	// Nullify the (possible) old request to be sure we start we something clean
    	// Note : this is very important for the order of the request below, 
    	//	see "TODO" in performSelectedWindowEventsRequest
    	dataBackgroundFullRequest = null;
    	selectedWindowRequest = null;
		
		// Perform both request. 
		// Order is important here, the small/synchronous request for the selection window should go first
		performSelectedWindowEventsRequest(newExperiment);
		performAllTraceEventsRequest(experimentCopy);
    }
    
    /**
     * Perform a new request for the Selection window.<p>
     * This assume the full experiment canvas has correct information about the selected window; 
     * 		we need the fullExperimentCanvas' HistogramContent to be created and a selection window to be set.
     * 
     * @param experiment	The experiment we will select from
     */
    public void performSelectedWindowEventsRequest(TmfExperiment<LttngEvent> experiment) {
    	
    	HistogramSelectedWindow curSelectedWindow = fullExperimentCanvas.getCurrentWindow();
    	
    	// If no selection window exists, we will try to cerate one; 
    	//	however this will most likely fail as the content is probably not created either
    	if ( curSelectedWindow == null ) {
    		fullExperimentCanvas.createNewSelectedWindow( DEFAULT_WINDOW_SIZE );
    		curSelectedWindow = fullExperimentCanvas.getCurrentWindow();
    	}
    	
    	// The request will go from the Left timestamp of the window to the Right timestamp
    	// This assume that out-of-bound value are handled by the SelectionWindow itself
		LttngTimestamp ts1 = new LttngTimestamp( curSelectedWindow.getTimestampLeft() );
		LttngTimestamp ts2 = new LttngTimestamp( curSelectedWindow.getTimestampRight() );
        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
        
        // Set a (dynamic) time interval
        long intervalTime = ( (ts2.getValue() - ts1.getValue()) / selectedWindowCanvas.getHistogramContent().getNbElement() );
        
        selectedWindowRequest = performRequest(experiment, selectedWindowCanvas, tmpRange, intervalTime);
        selectedWindowCanvas.redrawAsynchronously();
    }
    
    /**
     * Perform a new request for the full experiment.<p>
     * NOTE : this is very long, we need to implement a way to run this in parallel (see TODO)
     * 
     * @param experiment	The experiment we will select from
     */
    public void performAllTraceEventsRequest(TmfExperiment<LttngEvent> experiment) {
    	// Create a new time range from "start" to "end"
        //	That way, we will get "everything" in the trace
        LttngTimestamp ts1 = new LttngTimestamp( experiment.getStartTime() );
        LttngTimestamp ts2 = new LttngTimestamp( experiment.getEndTime() );
        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
        
        // Set a (dynamic) time interval
        long intervalTime = ( (ts2.getValue() - ts1.getValue()) / fullExperimentCanvas.getHistogramContent().getNbElement() );
        
        // *** VERIFY ***
        // This would enable "fixed interval" instead of dynamic one.
        // ... we don't need it, do we?
        //
        // long intervalTime = ((long)(0.001 * (double)1000000000));
        
        // *** TODO ***
        // It would be interesting if there was a way to tell the framework to run the request "in parallel" here.
        // Mean a completetly independant copy of the Expereiment would be done and we would proceed on that.
        //
        dataBackgroundFullRequest = performRequest(experiment, fullExperimentCanvas, tmpRange, intervalTime);
        fullExperimentCanvas.redrawAsynchronously();
    }
    
    // *** VERIFY ***
    // This function is synchronized, is it a good idea?
    // Tis is done to make sure requests arrive somewhat in order, 
    //	this is especially important when request are issued from different thread.
    /**
     * Create a new request from the given data and send it to the framework.<p>
     * The request will be queued and processed later.
     * 
     * @param experiment 		The experiment we will process the request on
     * @param targetCanvas		The canvas that will received the result
     * @param newRange			The range of the request
     * @param newInterval		The interval of time we use to store the result into the HistogramContent
     */
    public synchronized HistogramRequest performRequest(TmfExperiment<LttngEvent> experiment, HistogramCanvas targetCanvas, TmfTimeRange newRange, long newInterval) {
    	HistogramRequest returnedRequest = null;
    	
        // *** FIXME ***
        // EVIL BUG!
	    // We use integer.MAX_VALUE because we want every events BUT we don't know the number inside the range.
        // HOWEVER, this would cause the request to run forever (or until it reach the end of trace).
        // Seeting an EndTime does not seems to stop the request
        returnedRequest = new HistogramRequest(newRange, Integer.MAX_VALUE, targetCanvas, newInterval );
        
        // Send the request to the framework : it will be queued and processed later
        experiment.sendRequest(returnedRequest);
        
        return returnedRequest;
    }
    
    /**
     * Function used to warn that the selection window changed.<p>
     * This might be called because the window moved or because its size changed.<p>
     * 
     * We will update the different control related to the selection window.
     */
    public void windowChangedNotification() {
    	
    	if ( lastUsedExperiment != null ) {
    		// If a request is ongoing, try to stop it
    		if ( selectedWindowRequest.isCompleted() == false ) {
    			selectedWindowRequest.cancel();
    		}
    		
    		// Get the latest window information
    		selectedWindowTime = fullExperimentCanvas.getCurrentWindow().getTimestampCenter();
    		selectedWindowTimerange = fullExperimentCanvas.getCurrentWindow().getWindowTimeWidth();
    		
    		// If the current event time is outside the new window, change the current event
    		//		The new current event will be the one closest to the center of the new window
    		if ( isGivenTimestampInSelectedWindow(currentEventTime) == false ) {
    			currentEventChangeNotification( selectedWindowTime );
    		}
    		
    		// Perform a new request to read data about the new window
    		performSelectedWindowEventsRequest(lastUsedExperiment);
    	}
    }
    
    /**
     * Function used to tell that the current event changed.<p>
     * This might be called because the user changed the current event or
     * 		because the last current event is now outside the selection window.<p>
     * 
     * We update the related control and send a signal to notify other views of the new current event.
     * 
     * @param newCurrentEventTime
     */
    public void currentEventChangeNotification(Long newCurrentEventTime) {
    	// Notify other views in the framework
        if (currentEventTime != newCurrentEventTime) {
        	currentEventTime = newCurrentEventTime;
        	
        	// Update the UI control
        	updateSelectedEventTime();
        	
        	// Send a signal to the framework
            LttngTimestamp tmpTimestamp = new LttngTimestamp(newCurrentEventTime);
            broadcast(new TmfTimeSynchSignal(this, tmpTimestamp));
        }
    }
    
    /**
     * Function that will be called when one of the time text group value is changed.<p>
     * Since we don't (and can't unless we subclass them) know which one, we check them all.
     */
    public void timeTextGroupChangeNotification() {
    	
    	// Get all the time text group value
    	Long newCurrentTime = ntgCurrentEventTime.getValue();
    	Long newSelectedWindowTime = ntgCurrentWindowTime.getValue();
    	Long newSelectedWindowTimeRange = ntgTimeRangeWindow.getValue();
    	
    	// If the user changed the current event time, call the notification
    	if ( newCurrentTime != currentEventTime ) {
    		currentEventChangeNotification( newCurrentTime );
    	}
    	
    	// If the user changed the selected window time, recenter the window and call the notification
    	if ( newSelectedWindowTime != selectedWindowTime ) {
    		selectedWindowTime = newSelectedWindowTime;
    		fullExperimentCanvas.centerWindow( fullExperimentCanvas.getHistogramContent().getClosestXPositionFromTimestamp(selectedWindowTime) );
    		windowChangedNotification();
    	}
    	
    	// If the user changed the selected window size, resize the window and call the notification
    	if ( newSelectedWindowTimeRange != selectedWindowTimerange ) {
    		selectedWindowTimerange = newSelectedWindowTimeRange;
    		fullExperimentCanvas.resizeWindowByAbsoluteTime(selectedWindowTimerange);
    		windowChangedNotification();
    	}
    	
    }
    
    /**
     * Getter for the last used experiment.<p>
     * This might be different than the current experiment or even null.
     * 
     * @return	the last experiment we used in this view
     */
	public TmfExperiment<LttngEvent> getLastUsedExperiment() {
		return lastUsedExperiment;
	}
	
	/**
	 * Check if a given timestamp is inside the selection window.<p>
	 * This assume fullExperimentCanvas contain a valid HistogramContent
	 * 
	 * @param timestamp	the timestamp to check
	 * 
	 * @return	if the time is inside the selection window or not
	 */
	public boolean isGivenTimestampInSelectedWindow(Long timestamp) {
		boolean returnedValue = true;
		
		// If the content is not set correctly, this will return weird (or even null) result
		if ( (timestamp < fullExperimentCanvas.getCurrentWindow().getTimestampLeft()  ) ||
	         (timestamp > fullExperimentCanvas.getCurrentWindow().getTimestampRight() ) ) 
		{
			returnedValue = false;
		}
		
		return returnedValue;
	}
	
	/**
	 * Reset the content of all Controls.<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void resetControlsContent() {
		
		TmfExperiment<LttngEvent> tmpExperiment = getLastUsedExperiment();
		
		// Use the previous Start and End time, or default if they are not available
		String startTime = null;
		String stopTime = null;
		if ( tmpExperiment != null ) {
			startTime = HistogramConstant.formatNanoSecondsTime( tmpExperiment.getStartTime().getValue() );
			stopTime = HistogramConstant.formatNanoSecondsTime( tmpExperiment.getEndTime().getValue() );
		}
		else {
			startTime = HistogramConstant.formatNanoSecondsTime( 0L );
			stopTime = HistogramConstant.formatNanoSecondsTime( 0L );
		}
		
    	txtExperimentStartTime.setText( startTime );
		txtExperimentStopTime.setText( stopTime );
		txtExperimentStartTime.getParent().layout();
		
		txtWindowMaxNbEvents.setText("" + 0);
		txtWindowMinNbEvents.setText("" + 0);
		txtWindowStartTime.setText( HistogramConstant.formatNanoSecondsTime( 0L ) );
		txtWindowStopTime.setText( HistogramConstant.formatNanoSecondsTime( 0L ) );
		txtWindowStartTime.getParent().layout();
		
		ntgCurrentWindowTime.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
		ntgTimeRangeWindow.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
		ntgCurrentEventTime.setValue( HistogramConstant.formatNanoSecondsTime( 0L ) );
	}
	
	/**
	 * Update the content of the controls related to the full experiment canvas<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void updateFullExperimentInformation() {
		
		String startTime = HistogramConstant.formatNanoSecondsTime( fullExperimentCanvas.getHistogramContent().getStartTime() );
		String stopTime = HistogramConstant.formatNanoSecondsTime( fullExperimentCanvas.getHistogramContent().getEndTime() );
		
		txtExperimentStartTime.setText( startTime );
		txtExperimentStopTime.setText( stopTime );
		
		// Take one of the parent and call its layout to update control size
		// Since both control have the same parent, only one call is needed 
		txtExperimentStartTime.getParent().layout();
		
		// Update the selected window, just in case
		// This should give a better user experience and it is low cost 
		updateSelectedWindowInformation();
	}
	
	/**
	 * Update the content of the controls related to the selection window canvas<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void updateSelectedWindowInformation() {
		// Update the timestamp as well
		updateSelectedWindowTimestamp();
		
		txtWindowMaxNbEvents.setText( selectedWindowCanvas.getHistogramContent().getHeighestEventCount().toString() );
		txtWindowMinNbEvents.setText("0");
		
		// Refresh the layout
		txtWindowMaxNbEvents.getParent().layout();
	}
	
	/**
	 * Update the content of the controls related to the timestamp of the selection window<p>
	 * WARNING : Calls in there are not thread safe and can't be called from different thread than "main"
	 */
	public void updateSelectedWindowTimestamp() {
		String startTime = HistogramConstant.formatNanoSecondsTime( selectedWindowCanvas.getHistogramContent().getStartTime() );
		String stopTime = HistogramConstant.formatNanoSecondsTime( selectedWindowCanvas.getHistogramContent().getEndTime() );
		txtWindowStartTime.setText( startTime );
		txtWindowStopTime.setText( stopTime );
		
		ntgCurrentWindowTime.setValue( fullExperimentCanvas.getCurrentWindow().getTimestampCenter() );
		ntgTimeRangeWindow.setValue(  fullExperimentCanvas.getCurrentWindow().getWindowTimeWidth() );
		
		// If the current event time is outside the selection window, recenter our window 
		if ( isGivenTimestampInSelectedWindow(ntgCurrentEventTime.getValue()) == false ) {
			currentEventChangeNotification( fullExperimentCanvas.getCurrentWindow().getTimestampCenter() );
		}
		
		// Take one control in each group to call to refresh the layout
		// Since both control have the same parent, only one call is needed 
		txtWindowStartTime.getParent().layout();
		ntgCurrentWindowTime.getParent().layout();
	}
	
	/**
	 * Update the controls related current event.<p>
	 * The call here SHOULD be thread safe and can be call from any threads.
	 */
	public void updateSelectedEventTime() {
    	ntgCurrentEventTime.setValueAsynchronously(currentEventTime);
    	// Tell the selection canvas which event is currently selected
    	// This give a nice graphic output
    	selectedWindowCanvas.getHistogramContent().setSelectedEventTimeInWindow(currentEventTime);
    	selectedWindowCanvas.redrawAsynchronously();
	}
	
	/**
	 * Method called when the view is moved.<p>
	 * 
	 * Just redraw everything...
	 * 
	 * @param event 	The controle event generated by the move.
	 */
	public void controlMoved(ControlEvent event) {
		parent.redraw();
	}
	
	/**
	 * Method called when the view is resized.<p>
	 * 
	 * We will make sure that the size didn't change more than the content size.<p>
	 * Otherwise we need to perform a new request for the full experiment because we are missing data).
	 * 
	 * @param event 	The control event generated by the resize.
	 */
	public void controlResized(ControlEvent event) {
		
		// Ouch! The screen enlarged (screen resolution changed?) so far that we miss content to fill the space.
		// Perform a new full request... this is quite heavy.
		if ( parent.getDisplay().getBounds().width > fullExperimentCanvas.getHistogramContent().getNbElement() ) {
			if ( lastUsedExperiment != null ) {
				performAllTraceEventsRequest(lastUsedExperiment);
			}
		}
		
	}
}
