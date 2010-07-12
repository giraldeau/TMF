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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;


public abstract class CustomTraceDefinition {

    public static final int ACTION_SET = 0;
    public static final int ACTION_APPEND = 1;
    public static final int ACTION_APPEND_WITH_SEPARATOR = 2;

    public static final String TAG_TIMESTAMP = "Time Stamp";
    public static final String TAG_MESSAGE = "Message";
    public static final String TAG_OTHER = "Other";
    
    public String definitionName;
    public List<OutputColumn> outputs;
    public String timeStampOutputFormat;
    
    public static class OutputColumn {
        public String name;

        public OutputColumn() {};

        public OutputColumn(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public String formatTimeStamp(TmfTimestamp timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeStampOutputFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(timestamp.getValue());
    }
    
    public abstract void save();
    public abstract void save(String path);
}
