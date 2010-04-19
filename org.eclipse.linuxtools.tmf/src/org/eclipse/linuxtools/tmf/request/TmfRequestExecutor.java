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

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <b><u>TmfRequestExecutor</u></b>
 *
 * A simple, straightforward request executor.
 */
public class TmfRequestExecutor implements Executor {

	private final ExecutorService fExecutor;
	private final Queue<Runnable> fRequests = new LinkedBlockingQueue<Runnable>();
	private Runnable fRequest;
	
	public TmfRequestExecutor(ExecutorService executor) {
		fExecutor = executor;
	}

	public TmfRequestExecutor() {
		this(Executors.newSingleThreadExecutor());
	}

	public void start() {
		// Nothing to do
	}

	/**
	 * Stops the executor
	 */
	public void stop() {
		fExecutor.shutdown();
	}
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
	 */
	public synchronized void execute(final Runnable request) {
		fRequests.offer(new Runnable() {
			public void run() {
				try {
					request.run();
				} finally {
					scheduleNext();
				}
			}
		});
		if (fRequest == null) {
			scheduleNext();
		}
	}

	/**
	 * Executes the next pending request, if applicable.
	 */
	protected synchronized void scheduleNext() {
		if ((fRequest = fRequests.poll()) != null) {
			fExecutor.execute(fRequest);
		}
	}

	/**
	 * Queues the request and schedules it.
	 * 
	 * @param request the request to service
	 */
	public synchronized void queueRequest(Runnable request) {
		fRequests.add(request);
		scheduleNext();
	}

}
