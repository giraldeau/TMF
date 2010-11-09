/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.RandomAccessFile;
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
	
	/**
	 * Reading constructor, with which we create a QuarkTable object by reading an
	 * already-saved one in a file.
	 * See the writeSelf method below.
	 * 
	 * @param desc The descriptor from which to read, which must be positionned at the start of a QuarkTable section
	 */
	public QuarkTable(RandomAccessFile desc) {
		//TODO todo below also
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
	
	/**
	 * Write the content of the reverseConversionTable vector in a file.
	 * This method won't do any seeks, just dumb writes, so the descriptor
	 * needs to be pre-seeked to where we want it.
	 * 
	 * The file section created in this way can then be re-read by the reading constructor
	 * defined above, to re-create a Quark Table from a file contents.
	 * 
	 * @param desc The (pre-seeked) RandomAccessFile descriptor to which we will write
	 */
	public void writeSelf(RandomAccessFile desc) {
		//TODO decide on the file format then doo eet!
	}
}