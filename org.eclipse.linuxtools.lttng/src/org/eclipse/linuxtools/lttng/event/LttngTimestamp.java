/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>LttngTimestamp</u></b><p>
 * 
 * Lttng specific implementation of the TmfTimestamp.<p>
 * 
 * The Lttng implementation is the same as the basic Tmf Implementation but allow construction with a TmfTimestamp or a long.
 */
public class LttngTimestamp extends TmfTimestamp {
    
    // Required by Serializable
	private static final long serialVersionUID = -7016853105162491273L;
	
    /**
     * Default Constructor.<p>
     * 
     */
	public LttngTimestamp() {
	    super(Long.MIN_VALUE, (byte) -9);
	}
	
	/**
     * Constructor with parameters.<p>
     * 
     * @param newEventTime    Time as long, unit expected to be nanoseconds
     */
    public LttngTimestamp(long newEventTime) {
        super(newEventTime, (byte) -9);
    }
    
    /**
     * Copy Constructor.<p>
     * 
     * @param oldEventTime    The timestamp object we want to copy from
     */
    public LttngTimestamp(TmfTimestamp oldEventTime) {
        this(oldEventTime.getValue());
    }
    
    @Override
    public long getValue() {
        return fValue;
    }
    
    public void setValue(long newValue) {
        fValue = newValue;
    }
    
	/**
	 * Get the second part in timestamp.<p>
	 * 
	 * Note : We do not use scale and assumes contents to be in nano seconds.
	 * 
	 * @return Seconds in the object, in string.
	 */
	public String getSeconds() {
		return formatSecs(fValue);
	}

	/**
     * Get the nanosecond part in timestamp.<p>
     * 
     * Note : We do not use scale and assumes contents to be in nanoseconds.
	 * 
	 * @return Seconds in the object, in string.
	 */
	public String getNanoSeconds() {
		return formatNs(fValue);
	}
	
	/*
	 * Use the exponent to format the second in the correct format.
	 */
	private String formatSecs(long time) {
		long sec = (long) (time * 1E-9);
		return String.valueOf(sec);
	}

	/*
	 * Obtains the remainder fraction on unit Seconds of the entered value in
	 * nanoseconds. e.g. input: 1241207054171080214 ns.
	 * The number of fraction seconds can be obtained by removing the last 9 digits: 
	 * In 1241207054, the fractional portion of seconds, expressed in ns is: 171080214
	 */
	private String formatNs(long time) {
		boolean neg = time < 0;
		if (neg) {
			time = -time;
		}
		// The following approach could be used although performance
		// decreases in half.
		// String strVal = String.format("%09d", time);
		// String tmp = strVal.substring(strVal.length() - 9)
		StringBuffer temp = new StringBuffer();
		long ns = time;
		ns %= 1000000000;
		if (ns < 10) {
			temp.append("00000000"); //$NON-NLS-1$
		} else if (ns < 100) {
			temp.append("0000000"); //$NON-NLS-1$
		} else if (ns < 1000) {
			temp.append("000000"); //$NON-NLS-1$
		} else if (ns < 10000) {
			temp.append("00000"); //$NON-NLS-1$
		} else if (ns < 100000) {
			temp.append("0000"); //$NON-NLS-1$
		} else if (ns < 1000000) {
			temp.append("000"); //$NON-NLS-1$
		} else if (ns < 10000000) {
			temp.append("00"); //$NON-NLS-1$
		} else if (ns < 100000000) {
			temp.append("0"); //$NON-NLS-1$
		}

		temp.append(ns);
		return temp.toString();
	}

	
    /**
     * toString() method.
     * 
     * @return timestamp, as string
     */
    @Override
    @SuppressWarnings("nls")
	public String toString() {

        // If we are dealing with units of seconds (or higher),
        // use the plain formatter
        if (fScale >= 0) {
            Double value = fValue * Math.pow(10, fScale);
            return value.toString();
        }

        // Define a format string
        String format = String.format("%%1d.%%0%dd", -fScale);

        // And format the timestamp value
        double scale = Math.pow(10, fScale);
        long seconds = (long) (fValue * scale);
        long fracts  = fValue - (long) ((double) seconds / scale); 
        String result = String.format(format, seconds, fracts);

        return result;
    }

    @Override
    public LttngTimestamp clone() {
    	return (LttngTimestamp) super.clone();
    }

}
