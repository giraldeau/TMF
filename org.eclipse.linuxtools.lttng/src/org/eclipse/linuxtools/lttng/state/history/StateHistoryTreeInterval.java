/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The interval component, which will be contained in a node.
 * @author alexmont
 *
 */
class StateHistoryTreeInterval {
	
	/**
	 * Fields
	 */
	private Timevalue intervalStart;
	private Timevalue intervalEnd;
	
	/* This is the key used to uniquely identify a given state entry */
	private int key;
	
	private byte type;			/* type of the 'value': 0 = int, 1 = string */
	private int valueInt;		/* if type = int, this will store the value */
	private String valueStr;	/* if type = string, then this is a variable-size string.
	 							   if type = something else, then it's undefined */
	
	/**
	 * Standard constructors, with the value being either of type int or string
	 */
	public StateHistoryTreeInterval(Timevalue intervalStart, Timevalue intervalEnd,
									int key, int value) {
		
		this.intervalStart = intervalStart;
		this.intervalEnd = intervalEnd;
		this.key =  key;
		
		this.type = 0;
		this.valueInt = value;
		this.valueStr = null;
	}
	
	public StateHistoryTreeInterval(Timevalue intervalStart, Timevalue intervalEnd,
									int key, String value) {
		
		this.intervalStart = intervalStart;
		this.intervalEnd = intervalEnd;
		this.key =  key;
		
		this.type = 1;
		this.valueInt = -1;
		this.valueStr = value;
	}
	
//	/**
//	 * "Reader" constructor, build a SHT-Interval object by reading 2 arrays of bytes.
//	 * 
//	 * @param dataEntry : byte array coming from the Data section of the block
//	 * @param stringEntry : byte array coming from the String section of the block
//	 */
//	public StateHistoryTreeInterval(byte[] dataEntry, byte[] stringEntry) {
//		assert(dataEntry.length == 28);
//		assert(stringEntry[stringEntry.length - 1] == 0);	//variable-size arrays should always end with a "\0"
//		
//		byte[] longReader = new byte[8];
//		byte[] intReader = new byte[4];
//		
//		/* Read "intervalStart" */
//		System.arraycopy(dataEntry, 0, longReader, 0, 8);
//		this.intervalStart = new Timevalue(ArrayHelper.byteArrayToLong(longReader));
//		
//		/* read "intervalEnd" */
//		System.arraycopy(dataEntry, 8, longReader, 0, 8);
//		this.intervalEnd = new Timevalue(ArrayHelper.byteArrayToLong(longReader));
//		
//		/* read "key" */
//		System.arraycopy(dataEntry, 16, intReader, 0, 8);
//		this.key = ArrayHelper.byteArrayToInt(intReader);
//		
//		/* read "value" (var. size) */
//		stringEntry = (byte[]) ArrayHelper.resizeArray(stringEntry, stringEntry.length - 1);
//		this.value = ArrayHelper.byteArraytoCharArray(stringEntry);
//	}
	
