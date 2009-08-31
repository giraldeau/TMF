/**
 * 
 */
package org.eclipse.linuxtools.lttng.stubs;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * @author francois
 *
 */
public class LTTngTimestampStub extends TmfTimestamp {

    /**
     * @param value
     * @param scale
     * @param precision
     */
    public LTTngTimestampStub(long value) {
		super(value, (byte) -3, 0);	// millisecs
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.event.TmfTimestamp#toString()
	 */
	@Override
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
}
