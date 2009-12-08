/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.state;

import java.util.Set;

import org.eclipse.linuxtools.lttng.ActivateDebugging;
import org.eclipse.linuxtools.lttng.TraceDebug;
import org.eclipse.linuxtools.lttng.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.trace.TmfExperiment;
import junit.framework.TestCase;

/**
 * @author alvaro
 * 
 */
public class TestStateManager extends TestCase {
	
	public void testSetTraceSelection() {
		String logName = "traceset/trace1";
		
		LTTngTrace testStream = null;
		try {
			testStream = new LTTngTrace(logName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (testStream != null) {
		    LTTngTrace[] streamList = new LTTngTrace[1];
			streamList[0] = testStream;
			TmfExperiment newExp = new TmfExperiment(logName, streamList);
			
			//Get the Test StateManager
			StateManager manager = StateManagerFactoryTestSupport.getManager(newExp.getExperimentId());
			//Start execution.
			manager.setTraceSelection(newExp, false);
			
			//Print events not handled.
			Set<String> notHandledEvents = manager.getEventsNotHandled();
			StringBuilder sb = new StringBuilder();
			for (String event : notHandledEvents) {
				sb.append("\n" + event);
			}
			TraceDebug.debug("Events not Handled: " + sb.toString());
		}
	}
}
