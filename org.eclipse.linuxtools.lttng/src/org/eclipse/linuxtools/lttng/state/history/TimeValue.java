/**
 *
 */

package org.eclipse.linuxtools.lttng.state.history;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * The basic Time-value unit used in the State Tree.
 * Could be made to extend other timestamp structures instead if needed.
 * 
 * @author alexmont
 *
 */
class TimeValue extends TmfTimestamp {
	
	/**
	 * The three fields inherited from TmfTimestamp are:
	 * long fValue;
	 * byte fScale;
	 * long fPrecision;
	 */
    
	public TimeValue(long value) {
		super(value);
	}
	
}