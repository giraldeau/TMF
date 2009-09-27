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

package org.eclipse.linuxtools.tmf.ui.views;

import org.eclipse.linuxtools.tmf.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * <b><u>TmfViewer</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public abstract class TmfView extends ViewPart implements ITmfComponent {

	/**
	 * Constructor
	 */
	public TmfView() {
		TmfSignalManager.addListener(this);
	}

	/**
	 * Destructor
	 */
	@Override
	public void dispose() {
		TmfSignalManager.removeListener(this);
	}

	/**
	 * broadcastSignal
	 */
	public void broadcastSignal(TmfSignal signal) {
		TmfSignalManager.dispatchSignal(signal);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

}
