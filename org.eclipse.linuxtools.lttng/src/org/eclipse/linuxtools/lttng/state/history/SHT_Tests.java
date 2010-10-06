/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.*;

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
		Timevalue startTime, endTime;
		line.trim();
		String[] components = line.split(" ");
		
		/* The C program generates the files with in seconds, with a . separator.
		 * We need to remove those '.' since we directly use the number of nanoseconds
		 * (oh god so slow...) */
		String[] startTimeComponents = components[0].split("\\.");
		startTime = new Timevalue( Long.parseLong( startTimeComponents[0] + startTimeComponents[1] ) );
		String[] endTimeComponents = components[1].split("\\.");
		endTime = new Timevalue( Long.parseLong( endTimeComponents[0] + endTimeComponents[1] ) );
		
		/* Using the SHTInterval constructor: (int key, char[] value, Timevalue intervalStart, Timevalue intervalEnd) */
		return new StateHistoryTreeInterval(	components[2].hashCode(),
												components[3].toCharArray(),
												startTime,
												endTime
											);
	}
	
	/**
	 * Insert intervals directly in a State History Tree
	 * @param n : which intervals dataset to load
	 */
	private static void SHT_insertionTest(int n) {
		String filePath = intervalsDataPath + "testin-" + n;
		String line;
		long testStartTime, testEndTime;
		StateHistoryTree tree = new StateHistoryTree(treefilePath, new Timevalue(0), 64*1024, 10, 100);
		
		System.out.println("Beginning reading the test intervals file.");
		testStartTime = System.nanoTime();
		
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
		
		testEndTime = System.nanoTime();
		
		System.out.println("Finished reading the intervals file.");
		tree.printFullTreeHierarchy();
		System.out.println( tree.toString() );
		System.out.println( "Time taken: " + (testEndTime - testStartTime)/1000000000 + " sec  (" +
							(testEndTime - testStartTime)/n + "nsec/interval)"
						  );
		
	}
	
	
	
	public static void main(String[] args) {
		SHT_insertionTest(10000000);
	}
}





