	/**
	 * Reader constructor. Builds the interval using a RandomAccessFile descriptor,
	 * which is already positioned at the start of the Data Section of a node.
	 */
	public StateHistoryTreeInterval(RandomAccessFile desc, long nodeStartPosition) {
		int valueOffset, valueSize;
		byte valueArray[];
		long position;		/* To save the descriptor's position in the file, so we can restore it for the next read */
		
		try {
			/* Read the Data Section entry */
			this.intervalStart = new Timevalue(desc.readLong());
			this.intervalEnd = new Timevalue(desc.readLong());
			this.key = desc.readInt();
			
			/* Read the 'type' of the value, then react accordingly */
			this.type = desc.readByte();
			if ( type == 0 ) {
			/* the type of ValueOffset is 'value' */
				this.valueInt = desc.readInt();
				this.valueStr = null;
				
			} else if ( type == 1 ) {
			/* the type is 'offset' */
				this.valueInt = -1;
				valueOffset = desc.readInt();
				
				/* Go read the corresponding entry in the Strings section of the block */
				position = desc.getFilePointer();
				desc.seek( nodeStartPosition + (long) valueOffset );
				valueSize = (int) desc.readByte();	/* the first byte = the size to read */
				
				valueArray = new byte[valueSize];		//FIXME isn't there a way to generate the Strings object directly?
				for (int i=0; i < valueSize; i++) {
					valueArray[i] = desc.readByte();
				}
				this.valueStr = new String(valueArray);
				
				/* Confirm the 0'ed byte at the end */
				//FIXME make only used when in debug mode
				assert( desc.readByte() == 0 );
				
				/* Restore the file pointer's position (so we can read the next interval) */
				desc.seek(position);
				
			} else {
			/* Unrecognized type */
				assert( false );
			}
			
		
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	/**
	 * Antagonist of the previous constructor, write the Data entry corresponding to this interval
	 * in the Data section of the node containing it.
	 * The descriptor needs to be positioned at the start of the Data entry,
	 * and will end at the start of the next entry (or at the end of the Data section
	 * for the last interval in the node)
	 * 
	 * @param desc RandomAccessFile that's been passed on from the SHT IO object
	 * @param nodeStartPosition offset (in the file) where the node starts. passed on by the SHTNode object
	 * @param offsetForStringEntry Offset (in the *node*) where this interval is to write its Strings section entry, if any
	 * @return The length of the Strings entry that was used (so the SHT can get notified). 0 if no Strings entry.
	 */
	public int writeInterval(RandomAccessFile desc, long nodeStartPosition,
								int offsetForStringEntry) throws IOException {
		long position;
		
		desc.writeLong(intervalStart.getValue());
		desc.writeLong(intervalEnd.getValue());
		desc.writeInt(key);
		desc.writeByte(type);
		
		if ( type == 0 ) {
		/* We write the 'valueOffset' field as a straight value */
			desc.writeInt(valueInt);
			return 0; /* we didn't use a Strings section entry */
		
		} else if ( type == 1 ) {
		/* we use the valueOffset as an offset. */
			desc.writeInt(offsetForStringEntry);
			position = desc.getFilePointer();
			desc.seek( nodeStartPosition + (long) offsetForStringEntry );
			
			/* write the Strings entry (1st byte = size, then the bytes, then the 0) */
			desc.writeByte(valueStr.length());
			desc.write(valueStr.getBytes());
			desc.write(0);	//FIXME only for debug mode...
			desc.seek(position);
			return valueStr.length() + 2;	/* +1 size at the start, +1 for the 0 at the end */
		
		} else {
		/* unrecognized type */
			assert( false );
			return -1;
		}
		
	}


	/**
	 * Accessors
	 */
	public Timevalue getStart() {
		return intervalStart;
	}
	
	public Timevalue getEnd() {
		return intervalEnd;
	}
	
	public int getKey() {
		return key;
	}
	
	public byte getValueType() {
		return type;
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
	 * Calculate the (serialized) size of this interval
	 */
	public int getIntervalSize() {
		return StateHistoryTreeNode.getDataEntrySize() + this.getStringsEntrySize();
	}
	
	public int getStringsEntrySize() {
		if ( type == 0) {
			return 0;
			
		} else if ( type == 1 ) {
			return valueStr.length() + 2;
			/* (+1 for the first byte indicating the size, +1 for the 0'ed byte) */
			//TODO only +1 in non-debug mode
			
		} else {
		/* unrecognized type */
			assert( false );
			return -1;
		}
	}
	
	
//	/**
//	 * "Serialization" methods, used to put an interval in a format to be writable directly into a file.
//	 * The DataEntry is what goes in the Data section of the block (fixed-size stuff and pointers)
//	 * The StringsEntry contains whatever is of variable size
//	 * 
//	 * @return The relevant information, formatted as a byte array.
//	 */
//	
//	public byte[] generateDataEntry() {
//		/* The fixed-size entry contains the two Timevalues, the 'key' int and the "pointer"
//		 * to the value in the Strings section. That "pointer" consists of two int's (offset and size).
//		 * We'll reserve space [20-28], but the Node's toBytes() method will take care of that pointer
//		 * (since only it knows about what the offset needs to be) */
//		
//		byte[] array = new byte[28];	//2 long's + 3 int's (2*8 + 3*4)
//		System.arraycopy(intervalStart.toBytes(), 0, array, 0, 8);
//		System.arraycopy(intervalEnd.toBytes(), 0, array, 8, 8);
//		System.arraycopy(ArrayHelper.intToByteArray(key), 0, array, 16, 4);
//		return array;
//	}
//	
//	public byte[] generateStringEntry() {
//		/* The variable-size entry only contains the 'value' char array, with a C-style "\0" (0'ed byte) at the end. */
//		
//		byte[] array = new byte[value.length * 2 + 1];
//		System.arraycopy(ArrayHelper.charArrayToByteArray(value), 0, array, 0, value.length * 2);	// The 'value' array from the interval
//		array[value.length * 2] = 0;		// the '\0'.
//		return array;
//		
//	}
}








