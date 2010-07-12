/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.parsers.custom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;

public class CustomEvent extends TmfEvent {

    protected static final String TIMESTAMP_INPUT_FORMAT_KEY = "CE_TS_I_F";
    protected static final String NO_MESSAGE = "";
    public static final byte TIMESTAMP_SCALE = -3;
    
    protected CustomTraceDefinition fDefinition;
    protected Map<String, String> fData;
    private String[] fColumnData;

    public CustomEvent(CustomTraceDefinition definition, TmfEvent other) {
        super(other);
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    public CustomEvent(CustomTraceDefinition definition, TmfTimestamp timestamp, TmfEventSource source, TmfEventType type, TmfEventReference reference) {
        super(timestamp, source, type, reference);
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    public CustomEvent(CustomTraceDefinition definition, TmfTimestamp originalTS, TmfTimestamp effectiveTS, TmfEventSource source, TmfEventType type, TmfEventReference reference) {
        super(originalTS, effectiveTS, source, type, reference);
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }
    
    @Override
    public TmfTimestamp getTimestamp() {
        if (fData != null) processData();
        return super.getTimestamp();
    }

    @Override
    public TmfTimestamp getOriginalTimestamp() {
        if (fData != null) processData();
        return super.getOriginalTimestamp();
    }

    @Override
    public TmfEventContent getContent() {
        if (fData != null) processData();
        return super.getContent();
    }

    public String[] extractItemFields() {
        if (fData != null) processData();
        return fColumnData;
    }

    private void processData() {
        String timeStampString = fData.get(CustomTraceDefinition.TAG_TIMESTAMP);
        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
        Date date = null;
        if (timeStampInputFormat != null && timeStampString != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeStampInputFormat);
            try {
                date = dateFormat.parse(timeStampString);
                fOriginalTimestamp = fEffectiveTimestamp = new TmfTimestamp(date.getTime(), TIMESTAMP_SCALE);
            } catch (ParseException e) {
                fOriginalTimestamp = fEffectiveTimestamp = TmfTimestamp.Zero;
            }
        } else {
            fOriginalTimestamp = fEffectiveTimestamp = TmfTimestamp.Zero;
        }
        
        int i = 0;
        fColumnData = new String[fDefinition.outputs.size()];
        for (OutputColumn outputColumn : fDefinition.outputs) {
            String value = fData.get(outputColumn.name);
            if (outputColumn.name.equals(CustomTraceDefinition.TAG_TIMESTAMP) && date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(fDefinition.timeStampOutputFormat);
                fColumnData[i++] = dateFormat.format(date);
            } else {
                fColumnData[i++] = value;
            }
        }
        String message = fData.get(CustomTraceDefinition.TAG_MESSAGE);;
        setContent(new TmfEventContent(this, message));
        fData = null;
    }
}
