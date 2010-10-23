/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.*;
import java.util.Vector;

/**
 * Meta-container for the State History Tree. This structure contains all the
 * high-level data relevant to the tree.
 * 
 * @author alexmont
 *
 */
class StateHistoryTree {
	
	/**
	 * Configuration constants
	 */
	protected final int BLOCKSIZE;			/* Size (in bytes) of each block on disk */
	protected final int MAX_NB_CHILDREN;	/* Max amount of children per non-leaf node */
	
	
	/**
	 * Fields
	 */
	
	private StateHistoryTreeIO treeIO;		/* Reader/writer/cacher object */
	
	private final TimeValue treeStart;		/* Beginning timestamp of the tree (lowest time possible) */
	
	private int nodeCount;					/* How many nodes exist in this tree, total */
	private int depth;						/* How many "levels" there are */
	private int rootNode;					/* Sequence number of the root node */
	private int latestLeaf;					/* Sequence number of the "latest leaf" (where insertions are tried first) */
	
	
	/**
	 * Basic constructor : instantiate a new empty tree from scratch
	 * 
	 * @param newFileName : Name/path of the tree-file to create
	 * @param start : origin (minimum) timestamp of the tree
	 * @param blockSize : desired size (bytes) of each block on disk. Should be a multiple of 4096
	 * @param maxChildren : Max number of children per node.
	 * @param cacheSize : (not a "size" in bytes!) Size of the cache, in number of nodes
	 */
	public StateHistoryTree(String newTreeFileName, TimeValue start, int blockSize, int maxChildren, int cacheSize) {
		/* Simple assertion to make sure we have enough place in the 0th block for the tree configuration */
		assert( blockSize >= getTreeHeaderSize() );
		
		this.BLOCKSIZE = blockSize;
		this.MAX_NB_CHILDREN = maxChildren;
		this.treeStart = start;
		this.nodeCount = 0;
		this.depth = 1;
		
		/* Prepare the IO object */
		this.treeIO = new StateHistoryTreeIO(this, newTreeFileName, cacheSize);
		
		/* Add the first node to the tree */
		StateHistoryTreeNode firstNode = initNewNode(-1, this.treeStart);
		this.rootNode = firstNode.getSequenceNumber();
		this.latestLeaf = firstNode.getSequenceNumber();
	
	}
	
	/**
	 * "Reader" constructor : instantiate a SHTree from an existing tree file on disk
	 * 
	 * @param existingFileName : Name of the tree-file we are opening. This file must already exist.
	 * @param cacheSize : size of the node cache, will be passed on to the TreeIO object
	 */
	public StateHistoryTree(String existingFileName, int cacheSize) throws IOException {
		/* 
		 * Open the file ourselves, get the tree header information we need,
		 * then pass on the descriptor to the TreeIO object.
		 */
		//FIXME test to make sure the file already exists
		RandomAccessFile desc = new RandomAccessFile(existingFileName, "rw");
		assert (desc.readInt() == 2114);	/* = magic number, to make sure we're opening the right type of file */
		
		this.BLOCKSIZE = desc.readInt();
		this.MAX_NB_CHILDREN = desc.readInt();
		this.treeStart = new TimeValue(desc.readLong());
		
		this.nodeCount = desc.readInt();
		this.depth = desc.readInt();
		this.rootNode = desc.readInt();
		this.latestLeaf = desc.readInt();
			
		this.treeIO = new StateHistoryTreeIO(this, desc, nodeCount, cacheSize);
	}
	
