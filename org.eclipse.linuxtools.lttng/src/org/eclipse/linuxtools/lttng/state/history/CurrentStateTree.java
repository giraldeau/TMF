/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * Here in TMF, the Strings will contain the "path" in the state system's /proc-like tree, for example:
 * "System/Processes/PID150/exec_mode"
 * "System/CPUs/CPU0/current_process"
 * 
 * ... and the char[] will contain the "values" that goes in this path, for example:
 * "running"
 * "2014" (representing a PID)
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
	
	/* References to the underneath State History and Builder trees */
	private StateHistoryTree stateHistTree;
	private BuilderTree builderTree;
	
	/* The quark table, that allows to convert path-strings into integers (that will be used in the SHT) */
	private PathConversionTable indexTable;
	
	/* The state info vector, which will contain the "system state" at one given requested time
	 * currentStateInfo.size() = number of different entries we've seen so far */
	private Vector<StateValue> currentStateInfo;
	
	/* Finally, the root of the /proc-like filesystem tree, which is the human-usable index to read the stateInfo */
	private final Path root;
	
	
	/**
	 * Constructor for when we're building a new tree from scratch
	 * 
	 * @param newTreeFileName The "name" of the tree, which will be the filename on disk
	 * @param treeStart The minimum Timestamp that will be stored in the tree
	 * @param blockSize The size of each node-block in the tree file. Should be a multiple of 4K bytes
	 * @param maxChildren The max. number of children every node (other than the leafs) can have
	 * @param cacheSize The size of the cache to use, in number of nodes (not a size in bytes!)
	 */
	protected CurrentStateTree(String newTreeFileName, TimeValue treeStart,
							int blockSize, int maxChildren, int cacheSize) {
		
		this.indexTable = new PathConversionTable();
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
	protected CurrentStateTree(String existingFileName, int cacheSize) throws IOException {
		/* Initialize the StateHistoryTree object, which will also rebuild the indexTable
		 * by reading the information in the file on disk. */
		this.stateHistTree = new StateHistoryTree(existingFileName, this.indexTable, cacheSize);
		
		/* Initialize the builderTree in inactive state */
		this.builderTree = new BuilderTree(stateHistTree);
		builderTree.closeBuilderTree();
		
		/* Initialize currentStateInfo at the required size */
		this.currentStateInfo = new Vector<StateValue>( indexTable.getSize() );
		
		/* Rebuild the "filesystem" paths */
		this.root = new Path("root", -1);
		for ( int i = 0; i < indexTable.getSize(); i++ ) {
			processNewAttribute( indexTable.getMatchingKey(i) );
		}
	}
	
	/**
	 * Accessors
	 */
	
	/**
	 * Conventional getter to retrieve the StateValue in the generated currentStateInfo
	 */
	protected StateValue getStateValue(Vector<String> attribute) {
		return currentStateInfo.get( indexTable.getMatchingInt(attribute) );
	}
	
	/**
	 * Alternate, singular getter method. This launches a search in the Builder Tree and SHT
	 * without re-generating the whole current state.
	 * 
	 * It's more efficient to use this if we want very few queries for the same timestamp.
	 * 
	 */
	protected StateValue getSingleStateValue(Vector<String> attribute, TimeValue t) {
		return builderTree.doSingularQuery( indexTable.getMatchingInt(attribute), t);
	}
	
	/**
	 * Internal method to convert from a Vector-type attribute to the Path/sub-path element
	 * 
	 * @param attribute
	 * @return
	 */
	private Path getMatchingPath(Vector<String> attribute) {
		assert ( indexTable.containsEntry(attribute) );
		Path currentPath = root;
		
		for ( int i = 0; i < attribute.size(); i++ ) {
			currentPath = currentPath.getSubPath(attribute.get(i));
		}
		return currentPath;
	}
	
	/**
	 * A state-changing event was passed on from the Interface.
	 * We need to check if it's in the quark database, in the /proc-like FS,
	 * and then pass it on to the Builder Tree.
	 * 
	 * @param pathAsString Dash-separated string we received from SHInterface
	 * @param value The StateValue (the interface built for us) which we need to associate to this entry
	 * @param eventTime The timestamp of this state-change
	 */
	protected void readStateChange(Vector<String> attribute, StateValue value, TimeValue eventTime) {
		
		if ( indexTable.containsEntry(attribute)) {
			/* We have seen this entry name before, it should (hopefully) be in all the tables already.
			 * We only need to pass it to the Builder Tree so it can do its thing with it. */
			builderTree.processStateChange( indexTable.getMatchingInt(attribute), value, eventTime );
			
		} else {
			/* The request entry is not in the tables. So we add it to them, THEN pass it on to the builder tree. */
			builderTree.processStateChange( processNewAttribute(attribute), value, eventTime );
		}
		
	}
	
	/**
	 * Function that will be called when we see a new entry in the quark table, which means we
	 * also need a new entry (or more than one) in the database.
	 * 
	 * @param path The path formatted as a vector of strings, which got passed on from the StateHistoryInterface.
	 * @return The integer representation of the (complete) path in the quark table
	 */
	private int processNewAttribute(Vector<String> attribute) {
		Path currentPath = this.root;
		Vector<String> currentSubVector = new Vector<String>();
		
		for ( int i = 0; i < attribute.size(); i++ ) {
			currentSubVector.add( attribute.get(i) );
			if ( !indexTable.containsEntry(currentSubVector) ) {
				/* We need to add this partial path to the table */
				currentPath.addSubPath( attribute.get(i), currentStateInfo.size() );
				currentStateInfo.add(null);			/* just to increment the size */
				indexTable.addEntry(currentSubVector);
			}
			currentPath = currentPath.getSubPath( attribute.get(i) );
		}
		
		return currentStateInfo.size()-1;
	}
	
	/**
	 * Helper method to set "null" values to the given path but also to all its children
	 * (a bit like "rm -rf")
	 * 
	 * @param path The attribute we want to set to null, including all its children
	 * @param t The timestamp associated with this state change
	 */
	protected void removeAttribute(Vector<String> attribute, TimeValue t) {
		assert ( indexTable.containsEntry(attribute) );
		nullifyPath( getMatchingPath(attribute), t );
		
	}
	
	private void nullifyPath(Path currentPath, TimeValue t) {
		/* "Nullify our children first, recursively */
		for ( int i = 0; i < currentPath.getNbSubPaths(); i++ ) {
			nullifyPath(currentPath.getSubPath(i), t);
		}
		/* Nullify ourselves */
		builderTree.processStateChange(currentPath.getKey(), new StateValue(), t);
	}
	
	/**
	 * This function is used to query the State History Tree, which will set() the currentStateInfo to
	 * the recorded values at the given targetTime.
	 * If the Builder Tree is active, we will also query it for information which might not yet be in
	 * SHT (which is the case when doing live trace reading).
	 * 
	 * @param t Target time
	 */
	protected void setStateAtTime(TimeValue t) {
		nullifyStateInfo();
		stateHistTree.doQuery(currentStateInfo, t);
		
		if ( builderTree.isActive() ) {
			builderTree.doQuery(currentStateInfo, t);
		}
	}
	
	/**
	 * Simple method to write -1/null to all the contents of currentStateInfo.
	 * This should always be called before recreating the state info, since not all
	 * state information may be available at all times
	 * (and -1 means "this attribute didn't exist at that time")
	 */
	private void nullifyStateInfo() {
		for ( int i = 0; i < currentStateInfo.size(); i++ ) {
			currentStateInfo.get(i).setNull();
		}
	}
	
	/**
	 * Method to close off the SHT and Builder trees.
	 * This happens for example when we are done reading an offline trace.
	 * We want to :
	 * 1) close the Builder Tree, commit it to the SHT, mark it as inactive
	 * 2) Have the cache in the SHT be written to disk.
	 * 3) Write the Quark Table at the end of the file, so we can reopen it later.
	 */
	protected void closeTree() {
		builderTree.closeBuilderTree();
		stateHistTree.closeTree(indexTable);
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
	
	/* =~ file content.
	 * This key = the offset in the CurrentStateTree.stateInfo vector,
	 * which is also the integer representation of the string, according to CurrentStateTree.indexTable */
	private int key;
	
	/* The sub-directories of this directory, if any */
	private Vector<Path> subDirs;
	
	/* The lookup table, to make looking up sub-directories faster
	 * The String = the name of only the sub-directory (not the whole path).
	 * The Integer = the offset in this.subDirs vector */
	private Hashtable<String, Integer> lookup;
	//FIXME is it worth having this used only when we start having a lot of entries?
	
	
	protected Path(String name, int key) {
		this.name = name;
		this.key = key;
		this.subDirs = new Vector<Path>();
		this.lookup = new Hashtable<String, Integer>();
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
	
	protected Path getSubPath(String subPathName) {
		assert( lookup.containsKey(subPathName) );
		return subDirs.get( lookup.get(subPathName) );
	}
	
	protected Path getSubPath(int index) {
		assert ( index < subDirs.size() );
		return subDirs.get(index);
	}
	
	protected int getNbSubPaths() {
		return subDirs.size();
	}
	
	
	protected void addSubPath(String subPathName, int key) {
		assert ( !lookup.containsKey(subPathName) );
		lookup.put( subPathName, subDirs.size() );
		subDirs.add( new Path(subPathName, key) );
	}
	
	
}









