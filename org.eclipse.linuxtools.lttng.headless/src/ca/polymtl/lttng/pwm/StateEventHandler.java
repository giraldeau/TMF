package ca.polymtl.lttng.pwm;

import java.util.*;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.state.model.LttngTraceState;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;

public class StateEventHandler extends TmfEventRequest<LttngEvent> {

	private int nbEvent;
	private boolean done;
	private ArrayList<Integer> nbEventsPerCPU;
	private HashMap<String, Integer> markerMap;
	
	public StateEventHandler() {
		this(null, null, 0);
	}
	
    @SuppressWarnings("unchecked")
	public StateEventHandler(Class<? extends TmfEvent> dataType, TmfTimeRange range,
							int nbRequested) {
        super((Class<LttngEvent>)dataType, range, nbRequested, 1);
        nbEvent = 0;
        done = false;
        nbEventsPerCPU = new ArrayList<Integer>();
        markerMap = new HashMap<String, Integer>();
    }
    
	@Override
    public void handleData(LttngEvent event) {
		super.handleData(event);
        if ( (event != null)) {
            ((LttngEvent) event).getContent().getFields();
            
            // *** Uncomment the following to print the parsed content
            // Warning : this is VERY intensive
			//
            //System.out.println((LttngEvent)evt[0]);
            //System.out.println(((LttngEvent)evt[0]).getContent());
            int cpu =  (int)event.getCpuId();
            while ( cpu >= nbEventsPerCPU.size() ) {
            	nbEventsPerCPU.add(0);
            }
            Integer i = nbEventsPerCPU.get(cpu) + 1;
            nbEventsPerCPU.set(cpu, i);
            nbEvent++;
            
            /* Add the marker name to the map if needed */
            if ( !markerMap.containsKey(event.getMarkerName()) ) {
            	markerMap.put(event.getMarkerName(), 0);
            }
            Integer m = markerMap.get(event.getMarkerName()) + 1;
            markerMap.put(event.getMarkerName(), m);
            /*
            if (event.getMarkerName().compareTo("process_state")==0) {
            	System.out.println(event.getContent());
            }*/
            
            /* Feed event to history system */
            if (event.getMarkerName().compareTo("syscall_entry")==0) {
            	//System.out.println(event.getContent());
            } else if (event.getMarkerName().compareTo("syscall_exit")==0) {
            	//System.out.println(event.getContent());
            }
        }
    }
	
    @Override
    public void handleCompleted() {
    	markerMap = (HashMap<String, Integer>) sortByValue(markerMap);
    	done=true;
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
    
    @SuppressWarnings("rawtypes")
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
		return nbEvent;
	}

	public void setNbEvent(int nbEvent) {
		this.nbEvent = nbEvent;
	}
    
}