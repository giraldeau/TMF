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

package org.eclipse.linuxtools.lttng.ui.views.events;

import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.views.TmfEventsView;
import org.eclipse.swt.widgets.Composite;

/**
 * <b><u>EventsView</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class EventsView extends TmfEventsView {

    public static final String ID = "org.eclipse.linuxtools.lttng.ui.views.events";

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public EventsView() {
    	super(1);
    }

    @Override
    protected TmfEventsTable createEventsTable(Composite parent, int cacheSize) {
        return new EventsTable(parent, cacheSize);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
    	return "[EventsView]";
    }


}
