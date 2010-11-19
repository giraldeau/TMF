/**
 *
 */

package org.eclipse.linuxtools.lttng.state.history;

import org.eclipse.linuxtools.lttng.event.LttngTimestamp;

/**
 * The basic Time-value unit used in the State Tree.
 * Could be made to extend other timestamp structures instead if needed.
 * 
 * @author alexmont
 *
 */
class TimeValue extends LttngTimestamp {
	
	/*
	 * The three fields inherited from TmfTimestamp are:
	 * long fValue;
	 * byte fScale;
	 * long fPrecision;
	 */
    
	/**
	 * Standard constructor
	 */
	public TimeValue(long value) {
		super(value);
	}
	
	/**
	 * Downgrading constructor, creates a TimeValue from an existing LttngTimestamp
	 * @param t
	 */
	public TimeValue(LttngTimestamp t) {
		this.fValue = t.getValue();
		this.fScale = t.getScale();
		this.fPrecision = t.getPrecision();
	}
	
	/**
	 * Returns if this timestamp intersects the Interval passed in parameter
	 * 
	 * @param interval The SHTInterval to compare
	 * @return Y/N
	 */
	protected boolean intersects(StateHistoryTreeInterval interval) {
		if ( this.compareTo( interval.getEnd(), false) <= 0 ) {
			if ( this.compareTo( interval.getStart(), false) >= 0 ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add one nanosecond (smallest possible step) to the fValue.
	 * Used when generating intervals to make sure there is no clash.
	 */
	protected void increment() {
		this.fValue++;
	}
	
}