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

import java.util.regex.Matcher;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTxtTraceDefinition.InputData;
import org.eclipse.linuxtools.tmf.ui.parsers.custom.CustomTxtTraceDefinition.InputLine;

public class CustomTxtEvent extends CustomEvent {

    public CustomTxtEvent(CustomTxtTraceDefinition definition, TmfEvent other) {
        super(definition, other);
    }

    public CustomTxtEvent(CustomTxtTraceDefinition definition, TmfTimestamp timestamp, TmfEventSource source, TmfEventType type, TmfEventReference reference) {
        super(definition, timestamp, source, type, reference);
    }

    public CustomTxtEvent(CustomTxtTraceDefinition definition, TmfTimestamp originalTS, TmfTimestamp effectiveTS, TmfEventSource source, TmfEventType type, TmfEventReference reference) {
        super(definition, originalTS, effectiveTS, source, type, reference);
    }

    public void processGroups(InputLine input, Matcher matcher) {
        for (int i = 0; i < input.columns.size(); i++) {
            InputData column = input.columns.get(i);
            if (i < matcher.groupCount() && matcher.group(i + 1) != null) {
                String value = matcher.group(i + 1).trim();
                if (value.length() == 0) {
                    continue;
                }
                String name = column.name;
                if (column.action == CustomTxtTraceDefinition.ACTION_SET) {
                    fData.put(name, value);
                    if (name.equals(CustomTxtTraceDefinition.TAG_TIMESTAMP)) {
                        fData.put(TIMESTAMP_INPUT_FORMAT_KEY, column.format);
                    }
                } else if (column.action == CustomTxtTraceDefinition.ACTION_APPEND) {
                    String s = fData.get(name);
                    if (s != null) {
                        fData.put(name, s + value);
                    } else {
                        fData.put(name, value);
                    }
                    if (name.equals(CustomTxtTraceDefinition.TAG_TIMESTAMP)) {
                        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
                        if (timeStampInputFormat != null) {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, timeStampInputFormat + column.format);
                        } else {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, column.format);
                        }
                    }
                } else if (column.action == CustomTxtTraceDefinition.ACTION_APPEND_WITH_SEPARATOR) {
                    String s = fData.get(name);
                    if (s != null) {
                        fData.put(name, s + " | " + value);
                    } else {
                        fData.put(name, value);
                    }
                    if (name.equals(CustomTxtTraceDefinition.TAG_TIMESTAMP)) {
                        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
                        if (timeStampInputFormat != null) {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, timeStampInputFormat + " | " + column.format);
                        } else {
                            fData.put(TIMESTAMP_INPUT_FORMAT_KEY, column.format);
                        }
                    }
                }
            }
        }
    }

}
