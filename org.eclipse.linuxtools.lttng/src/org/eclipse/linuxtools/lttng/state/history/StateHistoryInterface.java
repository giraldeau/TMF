/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.LinkedList;
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

	private LinkedList<CurrentStateTree> treeList;
	
	
	/**
	 * Constructor
	 */
	public StateHistoryInterface() {
		this.treeList = new LinkedList<CurrentStateTree>();
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
		treeList.add( new CurrentStateTree(fileName, (TimeValue) treeStart, blockSize, maxChildren, cacheSize) );
		return treeList.size()-1;
	}
	
	public int createNewStateHistoryFile(String fileName, LttngTimestamp treeStart) {
		treeList.add( new CurrentStateTree(fileName, (TimeValue) treeStart, 64*1024, 10, 100) );
		return treeList.size()-1;
	}
	
	
	
	public int loadExistingStateHistoryFile(String fileName) {
		//TODO NYI
		
		/* File was not found */
		return -1;
	}
	
	/**
	 * External wrappers to the real addStateChange method below.
	 * They will convert the value and timestamp to the internal class format.
	 * 
	 * @param treeIndex To which tree (in the treeList) we want to add this event. It was returned at the tree creation.
	 * @param pathName The 'path' to which we want to add the record (ex.: "hostname/processes/PID2001/execMode")
	 * @param valueInt (or valueStr) The value this entry needs to have (either String or int, ex.: "syscall" or '1234')
	 * @param t The timestamp associated with this state change
	 */
	public void addStateChange(int treeIndex, String pathName, int valueInt, LttngTimestamp t) {
		StateValue sv = new StateValue(valueInt);
		addStateChange(treeIndex, pathName, sv, (TimeValue) t);
		return;
	}
	
	public void addStateChange(int treeIndex, String pathName, String valueStr, LttngTimestamp t) {
		StateValue sv = new StateValue(valueStr);
		addStateChange(treeIndex, pathName, sv, (TimeValue) t);
		return;
	}
	
	
	
	/**
	 * Internal event-recording method, which will add a given state change to the
	 * database. The methods lower down the stack will take care of generating the intervals, etc.
	 */
	private void addStateChange(int treeIndex, String pathName, StateValue value, TimeValue t) {
		/* Strip the leading or trailing slashes in the path, if any */
		if ( pathName.charAt(0) == '/' ) {
			pathName = pathName.substring(1);
		}
		if ( pathName.charAt(pathName.length()-1) == '/') {
			pathName = pathName.substring(0, pathName.length());
		}
		
		treeList.get(treeIndex).readStateChange(pathName, value, t);
	}
	
	
	/**
	 * Query methods...
	 */
	
	/* Example method, for now, that will set the CurrentStateTree's stateInfo vector to the
	 * corresponding State at the requested time. */
	public void readStateAtTime(int treeIndex, LttngTimestamp t) {
		treeList.get(treeIndex).setStateAtTime( (TimeValue) t );
	}
}