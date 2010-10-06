/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.*;
//import java.nio.*;
//import java.nio.channels.*;
import java.util.Random;
import java.util.Vector;

/**
 * This class exists mainly for code isolation/clarification purposes.
 * It contains all the methods and descriptors to handle reading/writing to
 * the tree-file on disk and all the caching mechanisms.
 * Every StateHistoryTree should contain 1 and only 1 StateHistoryTreeIO element.
 * 
 * @author alexmont
 *
 */
class StateHistoryTreeIO {
	
	private StateHistoryTree tree;				/* reference to the tree to which this IO-object belongs */
	
	/* Fields related to the file I/O */
	private RandomAccessFile desc;				/* File descriptor */
	
	/* Fields related to the cache */
	private Vector<Integer> isCachedList;		/* Table (index=seqNumber) indicating if the node is cached
	 											 * If it is, the Integer value indicates at which index in the following table it is located
	 											 * If it's not cached, the Integer value is -1 */
	private StateHistoryTreeNode[] nodeCache;	/* Nodes stored in memory (ie, the cache itself). length = cacheSize */
	private boolean[] isDirty;					/* Keep track if the node was changed while in cache. Uses the same index'es as 'nodeCache' */
	private int cachedCount;					/* Keep track of how many nodes are currently cached */
	
	private Random rgen;						/* To generate random numbers. Could be removed if the cache system doesn't use it */
	
