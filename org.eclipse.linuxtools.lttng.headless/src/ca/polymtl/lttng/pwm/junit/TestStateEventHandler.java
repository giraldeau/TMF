package ca.polymtl.lttng.pwm.junit;

import static org.junit.Assert.*;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.polymtl.lttng.pwm.StateEventHandler;


public class TestStateEventHandler {

	private static String traceset_dir;
	private static TmfExperiment<LttngEvent> fExperiment;
	private static TmfTimeRange tmpRange; 
	
	@BeforeClass
	static public void setUp() throws Exception {
		traceset_dir = System.getenv("TRACESET_DIR");
		if (traceset_dir == null) {
			throw new Exception("TRACESET_DIR env variable is not set");
		}
        // Create a new time range from -infinity to +infinity
        //	That way, we will get "everything" in the trace
        LttngTimestamp ts1 = new LttngTimestamp(Long.MIN_VALUE);
        LttngTimestamp ts2 = new LttngTimestamp(Long.MAX_VALUE);
        tmpRange = new TmfTimeRange(ts1, ts2);
	}

	public void setUpExperiment(String name) throws Exception {
    	ITmfTrace[] traces = new ITmfTrace[1];
		traces[0] = new LTTngTrace(traceset_dir + name);
    	// Create our new experiment
        fExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class, "Headless", traces);
   
	}
	
	@Test
	public void testSomething() throws Exception {

		setUpExperiment("trace-sleep1sec");
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