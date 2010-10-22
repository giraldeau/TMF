/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.Vector;

/**
 * This is the BuilderTree, which mainly contains a "state info" vector similar
 * to the one in the Current State Tree, except here we also record the
 * start time of every state stored in it.
 * 
 * We can then build StateHistoryIntervals to be inserted in the State History Tree,
 * when we detect state changes : the "start time" of the interval will be the recorded time
 * we have here, and the "end time" will be the timestamp of the new state-changing even we
 * just read.
 * 
 * @author alexmont
 *
 */
class BuilderTree {
	
	private boolean isActive;
	
	/* Reference to which SHT this builder tree, well, builds */
	private StateHistoryTree stateHistTree;
	
	private Vector<StateValue> ongoingStateInfo;
	private Vector<TimeValue> ongoingStateStartTimes;
	
	
	protected BuilderTree(StateHistoryTree SHTree) {
		this.isActive = true;
		this.stateHistTree = SHTree;
		this.ongoingStateInfo = new Vector<StateValue>();
		this.ongoingStateStartTimes = new Vector<TimeValue>();
	
	}
	

 	/**
	 * This is the lower-level method that will be called by the StateHistoryInterface
	 * (with already-built StateValues and TimeValues)
	 * 
	 * @param index : The index in the vectors (which is the integer representation of the path in the CST)
	 * @param value : The StateValue associated to this path in the table (ex.: "syscall" or a numerical value)
	 * @param eventTime : The timestamp associated with this state change
	 */
	protected void processStateChange(int index, StateValue value, TimeValue eventTime) {
		StateHistoryTreeInterval newInterval;
		
		if ( index > ongoingStateInfo.size() ) {
			/* We are in the presence of a new "state info" this tree hasn't seen so far,
			 * so we need to add a new element to the 2 vectors.
			 */
			assert( index == ongoingStateInfo.size() + 1 );	/* If not, this would mean we have missed a previous even the CST has read, which would be bad */
			assert( index == ongoingStateStartTimes.size() + 1);
			
			ongoingStateInfo.add(value);
			ongoingStateStartTimes.add(eventTime);
			
		} else {
			/* We are reading an entry we already have in the tree, which means we're modifying it.
			 * We need to generate a SHTInterval from that information and insert it in the History tree.
			 */

			newInterval = new StateHistoryTreeInterval(
									ongoingStateStartTimes.get(index),		/* Start Time */
									eventTime,								/* End Time */
									index,									/* "key" */
									ongoingStateInfo.get(index) );			/* StateValue */
			
			stateHistTree.insertInterval(newInterval);
			
			/* Replace this spot in the Builder Tree with the new information from the event */
			ongoingStateInfo.set(index, value);
			ongoingStateStartTimes.set(index, eventTime);
		}
		
	}
	
	/**
	 * Run a "get state at time" query on the Builder Tree
	 * If someday we get to multi-thread the state system, we need to make sure to
	 * do some correct locking here (we don't want the Builder Tree to get modified while we're
	 * querying it)
	 * 
	 * @param stateInfo The stateInfo object in which we will put our relevant information
	 * @param t The requested timestamp
	 */
	protected void doQuery(Vector<StateValue> stateInfo, TimeValue t) {
		
		assert( this.isActive );
		assert( stateInfo.size() == ongoingStateInfo.size() );
		
		for ( int i=0; i < ongoingStateInfo.size(); i++ ) {
			/* If the information about 'i' at time 't' is in this tree, return it */
			if ( t.compareTo(ongoingStateStartTimes.get(i), false) >= 0 ) {
				stateInfo.set(i, ongoingStateInfo.get(i) );
			}
		}
		
	}
	
	/**
	 * Close off the Builder Tree, used for example when we are done reading a static trace file
	 * All the information currently contained in it will be converted to intervals and inserted
	 * in the State History.
	 * 
	 * @param t Timestamp to apply as the End Time of all intervals we'll create.
	 */
	protected void closeBuilderTree(TimeValue t) {
		StateHistoryTreeInterval newInterval;
		
		for ( int i=0; i < ongoingStateInfo.size(); i++ ) {
			
			newInterval = new StateHistoryTreeInterval(
					ongoingStateStartTimes.get(i),		/* Start Time */
					t,									/* End Time */
					i,									/* "key" */
					ongoingStateInfo.get(i) );			/* StateValue */
			
			stateHistTree.insertInterval(newInterval);
		}
		
		ongoingStateInfo.clear();
		ongoingStateStartTimes.clear();
		this.isActive = false;
	}
	
	/**
	 * Simply returns if this Builder Tree is currently being used or not
	 * @return
	 */
	protected boolean isActive() {
		return this.isActive;
	}
	
}