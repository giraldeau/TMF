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
		assert ( this.isActive );
		StateHistoryTreeInterval newInterval;
		
		if ( index > ongoingStateInfo.size() ) {
			/* We are in the presence of a new "state info" this tree hasn't seen so far,
			 * so we need to add a new element to the 2 vectors. */
			
			/* If these are not true, this would mean we have missed a previous even the CST has read, which would be bad */
			assert( index == ongoingStateInfo.size() + 1 );
			assert( index == ongoingStateStartTimes.size() + 1);
			
			/* We shouldn't be inserting a "new" null value in any case */
			assert ( !value.isNull() );
			
			ongoingStateInfo.add(value);
			ongoingStateStartTimes.add(eventTime);
			
		} else {
			/* We are reading an entry we already have seen in the tree */
			if ( !ongoingStateInfo.get(index).isNull() ) {
				/* We are replacing a state that is currently already in the Builder Tree, which means
				 * we need to create an interval to be inserted in the State History.
				 */
				newInterval = new StateHistoryTreeInterval(
						ongoingStateStartTimes.get(index),		/* Start Time */
						eventTime,								/* End Time */
						index,									/* "key" */
						ongoingStateInfo.get(index) );			/* StateValue */

				stateHistTree.insertInterval(newInterval);
			}
			
			/* Finally, we write the new information in the Builder Tree */
			ongoingStateInfo.set(index, value);
			ongoingStateStartTimes.set(index, eventTime);
		}
		return;
		
	}
	
	/**
	 * Run a "get state at time" query on the Builder Tree.
	 * 
	 * If someday we get to multi-thread the state system, we need to make sure to
	 * do some correct locking here (we don't want the Builder Tree to get modified while we're
	 * querying it!)
	 * 
	 * @param stateInfo The stateInfo object in which we will put our relevant information
	 * @param t The requested timestamp
	 */
	protected void doQuery(Vector<StateValue> stateInfo, TimeValue t) {
		assert( this.isActive );
		assert( stateInfo.size() == ongoingStateInfo.size() );
		
		for ( int i=0; i < ongoingStateInfo.size(); i++ ) {
			/* NOTE: here, if we see "null" values, we will write them over in the stateInfo (uselessly)
			 * In cases where we have more non-null values than null ones, this should be faster than testing every single one of them
			 * TODO is this usually the case?
			 */
			
			/* If the information about 'i' at time 't' is in this tree, return it */
			if ( t.compareTo(ongoingStateStartTimes.get(i), false) >= 0 ) {
				stateInfo.set(i, ongoingStateInfo.get(i) );
			}
		}
		
	}
	
	/**
	 * The "singular query" method, which returns a StateValue at a given time without
	 * re-updating the whole current State.
	 * 
	 * If the information is currently in here, we simply return it. If not, we go look
	 * in the State History Tree
	 * 
	 * @param index The index in the ongoing... tables. = the integer representation of the attribute
	 * @param t The TimeValue of the query
	 * @return The StateValue associated to that attribute/timestamp
	 */
	protected StateValue doSingularQuery(int index, TimeValue t) {
		if ( this.isActive ) {
			if ( t.compareTo( ongoingStateStartTimes.get(index), false) > 0 ) {
				/* The information we want is currently located in this Builder Tree */
				return ongoingStateInfo.get(index);
			}
		} 
		/* else */
		/* We don't have the information at hand, we'll need to go look in the SHT */
		return stateHistTree.doSingularQuery(index, t);
	}
	
	/**
	 * Close off the Builder Tree, used for example when we are done reading a static trace file
	 * All the information currently contained in it will be converted to intervals and inserted
	 * in the State History.
	 * 
	 * @param t Timestamp to apply as the End Time of all intervals we'll create.
	 */
	protected void closeBuilderTree() {
		assert( this.isActive );
		StateHistoryTreeInterval newInterval;
		
		for ( int i=0; i < ongoingStateInfo.size(); i++ ) {
			if ( !ongoingStateInfo.get(i).isNull() ) {
				newInterval = new StateHistoryTreeInterval(
						ongoingStateStartTimes.get(i),		/* Start Time */
						getLatestEndTime(),					/* End Time */
						i,									/* "key" */
						ongoingStateInfo.get(i) );			/* StateValue */
				
				stateHistTree.insertInterval(newInterval);
			}
		}
		
		ongoingStateInfo.clear();
		ongoingStateStartTimes.clear();
		this.isActive = false;
		return;
	}
	
	/**
	 * Simply returns if this Builder Tree is currently being used or not
	 * @return
	 */
	protected boolean isActive() {
		return this.isActive;
	}
	
	/**
	 * Find the latest end time we current have in the Builder Tree
	 * This is used, among others, so we can close the tree without depending on a
	 * TimeValue passed on from higher-level methods (we should have this information anyway).
	 * 
	 * @return The TimeValue corresponding to the latest existing timestamp
	 */
	private TimeValue getLatestEndTime() {
		assert( this.isActive );
		assert ( ongoingStateStartTimes.size() >= 1 );	//this shouldn't get called on a tree with no information yet
		TimeValue latestTV = ongoingStateStartTimes.get(0);
		
		/* We simply look in ongoingStateStartTimes for the latest value we can find */
		for ( int i = 1; i < ongoingStateStartTimes.size(); i++ ) {
			if ( latestTV.compareTo(ongoingStateStartTimes.get(i), false) > 1 ) {
				latestTV = ongoingStateStartTimes.get(i);
			}
		}
		return latestTV;
	}
	
}