
package org.eclipse.linuxtools.lttng.state.history.helpers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.state.history.StateHistoryInterface;


/**
 * This is the specific type of QuarkTable that is used in the Current State Tree
 * to convert paths to and from integer representations.
 * 
 * We extend the QuarkTable class here because we want to provide methods to read to and
 * write from a file (and for this, the type K must be known).
 * 
 * @author alexmont
 *
 */
public class PathConversionTable extends QuarkTable<Vector<String>> {
	
	/*
	 * The attributes inherited from QuarkTable are:
	 * 
	 * private Hashtable<Vector<String>, Integer> conversionTable;
	 * private Vector<Vector<String>> reverseConversionTable;
	 */
	
	
	/**
	 * Default constructor
	 */
	public PathConversionTable() {
		super();
	}
	
	/**
	 * Reading constructor, with which we create a PathConversionTable object by reading an
	 * already-saved one in a file. See the writeSelf method below.
	 * 
	 * @param desc The descriptor from which to read, which must be positioned at the start of a QuarkTable section
	 */
	public PathConversionTable(RandomAccessFile desc) throws IOException {

		int sizeToRead, numberOfEntries;
		byte byteArray[];
		String completeString;
		Vector<String> innerVector;
		
		numberOfEntries = desc.readInt();
		
		this.conversionTable = new Hashtable<Vector<String>, Integer>(numberOfEntries);
		this.reverseConversionTable = new Vector<Vector<String>>(numberOfEntries);
		
		for ( int i = 0; i < numberOfEntries; i++) {
			/* Read the first byte = the number of characters in this String */
			sizeToRead = (int) desc.readByte();
			
			/* Generate the String object */
			byteArray = new byte[sizeToRead];
			desc.read(byteArray);
			completeString = new String(byteArray);
			
			/* Make sure we didn't read garbage (there should be a 0 at the end) */
			assert ( desc.readByte() == 0 );
			
			/* Create the inner vector object from the /-delimited components */
			innerVector = StateHistoryInterface.convertStringToVector(completeString);
			
			/* Insert that Vector into the two conversion tables */
			conversionTable.put( innerVector, i);
			reverseConversionTable.addElement(innerVector);
		}
	}
	

	/**
	 * Write the content of the reverseConversionTable vector in a file.
	 * This method won't do any seeks, just dumb writes, so the descriptor
	 * needs to be pre-seeked to where we want it.
	 * 
	 * The file section created this way can then be re-read by the reading constructor
	 * defined above, to re-create a PathConversionTable from a file's contents.
	 * 
	 * @param desc The (pre-seeked) RandomAccessFile descriptor to which we will write
	 */
	public void writeSelf(RandomAccessFile desc) throws IOException {
		String bigString;
		
		/* First, write a Int representing the number of entries in the outer Vector */
		desc.writeInt(reverseConversionTable.size());
		
		for ( int i = 0; i < reverseConversionTable.size(); i++ ) {
		
			/* Then we will convert all the inner Vector<String> to one big String, with slashes delimitating the elements */
			bigString = new String(reverseConversionTable.get(i).get(0));
			for ( int j = 0; j < reverseConversionTable.get(i).size(); j++ ) {
				bigString.concat("/");
				bigString.concat( reverseConversionTable.get(i).get(j) );
			}
			
			/* Finally, write the data in the file:
			 *  - An 8-bit integer (= a Byte) corresponding to the number of characters (bytes) in the bigString
			 *  - the String itself, in byte array form
			 *  - a 0'ed byte at the end, to make sure we don't read corrupt data
			 */
			desc.writeByte( bigString.length() );
			desc.write( bigString.getBytes() );
			desc.writeByte(0);
		}
		return;
	}
}