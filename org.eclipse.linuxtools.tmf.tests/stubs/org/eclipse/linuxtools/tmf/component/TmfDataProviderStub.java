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

package org.eclipse.linuxtools.tmf.component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.event.TmfData;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfDataProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("nls")
public class TmfDataProviderStub extends TmfDataProvider<TmfData> {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-10K";

    private TmfTraceStub fTrace;

    public TmfDataProviderStub(String path) throws IOException {
    	super("TmfDataProviderStub", TmfData.class);
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
		try {
			File test = new File(FileLocator.toFileURL(location).toURI());
			fTrace = new TmfTraceStub(test.getPath(), true);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
    }

    public TmfDataProviderStub() throws IOException {
    	this(DIRECTORY + File.separator + TEST_STREAM);
    }
    
    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

	@Override
	public ITmfContext armRequest(ITmfDataRequest<TmfData> request) {
		if (request instanceof ITmfEventRequest<?>) {
			ITmfContext context = fTrace.seekEvent(((ITmfEventRequest<?>) request).getRange().getStartTime());
			return context;
		}
		return null;
	}

	@Override
	public TmfData getNext(ITmfContext context) {
		return fTrace.getNext(context);
	}

	@Override
	public boolean isCompleted(ITmfDataRequest<TmfData> request, TmfData data, int nbRead) {
		return false;
	}

}
