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

package org.eclipse.linuxtools.tmf.tests.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfTimestampTest</u></b>
 * <p>
 * Test suite for the TmfTimestamp class.
 */
public class TmfTimestampTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private final TmfTimestamp ts0 = new TmfTimestamp();
	private final TmfTimestamp ts1 = new TmfTimestamp(12345);
	private final TmfTimestamp ts2 = new TmfTimestamp(12345, (byte) -1);
	private final TmfTimestamp ts3 = new TmfTimestamp(12345, (byte)  2, 5);

	private final TmfTimestamp ts0copy = new TmfTimestamp();
	private final TmfTimestamp ts1copy = new TmfTimestamp(12345);

	private final TmfTimestamp ts0copy2 = new TmfTimestamp();
	private final TmfTimestamp ts1copy2 = new TmfTimestamp(12345);

	private final TmfTimestamp bigBang   = new TmfTimestamp(TmfTimestamp.BigBang);
	private final TmfTimestamp bigCrunch = new TmfTimestamp(TmfTimestamp.BigCrunch);

	// ------------------------------------------------------------------------
	// Housekeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfTimestampTest(String name) {
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

	public void testDefaultConstructor() throws Exception {
		assertEquals("getValue",     0, ts0.getValue());
		assertEquals("getscale",     0, ts0.getScale());
		assertEquals("getPrecision", 0, ts0.getPrecision());
	}

	public void testSimpleConstructor() throws Exception {
		assertEquals("getValue", 12345, ts1.getValue());
		assertEquals("getscale", 0,     ts1.getScale());
		assertEquals("getPrecision", 0, ts1.getPrecision());
	}

	public void testSimpleConstructor2() throws Exception {
		assertEquals("getValue", 12345, ts2.getValue());
		assertEquals("getscale", -1,    ts2.getScale());
		assertEquals("getPrecision", 0, ts2.getPrecision());
	}

	public void testFullConstructor() throws Exception {
		assertEquals("getValue", 12345, ts3.getValue());
		assertEquals("getscale", 2,     ts3.getScale());
		assertEquals("getPrecision", 5, ts3.getPrecision());
	}

	public void testCopyConstructor() throws Exception {
		TmfTimestamp ts0 = new TmfTimestamp(12345, (byte) 2, 5);
		TmfTimestamp ts = new TmfTimestamp(ts0);
		assertEquals("getValue", 12345, ts.getValue());
		assertEquals("getscale", 2, ts.getScale());
		assertEquals("getPrecision", 5, ts.getPrecision());
	}

	public void testCopyConstructor2() throws Exception {
		try {
			@SuppressWarnings("unused")
			TmfTimestamp timestamp = new TmfTimestamp(null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

	public void testCopyConstructorBigBang() throws Exception {
		assertEquals("getValue", TmfTimestamp.BigBang.getValue(), bigBang.getValue());
		assertEquals("getscale", TmfTimestamp.BigBang.getScale(), bigBang.getScale());
		assertEquals("getPrecision", TmfTimestamp.BigBang.getPrecision(), bigBang.getPrecision());
	}

	public void testCopyConstructorBigCrunch() throws Exception {
		assertEquals("getValue", TmfTimestamp.BigCrunch.getValue(), bigCrunch.getValue());
		assertEquals("getscale", TmfTimestamp.BigCrunch.getScale(), bigCrunch.getScale());
		assertEquals("getPrecision", TmfTimestamp.BigCrunch.getPrecision(), bigCrunch.getPrecision());
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		assertTrue("equals", ts0.equals(ts0));
		assertTrue("equals", ts1.equals(ts1));

		assertTrue("equals", !ts0.equals(ts1));
		assertTrue("equals", !ts1.equals(ts0));
	}
	
	public void testEqualsSymmetry() throws Exception {
		assertTrue("equals", ts0.equals(ts0copy));
		assertTrue("equals", ts0copy.equals(ts0));

		assertTrue("equals", ts1.equals(ts1copy));
		assertTrue("equals", ts1copy.equals(ts1));
	}
	
	public void testEqualsTransivity() throws Exception {
		assertTrue("equals", ts0.equals(ts0copy));
		assertTrue("equals", ts0copy.equals(ts0copy2));
		assertTrue("equals", ts0.equals(ts0copy2));
		
		assertTrue("equals", ts1.equals(ts1copy));
		assertTrue("equals", ts1copy.equals(ts1copy2));
		assertTrue("equals", ts1.equals(ts1copy2));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !ts0.equals(null));
		assertTrue("equals", !ts1.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		assertTrue("hashCode", ts0.hashCode() == ts0copy.hashCode());
		assertTrue("hashCode", ts1.hashCode() == ts1copy.hashCode());

		assertTrue("hashCode", ts0.hashCode() != ts1.hashCode());
	}
	
	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() throws Exception {
		assertEquals("toString", "[TmfTimestamp(0,0,0)]",      ts0.toString());
		assertEquals("toString", "[TmfTimestamp(12345,0,0)]",  ts1.toString());
		assertEquals("toString", "[TmfTimestamp(12345,-1,0)]", ts2.toString());
		assertEquals("toString", "[TmfTimestamp(12345,2,5)]",  ts3.toString());
	}

	// ------------------------------------------------------------------------
	// clone
	// ------------------------------------------------------------------------

	public class MyTimestamp extends TmfTimestamp {
		@Override
		public boolean equals(Object other) {
			return super.equals(other);
		}
		@Override
		public MyTimestamp clone() {
			return (MyTimestamp) super.clone();
		}
	}

	public void testClone() throws Exception {
		TmfTimestamp timestamp = ts0.clone();
		assertEquals("clone", timestamp, ts0);
	}

	public void testClone2() throws Exception {
		MyTimestamp timestamp = new MyTimestamp();
		MyTimestamp clone = timestamp.clone();
		assertEquals("clone", clone, timestamp);
	}

	// ------------------------------------------------------------------------
	// synchronize
	// ------------------------------------------------------------------------

	public void testSynchronizeOffset() throws Exception {

		TmfTimestamp ts = ts0.synchronize(0, (byte) 0);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.synchronize(12345, (byte) 0);
		assertEquals("getValue", 12345, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.synchronize(10, (byte) 0);
		assertEquals("getValue",    10, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.synchronize(-10, (byte) 0);
		assertEquals("getValue",   -10, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());
	}

	public void testSynchronizeScale() throws Exception {

		TmfTimestamp ts = ts0.synchronize(0, (byte) 10);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale",    10, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.synchronize(0, (byte) -10);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale",   -10, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());
	}

	public void testSynchronizeOffsetAndScale() throws Exception {
		byte SCALE = 12;

		TmfTimestamp ts = ts0.synchronize(0, SCALE);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.synchronize(12345, SCALE);
		assertEquals("getValue", 12345, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.synchronize(10, SCALE);
		assertEquals("getValue",    10, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.synchronize(-10, SCALE);
		assertEquals("getValue",   -10, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());
	}	
	
	// ------------------------------------------------------------------------
	// getAdjustment
	// ------------------------------------------------------------------------

	public void testGetAdjustmentSameScale() throws Exception {
		long delta = ts0.getAdjustment(ts0 , ts0.getScale());
		assertEquals("delta", 0, delta);

		delta = ts1.getAdjustment(ts1, ts1.getScale());
		assertEquals("delta", 0, delta);

		delta = ts0.getAdjustment(ts1, ts1.getScale());
		assertEquals("delta", 12345, delta);

		delta = ts1.getAdjustment(ts0, ts0.getScale());
		assertEquals("delta", -12345, delta);
	}

	public void testGetAdjustmentDifferentScales() throws Exception {
		long delta = ts0.getAdjustment(ts2, ts2.getScale());
		assertEquals("delta", 12345, delta);

		delta = ts2.getAdjustment(ts0, ts0.getScale());
		assertEquals("delta", -1234, delta);
	}

	// ------------------------------------------------------------------------
	// CompareTo
	// ------------------------------------------------------------------------

	public void testCompareToSameScale() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(900,  (byte) 0, 50);
		TmfTimestamp ts2 = new TmfTimestamp(1000, (byte) 0, 50);
		TmfTimestamp ts3 = new TmfTimestamp(1100, (byte) 0, 50);
		TmfTimestamp ts4 = new TmfTimestamp(1000, (byte) 0, 75);

		assertTrue(ts1.compareTo(ts1, false) == 0);

		assertTrue("CompareTo", ts1.compareTo(ts2, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, false) == 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, false) > 0);
	}

	public void testCompareToDifferentScale() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(9000, (byte) -1, 50);
		TmfTimestamp ts2 = new TmfTimestamp(1000, (byte) 0, 50);
		TmfTimestamp ts3 = new TmfTimestamp(110,  (byte) 1, 50);
		TmfTimestamp ts4 = new TmfTimestamp(1,    (byte) 3, 75);

		assertTrue("CompareTo", ts1.compareTo(ts1, false) == 0);

		assertTrue("CompareTo", ts1.compareTo(ts2, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, false) == 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, false) > 0);
	}

	public void testCompareToWithinPrecision() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(900,  (byte) 0, 50);
		TmfTimestamp ts2 = new TmfTimestamp(1000, (byte) 0, 50);
		TmfTimestamp ts3 = new TmfTimestamp(1100, (byte) 0, 50);
		TmfTimestamp ts4 = new TmfTimestamp(1000, (byte) 0, 75);

		assertTrue("CompareTo", ts1.compareTo(ts1, true) == 0);

		assertTrue("CompareTo", ts1.compareTo(ts2, true) == 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, true) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, true) == 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, true) == 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, true) == 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, true) == 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, true) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, true) == 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, true) == 0);
	}

	public void testCompareToLargeScale() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(-1,    (byte) 100);
		TmfTimestamp ts2 = new TmfTimestamp(-1000, (byte) -100);
		TmfTimestamp ts3 = new TmfTimestamp(1,     (byte) 100);
		TmfTimestamp ts4 = new TmfTimestamp(1000,  (byte) -100);

		assertTrue("CompareTo", ts1.compareTo(ts2, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, false) > 0);

		assertTrue("CompareTo", ts4.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts4.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts4.compareTo(ts3, false) < 0);
	}

	public void testCompareToBigRanges() throws Exception {
		TmfTimestamp ts0a = new TmfTimestamp(0, Byte.MAX_VALUE);
		TmfTimestamp ts0b = new TmfTimestamp(0, Byte.MIN_VALUE);
		TmfTimestamp ts1 = new TmfTimestamp(-1, Byte.MAX_VALUE);
		TmfTimestamp ts2 = new TmfTimestamp(-1, Byte.MIN_VALUE);
		TmfTimestamp ts3 = new TmfTimestamp(1, Byte.MAX_VALUE);
		TmfTimestamp ts4 = new TmfTimestamp(1, Byte.MIN_VALUE);

		assertTrue("CompareTo", ts0a.compareTo(TmfTimestamp.BigBang, false) > 0);
		assertTrue("CompareTo", ts0a.compareTo(TmfTimestamp.BigCrunch, false) < 0);

		assertTrue("CompareTo", ts0b.compareTo(TmfTimestamp.BigBang, false) > 0);
		assertTrue("CompareTo", ts0b.compareTo(TmfTimestamp.BigCrunch, false) < 0);

		assertTrue("CompareTo", ts0a.compareTo(ts0b, false) == 0);
		assertTrue("CompareTo", ts0b.compareTo(ts0a, false) == 0);

		assertTrue("CompareTo", ts0a.compareTo(TmfTimestamp.BigBang, false) > 0);
		assertTrue("CompareTo", ts0a.compareTo(TmfTimestamp.BigCrunch, false) < 0);

		assertTrue("CompareTo", ts1.compareTo(TmfTimestamp.BigBang, false) > 0);
		assertTrue("CompareTo", ts1.compareTo(TmfTimestamp.BigCrunch, false) < 0);

		assertTrue("CompareTo", ts2.compareTo(TmfTimestamp.BigBang, false) > 0);
		assertTrue("CompareTo", ts2.compareTo(TmfTimestamp.BigCrunch, false) < 0);

		assertTrue("CompareTo", ts3.compareTo(TmfTimestamp.BigBang, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(TmfTimestamp.BigCrunch, false) < 0);

		assertTrue("CompareTo", ts4.compareTo(TmfTimestamp.BigBang, false) > 0);
		assertTrue("CompareTo", ts4.compareTo(TmfTimestamp.BigCrunch, false) < 0);
	}

}
