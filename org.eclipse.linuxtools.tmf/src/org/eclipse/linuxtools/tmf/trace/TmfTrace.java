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

package org.eclipse.linuxtools.tmf.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;

/**
 * <b><u>TmfTrace</u></b>
 * <p>
 * Abstract implementation of ITmfTrace. It should be sufficient to extend this
 * class and provide implementation for <code>getCurrentLocation()</code> and
 * <code>seekLocation()</code>, as well as a proper parser, to have a working
 * concrete implementation.
 * 
 * TODO: Add support for live streaming (notifications, incremental indexing, ...)
 */
public abstract class TmfTrace<T extends TmfEvent> extends TmfEventProvider<T> implements ITmfTrace {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // The default number of events to cache
	// TODO: Make the DEFAULT_CACHE_SIZE a preference
    public static final int DEFAULT_CACHE_SIZE = 1000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The trace path
    private final String fPath;

    // The trace name
    private final String fName;

    // The cache page size AND checkpoints interval
    protected int fIndexPageSize;

    // The set of event stream checkpoints (for random access)
    protected Vector<TmfCheckpoint> fCheckpoints = new Vector<TmfCheckpoint>();

    // The number of events collected
    protected long fNbEvents = 0;

    // The time span of the event stream
    private TmfTimeRange fTimeRange = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(Class<T> type, String path, int cacheSize) throws FileNotFoundException {
    	super(type);
    	int sep = path.lastIndexOf(File.separator);
    	fName = (sep >= 0) ? path.substring(sep + 1) : path;
    	fPath = path;
        fIndexPageSize = (cacheSize > 0) ? cacheSize : DEFAULT_CACHE_SIZE;
    }

