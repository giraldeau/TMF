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

package org.eclipse.linuxtools.tmf.tests.request;


import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequestStub;

/**
 * <b><u>TmfCoalescedDataRequestTest</u></b>
 * <p>
 * Test suite for the TmfCoalescedDataRequest class.
 */
@SuppressWarnings("nls")
public class TmfCoalescedDataRequestTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private static TmfCoalescedDataRequest<TmfEvent> fRequest1;
	private static TmfCoalescedDataRequest<TmfEvent> fRequest2;
	private static TmfCoalescedDataRequest<TmfEvent> fRequest3;
	private static TmfCoalescedDataRequest<TmfEvent> fRequest4;

	private static TmfCoalescedDataRequest<TmfEvent> fRequest1b;
	private static TmfCoalescedDataRequest<TmfEvent> fRequest1c;

	private static int fRequestCount;
	
	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	public TmfCoalescedDataRequestTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		TmfDataRequest.reset();
		fRequest1  = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200);
		fRequest2  = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 20, 100, 200);
		fRequest3  = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 20, 200, 200);
		fRequest4  = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 20, 200, 300);

		fRequest1b = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200);
		fRequest1c = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200);

		fRequestCount = fRequest1c.getRequestId() + 1;
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private TmfCoalescedDataRequest<TmfEvent> setupTestRequest(final boolean[] flags) {
		
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200) {
		    @Override
			public void handleCompleted() {
		    	super.handleCompleted();
		    	flags[0] = true;
		    }
		    @Override
		    public void handleSuccess() {
		    	super.handleSuccess();
		    	flags[1] = true;
		    }
		    @Override
		    public void handleFailure() {
		    	super.handleFailure();
		    	flags[2] = true;
		    }
		    @Override
		    public void handleCancel() {
		    	super.handleCancel();
		    	flags[3] = true;
		    }
		};
		return request;
	}
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testTmfCoalescedDataRequest() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfCoalescedDataRequestIndex() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfCoalescedDataRequestIndexNbRequested() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfCoalescedDataRequestIndexNbEventsBlocksize() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
        assertTrue("equals", fRequest1.equals(fRequest1));
        assertTrue("equals", fRequest2.equals(fRequest2));

        assertFalse("equals", fRequest1.equals(fRequest2));
        assertFalse("equals", fRequest2.equals(fRequest1));
	}

	public void testEqualsSymmetry() throws Exception {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1));

        assertFalse("equals", fRequest1.equals(fRequest3));
        assertFalse("equals", fRequest2.equals(fRequest3));
        assertFalse("equals", fRequest3.equals(fRequest1));
        assertFalse("equals", fRequest3.equals(fRequest2));
	}
	
	public void testEqualsTransivity() throws Exception {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1c));
        assertTrue("equals", fRequest1.equals(fRequest1c));
	}
	
	public void testEqualsNull() throws Exception {
        assertFalse("equals", fRequest1.equals(null));
        assertFalse("equals", fRequest2.equals(null));
	}

	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
        assertTrue("hashCode", fRequest1.hashCode() == fRequest1.hashCode());
        assertTrue("hashCode", fRequest2.hashCode() == fRequest2.hashCode());
		assertTrue("hashCode", fRequest1.hashCode() != fRequest2.hashCode());
	}
	
	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() {
        String expected1 = "[TmfCoalescedDataRequest(0,TmfEvent,10,100)]";
        String expected2 = "[TmfCoalescedDataRequest(1,TmfEvent,20,100)]";
        String expected3 = "[TmfCoalescedDataRequest(2,TmfEvent,20,200)]";
        String expected4 = "[TmfCoalescedDataRequest(3,TmfEvent,20,200)]";

        assertEquals("toString", expected1, fRequest1.toString());
        assertEquals("toString", expected2, fRequest2.toString());
        assertEquals("toString", expected3, fRequest3.toString());
        assertEquals("toString", expected4, fRequest4.toString());
	}

	// ------------------------------------------------------------------------
	// isCompatible
	// ------------------------------------------------------------------------

	public void testIsCompatible() {
		TmfCoalescedDataRequest<TmfEvent> coalescedRequest = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200);
		TmfDataRequest<TmfEvent> request1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		TmfDataRequest<TmfEvent> request2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 11, 100, 200);
		TmfDataRequest<TmfEvent> request3 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 101, 200);

        assertTrue ("isCompatible", coalescedRequest.isCompatible(request1));
        assertFalse("isCompatible", coalescedRequest.isCompatible(request2));
        assertFalse("isCompatible", coalescedRequest.isCompatible(request3));
	}

	// ------------------------------------------------------------------------
	// done
	// ------------------------------------------------------------------------

	public void testDone() {
		
		// Test request
		final boolean[] crFlags = new boolean[4];
		TmfCoalescedDataRequest<TmfEvent> request = setupTestRequest(crFlags);
		TmfDataRequest<TmfEvent> subRequest1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		TmfDataRequest<TmfEvent> subRequest2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		request.addRequest(subRequest1);
		request.addRequest(subRequest2);

		request.done();

		// Validate the coalescing request
		assertTrue ("isCompleted", request.isCompleted());
		assertFalse("isFailed",    request.isFailed());
		assertFalse("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", crFlags[0]);
		assertTrue ("handleSuccess",   crFlags[1]);
		assertFalse("handleFailure",   crFlags[2]);
		assertFalse("handleCancel",    crFlags[3]);

		// Validate the first coalesced request
		assertTrue ("isCompleted", subRequest1.isCompleted());
		assertFalse("isFailed",    subRequest1.isFailed());
		assertFalse("isCancelled", subRequest1.isCancelled());

		// Validate the second coalesced request
		assertTrue ("isCompleted", subRequest2.isCompleted());
		assertFalse("isFailed",    subRequest2.isFailed());
		assertFalse("isCancelled", subRequest2.isCancelled());
	}

	// ------------------------------------------------------------------------
	// fail
	// ------------------------------------------------------------------------

	public void testFail() {
		
		final boolean[] crFlags = new boolean[4];
		TmfCoalescedDataRequest<TmfEvent> request = setupTestRequest(crFlags);
		TmfDataRequest<TmfEvent> subRequest1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		TmfDataRequest<TmfEvent> subRequest2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		request.addRequest(subRequest1);
		request.addRequest(subRequest2);

		request.fail();

		// Validate the coalescing request
		assertTrue ("isCompleted", request.isCompleted());
		assertTrue ("isFailed",    request.isFailed());
		assertFalse("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", crFlags[0]);
		assertFalse("handleSuccess",   crFlags[1]);
		assertTrue ("handleFailure",   crFlags[2]);
		assertFalse("handleCancel",    crFlags[3]);

		// Validate the first coalesced request
		assertTrue ("isCompleted", subRequest1.isCompleted());
		assertTrue ("isFailed",    subRequest1.isFailed());
		assertFalse("isCancelled", subRequest1.isCancelled());

		// Validate the second coalesced request
		assertTrue ("isCompleted", subRequest2.isCompleted());
		assertTrue ("isFailed",    subRequest2.isFailed());
		assertFalse("isCancelled", subRequest2.isCancelled());
	}

	// ------------------------------------------------------------------------
	// cancel
	// ------------------------------------------------------------------------

	public void testCancel() {

		final boolean[] crFlags = new boolean[4];
		TmfCoalescedDataRequest<TmfEvent> request = setupTestRequest(crFlags);
		TmfDataRequest<TmfEvent> subRequest1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		TmfDataRequest<TmfEvent> subRequest2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		request.addRequest(subRequest1);
		request.addRequest(subRequest2);

		request.cancel();

		// Validate the coalescing request
		assertTrue ("isCompleted", request.isCompleted());
		assertFalse("isFailed",    request.isFailed());
		assertTrue ("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", crFlags[0]);
		assertFalse("handleSuccess",   crFlags[1]);
		assertFalse("handleFailure",   crFlags[2]);
		assertTrue ("handleCancel",    crFlags[3]);

		// Validate the first coalesced request
		assertTrue ("isCompleted", subRequest1.isCompleted());
		assertFalse("isFailed",    subRequest1.isFailed());
		assertTrue ("isCancelled", subRequest1.isCancelled());

		// Validate the second coalesced request
		assertTrue ("isCompleted", subRequest2.isCompleted());
		assertFalse("isFailed",    subRequest2.isFailed());
		assertTrue ("isCancelled", subRequest2.isCancelled());
	}

	
	// ------------------------------------------------------------------------
	// cancel sub-requests
	// ------------------------------------------------------------------------

    public void testCancelSubRequests() {

	        final boolean[] crFlags = new boolean[4];
	        TmfCoalescedDataRequest<TmfEvent> request = setupTestRequest(crFlags);
	        TmfDataRequest<TmfEvent> subRequest1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
	        TmfDataRequest<TmfEvent> subRequest2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
	        request.addRequest(subRequest1);
	        request.addRequest(subRequest2);

	        subRequest1.cancel();

	        // Validate the first coalesced request
	        assertTrue ("isCompleted", subRequest1.isCompleted());
	        assertFalse("isFailed",    subRequest1.isFailed());
	        assertTrue ("isCancelled", subRequest1.isCancelled());

	        // Validate the coalescing request
	        assertFalse("isCompleted", request.isCompleted());
	        assertFalse("isFailed",     request.isFailed());
	        assertFalse("isCancelled", request.isCancelled());  

	        // Cancel second sub-request
	        subRequest2.cancel();

	        // Validate the second coalesced request
	        assertTrue ("isCompleted", subRequest2.isCompleted());
	        assertFalse("isFailed",    subRequest2.isFailed());
	        assertTrue ("isCancelled", subRequest2.isCancelled());

	        // Validate the coalescing request
	        assertTrue ("isCompleted", request.isCompleted());
	        assertFalse("isFailed",    request.isFailed());
	        assertTrue ("isCancelled", request.isCancelled());

	        // Finalize coalescing request - 
	        // Note: No need to check "request.isCancelled()" since it was verified above
            request.cancel();

	        assertTrue ("handleCompleted", crFlags[0]);
	        assertFalse("handleSuccess",   crFlags[1]);
	        assertFalse("handleFailure",   crFlags[2]);
	        assertTrue ("handleCancel",   crFlags[3]);
	    }

	// ------------------------------------------------------------------------
	// waitForCompletion
	// ------------------------------------------------------------------------

}
