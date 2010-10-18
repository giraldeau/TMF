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

package org.eclipse.linuxtools.tmf.tests.event;

import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

import junit.framework.TestCase;

/**
 * <b><u>TmfTimeRangeTest</u></b>
 * <p>
 * Test suite for the TmfTimeRange class.
 */
public class TmfTimeRangeTest extends TestCase {


	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	public TmfTimeRangeTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testConstructor() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range = new TmfTimeRange(ts1, ts2);
		assertEquals("startTime", ts1, range.getStartTime());
		assertEquals("endTime",   ts2, range.getEndTime());
	}

	public void testBadConstructor() throws Exception {
		try {
			new TmfTimeRange(TmfTimestamp.BigBang, null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}

		try {
			new TmfTimeRange(null, TmfTimestamp.BigCrunch);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

	public void testOpenRange1() throws Exception {
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, ts2);
		assertEquals("startTime", TmfTimestamp.BigBang, range.getStartTime());
		assertEquals("endTime",   ts2, range.getEndTime());
	}

	public void testOpenRange2() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimeRange range = new TmfTimeRange(ts1, TmfTimestamp.BigCrunch);
		assertEquals("startTime", ts1, range.getStartTime());
		assertEquals("endTime",   TmfTimestamp.BigCrunch, range.getEndTime());
	}

	public void testOpenRange3() throws Exception {
		TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang,	TmfTimestamp.BigCrunch);
		assertEquals("startTime", TmfTimestamp.BigBang,   range.getStartTime());
		assertEquals("endTime",   TmfTimestamp.BigCrunch, range.getEndTime());
	}
	
	public void testCopyConstructor() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range0 = new TmfTimeRange(ts1, ts2);
		TmfTimeRange range1 = new TmfTimeRange(range0);

		assertEquals("startTime", ts1, range1.getStartTime());
		assertEquals("endTime",   ts2, range1.getEndTime());

		TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
		TmfTimeRange range3 = new TmfTimeRange(range2);
		assertEquals("startTime", TmfTimestamp.BigBang,   range3.getStartTime());
		assertEquals("endTime",   TmfTimestamp.BigCrunch, range3.getEndTime());
	}

	public void testCopyConstructor2() throws Exception {
		try {
			new TmfTimeRange(null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);
		TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);

		assertTrue("equals", range1.equals(range1));
		assertTrue("equals", range2.equals(range2));

		assertTrue("equals", !range1.equals(range2));
		assertTrue("equals", !range2.equals(range1));
	}
	
	public void testEqualsSymmetry() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range1a = new TmfTimeRange(ts1, ts2);
		TmfTimeRange range1b = new TmfTimeRange(ts1, ts2);

		TmfTimeRange range2a = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
		TmfTimeRange range2b = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);

		assertTrue("equals", range1a.equals(range1b));
		assertTrue("equals", range1b.equals(range1a));

		assertTrue("equals", range2a.equals(range2b));
		assertTrue("equals", range2b.equals(range2a));
	}
	
	public void testEqualsTransivity() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range1a = new TmfTimeRange(ts1, ts2);
		TmfTimeRange range1b = new TmfTimeRange(ts1, ts2);
		TmfTimeRange range1c = new TmfTimeRange(ts1, ts2);

		assertTrue("equals", range1a.equals(range1b));
		assertTrue("equals", range1b.equals(range1c));
		assertTrue("equals", range1a.equals(range1c));
	}
	
	public void testEqualsNull() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);

		assertTrue("equals", !range1.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);
		TmfTimeRange range1b = new TmfTimeRange(range1);
		TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
		TmfTimeRange range2b = new TmfTimeRange(range2);

		assertTrue("hashCode", range1.hashCode() == range1b.hashCode());
		assertTrue("hashCode", range2.hashCode() == range2b.hashCode());

		assertTrue("hashCode", range1.hashCode() != range2.hashCode());
	}
	
	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range = new TmfTimeRange(ts1, ts2);

		String expected = "[TmfTimeRange(" + ts1 + ":" + ts2 + ")]";
		assertEquals("toString", expected, range.toString());
	}
	
	// ------------------------------------------------------------------------
	// contains
	// ------------------------------------------------------------------------

	public void testContains() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(12345);
		TmfTimestamp ts2 = new TmfTimestamp(12350);
		TmfTimeRange range = new TmfTimeRange(ts1, ts2);

		assertTrue("contains (lower bound)", range.contains(new TmfTimestamp(12345)));
		assertTrue("contains (higher bound)", range.contains(new TmfTimestamp(12350)));
		assertTrue("contains (within bounds)", range.contains(new TmfTimestamp(12346)));

		assertFalse("contains (low value)", range.contains(new TmfTimestamp(12340)));
		assertFalse("contains (high value)", range.contains(new TmfTimestamp(12351)));
	}

	// ------------------------------------------------------------------------
	// getIntersection
	// ------------------------------------------------------------------------

	public void testGetIntersection() throws Exception {

		TmfTimestamp ts1a = new TmfTimestamp(1000);
		TmfTimestamp ts1b = new TmfTimestamp(2000);
		TmfTimeRange range1 = new TmfTimeRange(ts1a, ts1b);

		TmfTimestamp ts2a = new TmfTimestamp(2000);
		TmfTimestamp ts2b = new TmfTimestamp(3000);
		TmfTimeRange range2 = new TmfTimeRange(ts2a, ts2b);
		
		TmfTimestamp ts3a = new TmfTimestamp(3000);
		TmfTimestamp ts3b = new TmfTimestamp(4000);
		TmfTimeRange range3 = new TmfTimeRange(ts3a, ts3b);
		
		TmfTimestamp ts4a = new TmfTimestamp(1500);
		TmfTimestamp ts4b = new TmfTimestamp(2500);
		TmfTimeRange range4 = new TmfTimeRange(ts4a, ts4b);
		
		TmfTimestamp ts5a = new TmfTimestamp(1500);
		TmfTimestamp ts5b = new TmfTimestamp(2000);
		TmfTimeRange range5 = new TmfTimeRange(ts5a, ts5b);
		
		TmfTimestamp ts6a = new TmfTimestamp(2000);
		TmfTimestamp ts6b = new TmfTimestamp(2500);
		TmfTimeRange range6 = new TmfTimeRange(ts6a, ts6b);
		
		TmfTimestamp ts7a = new TmfTimestamp(1500);
		TmfTimestamp ts7b = new TmfTimestamp(3500);
		TmfTimeRange range7 = new TmfTimeRange(ts7a, ts7b);
		
		TmfTimestamp ts8a = new TmfTimestamp(2250);
		TmfTimestamp ts8b = new TmfTimestamp(2750);
		TmfTimeRange range8 = new TmfTimeRange(ts8a, ts8b);
		
		assertEquals("getIntersection (below - not contiguous)", null, range1.getIntersection(range3));
		assertEquals("getIntersection (above - not contiguous)", null, range3.getIntersection(range1));

		assertEquals("getIntersection (below - contiguous)", new TmfTimeRange(ts1b, ts1b), range1.getIntersection(range2));
		assertEquals("getIntersection (above - contiguous)", new TmfTimeRange(ts3a, ts3a), range3.getIntersection(range2));

		assertEquals("getIntersection (below - overlap)", new TmfTimeRange(ts2a, ts4b), range2.getIntersection(range4));
		assertEquals("getIntersection (above - overlap)", new TmfTimeRange(ts2a, ts4b), range4.getIntersection(range2));

		assertEquals("getIntersection (within - overlap1)", range6, range2.getIntersection(range6));
		assertEquals("getIntersection (within - overlap2)", range6, range6.getIntersection(range2));

		assertEquals("getIntersection (within - overlap3)", range5, range1.getIntersection(range5));
		assertEquals("getIntersection (within - overlap4)", range5, range5.getIntersection(range1));
	
		assertEquals("getIntersection (within - overlap5)", range8, range2.getIntersection(range8));
		assertEquals("getIntersection (within - overlap6)", range8, range8.getIntersection(range2));

		assertEquals("getIntersection (accross1)", range2, range2.getIntersection(range7));
		assertEquals("getIntersection (accross2)", range2, range7.getIntersection(range2));
	}

}