	/**
	 * "Destructor" method.
	 * This method will cause the treeIO object to commit all nodes to disk, destroy itself,
	 * and then return the RandomAccessFile descriptor so the Tree object can save its configuration
	 * into the header of the file.
	 */
	public void destroyTree() {
		RandomAccessFile desc = treeIO.closeIO();
		
		/* Save the config of the tree to the header of the file */
		try {
			desc.seek(0);
			desc.writeInt(2114);	/* Magic number for this file type */
			
			desc.writeInt(BLOCKSIZE);
			desc.writeInt(MAX_NB_CHILDREN);
			desc.writeLong(treeStart.getValue());
			
			desc.writeInt(nodeCount);
			desc.writeInt(depth);
			desc.writeInt(rootNode);
			desc.writeInt(latestLeaf);
			
			desc.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Accessors
	 */
	protected TimeValue getTreeStart() {
		return treeStart;
	}
	
	protected int getNodeCount() {
		return nodeCount;
	}

	
	/**
	 * Insertion methods, for when we want to insert intervals into the tree
	 */
	public void insertInterval(StateHistoryTreeInterval interval) {
		//TODO try other insertion methods? (at root node, etc.)
		tryInsertAtNode(interval, this.latestLeaf);
	}
	
	/**
	 * Try to insert the desired interval into the tree, starting at the node indicated
	 * by nodeSeqNumber.
	 * 
	 * @param interval : Filled up interval we want to insert in the tree (will need to be already "constructed")
	 * @param nodeSeqNumber : Sequence number of the node where we want to try the insertion for this call.
	 * @return The Seq. Number of the node where the interval was finally inserted
	 */
	private int tryInsertAtNode(StateHistoryTreeInterval interval, int nodeSeqNumber) {
		StateHistoryTreeNode targetNode, nextNode;
		targetNode = treeIO.readNode(nodeSeqNumber);
		
		System.out.print("Debug: Trying to insert interval in node #" + nodeSeqNumber + ": ");
		
		/* Check if this interval wouldn't fit in a child instead
		 * (only do this check if we are not at the leaf level,
		 * during recursive calls for example)
		 */
		if ( targetNode.getNbChildren() > 0 ) {
			//TODO ...but we need to make sure we don't end up with two full nodes
			//		throwing it back and forth
		}
		
		/* Verify if there is enough room in this node to store this interval */
		if ( interval.getIntervalSize() > targetNode.getNodeFreeSpace() ) {
			/* Nope, not enough room. Insert in a new sibling instead. */
			System.out.println("Not enough room, we'll add a new sibling node");
			treeIO.writeNode(targetNode);
			nextNode = addSiblingNode(targetNode, interval.getEnd());
			assert( nextNode.getSequenceNumber() != nodeSeqNumber );	//So we don't get stuck in a circle
			return tryInsertAtNode(interval, nextNode.getSequenceNumber());
		}
		
		/* Make sure the interval time range fits this node */
		if ( interval.getStart().compareTo(targetNode.getNodeStart(), false) == -1 ) {
			System.out.println("Time range doesn't fit, we'll try in parent next.");
			/* No, this interval starts before the startTime of this node.
			 * We need to check recursively in parents if it can fit.
			 */
			assert( targetNode.getParentSequenceNumber() != -1 );		/* This would mean we're trying to insert something
																		   that starts *before* the root node. */
			return tryInsertAtNode( interval, targetNode.getParentSequenceNumber() );
		}
		
		/* Ok, there is room, and the interval fits in this time slot. Let's add it. */
		System.out.println("Great succes!");
		targetNode.addInterval(interval);
		return targetNode.getSequenceNumber();
	}
	
	
	/**
	 * Add a new empty node to the tree.
	 * @param parentSeqNumber : Sequence number of this node's parent
	 * @param start : Start time of this node
	 * @return The newly created node, duh
	 */
	private StateHistoryTreeNode initNewNode(int parentSeqNumber, TimeValue start) {
		StateHistoryTreeNode newNode = new StateHistoryTreeNode(this, this.nodeCount, parentSeqNumber, start);
		this.nodeCount++;
		/* Let the IO/caching system know we added a node so it needs to update its lists */
		treeIO.newNodeAdded(newNode, this.nodeCount);
		return newNode;
	}
	
	/**
	 * Add a child to the 'parent' node
	 * 
	 * @param parentSeqNumber : Seq. number of the parent node
	 * @param start : Start time of the new node
	 * @return the newly-created node
	 */
	private StateHistoryTreeNode addChildNode(int parentSeqNumber, TimeValue start) {
		StateHistoryTreeNode parentNode = treeIO.readNode(parentSeqNumber);
		StateHistoryTreeNode newNode = initNewNode(parentSeqNumber, start);
		
		parentNode.linkNewChild(newNode.getSequenceNumber(), start);

		treeIO.writeNode(parentNode);
		treeIO.writeNode(newNode);
		
		return newNode;
	}
	
	/**
	 * Add 'depth' recursive children to the 'parent' node
	 * 
	 * @param parentSeqNumber : Seq. number of the initial parent
	 * @param depth : how many generations do we need to add
	 * @param start : Start time of each newly-created node
	 * @return The lowest-level new node that was created
	 */
	private StateHistoryTreeNode addBranch(int parentSeqNumber, int depth, TimeValue start) {
		StateHistoryTreeNode parentNode = treeIO.readNode(parentSeqNumber);
		StateHistoryTreeNode newNode = null;
		
		for ( int i=0; i < depth; i++ ) {
			newNode = addChildNode(parentNode.getSequenceNumber(), start);
			parentNode = newNode;		//for the following loops, if any
		}
		assert( newNode != null );
		treeIO.writeNode(newNode);		//FIXME useful?
		return newNode;
	}
	
	/**
	 * Add a new root node at the top of the tree, and create a new branch
	 * all the way down to the leaf level
	 * 
	 * @param splitTime : Timevalue that will separate the new branch from the last
	 * @return The new latest leaf of the tree
	 */
	private StateHistoryTreeNode addNewRootNode(TimeValue splitTime) {
		StateHistoryTreeNode oldRootNode, newRootNode, newLatestNode;
		
		oldRootNode = treeIO.readNode(this.rootNode);
		newRootNode = initNewNode(-1, this.treeStart);
		
		/* Tell the old root node that it isn't root anymore */
		oldRootNode.setParentSequenceNumber(newRootNode.getSequenceNumber());
		closeNode(oldRootNode, splitTime);
		
		/* Link the new root to its first child (the previous root node) */
		newRootNode.linkNewChild(oldRootNode.getSequenceNumber(), splitTime);
		
		/* Create a new branch up to the leaf level
		 * (at this point, tree.depth is still = to the old value, which is
		 * the number of new children we want */
		newLatestNode = addBranch(newRootNode.getSequenceNumber(), this.depth, splitTime);
		
		/* Update the fields in the tree object */
		this.depth++;
		this.rootNode = newRootNode.getSequenceNumber();
		this.latestLeaf = newLatestNode.getSequenceNumber();
		
		treeIO.commitAll();		//TODO test if useful or not
		
		return newLatestNode;
	}
	
	/**
	 * "Close" this node and its latest child, recursively
	 * 
	 * @param node : The node to "close" off
	 * @param endtime : The nodeEnd time that the closed node(s) will have
	 */
	private void closeNode(StateHistoryTreeNode node, TimeValue endtime) {
		node.closeThisNode(endtime);
		treeIO.writeNode(node);
		
		/* Recursively close off all of this node's latest children */
		if ( node.getNbChildren() > 0 ) {
			closeNode(treeIO.readNode(node.getLatestChild()), endtime);
		}
		return;
	}
	
	/**
	 * Wrapper using the above functions to simply "add a sibling to the refnode".
	 * In the easiest case, we will add a new node to the parent. In the worst case,
	 * we will need to create a new root node and a new branch all the way back down.
	 * 
	 * @param refNode : Current existing node for which we want a sibling
	 * @param splitTime : Timevalue that will separate the new node (or whole branch, depending) from the old one.
	 * @return The newly-created sibling node we so desperately asked for.
	 */
	private StateHistoryTreeNode addSiblingNode(StateHistoryTreeNode refNode, TimeValue splitTime) {
		int level = 1;
		StateHistoryTreeNode parentNode;
		
		refNode.closeThisNode(splitTime);
		
		if ( refNode.getParentSequenceNumber() == -1 ) {
			/* The function was called on the current root node */
			refNode = addNewRootNode(splitTime);
			return refNode;
		}
		
		parentNode = treeIO.readNode(refNode.getParentSequenceNumber());
		while ( parentNode.getNbChildren() >= MAX_NB_CHILDREN ) {
			/* This parent already has the max amount of children allowed,
			 * we'll go up and look one level higher.
			 */
			level++;
			refNode = parentNode;
			refNode.closeThisNode(splitTime);
			if ( refNode.getParentSequenceNumber() == -1 ) {
				/* We have reached the root node, and it is still full.
				 * That means we need to add a new root node.
				 */
				refNode = addNewRootNode(splitTime);
				return refNode;
			} else {
				parentNode = treeIO.readNode(refNode.getParentSequenceNumber());
			}
		}
		
		/* If we get here, we have attained a node where we can add children
		 * (without it being the root node)
		 * "level" is now = to the length of the branch we need to add to get back to the leaf level.
		 */
		//FIXME: handle the, albeit rare, case where we'd call this function on a non-leaf node, and end up here.
		//We would still need to add nodes farther down to get to the leaf level. level+something...
		//maybe check with tree.depth
		closeNode(refNode, splitTime);
		refNode = addBranch(parentNode.getSequenceNumber(), level, splitTime);
		this.latestLeaf = refNode.getSequenceNumber();
		return refNode;
	}
	
	
	/**
	 * Main query method, usually called by the Current State Tree, which
	 * sends in parameter a reference to its stateInfo array, which the SHT will
	 * fill up with the information it gets by exploring its own nodes.
	 * 
	 * @param stateInfo : the currentStateInfo of the CurrentStateTree, which this method will fill up
	 * @param t : the timestamp for which we want the query
	 */
	public void doQuery(Vector<StateValue> stateInfo, TimeValue t) {
		int potentialNext = 0;
		/* We start by reading the information in the root node */
		StateHistoryTreeNode currentNode = treeIO.readNode(rootNode);
		currentNode.getInfoFromNode(stateInfo, t);
		
		while ( currentNode.getNbChildren() > 0 ) {
			/* Look at the children's start times, to see which branch we must follow next */
			for ( int i = 0; i < currentNode.getNbChildren(); i++ ) {
				if ( t.compareTo(currentNode.getChildStart(i), false) >= 0 ) {
					potentialNext = currentNode.getChild(i);
				} else {
					break;
				}
			}
			/* Once we exit this loop, we should have found a children to follow.
			 * If we didn't, there's a problem. */
			assert ( potentialNext != currentNode.getSequenceNumber() );
			
			/* Go into this next node, and repeat until we get to the leaf level */
			currentNode = treeIO.readNode(potentialNext);
			currentNode.getInfoFromNode(stateInfo, t);
		}
		
		/* The stateInfo should now be filled with everything needed, we pass the control
		 * back to the Current State Tree. */
		return;
	}

	
	/**
	 * Helper function to get the size of the "tree header" in the tree-file
	 * The nodes will use this offset to know where they should be in the file.
	 * This should always be a multiple of 4K.
	 */
	protected static int getTreeHeaderSize() {
		return 4096;
	}
	
	/**
	 * Test/debugging functions
	 */
	
	public int getAverageNodeUsage() {
		StateHistoryTreeNode node;
		int total = 0;
		
		for ( int seq = 0; seq < nodeCount; seq++ ) {
			node = treeIO.readNode(seq);
			total += node.getNodeUsagePRC();
		}
		
		return total / nodeCount;
	}
	
	@Override
	public String toString() {
		return "\n\n" +
				"Information on the current tree:\n\n" +
				"Number of nodes: " + nodeCount + "\n" +
				"Depth of the tree: " + depth + "\n" +
				"Size of the treefile: " + "???" + "\n" +
				"Root node has sequence number: " + rootNode + "\n" +
				"'Latest leaf' has sequence number: " + latestLeaf + "\n" +
				"Average node utilization: " + this.getAverageNodeUsage() + "\n"
				;
	}
	
	private int curDepth;
	/**
	 * Start at currentNode and visit all its children in pre-order.
	 * Give the root node in parameter to visit the whole tree, and
	 * have a nice overview.
	 */
	private void preOrderVisit(StateHistoryTreeNode currentNode) {
		int i, j;
		StateHistoryTreeNode nextNode;
		
		System.out.println(currentNode.toString());
		curDepth++;
		
		for ( i = 0; i < currentNode.getNbChildren(); i++ ) {
			nextNode = treeIO.readNode( currentNode.getChild(i) );
			for ( j=0; j < curDepth - 1; j++ ) {
				System.out.print("  ");
			}
			System.out.print("+-");
			preOrderVisit(nextNode);
		}
		curDepth--;
		return;
	}
	
	public void printFullTreeHierarchy() {
		curDepth = 0;
		this.preOrderVisit( treeIO.readNode(rootNode) );
	}
}





