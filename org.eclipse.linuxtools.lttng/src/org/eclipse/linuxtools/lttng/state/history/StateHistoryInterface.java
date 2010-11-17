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

	private CurrentStateTree innerCST;
	private boolean treeLoaded;
	
	/**
	 * Constructor
	 */
	public StateHistoryInterface() {
		this.treeLoaded = false;
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
	 */
	public void createNewStateHistoryFile(String fileName, LttngTimestamp treeStart,
											int blockSize, int maxChildren, int cacheSize) {
		assert ( treeLoaded == false );
		innerCST = new CurrentStateTree(fileName, new TimeValue(treeStart), blockSize, maxChildren, cacheSize);
		treeLoaded = true;
		return;
	}
	
	public void createNewStateHistoryFile(String fileName, LttngTimestamp treeStart) {
		assert ( treeLoaded == false );
		innerCST =  new CurrentStateTree(fileName, new TimeValue(treeStart), 64*1024, 10, 100);
		treeLoaded = true;
		return;
	}
	
	
	/**
	 * Add a new StateTree to the list, by loading a file already existing on disk.
	 * All the relevant parameters will be read from that file.
	 * 
	 * @param fileName Path/name of the already-existing State History file
	 */
	public void loadExistingStateHistoryFile(String fileName) {
		assert ( treeLoaded == false );
		try {
			innerCST = new CurrentStateTree(fileName, 100);
			treeLoaded = true;
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	
	/**
	 * Methods to register modifications to the Current State
	 */
	
	/**
	 * External wrappers to the real "stateChange" method below.
	 * They will convert the path (if needed), value and timestamp to the internal class format.
	 * 
	 * @param attribute The 'path' to which we want to add the record (ex.: {hostname, processes, PID2001, execMode} )
	 * 					The single-string-with-slashes representation can also be used (it will be converted to a Vector)
	 * @param valueInt (or valueStr) The value this entry needs to have (either String or int, ex.: "syscall" or '1234')
	 * @param t The timestamp associated with this state change
	 */
	public void modifyAttribute(Vector<String> attribute, int valueInt, LttngTimestamp t) {
		stateChange(attribute, new StateValue(valueInt), new TimeValue(t));
		return;
	}
	
	public void modifyAttribute(Vector<String> attribute, String valueStr, LttngTimestamp t) {
		stateChange(attribute, new StateValue(valueStr), new TimeValue(t));
		return;
	}
	
	/**
	 * Internal event-recording method, which will add a given state change to the
	 * database. The methods lower down the stack will take care of generating the intervals, etc.
	 */
	private void stateChange(Vector<String> attribute, StateValue value, TimeValue t) {
		assert ( treeLoaded );
		innerCST.readStateChange(attribute, value, t);
		return;
	}
	
	/**
	 * Similar to the above methods, except we will also "nullify" all the sub-contents of
	 * the requested path.
	 */
	public void removeAttribute(Vector<String> attribute, LttngTimestamp t) {
		assert ( treeLoaded );
		innerCST.removeAttribute(attribute, new TimeValue(t));
	}
	
	/**
	 * This method indicates that we finished reading a trace file, and we should now
	 * commit the contents of the StateHistoryTree and the CurrentStateTree to disk.
	 * 
	 * The tree will still be available for queries.
	 */
	public void closeTree() {
		assert ( treeLoaded );
		innerCST.closeTree();
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
		assert ( treeLoaded );
		innerCST.setStateAtTime( new TimeValue(t) );
		return;
	}
	
	/**
	 * Once we have set up the "current state" using the above methods, we can now run
	 * queries to get individual attributes.
	 * This method returns a value that was given as type 'int' for the given pathName.
	 * 
	 * @param attribute The attribute we want
	 * @return The value that was associated to this pathname at the requested time
	 */
	public int getStateValueInt(Vector<String> attribute) {
		assert ( treeLoaded );
		return innerCST.getStateValue(attribute).getValueInt();
	}
	
	public String getStateValueStr(Vector<String> attribute) {
		assert ( treeLoaded );
		return innerCST.getStateValue(attribute).getValueStr();
	}
	
	/**
	 * Alternative, singular version of the "getStateValue" methods.
	 * 
	 * These do not update the whole stateInfo vector, like loadStateAtTimes does. They only search
	 * for one specific entry in the state history.
	 * 
	 * They should be used when you only want very few entries instead of the whole state (or many entries
	 * but all at different timestamps). If you to request many entries all at the same time, you should use
	 * the conventional loadStateAtTime() + getStateValue...()
	 * 
	 * @param attribute Which attribute we want to get the state of
	 * @param t The timestamp at which we want the state
	 * @return The integer State Value we previously inserted at this point/time.
	 */
	public int getSingleStateValueInt(Vector<String> attribute, LttngTimestamp t) {
		assert ( treeLoaded );
		return innerCST.getSingleStateValue( attribute, new TimeValue(t) ).getValueInt();
	}
	
	public String getSingleStateValueStr(Vector<String> attribute, LttngTimestamp t) {
		assert ( treeLoaded );
		return innerCST.getSingleStateValue( attribute, new TimeValue(t) ).getValueStr();
	}
	
	/**
	 * The "get next/previous state change" methods. Added by popular demand!
	 * 
	 * If you know what state a given attribute is in at a given moment, but you are interested in knowing
	 * until when (or since when) it is or has been in that state, use this.
	 * 
	 * This is actually a new feature of the State History, which wasn't really possible to do efficiently
	 * with the old method.
	 * 
	 * @param attribute The attribute we want to know the state change
	 * @param t *Any* timestamp we know the given attribute will be in the state we want to compare
	 * @return The timestamp at which the attribute will stop (or started) being in that state
	 */
	public LttngTimestamp getNextStateChange(Vector<String> attribute, LttngTimestamp t) {
		
	}
	
	public LttngTimestamp getPreviousStateChange(Vector<String> attribute, LttngTimestamp t) {
		
	}
	
	/**
	 * In case we want to support supplying path as slash-delimited Strings,
	 * instead of Vectors of Strings, we can use this method to go from the former
	 * to the latter.
	 * 
	 * @param slashSeparatedString The slash-separated string. duh
	 * @return A Vector<String> containing the partial strings, with the slashes stripped out
	 */
	protected static Vector<String> convertStringToVector(String slashSeparatedString) {
		String components[];
		Vector<String> componentVector = new Vector<String>();
		
		/* Strip the leading or trailing slashes in the path, if any */
		if ( slashSeparatedString.charAt(0) == '/' ) {
			slashSeparatedString = slashSeparatedString.substring(1);
		}
		if ( slashSeparatedString.charAt(slashSeparatedString.length()-1) == '/') {
			slashSeparatedString = slashSeparatedString.substring(0, slashSeparatedString.length()-1);
		}
		
		components = slashSeparatedString.split("/");
		for ( int i = 0; i < components.length; i++ ) {
			componentVector.add(components[i]);
		}
		return componentVector;
	}
}