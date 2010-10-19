/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.Hashtable;
import java.util.Vector;

/**
 * This class provides the interface to the "generic state store" with the external
 * application. In the implementation here, the app will pass on Strings and char[]'s
 * to be stored/retrieved.
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
class CurrentStateTree {
	
	protected Hashtable<String, Integer> conversionTable;
	protected Vector<Object> currentStateInfo;				/* currentStateInfo.size() = number of different entries we've seen so far */
	
	protected StateHistoryTree stateHistTree;

	
	/**
	 * Default constructor
	 */
	public CurrentStateTree() {
		//FIXME use some starting values. get them from somewhere?
		conversionTable = new Hashtable<String, Integer>();
		currentStateInfo = new Vector<Object>();
		
		
		/* Default values*/
		//TODO add a constructor in which we can specify those
		String treeFileName = "test";
		Timevalue treeStart = new Timevalue(0);
		int treeBlockSize = 64*1024;
		int treeMaxChildren = 10;
		int treeCacheSize = 100;
		
		stateHistTree = new StateHistoryTree(treeFileName, treeStart, treeBlockSize, treeMaxChildren, treeCacheSize);
		
	}
	
	/**
	 * "Downgrading" constructor, with which we build a CST from a Builder Tree.
	 * It's important to use the getStateAtTime from a CST, or else we'd mess up the
	 * important information from the Builder Tree
	 * @param bt : The BuilderTree we want to copy (sans the Timevalue vector)
	 */
	public CurrentStateTree(BuilderTree bt) {
		this.conversionTable = new Hashtable<String, Integer>( bt.conversionTable.size() );  //FIXME useful/correct to do this?
		bt.conversionTable.putAll(this.conversionTable);
		
		this.currentStateInfo = new Vector<Object>( bt.currentStateInfo.size() );
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
	public void getStateAtTime(Timevalue t) {
		stateHistTree.doQuery(currentStateInfo, t);
		/* 
		 * TODO if we ever want to support getting state WHILE building a SHT (streaming, etc)
		 * then *here* we should add a way to get the state information from the currently-running
		 * BuilderTree too, since the currently-active information won't be stored in the State History yet.
		 */
	}
	
}












