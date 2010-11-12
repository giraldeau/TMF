/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

/**
 * This is the wrapper class that exposes the different types of 'values' available
 * to use in the CST and the SHT.
 * We can then save numerical values as integers instead of arrays of 1-digit characters,
 * for example.
 * For now the two available types are either int or String.
 * 
 * @author alexmont
 *
 */
class StateValue {
	
	private byte type;			/* type of the 'value': 0 = int, 1 = string */
	private int valueInt;		/* if type = int, this will store the value */
	private String valueStr;	/* if type = string, then this is a variable-size string.
	 							   if type = something else, then it's undefined */
	
	/**
	 * Create a null value
	 */
	public StateValue() {
		this.type = 0;
		this.valueInt = -1;
		this.valueStr = null;
	}
	
	public StateValue(int valueAsInt) {
		this.type = 0;
		this.valueInt = valueAsInt;
		this.valueStr = null;
	}
	
	public StateValue(String valueAsString) {
		this.type = 1;
		this.valueInt = -1;
		this.valueStr = valueAsString;
	}
	
	/**
	 * Accessors
	 */
	
	/**
	 * Return the "type" of the stored value
	 * 0 = int
	 * 1 = String
	 */
	public byte getType() {
		return type;
	}
	
	public boolean isNull() {
		return ( (valueInt == -1) && (valueStr == null) );
	}
	
	public int getValueInt() {
		assert( type == 0 );
		return valueInt;
	}
	
	public String getValueStr() {
		assert( type == 1 );
		return valueStr;
	}
	
	/**
	 * Nullifying method. This should be a little bit faster than re-creating a new object.
	 * (but hey, this is Java, you never know)
	 */
	public void setNull() {
		valueInt = -1;
		valueStr = null;
		return;
	}
}