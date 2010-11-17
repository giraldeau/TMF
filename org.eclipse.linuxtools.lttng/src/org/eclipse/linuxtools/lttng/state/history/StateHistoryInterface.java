/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.IOException;
import java.util.Vector;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;

/**
 * This is the highest-level container of the whole State History system.
 * It keeps track of all the Current State Trees used in the experiment.
 * (each CST then contains its related Builder Tree and History Tree)
 * 
 * This class also provides the external public API.
 * 
 * @author alexmont
 *
 */
public class StateHistoryInterface {

	private Vector<CurrentStateTree> treeList;
	
	
	/**
	 * Constructor
	 */
	public StateHistoryInterface() {
		this.treeList = new Vector<CurrentStateTree>();
	}
	
	/**
	 * Add a new StateTree (including CST, SHT, and everything) to the system.
	 * You can also use the simplified method below, which uses default configuration values.
	 * 
	 * @param fileName Filename of the tree-file
	 * @param treeStart Minimal timestamp that can be stored in the tree
	 * @param blockSize Size of the blocks on disk
	 * @param maxChildren Max. number of children allowed per node
	 * @param cacheSize Size of the cache (in number of nodes, not in bytes!)
	 * @return The index this tree will have in the list. It can then be used to add events to this specific tree.
	 */
	public int createNewStateHistoryFile(String fileName, LttngTimestamp treeStart,
											int blockSize, int maxChildren, int cacheSize) {
		treeList.add( new CurrentStateTree(fileName, new TimeValue(treeStart), blockSize, maxChildren, cacheSize) );
		return treeList.size()-1;
	}
	
