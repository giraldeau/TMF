/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.Hashtable;
import java.util.Vector;

/**
 * A simple implementation of a Quark table, which consists of a 
 * Hashtable<Key, Integer> and a Vector<Key>. "Key" being a generic object.
 * 
 * This allows stored objects to have an integer representation (faster compares, etc.),
 * while being able to do both conversions Object <-> Int  in constant time.
 * 
 * @author alexmont
 *
 */
public class QuarkTable<K> {
	
	protected Hashtable<K, Integer> conversionTable;
	protected Vector<K> reverseConversionTable;
	
	
	/**
	 * Default constructor
	 */
	public QuarkTable() {
		this.conversionTable = new Hashtable<K, Integer>();
		this.reverseConversionTable = new Vector<K>();
	}
	
	
	public boolean containsEntry(K entry) {
		return conversionTable.containsKey(entry);
	}
	
	/**
	 * Accessors
	 */
	public int getMatchingInt(K entry) {
		assert ( this.containsEntry(entry) );
		return conversionTable.get(entry);
	}
	
	public K getMatchingKey(int index) {
		assert ( index < reverseConversionTable.size() );
		return reverseConversionTable.get(index);
	}
	
	/**
	 * Return the number of elements currently in the table
	 */
	public int getSize() {
		return reverseConversionTable.size();
	}
	
	
	public void addEntry(K entry) {
		assert ( !this.containsEntry(entry) );
		conversionTable.put( entry, reverseConversionTable.size() );
		reverseConversionTable.addElement(entry);
		
	}
	
	/**
	 * Remove all the entries in the table
	 */
	public void clear() {
		conversionTable.clear();
		reverseConversionTable.clear();
	}
}




