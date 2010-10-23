/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.Hashtable;
import java.util.Vector;

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
	
	/* References to the underneath State History and Builder trees */
	private StateHistoryTree stateHistTree;
	private BuilderTree builderTree;
	
	/* The next two constructs implement the "quark table" to convert to and from the dash-separated paths */
	private Hashtable<String, Integer> conversionTable;
	private Vector<String> reverseConversionTable;
	
	/* The state info vector, which will contain the "system state" at one given requested time
	 * currentStateInfo.size() = number of different entries we've seen so far */
	private Vector<StateValue> currentStateInfo;
	
	/* Finally, the root of the /proc-like filesystem tree, which is the human-usable index to read the stateInfo */
	private Path root;
	
	
	/**
	 * Constructor for when we're building a new tree from scratch
	 * 
	 * @param newTreeFileName The "name" of the tree, which will be the filename on disk
	 * @param treeStart The minimum Timestamp that will be stored in the tree
	 * @param blockSize The size of each node-block in the tree file. Should be a multiple of 4K bytes
	 * @param maxChildren The max. number of children every node (other than the leafs) can have
	 * @param cacheSize The size of the cache to use, in number of nodes (not a size in bytes!)
	 */
	public CurrentStateTree(String newTreeFileName, TimeValue treeStart,
							int blockSize, int maxChildren, int cacheSize) {
		
		//FIXME use some good starting values. get them from somewhere?
		this.conversionTable = new Hashtable<String, Integer>();
		this.reverseConversionTable = new Vector<String>();
		this.currentStateInfo = new Vector<StateValue>();
		
		this.stateHistTree = new StateHistoryTree( newTreeFileName, treeStart, blockSize, maxChildren, cacheSize);
		this.builderTree = new BuilderTree( stateHistTree );
		
		this.root = new Path("root", -1);
	}
	
	/**
	 * Constructor for loading an existing tree file on disk
	 * 
	 * @param existingFileName
	 * @param cacheSize Cache size to use, in number of nodes
	 */
	public CurrentStateTree(String existingFileName, int cacheSize) {
		//TODO NYI
	}
	
	/**
	 * Accessors
	 */
	public StateValue getStateValue(String path) {
		return currentStateInfo.get( conversionTable.get(path) );
	}
	
	/**
	 * A state-changing event was passed on from the Interface.
	 * We need to check if it's in the quark database, in the /proc-like FS,
	 * and then pass it on to the Builder Tree.
	 * 
	 * @param pathAsString Dash-separated string we received from SHInterface
	 * @param value The StateValue (the interface built for us) we need to associate to this entry
	 * @param eventTime The timestamp of this state-change
	 */
	protected void readStateChange(String pathAsString, StateValue value, TimeValue eventTime) {
		
		if ( conversionTable.containsKey(pathAsString)) {
			/* We have seen this entry name before, it should (hopefully) be in all the tables already.
			 * We only need to pass it to the Builder Tree so it can do its thing with it. */
			builderTree.processStateChange( conversionTable.get(pathAsString), value, eventTime );
			
		} else {
			/* The request entry is not in the tables. So we add it to them, THEN pass it on to the builder tree. */
			builderTree.processStateChange( processPath(pathAsString), value, eventTime );
		}
		
	}
	
	/**
	 * Function that will be called when we see a new entry in the quark table, which means we
	 * also need a new entry (or more than one) in the database.
	 * 
	 * @param pathAsString The dash-separated string that got passed on from the StateHistoryInterface.
	 * @return The integer representation of the (complete) String in the quark table
	 */
	private int processPath(String pathAsString) {
		Path currentPath = this.root;
		String currentString;
		String[] components = pathAsString.split("/");
		
		for ( int i=0; i < components.length; i++) {
			/* Generate the partial String we are now processing */
			currentString = components[0];
			for ( int j=1; j <= i; j++ ) {
				currentString += "/" + components[j];
			}
			
			if ( !conversionTable.containsKey(currentString) ) {
				/* We need to add this partial path to the tables */
				currentStateInfo.add(null);		/* just to increment the size */
				conversionTable.put(currentString, currentStateInfo.size()-1 );
				reverseConversionTable.add(currentString);
				currentPath.addSubPath(components[i], currentStateInfo.size()-1 );
			}
			currentPath = currentPath.getSubPath(components[i]);
		}
		return currentStateInfo.size()-1;
	}
	
	/**
	 * This function is used to query the State History Tree, which will set() the currentStateInfo to
	 * the recorded values at the given targetTime.
	 * If the Builder Tree is active, we will also query it for information which might not yet be in
	 * SHT (which is the case when doing live trace reading).
	 * 
	 * @param t Target time
	 */
	public void setStateAtTime(TimeValue t) {
		stateHistTree.doQuery(currentStateInfo, t);
		
		if ( builderTree.isActive() ) {
			builderTree.doQuery(currentStateInfo, t);
		}
	}
	
}

/**
 * A "Path" is like both a file and a directory in the /proc-like filesystem used to represent
 * the State at any given time.
 * 
 * @author alexmont
 *
 */
class Path {
	
	/* =~ directory/file name */
	private String name;
	
	/* =~ file content. The key = the integer representation of the string representing this path */
	private int key;
	
	/* The sub-directories of this directory, if any */
	private Vector<Path> contents;
	
	/* The lookup table, to make looking up sub-directories faster
	 * The String = the name of only the sub-directory (not the whole path).
	 * The Integer = the index in this Path's contents vector */
	private Hashtable<String, Integer> lookup;
	//FIXME is it worth having this used only when we start having a lot of entries?
	
	protected Path(String name, int key) {
		this.name = name;
		this.key = key;
		this.contents = new Vector<Path>();
	}
	
	/**
	 * Accessors
	 */
	protected String getName() {
		return name;
	}
	
	protected int getKey() {
		return key;
	}
	
	
	protected void addSubPath(String subPathName, int key) {
		if ( !lookup.containsKey(subPathName) ) {
			contents.add( new Path(subPathName, key) );
			lookup.put( subPathName, contents.size()-1 );
		}
	}
	
	protected Path getSubPath(String subPathName) {
		assert( lookup.containsKey(subPathName) );
		return contents.get( lookup.get(subPathName) );
	}
}









