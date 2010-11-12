/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history;

import java.io.*;

import org.eclipse.linuxtools.lttng.event.LttngTimestamp;

/**
 * Parser for textdump files, whose are coming from LTTV's textDump module
 * (generated with "lttv -m textDump -t path/to/trace > file.txt")
 * 
 * It converts the events read in that file in "state changes" that will then be
 * passed to the StateHistoryInterface.
 * 
 * @author alexmont
 *
 */
class TextdumpParser {
	
	private RandomAccessFile textdumpFile;
	private StateHistoryInterface stateInterface;
	private int treeIndex;
	private QuarkTable<String> knownEventTypes;
	
	/**
	 * Constructor
	 * @param filename Name of the trace textdump file to read
	 * @param targetInterface SHInterface in which to send the state changes
	 */
	protected TextdumpParser(String filename, StateHistoryInterface targetInterface, int targetTreeIndex) {
		this.stateInterface = targetInterface;
		this.treeIndex = targetTreeIndex;
		this.knownEventTypes = new QuarkTable<String>();
		registerKnownEvents();
		
		try {
			this.textdumpFile = new RandomAccessFile(filename, "r");
			
			/* Skip the first 2 lines */
			textdumpFile.readLine();
			textdumpFile.readLine();
			
			/* Read the contents of the file */
			while ( this.readEventLine() ) {
				;
			}
			
			/* Tell the Interface we are now done reading the trace and want to save everything to disk */
			stateInterface.closeTree(treeIndex);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Reads the next line in the file
	 * @return false if we are at the end, true if not
	 * @throws IOException 
	 */
	private boolean readEventLine() throws IOException {
		String line = textdumpFile.readLine();
		String components[], subComponents[], subSubComponents[];
		
		/* Event information */
		String eventType;
		LttngTimestamp timestamp;
		int cpu, pid;
		String extraInfo;
		
		if ( line.equals("End trace set") ) {
			return false;
		}
		
		
		/* Lines in the file are in the following format:
		 * kernel.sched_schedule: 991.437156060 (/home/alexandre/bin/traces/trace1/kernel_1), 1659, 1659, compiz, , 1496, 0x0, SYSCALL { prev_pid = 0, next_pid = 1659, prev_state = 0 }
		 * channel.event type: timestamp (path/to/trace/channel_cpu#), pid, pid?, process name, blank?, number?, 0x0?, extra info?
		 */
		
		/* Get Event type */
		components = line.split(": ");
		eventType = components[0];
		
		/* Check if the event type is known, stop processing if it's not */
		if ( knownEventTypes.containsEntry(eventType) ) {
			
			/* Get the timestamp */
			components = components[1].split(", ");
			subComponents = components[0].split(" ");
			subSubComponents = subComponents[0].split("\\.");
			timestamp = new LttngTimestamp( Long.parseLong( subSubComponents[0] + subSubComponents[1] ) );
			
			/* Get CPU number */
			subComponents = subComponents[1].split("_");
			cpu = Integer.parseInt( subComponents[subComponents.length-1] );
			
			/* Get PID */
			pid = Integer.parseInt( components[1] );
			
			/* Extra-info string */
			extraInfo = components[7];
			
			/* Send the extracted information to the correct parsing method
			 * The methods to call just have to be in the same order as they were
			 * inserted in the knownEventTypes table.
			 */
			switch ( knownEventTypes.getMatchingInt(eventType) ) {
			case 0:
				schedulingEvent(timestamp, cpu, pid, extraInfo);
				break;
			}

		}
		
		
		return true;
	}
	
	/**
	 * Here we simply define which event types the parser will recognize as state changes.
	 * This allows us to skip parsing the lines we don't care about
	 * Every entry added here should refer to a method implemented below.
	 */
	private void registerKnownEvents() {
		knownEventTypes.addEntry("sched_schedule");
	}
	
	private void schedulingEvent(LttngTimestamp timestamp, int cpu, int new_pid, String extraInfo) {
		String components[], subComponents[];
		int old_pid;
		String prevState;
		
		/* get old_pid, the PID of the process that got scheduled out */
		components = extraInfo.split(", ");
		subComponents = components[0].split(" = ");
		old_pid = Integer.parseInt( subComponents[1] );
		
		/* get prev_state, the state the removed process is now in?? */
		subComponents = components[2].split(" = ");
		switch ( Integer.parseInt(subComponents[1]) ) {
		//FIXME what does prev_state mean anyway?
		case 0:
			prevState = "not_running";
			break;
		default:
			prevState = "unknown";
			break;
		}
		
		//DEBUG
		System.out.println("Process #" + new_pid + " now scheduled on CPU " + cpu);
		
		/* Update "what is being run on which CPU" */
		stateInterface.modifyAttribute(treeIndex, "System/CPUs/" + cpu + "/Scheduled_process", new_pid, timestamp);
		
		/* Update the status of the process that was scheduled and the one that was thrown out. */
		stateInterface.modifyAttribute(treeIndex, "System/Processes/" + new_pid + "Current_state", "running", timestamp);
		stateInterface.modifyAttribute(treeIndex, "System/Processes/" + old_pid + "Current_state", prevState, timestamp);
	}
	
	
}