	public int createNewStateHistoryFile(String fileName, LttngTimestamp treeStart) {
		treeList.add( new CurrentStateTree(fileName, new TimeValue(treeStart), 64*1024, 10, 100) );
		return treeList.size()-1;
	}
	
	
	/**
	 * Add a new StateTree to the list, by loading a file already existing on disk.
	 * All the relevant parameters will be read from that file.
	 * 
	 * @param fileName Path/name of the already-existing State History file
	 * @return The index this tree has in the list, or -1 if the file was not found.
	 */
	public int loadExistingStateHistoryFile(String fileName) {
		try {
			treeList.add( new CurrentStateTree(fileName, 100) );
			return treeList.size()-1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	
	/**
	 * Methods to register modifications to the Current State
	 */
	
	/**
	 * External wrappers to the real "stateChange" method below.
	 * They will convert the path (if needed), value and timestamp to the internal class format.
	 * 
	 * @param treeIndex To which tree (in the treeList) we want to add this event. It was returned at the tree creation.
	 * @param pathName The 'path' to which we want to add the record (ex.: {hostname, processes, PID2001, execMode} )
	 * 					The single-string-with-slashes representation can also be used (it will be converted to a Vector)
	 * @param valueInt (or valueStr) The value this entry needs to have (either String or int, ex.: "syscall" or '1234')
	 * @param t The timestamp associated with this state change
	 */
	public void modifyAttribute(int treeIndex, Vector<String> path, int valueInt, LttngTimestamp t) {
		stateChange(treeIndex, path, new StateValue(valueInt), new TimeValue(t));
		return;
	}
	
	public void modifyAttribute(int treeIndex, String pathAsString, int valueInt, LttngTimestamp t) {
		stateChange(treeIndex, convertPathToVector(pathAsString), new StateValue(valueInt), new TimeValue(t));
		return;
	}
	
	public void modifyAttribute(int treeIndex, Vector<String> path, String valueStr, LttngTimestamp t) {
		stateChange(treeIndex, path, new StateValue(valueStr), new TimeValue(t));
		return;
	}
	
	public void modifyAttribute(int treeIndex, String pathAsString, String valueStr, LttngTimestamp t) {
		stateChange(treeIndex, convertPathToVector(pathAsString), new StateValue(valueStr), new TimeValue(t));
		return;
	}
	
	/**
	 * Internal event-recording method, which will add a given state change to the
	 * database. The methods lower down the stack will take care of generating the intervals, etc.
	 */
	private void stateChange(int treeIndex, Vector<String> path, StateValue value, TimeValue t) {
		treeList.get(treeIndex).readStateChange(path, value, t);
		return;
	}
	
	/**
	 * Similar to the above methods, except we will also "nullify" all the sub-contents of
	 * the requested path.
	 */
	public void removeAttribute(int treeIndex, Vector<String> path, LttngTimestamp t) {
		treeList.get(treeIndex).removeAttribute(path, new TimeValue(t));
	}
	
	public void removeAttribute(int treeIndex, String pathAsString, LttngTimestamp t) {
		removeAttribute(treeIndex, convertPathToVector(pathAsString), t);
	}
	
	
	/**
	 * This method indicates that we finished reading a trace file, and we should now
	 * commit the contents of the StateHistoryTree and the CurrentStateTree to disk.
	 * 
	 * The tree will still be available for queries.
	 * 
	 * @param treeIndex Which tree in the treeList we want to close off
	 */
	public void closeTree(int treeIndex) {
		treeList.get(treeIndex).closeTree();
		return;
	}
	
	
	/**
	 * Query methods
	 */
	
	/**
	 * Load the state information at time t, for all the trees in the treeList.
	 * We will usually call this first, then run a series of queries with the other
	 * methods below.
	 * 
	 * @param t We will recreate the state information to what it was at time t.
	 */
	public void loadStateAtTime(LttngTimestamp t) {
		for ( int i = 0; i < treeList.size(); i++ ) {
			treeList.get(i).setStateAtTime( new TimeValue(t) );
		}
		return;
	}
	
	/**
	 * Once we have set up the "current state" using the above methods, we can now run
	 * queries to get individual attributes.
	 * This method returns a value that was given as type 'int' for the given pathName.
	 * 
	 * @param treeIndex In which tree in the list we want to run this query
	 * @param path The pathname (in the same format we inserted earlier) of the attribute we want
	 * @return The value that was associated to this pathname at the requested time
	 */
	public int getStateValueInt(int treeIndex, Vector<String> path) {
		StateValue value = treeList.get(treeIndex).getStateValue(path);
		assert ( value.getType() == 0 );
		return value.getValueInt();
	}
	
	public int getStateValueInt(int treeIndex, String pathAsString) {
		return getStateValueInt(treeIndex, convertPathToVector(pathAsString));
	}
	
	public String getStateValueStr(int treeIndex, Vector<String> path) {
		StateValue value = treeList.get(treeIndex).getStateValue(path);
		assert ( value.getType() == 1 );
		return value.getValueStr();
	}
	
	public String getStateValueStr(int treeIndex, String pathAsString) {
		return getStateValueStr(treeIndex, convertPathToVector(pathAsString));
	}
	
	
	/**
	 * In case we want to support supplying path as slash-delimited Strings,
	 * instead of Vectors of Strings, we can use this method to go from the former
	 * to the latter.
	 * 
	 * @param pathAsString The path as string. duh
	 * @return The same path, but with slashes stripped out and formatted as Vector<String>
	 */
	protected static Vector<String> convertPathToVector(String pathAsString) {
		String components[];
		Vector<String> path = new Vector<String>();
		
		/* Strip the leading or trailing slashes in the path, if any */
		if ( pathAsString.charAt(0) == '/' ) {
			pathAsString = pathAsString.substring(1);
		}
		if ( pathAsString.charAt(pathAsString.length()-1) == '/') {
			pathAsString = pathAsString.substring(0, pathAsString.length()-1);
		}
		
		components = pathAsString.split("/");
		for ( int i = 0; i < components.length; i++ ) {
			path.add(components[i]);
		}
		return path;
	}
}