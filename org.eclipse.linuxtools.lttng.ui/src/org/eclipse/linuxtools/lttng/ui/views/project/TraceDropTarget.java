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

package org.eclipse.linuxtools.lttng.ui.views.project;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DropTargetAdapter;

/**
 * <b><u>TraceDropTarget</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TraceDropTarget extends DropTargetAdapter {

	@SuppressWarnings("unused")
	private final TreeViewer fViewer;

	/**
	 * @param viewer
	 */
	public TraceDropTarget(TreeViewer viewer) {
		fViewer = viewer;
	}

}
