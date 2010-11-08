/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.Hashtable;
import java.util.Vector;

/**
 * A simple implementation of a Quark table, which consists of a 
 * Hashtable<String, Integer> and a Vector<String>.
 * 
 * This allows stored Strings to have an integer representation (faster compares, etc.),
 * while being able to do both conversions String <-> Int  in constant time.
 * 
 * @author alexmont
 *
 */
public class QuarkTable {
	
	private Hashtable<String, Integer> conversionTable;
	private Vector<String> reverseConversionTable;
	
	
	/**
	 * Default constructor
	 */
	public QuarkTable() {
		//FIXME use suitable starting default sizes?
		this.conversionTable = new Hashtable<String, Integer>();
		this.reverseConversionTable = new Vector<String>();
	}
	
	
	public boolean containsEntry(String entry) {
		return conversionTable.containsKey(entry);
	}
	
	/**
	 * Accessors
	 */
	public int getMatchingInt(String entry) {
		assert ( this.containsEntry(entry) );
		return conversionTable.get(entry);
	}
	
	public String getMatchingString(int index) {
		assert ( index < reverseConversionTable.size() );
		return reverseConversionTable.get(index);
	}
	
	
	public void addEntry(String entry) {
		assert ( !this.containsEntry(entry) );
		conversionTable.put( entry, reverseConversionTable.size() );
		reverseConversionTable.addElement(entry);
		
	}
}