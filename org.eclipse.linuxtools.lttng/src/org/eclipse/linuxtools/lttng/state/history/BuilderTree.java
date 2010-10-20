/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * This is the BuilderTree, which is basically a CurrentStateTree, except it also
 * records the "start time" of every state stored in currentStateInfo.
 * It is used when building a State History Tree (hence its name), since we can
 * then easily create Intervals to be inserted in the SHTree.
 * 
 * @author alexmont
 *
 */
class BuilderTree extends CurrentStateTree {
	
	private Vector<TimeValue> currentStateStartTimes;
	
	
	public BuilderTree() {
		super();
		currentStateStartTimes = new Vector<TimeValue>();
	}
	
	/**
	 * These methods will be called by TMF, when we hit an
	 * event causing a state change that we want to record.
	 */
	public void addEvent(String path, int valueAsInt, TmfTimestamp eventTime) {
		StateValue value = new StateValue(valueAsInt);
		addEvent(path, value, (TimeValue) eventTime);
	}
	
	public void addEvent(String path, String valueAsString, TmfTimestamp eventTime) {
		StateValue value = new StateValue(valueAsString);
		addEvent(path, value, (TimeValue) eventTime);
	}

 	/**
	 * This is the lower-level method that will be called by the wrappers above.
	 * 
	 * @param path : The "path" in the system-state tree used in the TMF state system (ex.: "System/Processes/PID1001/execMode")
	 * @param value : The value of the this path (ex.: "syscall")
	 * @param eventTime : The timestamp associated with this state change
	 */
	private void addEvent(String path, StateValue value, TimeValue eventTime) {
		
		if ( conversionTable.containsKey(path) ) {
			/* 
			 * The given element already exist in the current state, that means it's being replaced.
			 * We will create a new SHTInterval object with the values we now have, and insert it in the SHTree.
			 */
			int index = conversionTable.get(path);
			StateValue oldValue = currentStateInfo.get(index);
			TimeValue oldEventStartTime = currentStateStartTimes.get(index);
			StateHistoryTreeInterval newInterval = new StateHistoryTreeInterval(oldEventStartTime, eventTime, index, oldValue);
			
			stateHistTree.insertInterval(newInterval);
			
			/* Replace the spot in the Current State Tree with the new information from the event */
			currentStateInfo.set(index, value);
			currentStateStartTimes.set(index, eventTime);
			
		} else {
			/* We are simply adding a new element that wasn't there before */
			currentStateInfo.add(value);
			currentStateStartTimes.add(eventTime);
			conversionTable.put(path, currentStateInfo.size());
		}
	}
	
	@Override
	public void getStateAtTime(TimeValue t) {
		/* No, no, no, this should only be called from a CST, not a builder tree! */
		assert( false );
	}
}