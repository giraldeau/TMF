/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history.tests;

import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.state.history.StateHistoryInterface;

/**
 * This is a series of tests for the SHT and CST trees
 * 
 * @author alexmont
 *
 */
class SHT_Tests {

	
	@SuppressWarnings("unused")
	private static void currentStateTest(String workingDir) {
		StateHistoryInterface stateInterface = new StateHistoryInterface();
		stateInterface.createNewStateHistoryFile(workingDir + "testfile", new LttngTimestamp(0));
		
		TextdumpParser parse = new TextdumpParser(workingDir + "dump.txt", stateInterface);
	}
	
	public static void main(String[] args) {
		//SHT_insertionTest(10000000);
		currentStateTest("/home/alexandre/bin/traces/");
	}
}



abstract class Timer {
	private static long timerStart, timerEnd;
	
	public static void setStart() {
		timerStart = System.nanoTime();
	}
	
	public static void setEnd() {
		timerEnd = System.nanoTime();
	}
	
	public static long getTimeTakenInNanosecs() {
		return timerEnd - timerStart;
	}
	
	public static void printTimeTakenInSecs() {
		System.out.println( "Time taken: " + ( getTimeTakenInNanosecs() )/1000000000 + " sec.");
	}
}


















