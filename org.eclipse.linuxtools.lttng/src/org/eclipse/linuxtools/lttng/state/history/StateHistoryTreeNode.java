/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.*;
import java.util.Vector;

/**
 * Node component of the State History Tree.
 * 
 * A node header read from the state history file will fill up this structure.
 * Note that *only* the header is read at first. If we want to read the containing
 * information, we need to "upgrade" this node to a StateHistoryTreeFullNode, declared below.
 *  
 * The toBytes() method converts all the fields to packed "bytes" so the node
 * can be written to disk.
 * 
 * @author alexmont
 *
 */
class StateHistoryTreeNode {
	
	private StateHistoryTree containerTree;		/* Tree to which tree this node belongs (for config. constants) */
	
	private Timevalue nodeStart;		/* Start time of this node */
	private Timevalue nodeEnd;			/* End time of this node */
	
	private int sequenceNumber;			/* Seq. number of this node */
	private int parentSequenceNumber;	/* Seq. number of the parent node. If = -1, that means it's the root node */
	
	private int nbChildren;				/* Nb. of children this node has */
	private int[] children;				/* Seq. numbers of the children nodes (size = MAX_NB_CHILDREN) */
	private Timevalue[] childStart;		/* Start times of each of the children (size = MAX_NB_CHILDREN) */
	private boolean isDone;				/* Is there newer nodes (time-wise) to the right of this one? */

	private boolean isFull;				/* Is this node full? (no more room for intervals) */
	private int intervalCount;			/* Number of intervals in this node */
	
	private int stringSectionOffset;	/* Position in the file/byte array where the string section BEGINS */
	
	private Vector<StateHistoryTreeInterval> intervals;	/* Vector containing all the intervals contained in this node */
	
	/**
	 * Initial constructor. Only use this to initialize a new EMPTY node
	 */
	public StateHistoryTreeNode(StateHistoryTree tree, int seqNumber, int parentSeqNumber, Timevalue start) {
		this.containerTree = tree;
		this.nodeStart = start;
		this.sequenceNumber = seqNumber;
		this.parentSequenceNumber = parentSeqNumber;
		
		this.isFull = false;
		this.isDone = false;
		
		this.nbChildren = 0;
		
		/* We instantiate the two following arrays at full size right away, since we
		 * want to reserve that space in the node's header. this.nbChildren will tell us
		 * how many relevant entries there are in those tables. */
		this.children = new int[containerTree.MAX_NB_CHILDREN];
		this.childStart = new Timevalue[containerTree.MAX_NB_CHILDREN];
		
		this.intervalCount = 0;
		
		this.stringSectionOffset = containerTree.BLOCKSIZE;
		
		this.intervals = new Vector<StateHistoryTreeInterval>();
	}
	