	/**
	 * "New tree" constructor. This is used when we want to create a new tree from scratch.
	 * We have to specify a cacheSize parameter.
	 * 
	 * @param tree : The calling SHTree
	 * @param newTreeFileName : filename of the tree file, passed on from the SHTree's constructor
	 * @param cacheSize : How many nodes can we have in cache at most? (size of the nodeCache array)
	 */
	protected StateHistoryTreeIO(StateHistoryTree tree, String newTreeFileName, int initCacheSize) {
		assert(initCacheSize < Integer.MAX_VALUE);	
		this.tree = tree;
		
		isCachedList = new Vector<Integer>();
		cachedCount = 0;
		nodeCache = new StateHistoryTreeNode[initCacheSize];
		isDirty = new boolean[initCacheSize];
		for (int i=0; i < initCacheSize; i++) {
			nodeCache[i] = null;
			isDirty[i] = false;
		}
		
		rgen = new Random();
		
		/* Open the writer and reader descriptors */
		//FIXME test to make sure the files DOESN'T exist first
		try {
			desc = new RandomAccessFile(newTreeFileName, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * "Loading" constructor. This is used when we load an existing tree file from disk.
	 * The cacheSize parameter will be read from the file.
	 * The cache is initialized empty (no cached nodes yet)
	 * 
	 * @param tree : The SHTree object which will contain the tree information
	 * @param existingFileName : *Existing* filename to load
	 */
	protected StateHistoryTreeIO(StateHistoryTree ownerTree, 
									RandomAccessFile existingDescriptor,
									int curNodeCount, int curCacheSize) {
		this.tree = ownerTree;
		this.desc = existingDescriptor;
		
		isCachedList = new Vector<Integer>(curNodeCount);
		for ( int i=0; i < curNodeCount; i++) {
			isCachedList.set(i, -1);
		}
		
		nodeCache = new StateHistoryTreeNode[curCacheSize];
		isDirty = new boolean[curCacheSize];
		for ( int i=0; i < curCacheSize; i++ ) {
			nodeCache[i] = null;
			isDirty[i] = false;
		}
		
		rgen = new Random();
		
	}
	
	/**
	 * This method is called by the owner Tree object when we are "closing off" the tree.
	 * The TreeIO must commit all its cache to disk, then pass the file descriptor back
	 * to the Tree object so it can save its configuration.
	 * @return The 'desc' file descriptor we've been using all along
	 */
	protected RandomAccessFile closeIO() {
		commitAll();
		return this.desc;
	}
	
	
	/**
	 * Node reading and writing methods. They will check the cache status of the requested
	 * nodes and read from disk only if necessary.
	 */
	protected StateHistoryTreeNode readNode(int seqNumber) {
		int index = loadNodeInCache(seqNumber);
		assert( nodeCache[index] != null );
		return nodeCache[index];
	}
	
	protected void writeNode(StateHistoryTreeNode node) {
		int index = loadNodeInCache(node.getSequenceNumber());
		nodeCache[index] = node;
		/* We assume the node was modified */
		isDirty[index] = true;
	}
	
	/**
	 * Load the requested node in the cache.
	 * If the node is already in the cache, we will simply return where it is.
	 * @param seqNumber : seq. number of the node to check for
	 * @return The index in nodeCache where it was added
	 */
	private int loadNodeInCache(int seqNumber) {
		int cachedStatus = isCachedList.get(seqNumber);
		
		if ( cachedStatus == -1 ) {
			/* The requested node is not in the cache, let's add it */
			
			try {
			/* Position ourselves at the start of the node in the file and read the node into serializedNode */
				desc.seek(StateHistoryTree.getTreeHeaderSize() + seqNumber * tree.BLOCKSIZE);
				StateHistoryTreeNode newNode = new StateHistoryTreeNode(tree, desc);
				return reserveNextEmptySlot(newNode);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
			
		} else {
			/* We take the node directly from the cache */
			return (int) cachedStatus;
		}
	}
	
	/**
	 * Take a node stored in the cache, serialize it and write it to disk. The cache slot will be freed.
	 * @param cacheIndex : The index in nodeCache[] of the node to commit.
	 */
	private void commitNodeToDisk(int cacheIndex) {
		assert(cacheIndex < nodeCache.length);
		
		if ( nodeCache[cacheIndex] == null ) {
			/* Nothing to do! */
			return;
		}
		
		if ( isDirty[cacheIndex] == true ) {
			/* The cache slot is dirty, so we need to write the node to disk */
			
			try {
				/* Position ourselves at the start of the node and write it */
				desc.seek(StateHistoryTree.getTreeHeaderSize() + nodeCache[cacheIndex].getSequenceNumber() * tree.BLOCKSIZE);
				nodeCache[cacheIndex].writeSelf(desc);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			isDirty[cacheIndex] = false;
		}
		
		/* Mark that SHTNode as uncached in the isCachedList table */
		isCachedList.set(nodeCache[cacheIndex].getSequenceNumber(), -1);
		nodeCache[cacheIndex] = null;		/* Free that cache slot */
		cachedCount--;
		return;
	}
	
	/**
	 * Flush all the whole cache to the disk
	 */
	protected void commitAll() {
		for (int i=0; i < nodeCache.length; i++) {
			commitNodeToDisk(i);
		}
		assert(cachedCount == 0);	/* To make sure the integrity was preserved */
	}
	
	/**
	 * We get notified when the parent object (the tree) instantiates a new node, so we can
	 * add it to the lists and load it in cache right away (since it's going to be modified)
	 * 
	 * @param newNode : The fresh node that was just created
	 * @param newNodeCount : the new tree.nodeCount, = the length the isCachedList array needs to have now
	 */
	protected void newNodeAdded(StateHistoryTreeNode newNode, int newNodeCount) {

		isCachedList.add(-1);
		assert ( isCachedList.size() == newNodeCount );
		reserveNextEmptySlot(newNode);
		return;
	}

	/**
	 * Put a SHTFullNode object in the node cache.
	 * Will check for existing stuff in the cache and rotate one out if needed
	 * @param nodeToAdd : The (full) node to add.
	 * @return The index in nodeCache where the node was put
	 */
	private int reserveNextEmptySlot(StateHistoryTreeNode nodeToAdd) {
		
		if (this.cachedCount < nodeCache.length) {
			/* We still have room in teh nodeCache[] table, no need to remove any existing cached node */
			int i;
			for ( i=0; i < nodeCache.length - 1; i++) {
				if ( nodeCache[i] == null ) {
					//FIXME keep a pointer, or use a Hashtable?, so we don't have to iterate through the whole table every time.
					break;
				}
			}
			assert (nodeCache[i] == null); 		/* This could indicate a problem with the "cachedCount" value */
			nodeCache[i] = nodeToAdd;
			cachedCount++;
			isCachedList.set(nodeToAdd.getSequenceNumber(), i);
			return i;
		} else {
			/* TODO see if a LRU, or some other "better" caching mechanism would be better */
			/* We have to remove something from the cache first to make room for the new node */
			int rand = rgen.nextInt( nodeCache.length );
			
			commitNodeToDisk(rand);

			nodeCache[rand] = nodeToAdd;
			isCachedList.set( nodeToAdd.getSequenceNumber(), rand);
			cachedCount++;		//since commitNodeToDisk decremented it, we need to re-increment the count
			return rand;
		}
		
	}
	
	
}