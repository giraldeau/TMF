/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.tests.trace;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.trace.LTTngExperiment;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;

/**
 * <b><u>TmfExperimentTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class LTTngExperimentTest extends TestCase {

    private static final String DIRECTORY   = "traceset";
    private static final String TEST_STREAM = "trace-15316events_nolost_newformat";
    private static final String EXPERIMENT  = "MyExperiment";
    private static int          NB_EVENTS   = 15316;

    // Note: Start/end times are for the LTTng *trace*, not the actual events
    private static final TmfTimestamp  fStartTime = new TmfTimestamp(13589759412128L, (byte) -9);
    private static final TmfTimestamp  fEndTime   = new TmfTimestamp(13589907059242L, (byte) -9);

    private static ITmfTrace[] fTraces;
    private static LTTngExperiment<TmfEvent> fExperiment;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    private synchronized static ITmfTrace[] setupTrace(String path) {
    	if (fTraces == null) {
    		fTraces = new ITmfTrace[1];
    		try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(path), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTrace trace = new LTTngTrace(testfile.getPath());
    			fTraces[0] = trace;
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return fTraces;
    }

    private synchronized static void setupExperiment() {
    	if (fExperiment == null) {
    		fExperiment = new LTTngExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, fTraces, TmfTimestamp.Zero, 1000, true);
    	}
    }

	public LTTngExperimentTest(String name) throws Exception {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setupTrace(DIRECTORY + File.separator + TEST_STREAM);
		setupExperiment();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

	public void testBasicTmfExperimentConstructor() {

		assertEquals("GetId", EXPERIMENT, fExperiment.getName());
        assertEquals("GetEpoch", TmfTimestamp.Zero, fExperiment.getEpoch());
        assertEquals("GetNbEvents", NB_EVENTS, fExperiment.getNbEvents());

        long nbTraceEvents = fExperiment.getTraces()[0].getNbEvents();
        assertEquals("GetNbEvents", NB_EVENTS, nbTraceEvents);

        TmfTimeRange timeRange = fExperiment.getTimeRange();
        assertTrue("getStartTime", fStartTime.equals(timeRange.getStartTime()));
        assertTrue("getEndTime",   fEndTime.equals(timeRange.getEndTime()));
	}

}