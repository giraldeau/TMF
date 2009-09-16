/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model;

public abstract class TimeEvent implements ITimeEvent {
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tlf.widgets.timeAnalysis.model.TmTaEventI#getTrace()
	 */
	public abstract ITmfTimeAnalysisEntry getEntry();
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tlf.widgets.timeAnalysis.model.TmTaEventI#getTime()
	 */
	public abstract long getTime();
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tlf.widgets.timeAnalysis.model.TmTaEventI#getDuration()
	 */
	public long getDuration() {
		return -1;
	}
}
