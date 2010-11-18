/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

public class TmfTraceTest extends TmfEventRequest<LttngEvent> {
    
    @SuppressWarnings("unchecked")
	public TmfTraceTest(Class<? extends TmfEvent> dataType, TmfTimeRange range, int nbRequested) {
        super((Class<LttngEvent>)dataType, range, nbRequested, 1);
    }
    
    
    // Path of the trace
    public static final String TRACE_PATH = "/home/francis/workspace/bench/trace-bonnie";
    
    // *** Change this to run several time over the same trace
    public static final int NB_OF_PASS = 1;
    
    // *** Change this to true to parse all the events in the trace
    //	Otherwise, events are just read
    public final boolean PARSE_EVENTS = true;
    
    
    // Work variables
    public static int nbEvent = 0;
    public static int nbPassDone = 0;
    public static TmfExperiment<LttngEvent> fExperiment = null;
    public static long t1 = 0;
    public static long t2 = 0;
    
	public static void main(String[] args) throws InterruptedException {
		
		Thread.sleep(10000);
		
		try {
			// OUr experiment will contains ONE trace
        	ITmfTrace[] traces = new ITmfTrace[1];
    		traces[0] = new LTTngTrace(TRACE_PATH);
        	// Create our new experiment
            fExperiment = new TmfExperiment<LttngEvent>(LttngEvent.class, "Headless", traces);
            
            
            // Create a new time range from -infinity to +infinity
            //	That way, we will get "everything" in the trace
            LttngTimestamp ts1 = new LttngTimestamp(Long.MIN_VALUE);
            LttngTimestamp ts2 = new LttngTimestamp(Long.MAX_VALUE);
            TmfTimeRange tmpRange = new TmfTimeRange(ts1, ts2);
            
            
            // We will issue a request for each "pass".
            // TMF will then process them synchonously
            TmfTraceTest request = null;
            t1 = System.nanoTime();
            for ( int x=0; x<NB_OF_PASS; x++ ) {
                request = new TmfTraceTest(LttngEvent.class, tmpRange, Integer.MAX_VALUE );
        		fExperiment.sendRequest(request);
        		nbPassDone++;
            }
        }
		catch (NullPointerException e) {
			// Silently dismiss Null pointer exception
			// The only way to "finish" the threads in TMF is by crashing them with null
		}
		catch (Exception e) {
            e.printStackTrace();
        }

	}

	@Override
    public void handleData(LttngEvent event) {
		super.handleData(event);
        if ( (event != null) && (PARSE_EVENTS) ) {
            LttngEventField[] fields = ((LttngEvent) event).getContent().getFields();
            
            // *** Uncomment the following to print the parsed content
            // Warning : this is VERY intensive
			//
            //System.out.println((LttngEvent)evt[0]);
            //System.out.println(((LttngEvent)evt[0]).getContent());
            
            nbEvent++;
        }
    }
	
    @Override
    public void handleCompleted() {
    	t2 = System.nanoTime();
            if ( nbPassDone >= NB_OF_PASS ) {
                try {
                	float enlaps = (t2 - t1) / 1000000000;
                	float throughtput = nbEvent / enlaps;
                	System.out.println("Nb events   : " + nbEvent);
                	System.out.println("Enlaps      : " + enlaps);
                	System.out.println("Throughtput : " + throughtput);
                    fExperiment.sendRequest(null);
               }
               catch (Exception e) {}
            }
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

}
