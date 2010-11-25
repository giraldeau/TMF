package org.eclipse.linuxtools.lttng.state.history;

import java.util.*;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.state.StateStrings;
import org.eclipse.linuxtools.lttng.state.history.helpers.VectorConvert;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;

public class StateEventHandler extends TmfEventRequest<LttngEvent> {

	private int nbEvents;
	private boolean done;
	private ArrayList<Integer> nbEventsPerCPU;
	private HashMap<String, Integer> markerMap;
	private StateStrings knownStates;
	private VectorConvert<String> convert;
    private StateHistoryInterface stateHistory;
	
	public StateEventHandler() {
		this(null, null, 0);
	}
	
    @SuppressWarnings("unchecked")
	public StateEventHandler(Class<? extends TmfEvent> dataType, TmfTimeRange range,
							int nbRequested) {
        super((Class<LttngEvent>)dataType, range, nbRequested, 1);
        nbEvents = 0;
        done = false;
        nbEventsPerCPU = new ArrayList<Integer>();
        markerMap = new HashMap<String, Integer>();
        knownStates = StateStrings.getInstance();
        convert = new VectorConvert<String>();
        
        stateHistory = new StateHistoryInterface();
        stateHistory.createNewStateHistoryFile( "/home/alexandre/tmp/bidon", new LttngTimestamp( range.getStartTime() ) );
    }
    
	@Override
	public void handleData(LttngEvent event) {
		super.handleData(event); /* Simply checks that event != null */
		
		int i, m, cpu;
		
		/* Uncomment the following to print the parsed content
		 * Warning : this is VERY intensive */
		//System.out.println(event);
		//System.out.println(event.getContent());
		
		/* Statistics: Events per CPU */
		cpu =  (int)event.getCpuId();
		while ( cpu >= nbEventsPerCPU.size() ) {
			nbEventsPerCPU.add(0);
		}
		i = nbEventsPerCPU.get(cpu) + 1;
		nbEventsPerCPU.set(cpu, i);
		nbEvents++;

		/* Statistics: Nb. of events per marker name */
		if ( !markerMap.containsKey(event.getMarkerName()) ) {
			markerMap.put(event.getMarkerName(), 0);
		}
		m = markerMap.get(event.getMarkerName()) + 1;
		markerMap.put(event.getMarkerName(), m);
		
		
		/* Feed event to the history system if it's known to cause a state transition */
		if ( knownStates.getStateTransEventMap().containsKey(event.getMarkerName()) ) {
			switch ( knownStates.getStateTransEventMap().get(event.getMarkerName()) ) {
			
			case LTT_EVENT_SYSCALL_ENTRY:
				//
				break;
				
			case LTT_EVENT_SYSCALL_EXIT:
				//
				break;
			
			case LTT_EVENT_TRAP_ENTRY:
				//
				break;
			
			case LTT_EVENT_TRAP_EXIT:
				//
				break;
			
			case LTT_EVENT_PAGE_FAULT_ENTRY:
				//
				break;
			
			case LTT_EVENT_PAGE_FAULT_EXIT:
				//
				break;
			
			case LTT_EVENT_PAGE_FAULT_NOSEM_ENTRY:
				//
				break;
			
			case LTT_EVENT_PAGE_FAULT_NOSEM_EXIT:
				//
				break;
			
			case LTT_EVENT_IRQ_ENTRY:
				//
				break;
			
			case LTT_EVENT_IRQ_EXIT:
				//
				break;
			
			case LTT_EVENT_SOFT_IRQ_RAISE:
				//
				break;
			
			case LTT_EVENT_SOFT_IRQ_ENTRY:
				//
				break;
				
			case LTT_EVENT_SOFT_IRQ_EXIT:
				//
				break;
			
			case LTT_EVENT_SCHED_SCHEDULE:
				/* prev_pid:5468,next_pid:111,prev_state:0 */
				/* Yes, 0 is next_pid and 1 is prev_pid , go figure */
				String next_pid = (String) event.getContent().getField(0).getValue();
				String prev_pid = (String) event.getContent().getField(1).getValue();
				Integer prev_state = (Integer) event.getContent().getField(2).getValue();
				
				/* Set the status of the new scheduled process */
				stateHistory.modifyAttribute(convert.parse("Hostname", "Processes", next_pid, "Status"),
													StateStrings.ProcessStatus.LTTV_STATE_RUN.ordinal(),
													event.getTimestamp());
				
				/* Set the status of the process that got scheduled out */
				stateHistory.modifyAttribute(convert.parse("Hostname", "Processes", prev_pid, "Status"),
													prev_state,
													event.getTimestamp());
				break;
			
			case LTT_EVENT_PROCESS_FORK:
				//
				break;
			
			case LTT_EVENT_KTHREAD_CREATE:
				//
				break;
			
			case LTT_EVENT_PROCESS_EXIT:
				//
				break;
			
			case LTT_EVENT_PROCESS_FREE:
				//
				break;
			
			case LTT_EVENT_EXEC:
				//
				break;
			
			case LTT_EVENT_THREAD_BRAND:
				//
				break;
			
			case LTT_EVENT_PROCESS_STATE:
				//
				break;
			
			case LTT_EVENT_STATEDUMP_END:
				//
				break;
			
			case LTT_EVENT_LIST_INTERRUPT:
				//
				break;
			
			case LTT_EVENT_REQUEST_ISSUE:
				//
				break;
			
			case LTT_EVENT_REQUEST_COMPLETE:
				//
				break;
			
			case LTT_EVENT_FUNCTION_ENTRY:
				//
				break;
			
			case LTT_EVENT_FUNCTION_EXIT:
				//
				break;
			
			case LTT_EVENT_SYS_CALL_TABLE:
				//
				break;
			
			case LTT_EVENT_SOFTIRQ_VEC:
				//
				break;
			
			case LTT_EVENT_KPROBE_TABLE:
				//
				break;
				
			default:
				/* Unknown event type, no state change */
				break;
			}
		}
    }
	
    @SuppressWarnings("unchecked")
	@Override
	public void handleCompleted() {
    	markerMap = (HashMap<String, Integer>) sortByValue(markerMap);
    	done = true;
	}

	@Override
	public void handleSuccess() {
	}

	@Override
	public void handleFailure() {
	}

	@Override
	public void handleCancel() {
	}
	
	/**
	 * Helper function to sort a Map
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map sortByValue(Map map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
		     public int compare(Object o2, Object o1) {
		          return ((Comparable) ((Map.Entry) (o1)).getValue())
		         .compareTo(((Map.Entry) (o2)).getValue());
		     }
		});
		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry)it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	
	/**
	 * Accessors
	 */
	
	public HashMap<String, Integer> getMarkerMap() {
		return markerMap;
	}
	
	public void setMarkerMap(HashMap<String, Integer> markerMap) {
		this.markerMap = markerMap;
	}
	
	public ArrayList<Integer> getNbEventPerCPU() {
		return nbEventsPerCPU;
	}
	
	public void setNbEventsPerCPU(ArrayList<Integer> nbEventsPerCPU) {
		this.nbEventsPerCPU = nbEventsPerCPU;
	}
	
	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public int getNbEvent() {
		return nbEvents;
	}

	public void setNbEvent(int nbEvent) {
		this.nbEvents = nbEvent;
	}
    
}
