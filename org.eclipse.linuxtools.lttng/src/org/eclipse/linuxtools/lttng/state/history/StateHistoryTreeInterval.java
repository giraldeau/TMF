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
	
}








