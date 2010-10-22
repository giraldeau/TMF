/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.LinkedList;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

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
	
	
	public void createNewStateHistoryFile(String fileName) {
		
	}
	
	public void createNewStateHistoryFile(String fileName, TmfTimestamp treeStart,
									int blockSize, int maxChildren, int cacheSize) {
		
	}
	
	public int loadExistingStateHistoryFile(String fileName) {
		//TODO NYI
		
		/* File was not found */
		return 0;
	}
}