/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.trace;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfTraceTest</u></b>
 * <p>
 * Test suite for the TmfTrace class.
 */
public class TmfTraceTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";
    private static final int    BLOCK_SIZE  = 500;
    private static final int    NB_EVENTS   = 10000;
    private static TmfTraceStub fTrace      = null;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    public TmfTraceTest(String name) throws Exception {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();	
		fTrace = setupTrace(DIRECTORY + File.separator + TEST_STREAM);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		fTrace.dispose();
		fTrace = null;
	}

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private TmfTraceStub setupTrace(String path) {
    	if (fTrace == null) {
    		try {
    	        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
    			File test = new File(FileLocator.toFileURL(location).toURI());
    	        fTrace = new TmfTraceStub(test.getPath(), BLOCK_SIZE, false);
    	        fTrace.indexTrace(true);
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	return fTrace;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public void testTmfTraceDefault() throws Exception {
		TmfTraceStub trace = null;
		File testfile = null;
		try {
	        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
			testfile = new File(FileLocator.toFileURL(location).toURI());
			trace = new TmfTraceStub(testfile.getPath());
		} catch (URISyntaxException e) {
			fail("URISyntaxException");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue  ("Open trace",   trace != null);
		assertEquals("getType",      TmfEvent.class, trace.getType());
		assertEquals("getPath",      testfile.getPath(), trace.getPath());
		assertEquals("getName",      TEST_STREAM, trace.getName());
		assertEquals("getCacheSize", TmfTrace.DEFAULT_CACHE_SIZE, trace.getCacheSize());
    }

    public void testTmfTraceDefaultCacheSize() throws Exception {
		TmfTraceStub trace = null;
		File testfile = null;
		try {
	        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
			testfile = new File(FileLocator.toFileURL(location).toURI());
			trace = new TmfTraceStub(testfile.getPath(), 0);
		} catch (URISyntaxException e) {
			fail("URISyntaxException");
		} catch (IOException e) {
			fail("IOException");
		}
		assertTrue  ("Open trace",   trace != null);
		assertEquals("getType",      TmfEvent.class, trace.getType());
		assertEquals("getPath",      testfile.getPath(), trace.getPath());
		assertEquals("getName",      TEST_STREAM, trace.getName());
		assertEquals("getCacheSize", TmfTrace.DEFAULT_CACHE_SIZE, trace.getCacheSize());
    }

    public void testTmfTrace() throws Exception {
		assertEquals("getType",      TmfEvent.class, fTrace.getType());
		assertEquals("getName",      TEST_STREAM,    fTrace.getName());
		assertEquals("getCacheSize", BLOCK_SIZE,     fTrace.getCacheSize());
    }

    public void testClone() throws Exception {
    	TmfTraceStub trace = fTrace.clone();
		assertEquals("getType",      TmfEvent.class,        trace.getType());
		assertEquals("getPath",      fTrace.getPath(),      trace.getPath());
		assertEquals("getName",      TEST_STREAM,           trace.getName());
		assertEquals("getCacheSize", BLOCK_SIZE,            trace.getCacheSize());
		assertEquals("getTimeRange", fTrace.getTimeRange(), trace.getTimeRange());
    }

    // ------------------------------------------------------------------------
    // Get/Set time range
    // ------------------------------------------------------------------------

    public void testSetTimeRange() throws Exception {
    	TmfTraceStub trace = fTrace.clone();
    	
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setTimeRange(new TmfTimeRange(new TmfTimestamp(100), new TmfTimestamp(200)));
        assertEquals("setTimeRange",   100, trace.getTimeRange().getStartTime().getValue());
        assertEquals("setTimeRange",   200, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setTimeRange",   100, trace.getStartTime().getValue());
        assertEquals("setTimeRange",   200, trace.getEndTime().getValue());
    }
    
    public void testSetStartTime() throws Exception {
    	TmfTraceStub trace = fTrace.clone();
    	
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setStartTime(new TmfTimestamp(100));
        assertEquals("setStartTime",   100,       trace.getTimeRange().getStartTime().getValue());
        assertEquals("setStartTime",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setStartTime",   100,       trace.getStartTime().getValue());
        assertEquals("setStartTime",   NB_EVENTS, trace.getEndTime().getValue());
    }
    
    public void testSetEndTime() throws Exception {
    	TmfTraceStub trace = fTrace.clone();
    	
        assertEquals("getRange-start", 1,         trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,         trace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS, trace.getEndTime().getValue());

        trace.setEndTime(new TmfTimestamp(100));
        assertEquals("setEndTime",     1,   trace.getTimeRange().getStartTime().getValue());
        assertEquals("setEndTime",     100, trace.getTimeRange().getEndTime().getValue());
        assertEquals("setEndTime",     1,   trace.getStartTime().getValue());
        assertEquals("setEndTime",     100, trace.getEndTime().getValue());
    }
    
    // ------------------------------------------------------------------------
    // Verify checkpoints
    // ------------------------------------------------------------------------

    public void testTmfTraceIndexing() throws Exception {
        assertEquals("getCacheSize",   BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals("getTraceSize",   NB_EVENTS,  fTrace.getNbEvents());
        assertEquals("getRange-start", 1,          fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS,  fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime",   1,          fTrace.getStartTime().getValue());
        assertEquals("getEndTime",     NB_EVENTS,  fTrace.getEndTime().getValue());

    	Vector<TmfCheckpoint> checkpoints = fTrace.getCheckpoints();
    	int pageSize = fTrace.getCacheSize();
		assertTrue("Checkpoints exist",  checkpoints != null);

		// Validate that each checkpoint points to the right event
		for (int i = 0; i < checkpoints.size(); i++) {
			TmfCheckpoint checkpoint = checkpoints.get(i);
			TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
			TmfEvent event = fTrace.parseEvent(context);
			assertTrue(context.getRank() == i * pageSize);
			assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
		}
    }

    // ------------------------------------------------------------------------
    // parseEvent - make sure parseEvent doesn't update the context
    // ------------------------------------------------------------------------

    public void testParseEvent() throws Exception {

    	// On lower bound, returns the first event (ts = 0)
    	TmfContext context = fTrace.seekEvent(new TmfTimestamp(0, SCALE, 0));

    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 1; i < 20; i++) {
            event = fTrace.getNextEvent(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 20, event.getTimestamp().getValue());

        event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 20, event.getTimestamp().getValue());
    }

    // ------------------------------------------------------------------------
    // getNextEvent - updates the context
    // ------------------------------------------------------------------------

    public void testGetNextEvent() throws Exception {

    	// On lower bound, returns the first event (ts = 0)
    	TmfContext context = fTrace.seekEvent(new TmfTimestamp(0, SCALE, 0));
        TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 2; i < 20; i++) {
            event = fTrace.getNextEvent(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }
    }

    // ------------------------------------------------------------------------
    // seekLocation
    // Note: seekLocation() does not reliably set the rank
    // ------------------------------------------------------------------------

    public void testSeekLocationOnCacheBoundary() throws Exception {

    	// Position trace at event rank 0
    	TmfContext context = fTrace.seekLocation(null);
        assertEquals("Event rank", 0, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

    	// Position trace at event rank 1000
        TmfContext tmpContext = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

    	// Position trace at event rank 4000
        tmpContext = fTrace.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    public void testSeekLocationNotOnCacheBoundary() throws Exception {

    	// Position trace at event rank 9
    	TmfContext tmpContext = fTrace.seekEvent(new TmfTimestamp(10, SCALE, 0));
        TmfContext context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

    	// Position trace at event rank 999
        tmpContext = fTrace.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

    	// Position trace at event rank 1001
        tmpContext = fTrace.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

    	// Position trace at event rank 4500
        tmpContext = fTrace.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    public void testSeekLocationOutOfScope() throws Exception {

    	// Position trace at beginning
    	TmfContext tmpContext = fTrace.seekLocation(null);
        TmfContext context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());

    	// Position trace at event passed the end
        tmpContext = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        context = fTrace.seekLocation(tmpContext.getLocation().clone());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent on timestamp
    // ------------------------------------------------------------------------

    public void testSeekEventOnTimestampOnCacheBoundary() throws Exception {

    	// Position trace at event rank 0
    	TmfContext context = fTrace.seekEvent(new TmfTimestamp(1, SCALE, 0));
        assertEquals("Event rank", 0, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

    	// Position trace at event rank 1000
        context = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        assertEquals("Event rank", 1000, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

    	// Position trace at event rank 4000
        context = fTrace.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        assertEquals("Event rank", 4000, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    public void testSeekEventOnTimestampNotOnCacheBoundary() throws Exception {

    	// Position trace at event rank 1
    	TmfContext context = fTrace.seekEvent(new TmfTimestamp(2, SCALE, 0));
        assertEquals("Event rank", 1, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 2, context.getRank());

    	// Position trace at event rank 9
    	context = fTrace.seekEvent(new TmfTimestamp(10, SCALE, 0));
        assertEquals("Event rank", 9, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

    	// Position trace at event rank 999
        context = fTrace.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        assertEquals("Event rank", 999, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

    	// Position trace at event rank 1001
        context = fTrace.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        assertEquals("Event rank", 1001, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

    	// Position trace at event rank 4500
        context = fTrace.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        assertEquals("Event rank", 4500, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    public void testSeekEventOnTimestampOutOfScope() throws Exception {

    	// Position trace at beginning
    	TmfContext context = fTrace.seekEvent(new TmfTimestamp(-1, SCALE, 0));
        assertEquals("Event rank", 0, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

    	// Position trace at event passed the end
        context = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // seekEvent on rank
    // ------------------------------------------------------------------------

    public void testSeekOnRankOnCacheBoundary() throws Exception {

    	// On lower bound, returns the first event (ts = 1)
    	TmfContext context = fTrace.seekEvent(0);
        assertEquals("Event rank", 0, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

    	// Position trace at event rank 1000
        context = fTrace.seekEvent(1000);
        assertEquals("Event rank", 1000, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

    	// Position trace at event rank 4000
        context = fTrace.seekEvent(4000);
        assertEquals("Event rank", 4000, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    public void testSeekOnRankNotOnCacheBoundary() throws Exception {

    	// Position trace at event rank 9
    	TmfContext context = fTrace.seekEvent(9);
        assertEquals("Event rank", 9, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

    	// Position trace at event rank 999
        context = fTrace.seekEvent(999);
        assertEquals("Event rank", 999, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

    	// Position trace at event rank 1001
        context = fTrace.seekEvent(1001);
        assertEquals("Event rank", 1001, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

    	// Position trace at event rank 4500
        context = fTrace.seekEvent(4500);
        assertEquals("Event rank", 4500, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    public void testSeekEventOnRankOfScope() throws Exception {

    	// Position trace at beginning
    	TmfContext context = fTrace.seekEvent(-1);
        assertEquals("Event rank", 0, context.getRank());
    	TmfEvent event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

    	// Position trace at event passed the end
        context = fTrace.seekEvent(NB_EVENTS);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    	event = fTrace.parseEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    	event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
        assertEquals("Event rank", NB_EVENTS, context.getRank());
    }
    
    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
	public void testProcessRequestForNbEvents() throws Exception {
        final int BLOCK_SIZE = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    @SuppressWarnings("unchecked")
	public void testProcessRequestForAllEvents() throws Exception {
        final int BLOCK_SIZE =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
	public void testCancel() throws Exception {
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, NB_EVENTS) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
                // Cancel request after the first chunk is received
                cancel();
            }
        };
        ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}