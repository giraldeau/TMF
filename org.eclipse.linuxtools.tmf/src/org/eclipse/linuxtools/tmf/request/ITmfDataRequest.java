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

package org.eclipse.linuxtools.tmf.request;

import org.eclipse.linuxtools.tmf.event.TmfData;

/**
 * <b><u>ITmfDataRequest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfDataRequest<T extends TmfData> {

	// ------------------------------------------------------------------------
	// Constants
	// ------------------------------------------------------------------------

    public enum ExecutionType { BACKGROUND, FOREGROUND };
    
	// ------------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------------

    /**
     * @return request data type (T)
     */
    public Class<T> getDataType();

    /**
     * @return request ID
     */
    public int getRequestId();

    /**
     * @return request ID
     */
    public ExecutionType getExecType();

    /**
	 * @return the index of the first event requested
     */
    public int getIndex();

    /**
     * @return the number of requested events
     */
    public int getNbRequested();

    /**
     * @return the number of events read so far
     */
    public int getNbRead();

	// ------------------------------------------------------------------------
	// Request state
	// ------------------------------------------------------------------------

    public boolean isRunning();
    public boolean isCompleted();
    public boolean isFailed();
    public boolean isCancelled();

	// ------------------------------------------------------------------------
	// Data handling
	// ------------------------------------------------------------------------

    public void handleData(T data);

	// ------------------------------------------------------------------------
	// Request handling
	// ------------------------------------------------------------------------

    public void handleStarted();
    public void handleCompleted();
    public void handleSuccess();
    public void handleFailure();
    public void handleCancel();

    /**
     * To suspend the client thread until the request completes
     * (or is canceled).
     */
    public void waitForCompletion() throws InterruptedException;

	// ------------------------------------------------------------------------
	// Request state modifiers
	// ------------------------------------------------------------------------

    public void start();
    public void done();
    public void fail();
    public void cancel();
}
