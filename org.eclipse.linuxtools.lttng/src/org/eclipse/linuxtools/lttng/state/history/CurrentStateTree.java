/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * 
 * Here in TMF, the Strings will contain the "path" in the state system's /proc-like tree, for example:
 * "System/Processes/PID150/ExecMode"
 * "System/CPUs/CPU0/CurrentProcess"
 * 
 * ... and the char[] will contain the "values" that goes in this path, for example:
 * "RUNNING"
 * "(PID)2014"
 * 
 * Note that this char[] will stay the same throughout the whole StateHistory environnment, and
 * will be contained in the field 'char[] value' of the SHT-Intervals
 * 
 * The Strings, however, will be converted to numbers, kind of like a dynamic Quark table.
 * To do so, they will be stored in a Hashtable, which will associate them with an index number (int)
 * This number is the 'key' that will be used in the State History Tree.
 * 
 * 
 * Finally, there is also the BuilderTree, which adds a Timevalue to every element in currentStateInfo.
 * This variant is used when building the State History Tree, as we retain the "start time" of the element. When
 * it gets replaced, we can build an interval from the old start time and the start time of the new info, which is = to the
 * end time of the one that got replaced (did that make sense?)
 * 
 * @author alexmont
 *
 */
public class CurrentStateTree {
	
	protected Hashtable<String, Integer> conversionTable;
	protected Vector<StateValue> currentStateInfo;				/* currentStateInfo.size() = number of different entries we've seen so far */
	
	protected StateHistoryTree stateHistTree;

	
	/**
	 * Default constructor, with pre-defined configuration values
	 */
	public CurrentStateTree(String newTreeFileName) {
		this(newTreeFileName, new TmfTimestamp(0), 64*1024, 10, 100);
	}
	
	/**
	 * Constructor for when we're building a new tree from scratch
	 * 
	 * @param newTreeFileName The "name" of the tree, which will be the filename on disk
	 * @param treeStart The minimum Timestamp that will be stored in the tree
	 * @param blockSize The size of each node-block in the tree file. Should be a multiple of 4K bytes
	 * @param maxChildren The max. number of children every node (other than the leafs) can have
	 * @param cacheSize The size of the cache to use, in number of nodes (not a size in bytes!)
	 */
	public CurrentStateTree(String newTreeFileName, TmfTimestamp treeStart,
							int blockSize, int maxChildren, int cacheSize) {
		
		//FIXME use some good starting values. get them from somewhere?
		this.conversionTable = new Hashtable<String, Integer>();
		this.currentStateInfo = new Vector<StateValue>();
		
		this.stateHistTree = new StateHistoryTree( newTreeFileName, (TimeValue) treeStart, blockSize, maxChildren, cacheSize);
	}
	
	
	public CurrentStateTree(String existingFileName, int cacheSize) {
		
	}
	
	/**
	 * "Downgrading" constructor, with which we build a CST from a Builder Tree.
	 * It's important to use the getStateAtTime from a CST, or else we'd mess up the
	 * important information from the Builder Tree
	 * @param bt : The BuilderTree we want to copy (sans the Timevalue vector)
	 */
	protected CurrentStateTree(BuilderTree bt) {
		this.conversionTable = new Hashtable<String, Integer>( bt.conversionTable.size() );  //FIXME useful/correct to do this?
		bt.conversionTable.putAll(this.conversionTable);
		
		this.currentStateInfo = new Vector<StateValue>( bt.currentStateInfo.size() );
		//We do not care about the data in that vector though, we'll get our own.
		
		/* Both objects still need to point to the same State History Tree: */
		this.stateHistTree = bt.stateHistTree;
	}
	
	
	/**
	 * This function is used to query the State History Tree, which will set() the currentStateInfo to
	 * the recorded values at the given targetTime
	 * 
	 * @param t Target Time
	 */
	public void getStateAtTime(TimeValue t) {
		stateHistTree.doQuery(currentStateInfo, t);
		/* 
		 * TODO if we ever want to support getting state WHILE building a SHT (streaming, etc)
		 * then *here* we should add a way to get the state information from the currently-running
		 * BuilderTree too, since the currently-active information won't be stored in the State History yet.
		 */
	}
	
}












