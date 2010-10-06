/**
 *
 */

package org.eclipse.linuxtools.lttng.state.history;

import org.eclipse.linuxtools.lttng.event.LttngTimestamp;

/**
 * The basic Time-value unit used in the State Tree.
 * Adds the serializing method.
 * Could be made to extend other timestamp structures instead if needed.
 * 
 * @author alexmont
 *
 */
class Timevalue extends LttngTimestamp {
	
	/**
	 * The three fields inherited from TmfTimestamp are:
	 * long fValue;
	 * byte fScale;
	 * long fPrecision;
	 */
    
	public Timevalue(long value) {
		super(value);
	}
	
//	public byte[] toBytes() {
//		/* We only bother with the fValue in our case (long = 8 bytes) */
//		byte[] array = new byte[8];
//		array = ArrayHelper.longToByteArray(this.fValue);
//		return array;
//	}
}