package org.eclipse.linuxtools.lttng.state.history.tests;

import static org.junit.Assert.*;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.state.history.StateEventHandler;
import org.eclipse.linuxtools.lttng.state.history.StateHistoryInterface;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.junit.Test;

public class TestAPI {
	
	static String TRACE_PATH = "/home/alexandre/tmp/traces/trace-cmd";
    public static TmfExperiment<LttngEvent> fExperiment = null;

	@Test
	public void testSomething() throws Exception {
		
    	ITmfTrace[] traces = new ITmfTrace[1];
		traces[0] = new LTTngTrace(TRACE_PATH);
    	// Create our new experiment
        fExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class, "Headless", traces);
        
        
        // Create a new time range from -infinity to +infinity
        //	That way, we will get "everything" in the trace
        LttngTimestamp ts1 = new LttngTimestamp(Long.MIN_VALUE);
        LttngTimestamp ts2 = new LttngTimestamp(Long.MAX_VALUE);
        TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);

		StateEventHandler handler = new StateEventHandler(LttngEvent.class, tmpRange, Integer.MAX_VALUE);
		fExperiment.sendRequest(handler);
		// FIXME: this is ugly hack because otherwise the sendRequest doesn't block
		while(!handler.isDone()) {
			Thread.sleep(100);
		}
		
		
    	System.out.println("Nb events : " + handler.getNbEvent());
    	System.out.println("Events per CPU" + handler.getNbEventPerCPU());
    	System.out.println("Marker founds: ");
    	for (String s: handler.getMarkerMap().keySet()) {
    		String spc = new String();
    		for(int i=0; i<40 - s.length();i++) {
    			spc += " ";
    		}
			System.out.println(s + spc + handler.getMarkerMap().get(s));
    	}
		
		System.out.println("test");
		assertTrue(true);
	}
}