	/**
	 * Reader constructor v2. Reads the serialized node information directly from the file,
	 * avoiding a useless copy.
	 * (Is this (passing on the file descriptor) considered bad practice?)
	 * 
	 * @param tree The container StateHistoryTree.
	 * @param desc The RandomAccessFile descriptor. Needs to be positioned (seek'ed) where the node starts
	 */
	public StateHistoryTreeNode(StateHistoryTree tree, RandomAccessFile desc) {
		
		containerTree = tree;
		
		try {
			/* Read the header */
			nodeStart = new Timevalue(desc.readLong());
			nodeEnd = new Timevalue(desc.readLong());
			sequenceNumber = desc.readInt();
			parentSequenceNumber = desc.readInt();
			nbChildren = desc.readInt();
			intervalCount = desc.readInt();
			isDone = desc.readBoolean();
			isFull = desc.readBoolean();
			stringSectionOffset = desc.readInt();
			
			children = new int[containerTree.MAX_NB_CHILDREN];
			for (int i=0; i < nbChildren; i++) {
				children[i] = desc.readInt();
			}
			
			this.childStart = new Timevalue[containerTree.MAX_NB_CHILDREN];
			for (int i=0; i < nbChildren; i++) {
				childStart[i] = new Timevalue(desc.readLong());
			}
			
			/* Read the intervals information */
			this.intervals = new Vector<StateHistoryTreeInterval>(intervalCount);
			
			for ( int i=0; i < intervalCount; i++ ) {
				intervals.add( new StateHistoryTreeInterval(desc, this.getStartPositionInFile()) );
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * The anti-thesis of the previous constructor. The node will write itself and its content
	 * into the file descriptor that's passed in parameter.
	 * That descriptor needs to be pre-seek'ed at the start of the node in the file.
	 * 
	 * @param desc The RandomAccessFile in which we'll write
	 */
	public void writeSelf(RandomAccessFile desc) throws IOException {
		int curStringsEntryPos = stringSectionOffset;
		int size;
		
		/* Write the header */
		desc.writeLong(this.getNodeStart().getValue());
		desc.writeLong(this.getNodeEnd().getValue());
		desc.writeInt(sequenceNumber);
		desc.writeInt(parentSequenceNumber);
		desc.writeInt(nbChildren);
		desc.writeInt(intervalCount);
		desc.writeBoolean(isDone);
		desc.writeBoolean(isFull);
		desc.writeInt(stringSectionOffset);
		
		for ( int i=0; i < nbChildren; i++ ) {
			desc.writeInt(children[i]);
		}
		
		for ( int i=0; i < nbChildren; i++ ) {
			desc.writeLong(childStart[i].getValue());
		}
		
		/* Write the intervals information */
		
		for ( int i=0; i < intervalCount; i++ ) {
			size = intervals.get(i).writeInterval(desc, this.getStartPositionInFile(), curStringsEntryPos);
			curStringsEntryPos += size;		// necessary to put on 2 lines? Not sure how Java would handle it on one... */
		}
		
		/* If the offsets were right, we should now be at the end of the block */
		assert( curStringsEntryPos == containerTree.BLOCKSIZE );
			
	}
	
	/**
	 * Accessors
	 */
	public Timevalue getNodeStart() {
		return nodeStart;
	}
	
	public Timevalue getNodeEnd() {
		if ( this.isDone ) {
			return nodeEnd;
		} else {
			return new Timevalue(0);
		}
	}
	
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	
	public int getParentSequenceNumber(){
		return parentSequenceNumber;
	}
	
	public void setParentSequenceNumber(int newParent) {
		parentSequenceNumber = newParent;
	}
	
	public int getNbChildren() {
		return nbChildren;
	}
	
	public int getChild(int index) {
		return children[index];
	}
	
	public int getLatestChild() {
		return children[nbChildren - 1];
	}
	
	public Timevalue getChildStart(int index) {
		return childStart[index];
	}
	
	public Timevalue getLatestChildStart() {
		return childStart[nbChildren - 1];
	}
	
	public StateHistoryTreeInterval getInterval(int index) {
		return intervals.get(index);
	}
	
	
	private long getStartPositionInFile() {
		return sequenceNumber * containerTree.BLOCKSIZE + StateHistoryTree.getTreeHeaderSize();
	}
	
	/**
	 * @return The offset, within the node, where the Data section ends
	 */
	private int getDataSectionEndOffset() {
		return this.getNodeHeaderSize() + StateHistoryTreeNode.getDataEntrySize() * intervalCount;
	}
	/**
	 * Returns the free space in the node, which is simply put,
	 * the stringSectionOffset - dataSectionOffset
	 */
	public int getNodeFreeSpace() {
		return stringSectionOffset - this.getDataSectionEndOffset();
	}
	
	/**
	 * Returns the current space utilization of this node, as a percentage.
	 * (used space / total usable space, which excludes the header)
	 */
	public int getNodeUsagePRC() {
		float freePercent = (float) this.getNodeFreeSpace() / (float) (containerTree.BLOCKSIZE - this.getNodeHeaderSize()) * 100f;
		return (int) (100 - freePercent);
	}

	/**
	 * Add an interval to this node
	 * @param newInterval
	 */
	public void addInterval(StateHistoryTreeInterval newInterval) {
		/* Just in case, but should be checked before even calling this function */
		assert( newInterval.getIntervalSize() <= this.getNodeFreeSpace() );
		assert( intervalCount == intervals.size() );
		
		intervalCount++;
		intervals.add( newInterval );
		
		/* Update the in-node offset "pointer" */
		stringSectionOffset -= ( newInterval.getStringsEntrySize() );
	}


	/**
	 * Tell this node that it has a new child (Congrats!)
	 * 
	 * @param seqNumber : Sequence number of the new child
	 * @param start : Start-time of that child
	 */
	public void linkNewChild(int childSeqNumber, Timevalue start) {
		assert( this.nbChildren < containerTree.MAX_NB_CHILDREN );
		
		this.children[nbChildren] = childSeqNumber;
		this.childStart[nbChildren] = start;
		this.nbChildren++;
	}
	
	/**
	 * We've received word from the containerTree that newest nodes now exist to our right.
	 * (Puts isDone = true and sets the endtime)
	 * 
	 * @param node : The node to "close" off
	 * @param endtime : The nodeEnd time that the node will have
	 */
	public void closeThisNode(Timevalue endtime) {
		//TODO sort the intervals by end time here?
		this.isDone = true;
		this.nodeEnd = endtime;
		return;
	}
	
	/**
	 * The method to fill up the stateInfo (passed on from the Current State Tree when
	 * it does a query on the SHT). We'll replace the data in that vector with whatever
	 * relevant we can find from this node
	 * 
	 * @param stateInfo : the same stateInfo that comes from SHT's doQuery()
	 * @param t : the Timevalue for which the query is for. Only return intervals that intersect t.
	 */
	public void getInfoFromNode(Vector<Object> stateInfo, Timevalue t) {
		/* 
		 * TODO: this here could be optimised (to half the time, on average) if we could
		 * guarantee that the intervals are sorted chronologically.
		 * Maybe do such a sort once the tree is built. Or once the node gets closed. Or not at all.
		 */
		
		for ( int i = 0; i < intervalCount; i++ ) {
			
			/* if:  t intersects intervals[i] then: write its value in stateInfo */
			if ( t.compareTo( intervals.get(i).getEnd(), false) <= 0 ) {
				if ( t.compareTo( intervals.get(i).getStart(), false) >= 0 ) {
					stateInfo.set( intervals.get(i).getKey(), intervals.get(i).getValueType() );
				}
			}
		}
		
		return;
	}

	/**
	 * Helper function to make up for Java's lack of sizeof()...
	 * Returns the size of this node's header.
	 * When the node will be serialized with the toBytes() function,
	 * this value represents at which offset the Map Section begins.
	 */
	private int getNodeHeaderSize() {
		
		int size =	  16	/* 2 x Timevalue ( = 2 x long ) */
					+ 8		/* 2 x int (sequenceNumber & parentSequenceNumber) */
					+ 8		/* 2 x int (nbChildren, intervalCount) */
					+ 2		/* 2 x bytes (isDone, isFull) (we will store the booleans as bytes) */
					+ 4		/* 1 x int (stringsSectionOffset) */
					+ 4 * containerTree.MAX_NB_CHILDREN		/* MAX_NB * int ('children' table) */
					+ 8 * containerTree.MAX_NB_CHILDREN;	/* MAX_NB * Timevalue ('childStart' table) */
		return size;
	}
	
	protected static int getDataEntrySize() {
		return    16	/* 2 x Timevalue/long (interval start + end) */
				+ 4		/* int (key) */
				+ 1		/* byte (type) */
				+ 4;	/* int (valueOffset) */
			 /* = 25 */
	}
	
	@Override
	public String toString() {
		String ret = "Node #" + sequenceNumber + ", " + nbChildren + " children, " +
					intervalCount + " intervals (" + this.getNodeUsagePRC() + "% used), [" +
					this.nodeStart.getValue() + " - ";
		
		if ( this.isDone ) {
			ret = ret.concat( "" + this.nodeEnd.getValue());	//FIXME  ""+  seriously needed? there has to be a fancier way to do this....
		} else {
			ret = ret.concat("...");
		}
		
		ret = ret.concat("]");
		return ret;
	}
}

















