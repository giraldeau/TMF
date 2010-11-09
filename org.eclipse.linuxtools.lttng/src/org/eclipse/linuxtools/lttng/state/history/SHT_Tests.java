/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.*;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;

/**
 * This is a series of tests for the SHT and CST trees
 * 
 * @author alexmont
 *
 */
class SHT_Tests {
	
	/* The path where the dummy intervals data files are contained.
	 * They should be named "testin-n" where n = number of samples in it. */
	private static final String intervalsDataPath = "/home/alexandre/Documents/project/statetree/testdata/";
	
	private static final String treefilePath = "/home/alexandre/Documents/project/statehistory-eclipse/test-treefile";
	
	
	/**
	 * Function to read a line from a generated intervals test file, which is of the format:
	 * "timevalue_start timevalue_end string_key string_value"
	 * "123.456 789.101 thisisthekey thisisthevalue"
	 * @param line
	 */
	private static StateHistoryTreeInterval readIntervalFromFile(String line){
		TimeValue startTime, endTime;
		line.trim();
		String[] components = line.split(" ");
		
		/* The C program generates the files with in seconds, with a . separator.
		 * We need to remove those '.' since we directly use the number of nanoseconds
		 * (oh god so slow...) */
		String[] startTimeComponents = components[0].split("\\.");
		startTime = new TimeValue( Long.parseLong( startTimeComponents[0] + startTimeComponents[1] ) );
		String[] endTimeComponents = components[1].split("\\.");
		endTime = new TimeValue( Long.parseLong( endTimeComponents[0] + endTimeComponents[1] ) );
		
		/* Using the SHTInterval constructor: (Timevalue intervalStart, Timevalue intervalEnd, int key, String value ) */
		return new StateHistoryTreeInterval( startTime, endTime, components[2].hashCode(), components[3] );
	}
	
	/**
	 * Insert intervals directly in a State History Tree
	 * @param n : which intervals dataset to load
	 */
	@SuppressWarnings("unused")
	private static void SHT_insertionTest(int n) {
		String filePath = intervalsDataPath + "testin-" + n;
		String line;
		StateHistoryTree tree = new StateHistoryTree(treefilePath, new TimeValue(0), 64*1024, 10, 100);
		
		System.out.println("Beginning reading the test intervals file.");
		Timer.setStart();
		
		try {
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
			line = br.readLine();
			for ( int i=1; line != null; i++ ) {
				System.out.println("Reading interval #" + i + " of " + n);
				tree.insertInterval( readIntervalFromFile(line) );
				line = br.readLine();
			}
			
		} catch (IOException e) {
			System.out.println("There was a problem reading the file " + filePath + ". Make sure it exists.");
			e.printStackTrace();
		}
		
		Timer.setEnd();
		
		System.out.println("Finished reading the intervals file.");
		tree.printFullTreeHierarchy();
		System.out.println( tree.toString() );
		Timer.printTimeTakenInSecs();
		System.out.println( "(" + Timer.getTimeTakenInNanosecs() / n + "nsec/interval)");
		
	}
	
	@SuppressWarnings("unused")
	private static void currentStateTest(String workingDir) {
		StateHistoryInterface stateInterface = new StateHistoryInterface();
		int a = stateInterface.createNewStateHistoryFile(workingDir + "testfile", new LttngTimestamp(0));
		
		TextdumpParser parse = new TextdumpParser(workingDir + "dump.txt", stateInterface, a);
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


















