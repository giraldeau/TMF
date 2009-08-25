/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.eventlog;

import static org.junit.Assert.*;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeWindow;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfEventRequestTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventRequestTest {

    private static ITmfRequestHandler fProcessor = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        fProcessor = new TmfRequestHandlerStub();
    }

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testConstructorForRange() throws Exception {
        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfEventRequest request = new TmfEventRequest(range, 0, -1, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",             0, request.getOffset());
        assertEquals("NbRequestedEvents", -1, request.getNbRequestedEvents());
    }

    @Test
    public void testConstructorForNbEvents() throws Exception {
        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfEventRequest request = new TmfEventRequest(range, 0, 10, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",             0, request.getOffset());
        assertEquals("NbRequestedEvents", 10, request.getNbRequestedEvents());
    }

    @Test
    public void testConstructorWithOffset() throws Exception {
        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfEventRequest request = new TmfEventRequest(range, 5, 10, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",             5, request.getOffset());
        assertEquals("NbRequestedEvents", 10, request.getNbRequestedEvents());
    }

    @Test
    public void testConstructorWithNegativeOffset() throws Exception {
        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfEventRequest request = new TmfEventRequest(range, -5, 10, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",            -5, request.getOffset());
        assertEquals("NbRequestedEvents", 10, request.getNbRequestedEvents());
    }

    // ========================================================================
    // process
    // ========================================================================

    @Test
    public void testProcessRequestForNbEvents() throws Exception {

        final int NB_EVENTS  = 10 * 1000;
        final int BLOCK_SIZE = 100;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.process(request, true);

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    @Test
    public void testProcessRequestForAllEvents() throws Exception {

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.process(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // @Test
    public void testProcessRequestWithOffset() throws Exception {

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final int OFFSET = 5;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.process(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i + OFFSET, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // l@Test
    public void testProcessRequestWithNegativeOffset() throws Exception {

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final int OFFSET = -5;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.process(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i + OFFSET, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // ========================================================================
    // cancel
    // ========================================================================

    @Test
    public void testCancel() throws Exception {

        final int NB_EVENTS  = 10 * 1000;
        final int BLOCK_SIZE = 100;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
                // Cancel request after the first chunk is received
                cancel();
            }
        };
        fProcessor.process(request, true);

        assertEquals("nbEvents",  BLOCK_SIZE, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}
