/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.trace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.parser.ITmfEventParser;

/**
 * <b><u>TmfEventParserStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventParserStub implements ITmfEventParser {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private final int NB_TYPES = 10;
    private final TmfEventType[] fTypes;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfEventParserStub() {
    	fTypes = new TmfEventType[NB_TYPES];
    	for (int i = 0; i < NB_TYPES; i++) {
    		Vector<String> format = new Vector<String>();
    		for (int j = 1; j <= i; j++) {
    			format.add(new String("Fmt-" + i + "-Fld-" + j));
    		}
    		String[] fields = new String[i];
    		fTypes[i] = new TmfEventType("Type-" + i, format.toArray(fields));
    	}
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    static final String typePrefix = "Type-";
    public TmfEvent parseNextEvent(ITmfTrace eventStream, TmfTraceContext context) throws IOException {

        if (! (eventStream instanceof TmfTraceStub)) {
            return null;
        }

       	// Highly inefficient...
       	RandomAccessFile stream = ((TmfTraceStub) eventStream).getStream();
       	String name = eventStream.getName();
       	name = name.substring(name.lastIndexOf('/') + 1);

        synchronized(stream) {
        	long location = 0;
        	if (context != null)
        		location = (Long) (context.getLocation());
        	stream.seek(location);

        	try {
        		long ts        = stream.readLong();
        		String source  = stream.readUTF();
        		String type    = stream.readUTF();
        		@SuppressWarnings("unused")
        		int reference  = stream.readInt();
        		int typeIndex  = Integer.parseInt(type.substring(typePrefix.length()));
        		String[] fields = new String[typeIndex];
        		for (int i = 0; i < typeIndex; i++) {
        			fields[i] = stream.readUTF();
        		}

        		String content = "[";
        		if (typeIndex > 0) {
        			content += fields[0];
        		}
        		for (int i = 1; i < typeIndex; i++) {
        			content += ", " + fields[i];
        		}
        		content += "]";

        		TmfEvent event = new TmfEvent(
        				new TmfTimestamp(ts, (byte) -3, 0),     // millisecs
        				new TmfEventSource(source),
        				fTypes[typeIndex],
        				new TmfEventReference(name));
				TmfEventContent cnt = new TmfEventContent(event, content);
				event.setContent(cnt);
				return event;
        	} catch (EOFException e) {
        	}
        }
        return null;
    }

}