    /**
     * @param path
     * @throws FileNotFoundException
     */
    protected TmfTrace(Class<T> type, String path) throws FileNotFoundException {
    	this(type, path, DEFAULT_CACHE_SIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the trace path
     */
    public String getPath() {
        return fPath;
    }

    /**
     * @return the trace name
     */
    @Override
	public String getName() {
        return fName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getNbEvents()
     */
    public long getNbEvents() {
        return fNbEvents;
    }

    /**
     * @return the size of the cache
     */
    public int getCacheSize() {
        return fIndexPageSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getTimeRange()
     */
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getStartTime()
     */
    public TmfTimestamp getStartTime() {
    	return fTimeRange.getStartTime();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getEndTime()
     */
    public TmfTimestamp getEndTime() {
    	return fTimeRange.getEndTime();
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    protected void setTimeRange(TmfTimeRange range) {
    	fTimeRange = range;
    }

    protected void setStartTime(TmfTimestamp startTime) {
    	fTimeRange = new TmfTimeRange(startTime, fTimeRange.getEndTime());
    }

    protected void setEndTime(TmfTimestamp endTime) {
    	fTimeRange = new TmfTimeRange(fTimeRange.getStartTime(), endTime);
    }

	// ------------------------------------------------------------------------
	// TmfProvider
	// ------------------------------------------------------------------------

	@Override
	public ITmfContext armRequest(TmfDataRequest<T> request) {
		if (request instanceof TmfEventRequest<?>) {
			return seekEvent(((TmfEventRequest<T>) request).getRange().getStartTime());
		}
		if (request instanceof TmfCoalescedEventRequest<?>) {
			return seekEvent(((TmfCoalescedEventRequest<T>) request).getRange().getStartTime());
		}
		return null;
	}

	/**
	 * Return the next piece of data based on the context supplied. The context
	 * would typically be updated for the subsequent read.
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getNext(ITmfContext context) {
		if (context instanceof TmfContext) {
			return (T) getNextEvent((TmfContext) context);
		}
		return null;
	}

//	@Override
//	public boolean isCompleted(TmfDataRequest<T> request, T data) {
//		if (request instanceof TmfEventRequest<?> && data != null) {
//			return data.getTimestamp().compareTo(((TmfEventRequest<T>) request).getRange().getEndTime(), false) > 0;
//		}
//		return true;
//	}

    
	// ------------------------------------------------------------------------
	// ITmfTrace
	// ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    public TmfContext seekEvent(TmfTimestamp timestamp) {

    	if (timestamp == null) {
    		timestamp = TmfTimestamp.BigBang;
    	}

    	// First, find the right checkpoint
    	int index = Collections.binarySearch(fCheckpoints, new TmfCheckpoint(timestamp, null));

        // In the very likely case that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get the event.
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the stream at the checkpoint
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
        	if (fCheckpoints.size() > 0) {
        		if (index >= fCheckpoints.size()) {
        			index = fCheckpoints.size() - 1;
        		}
        		location = fCheckpoints.elementAt(index).getLocation();
        	}
        	else {
        		location = null;
        	}
        }
        TmfContext nextEventContext = seekLocation(location);
        nextEventContext.setRank(index * fIndexPageSize);
        TmfContext currentEventContext = new TmfContext(nextEventContext);

        // And get the event
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	currentEventContext.setLocation(nextEventContext.getLocation());
        	currentEventContext.updateRank(1);
        	event = getNextEvent(nextEventContext);
        }

        return currentEventContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(int)
     */
    public TmfContext seekEvent(long rank) {

        // Position the stream at the previous checkpoint
        int index = (int) rank / fIndexPageSize;
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
        	if (fCheckpoints.size() > 0) {
        		if (index >= fCheckpoints.size()) {
        			index = fCheckpoints.size() - 1;
        		}
        		location = fCheckpoints.elementAt(index).getLocation();
        	}
        	else {
        		location = null;
        	}
        }
        TmfContext context = seekLocation(location);
        long pos = index * fIndexPageSize;
        context.setRank(pos);

        if (pos < rank) {
            TmfEvent event = getNextEvent(context);
            while (event != null && ++pos < rank) {
            	event = getNextEvent(context);
            }
        }

        return new TmfContext(context.getLocation(), context.getRank());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getNextEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext)
	 */
	public synchronized TmfEvent getNextEvent(TmfContext context) {
		// parseEvent() does not update the context
		TmfEvent event = parseEvent(context);
		context.setLocation(getCurrentLocation());
		context.updateRank(1);
		if (event != null) {
			processEvent(event);
		}
    	return event;
	}

    /**
	 * Hook for "special" processing by the concrete class
	 * (called by getNextEvent())
	 * 
	 * @param event
	 */
	public void processEvent(TmfEvent event) {
		// Do nothing by default
	}

    /**
     * To be implemented by the concrete class
     */
    public abstract TmfContext seekLocation(ITmfLocation<?> location);
	public abstract ITmfLocation<?> getCurrentLocation();
    public abstract TmfEvent parseEvent(TmfContext context);

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TmfTrace (" + fName + "]";
	}

    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

	/*
	 * The purpose of the index is to keep the information needed to rapidly
	 * access a trace event based on its timestamp or rank.
	 * 
	 * NOTE: As it is, doesn't work for streaming traces.
	 */

	private IndexingJob job;

	// Indicates that an indexing job is already running
	private Boolean fIndexing = false;
	private Boolean fIndexed  = false;

	public void indexTrace(boolean waitForCompletion) {
    	synchronized (fIndexing) {
			if (fIndexed || fIndexing) {
    			return;
    		}
    		fIndexing = true;
    	}

    	job = new IndexingJob("Indexing " + fName);
    	job.schedule();

    	if (waitForCompletion) {
    		try {
    			job.join();
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    }

    private class IndexingJob extends Job {

		public IndexingJob(String name) {
			super(name);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {

            monitor.beginTask("Indexing " + fName, IProgressMonitor.UNKNOWN);

            int nbEvents = 0;
            TmfTimestamp startTime = null;
            TmfTimestamp lastTime  = null;

            // Reset the index
            fCheckpoints = new Vector<TmfCheckpoint>();
            
            try {
            	// Position the trace at the beginning
                TmfContext context = seekLocation(null);
                ITmfLocation<?> location = context.getLocation();

                // Get the first event
               	TmfEvent event = getNextEvent(context);
               	if (event != null) {
                    startTime = new TmfTimestamp(event.getTimestamp());
               	}

               	// Index the trace
               	while (event != null) {
                	lastTime = event.getTimestamp();
           			if ((nbEvents++ % fIndexPageSize) == 0) {
           				lastTime = new TmfTimestamp(event.getTimestamp());
                   		fCheckpoints.add(new TmfCheckpoint(lastTime, location.clone()));
                   		fNbEvents = nbEvents;
                   		fTimeRange = new TmfTimeRange(startTime, lastTime);
                   		notifyListeners(new TmfTimeRange(startTime, lastTime));

                        monitor.worked(1);

                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                            monitor.done();
                        	return Status.CANCEL_STATUS;
                        }
                    }

                    // We will need this location at the next iteration
                    if ((nbEvents % fIndexPageSize) == 0) {
                        location = context.getLocation();
           			}

                    event = getNextEvent(context);
                }
            }
            finally {
                synchronized(this) {
                	fNbEvents = nbEvents;
                	fTimeRange = new TmfTimeRange(startTime, lastTime);
            		fIndexing = false;
            		fIndexed = true;
                }
                notifyListeners(new TmfTimeRange(startTime, lastTime));
                monitor.done();
            }

//          createOffsetsFile();
//            dumpCheckpoints();
            
            return Status.OK_STATUS;
		}
    }

    protected void notifyListeners(TmfTimeRange range) {
    	broadcast(new TmfTraceUpdatedSignal(this, this, range));
	}
   
	// ========================================================================
	// Toubleshooting code
	// ========================================================================

//	private void dumpCheckpoints() {
//		System.out.println("-----");
//		System.out.println("Checkpoints of " + fName);
//		for (int i = 0; i < fCheckpoints.size(); i++) {
//			TmfCheckpoint checkpoint = fCheckpoints.get(i);
//			TmfContext context = new TmfContext(checkpoint.getLocation(), i * fIndexPageSize);
//			TmfEvent event = getNext(context);
//			System.out.println("  Entry: " + i + " rank: " + (context.getRank() - 1) + " timestamp: " + checkpoint.getTimestamp() + ", event: " + event.getTimestamp());
//			assert((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
//		}
//		System.out.println();
//	}

//	private void createOffsetsFile() {
//
//	    try {
//			// The trace context validation file is read by TmfTraceContext
//	    	ObjectOutputStream  out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("TmfTraceContext.dat")));
//
//	    	TmfTraceContext context = null;
//           	context = seekLocation(null);
//			out.writeObject(context.getLocation());
//
//			int nbEvents = 0;
//            while (getNextEvent(context) != null) {
//    			out.writeObject(context.getLocation());
//    			nbEvents++;
//            }
//            out.close();
//            System.out.println("TmfTrace wrote " + nbEvents + " events");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private void createOffsetsFile() {
//
//		try {
//			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("LTTngOffsets.dat")));
//
//			TmfTraceContext context = null;
//			context = seekLocation(null);
//
//			TmfEvent event;
//			int nbEvents = 0;
//			while ((event = getNextEvent(context)) != null) {
//				out.writeUTF(event.getTimestamp().toString());
//				nbEvents++;
//			}
//			out.close();
//			System.out.println("TmfTrace wrote " + nbEvents + " events");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
