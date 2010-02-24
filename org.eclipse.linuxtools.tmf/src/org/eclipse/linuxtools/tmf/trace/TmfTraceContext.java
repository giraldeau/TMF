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

package org.eclipse.linuxtools.tmf.trace;

import org.eclipse.linuxtools.tmf.component.ITmfContext;

/**
 * <b><u>TmfTraceContext</u></b>
 * <p>
 * Trace context structure. It ties a trace location to an event index and
 * timestamp. The context should be enough to restore the trace state so the
 * corresponding event can be read.
 * <p>
 * Used to handle conflicting, concurrent accesses to the trace. 
 */
public class TmfTraceContext implements ITmfContext, Cloneable {

	private Object location;
//	private TmfTimestamp timestamp;
	private long rank;
	
//	public TmfTraceContext(Object loc, TmfTimestamp ts, long ind) {
//		location = loc;
//		timestamp = ts;
//		index = ind;
//	}

	public TmfTraceContext(Object loc, long ind) {
//		this(loc, null, 0);
		location = loc;
		rank = ind;
	}

	public TmfTraceContext(Object loc) {
		this(loc, 0);
	}

	public TmfTraceContext(TmfTraceContext other) {
//		this(other.location, other.timestamp, other.index);
		this(other.location, other.rank);
	}

	public TmfTraceContext clone() {
		try {
			return (TmfTraceContext) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Object getLocation() {
		return location;
	}

	public void setLocation(Object loc) {
		location = loc;
	}

//	public TmfTimestamp getTimestamp() {
//		return timestamp;
//	}

//	public void setTimestamp(TmfTimestamp ts) {
//		timestamp = ts;
//	}

	public void setRank(long value) {
		rank = value;
	}

	public long getRank() {
		return rank;
	}

	public void incrRank() {
		rank++;
	}

//	// ========================================================================
//	// Toubleshooting code
//	// ========================================================================
//
//	public Object getLocation() {
//		validateLocation(location);
//		return location;
//	}
//
//	// The FW expects the trace events to be ordered in time.
//	// If this is not the case, this invalidates the Trace index (at least)
//	private TmfTimestamp previous = TmfTimestamp.BigBang;
//	public void setLocation(Object loc) {
//		if (loc instanceof TmfTimestamp) {
//			TmfTimestamp ts = (TmfTimestamp) loc;
//			if (ts.compareTo(previous, false) < 0) {
//				System.out.println("Going back in time from " + previous + " to " + ts);
//			}
//			previous = ts;
//		}
//		validateLocation(loc);
//	}
//
//	static private DataInputStream in;
//	static private int size = 100000;
//	static private String locations[] = new String[size];
//	static public void init() {
//		System.out.println("TmfTraceContext: Loading valid locations...");
//		try {
//			// The trace context validation file is created by TmfTrace
//			in = new DataInputStream(new BufferedInputStream(new FileInputStream("TmfTraceContext.dat")));
//			int i = 0;
//			while (i < size) {
//				locations[i] = in.readUTF();
//				i++;
//			}
//			in.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//		}
//		System.out.println("TmfTraceContext: Done.");
//	}
//
//	private boolean bsearch(long key) {
//		int first = 0;
//		int last = size;
//		while (first < last) {
//			int mid = (first + last) / 2;
//			if (key < locations[mid]) {
//				last = mid;
//			} else if (key > locations[mid]) {
//				first = mid + 1;
//			} else {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private void validateLocation(Object loc) {
//		long l = (Long) loc;
//		if (!bsearch(l)) {
//			System.out.println("TmfTraceContext: location is invalid!");
//		}
//	}

}
