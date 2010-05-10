package org.eclipse.linuxtools.lttng.jni;
/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.eclipse.linuxtools.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniOpenTraceFailedException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTraceException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTracefileWithoutEventException;

/**
 * <b><u>JniTrace</u></b>
 * <p>
 * This is the top level class in the JNI. It provides access to the 
 * LttTrace C structure in java.
 * <p>
 * Most important fields in the JniTrace are :
 * <ul>
 * <li>a JniTrace path (a trace <b>directory</b>)
 * <li>a HashMap of tracefiles that exists in this trace
 * </ul>
 * <p>
 * <b>NOTE</b><p>
 * This class is ABSTRACT, you need to extends it to support your specific LTTng version.<br>
 * Please look at the abstract functions to override at the bottom of this file.<p>
 */
public abstract class JniTrace extends Jni_C_Common {
    
    private final static boolean DEFAULT_LTT_DEBUG = false;
    
    // Internal C pointer of the JniTrace used in LTT
    private Jni_C_Pointer_And_Library_Id thisTracePtr = new Jni_C_Pointer_And_Library_Id();

    // Data we should populate from LTT
    // Note that all type have been scaled up as there is no "unsigned" in java
    // This might be a problem about "unsigned long" as there is no equivalent
    // in java
    
    private String tracepath = ""; // Path of the trace. Should be a directory (like : /tmp/traceX)
    private int    cpuNumber = 0;
    private long   archType = 0;
    private long   archVariant = 0;
    private short  archSize = 0;
    private short  lttMajorVersion = 0;
    private short  lttMinorVersion = 0;
    private short  flightRecorder = 0;
    private long   freqScale = 0;
    private long   startFreq = 0;
    private long   startTimestampCurrentCounter = 0;
    private long   startMonotonic = 0;
    private JniTime startTimeNoAdjustement = null;
    private JniTime startTime = null;
    private JniTime endTime = null;
    
    // This Map holds a reference to the tracefiles owned by this trace
    private HashMap<String, JniTracefile> tracefilesMap = null;
    // The priority queue (similar to heap) hold events 
    private PriorityQueue<JniEvent> eventsHeap = null;
    
    // This variable will hold the content of the "last" event we read
    private JniEvent currentEvent = null;  
    
    // Should we print debug in the C library or not?
    private boolean printLttDebug = DEFAULT_LTT_DEBUG;
    
    
    // This need to be called prior to any operation
    protected native int ltt_initializeHandle(String libname);
    
    // This need to be called at the very end (destructor)
    protected native boolean ltt_freeHandle(int libId);
    
    // Open/close native functions
    protected native long ltt_openTrace(int libId, String pathname, boolean printDebug);
    protected native void ltt_closeTrace(int libId, long tracePtr);

    // Native access functions
    protected native String ltt_getTracepath(int libId, long tracePtr);
    protected native int    ltt_getCpuNumber(int libId, long tracePtr);
    protected native long   ltt_getArchType(int libId, long tracePtr);
    protected native long   ltt_getArchVariant(int libId, long tracePtr);
    protected native short  ltt_getArchSize(int libId, long tracePtr);
    protected native short  ltt_getLttMajorVersion(int libId, long tracePtr);
    protected native short  ltt_getLttMinorVersion(int libId, long tracePtr);
    protected native short  ltt_getFlightRecorder(int libId, long tracePtr);
    protected native long   ltt_getFreqScale(int libId, long tracePtr);
    protected native long   ltt_getStartFreq(int libId, long tracePtr);
    protected native long   ltt_getStartTimestampCurrentCounter(int libId, long tracePtr);
    protected native long   ltt_getStartMonotonic(int libId, long tracePtr);
    
    // Native function to fill out startTime
    protected native void ltt_feedStartTime(int libId, long tracePtr, JniTime startTime);
    
    // Native function to fill out startTimeFromTimestampCurrentCounter
    protected native void ltt_feedStartTimeFromTimestampCurrentCounter(int libId, long tracePtr, JniTime startTime);

    // Native function to fill out tracefilesMap
    protected native void ltt_feedAllTracefiles(int libId, long tracePtr);
    
    // Native function to fill out the start and end time of the trace
    protected native void ltt_feedTracefileTimeRange(int libId, long tracePtr, JniTime startTime, JniTime endTime);
    
    // Debug native function, ask LTT to print trace structure
    protected native void ltt_printTrace(int libId, long tracePtr);
    
    /*
     * Default constructor is forbidden
     */
    protected JniTrace() {
    }
    
    /**
     * Constructor that takes a tracepath parameter.<p>
     * 
     * This constructor also opens the trace.
     * 
     * @param newpath The <b>directory</b> of the trace to be opened
     * 
     * @exception JniException
     */
    public JniTrace(String newpath) throws JniException {
        this(newpath, DEFAULT_LTT_DEBUG);
    }
    
    /**
     * Constructor that takes a tracepath parameter and a debug value.<p>
     * 
     * This constructor also opens the trace.
     * 
     * @param newpath The <b>directory</b> of the trace to be opened
     * @param newPrintDebug Should the debug information be printed in the LTT C library
     * 
     * @exception JniException
     */
    public JniTrace(String newpath, boolean newPrintDebug) throws JniException {
        tracepath = newpath;
        thisTracePtr = new Jni_C_Pointer_And_Library_Id();
        printLttDebug = newPrintDebug;
        
        openTrace(newpath);
    }
    
    /**
     * Copy constructor.
     * 
     * @param oldTrace  A reference to the JniTrace to copy.           
     */
    public JniTrace(JniTrace oldTrace) {
        thisTracePtr  = oldTrace.thisTracePtr;
        
        tracepath       = oldTrace.tracepath;
        cpuNumber       = oldTrace.cpuNumber;
        archType        = oldTrace.archType;
        archVariant     = oldTrace.archVariant;
        archSize        = oldTrace.archSize;
        lttMajorVersion = oldTrace.lttMajorVersion;
        lttMinorVersion = oldTrace.lttMinorVersion;
        flightRecorder  = oldTrace.flightRecorder;
        freqScale       = oldTrace.freqScale;
        startFreq       = oldTrace.startFreq;
        startTimestampCurrentCounter = oldTrace.startTimestampCurrentCounter;
        startMonotonic  = oldTrace.startMonotonic;
        startTimeNoAdjustement = oldTrace.startTimeNoAdjustement;
        startTime       = oldTrace.startTime;
        endTime         = oldTrace.endTime;

        tracefilesMap = new HashMap<String, JniTracefile>(oldTrace.tracefilesMap.size());
        ltt_feedAllTracefiles(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        
        eventsHeap = new PriorityQueue<JniEvent>( oldTrace.eventsHeap.size());
        populateEventHeap();
        
        printLttDebug = oldTrace.printLttDebug;
    }        
        
    /**
     * Constructor, using C pointer.<p>
     * 
     * @param newPtr The pointer to an already opened LttTrace C structure.
     * @param newPrintDebug Should the debug information be printed in the LTT C library
     *            
     * @exception JniException
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public JniTrace(Jni_C_Pointer_And_Library_Id newPtr, boolean newPrintDebug) throws JniException {
        thisTracePtr = newPtr;
        printLttDebug = newPrintDebug;
        
        // Populate our trace
        populateTraceInformation();
    }
    
    
    @Override
	public void finalize() {
    	// If the trace is open, close it
        if (thisTracePtr.getPointer() != NULL) {
            closeTrace();
        }
        
        // Tell the C to free the allocated memory
        freeLibrary();
    }
    
    /**
     * Open an existing trace.<p>
     * 
     * The tracepath is a directory and needs to exist, otherwise
     * a JniOpenTraceFailedException is throwed.
     * 
     * @param newPath The <b>directory</b> of the trace to be opened
     * 
     * @exception JniOpenTraceFailedException Thrown if the open failed
     */
    public void openTrace(String newPath) throws JniException {
        // If open is called while a trace is already opened, we will try to close it first
        if (thisTracePtr.getPointer() != NULL) {
            closeTrace();
        }

        // Set the tracepath and open it
        tracepath = newPath;
        openTrace();
    }
        
    /**
     * Open an existing trace.<p>
     * 
     * The tracepath should have been set already,
     * 
     * @exception JniOpenTraceFailedException  Thrown if the open failed
     */
    public void openTrace() throws JniException {
    	
        // Raise an exception if the tracepath is empty, otherwise open the trace
        if (tracepath == "") {
            throw new JniTraceException("Tracepath is not set. (openTrace)");
        }
        
        // If the file is already opened, close it first
        if (thisTracePtr.getPointer() != NULL) {
            closeTrace();
        }
        
        // Initialization of the library is made here
        // It is very important that the id is kept!
        int newLibraryId = initializeLibrary();
        if ( newLibraryId != -1 ) {
	        // Call the LTT to open the trace
        	// Note that the libraryId is not yet and the pointer
	        long newPtr = ltt_openTrace(newLibraryId, tracepath, printLttDebug);
	        
	        if (newPtr == NULL) {
	        	thisTracePtr = new Jni_C_Pointer_And_Library_Id();
	            throw new JniOpenTraceFailedException("Error while opening trace. Is the tracepath correct? (openTrace)");
	        }
	        
	        // This is OUR pointer
	        thisTracePtr = new Jni_C_Pointer_And_Library_Id(newLibraryId, newPtr);
	
	        // Populate the trace with LTT information
	        populateTraceInformation();
        }
        else {
        	thisTracePtr = new Jni_C_Pointer_And_Library_Id();
        	throw new JniTraceException("Failed to initialize library! Is the trace version supported?\n" +
        			                    "Make sure you have the correct LTTv library compiled. (openTrace)");
        }
    }
        
    /**
     * Close a trace.<p>
     * 
     * If the trace is already closed, will silently do nothing.
     */
    public void closeTrace() {
    	
        if (thisTracePtr.getPointer() != NULL) {
            ltt_closeTrace(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
            
            // Clear the tracefile map
            tracefilesMap.clear();
            tracefilesMap = null;
            
            // Clear the eventsHeap and make it points to null
            eventsHeap.clear();
            eventsHeap = null;
            
            // Nullify the pointer
            thisTracePtr = new Jni_C_Pointer_And_Library_Id();
        }
    }
    
    /**
     * This function force the library to free its memory.<p>
     * 
     * Note : No call to the library will work after this until ltt_initializeHandle is called again
     */
    public void freeLibrary() {
    	ltt_freeHandle(thisTracePtr.getLibraryId());
    }
    
    /* 
     * This function populates the trace data with data from LTT
     * 
     * @throws JniException
     */
    private void populateTraceInformation() throws JniException {
        if (thisTracePtr.getPointer() == NULL) {
            throw new JniTraceException("Pointer is NULL, trace not opened/already closed? (populateTraceInformation)");
        }

        // Populate from the LTT library
        tracepath   = ltt_getTracepath(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        cpuNumber   = ltt_getCpuNumber(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        archType    = ltt_getArchType(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        archVariant = ltt_getArchVariant(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        archSize    = ltt_getArchSize(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        lttMajorVersion = ltt_getLttMajorVersion(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        lttMinorVersion = ltt_getLttMinorVersion(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        flightRecorder  = ltt_getFlightRecorder(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        freqScale   = ltt_getFreqScale(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        startFreq   = ltt_getStartFreq(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        startTimestampCurrentCounter = ltt_getStartTimestampCurrentCounter(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        startMonotonic = ltt_getStartMonotonic(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());

        // Creation of time is a bit different, we need to pass the object reference to C
        //
        // *** NOTE : LTTv consider "raw startTime" (time without any frequency adjustement) to be default startTime
        //			  So "startTimeNoAdjustement" is obtain throught "ltt_feedStartTime()" and
        //			  "startTime" is obtained from ltt_feedStartTimeFromTimestampCurrentCounter()
        startTimeNoAdjustement = new JniTime();
        ltt_feedStartTime(thisTracePtr.getLibraryId(), thisTracePtr.getPointer(), startTimeNoAdjustement);
        
        startTime = new JniTime();
        ltt_feedStartTimeFromTimestampCurrentCounter(thisTracePtr.getLibraryId(), thisTracePtr.getPointer(), startTime);

        // Call the fill up function for the tracefiles map
        if ( tracefilesMap== null ) {
            tracefilesMap = new HashMap<String, JniTracefile>();
        }
        ltt_feedAllTracefiles(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
        
        // Now, obtain the trace "endTime"
        // Note that we discard "startTime" right away, as we already have it
        endTime = new JniTime();
        ltt_feedTracefileTimeRange(thisTracePtr.getLibraryId(), thisTracePtr.getPointer(), new JniTime(), endTime);
        
        if (eventsHeap == null) {
            eventsHeap = new PriorityQueue<JniEvent>(tracefilesMap.size());
        }
        
        // Populate the heap with events
        populateEventHeap();
    }
    
    /* 
     * This function populates the event heap with one event from each tracefile
     * It should be called after each seek or when the object is constructed
     */
    private void populateEventHeap() {
        currentEvent = null;
        eventsHeap.clear();
        
        Object new_key = null;
        JniTracefile tmpTracefile = null;
        
        Iterator<String> iterator = tracefilesMap.keySet().iterator();
        while( iterator.hasNext() ) {
            new_key = iterator.next();
            
            tmpTracefile = tracefilesMap.get(new_key);
            if ( tmpTracefile.getCurrentEvent().getEventState() == EOK ) {
                eventsHeap.add( tmpTracefile.getCurrentEvent() );
            }
        }
    }
    
    /* 
     * Fills a map of all the trace files.
     * 
     * Note: This function is called from C and there is no way to propagate
     * exception back to the caller without crashing JNI. Therefore, it MUST
     * catch all exceptions.
     * 
     * @param tracefileName
     * @param tracefilePtr
     */
    protected void addTracefileFromC(String tracefileName, long tracefilePtr) {
        
        JniTracefile newTracefile = null;
            
        // Create a new tracefile object and insert it in the map
        //    the tracefile fill itself with LTT data while being constructed
        try {
            newTracefile = allocateNewJniTracefile(new Jni_C_Pointer_And_Library_Id(thisTracePtr.getLibraryId(), tracefilePtr), this);
            getTracefilesMap().put( (tracefileName + newTracefile.getCpuNumber()), newTracefile);
        }
        catch(JniTracefileWithoutEventException e) {
        	if ( printLttDebug == true ) {
        		printlnC(thisTracePtr.getLibraryId(), "JniTracefile " + tracefileName + " has no event (addTracefileFromC). Ignoring.");
        	}
        }
        catch(Exception e) {
        	if ( printLttDebug == true ) {
        		printlnC(thisTracePtr.getLibraryId(), "Failed to add tracefile " + tracefileName + " to tracefilesMap!(addTracefileFromC)\n\tException raised : " + e.toString() );
        	}
        }
    }
    
    /**
     * Return the top event in the events stack, determined by timestamp, in the trace (all the tracefiles).<p>
     * 
     * Note : If the events were read before, the top event and the event currently loaded (currentEvent) are most likely the same.
     * 
     * @return The top event in the stack or null if no event is available.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent findNextEvent() {
        return eventsHeap.peek();
    }
    
    /**
     * Return the next event in the events stack, determined by timestamp, in the trace (all the tracefiles).<p>
     * 
     * @return The next event in the trace or null if no event is available.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent readNextEvent() {
        // Get the "next" event on the top of the heap but DO NOT remove it
        JniEvent tmpEvent = eventsHeap.peek();
        
        // If the event is null, it was the last one in the trace we can leave the function
        if (tmpEvent == null) {
            return null;
        }
        
        // Otherwise, we need to make sure the timestamp of the event we got is not the same as the last "NextEvent" we requested 
        // NOTE : JniEvent.compareTo() compare by timestamp AND type, as 2 events of different type could have the same timestamp.
        if ( tmpEvent.compareTo(currentEvent) == 0 ) {
            // Remove the event on top as it is the same currentEventTimestamp
            eventsHeap.poll();
            
            // Read the next event for this particular event type
            tmpEvent.readNextEvent();
            
            // If the event state is sane (not Out of Range), put it back in the heap
            if ( tmpEvent.getEventState() == EOK ) {
                eventsHeap.add(tmpEvent);
            }
            
            // Pick the top event again
            tmpEvent = eventsHeap.peek();
            
            // Save the event we just read as the "current event"
            currentEvent = tmpEvent;
        }
        // If the event on top has different timestamp than the currentTimestamp, just save this timestamp as current
        else {
            currentEvent = tmpEvent;
        }
        
        return tmpEvent;
    }
    
    /**
     * Read the next event on a certain tracefile.<p>
     * 
     * By calling this function make sure the "global" readNextEvent() stay synchronised.
     * Calling  readNextEvent() after this function will consider this tracefile moved and is then consistent.
     * 
     * @param targetTracefile 	The tracefile object to read from
     * 
     * @return The next event in the tracefile or null if no event is available.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent readNextEvent(JniTracefile targetTracefile) {
        JniEvent returnedEvent = null;
    	
        // There is 2 special cases where we should read the CURRENT event, not the next one
        // 1- The currentEvent is null                      --> We never read or we just seeked
        // 2- The currentEvent is of another type  --> We last read on a DIFFERENT tracefile
        if ( (currentEvent == null) || 
              (currentEvent.getParentTracefile().equals(targetTracefile) == false)
            ) {
            returnedEvent = targetTracefile.getCurrentEvent();
            // Save the event we read
            currentEvent = returnedEvent; 
        }
        else {
        	// Remove from the event related to this tracefile from the event heap, if it exists.
        	// WARNING : This only safe as long getCurrentEvent() never return "null" in any case.
        	eventsHeap.remove(targetTracefile.getCurrentEvent() );
        	
        	// If status EOK, we can return the event, otherwise something wrong happen (out of range, read error, etc...)
        	if ( targetTracefile.readNextEvent() == EOK) {
        	    returnedEvent = targetTracefile.getCurrentEvent();
        		// Add back to the heap the read event
        		eventsHeap.add(returnedEvent);
        	}
        	// Save the event we read... 
        	// Note : might be null if the read failed and it's ok
        	currentEvent = targetTracefile.getCurrentEvent(); 
        }
        
    	return returnedEvent;
    }
    
    /**
    * Seek to a certain time but <b>do not</b> read the next event.<p>
    * 
    * This only position the trace, it will not return anything.<p>
    * 
    * @param seekTime     The time where we want to seek to
    * 
    * @see org.eclipse.linuxtools.lttng.jni.common.JniTime
    */
    public void seekToTime(JniTime seekTime) {
        
        // Invalidate the last read event
        currentEvent = null;
        
        Object tracefile_name = null;
    	Iterator<String> iterator = tracefilesMap.keySet().iterator();
    	
    	while (iterator.hasNext() ) {
    	    // We seek to the given event for ALL tracefiles
    	    tracefile_name = iterator.next();
    		seekToTime(seekTime, tracefilesMap.get(tracefile_name));
    	}
    	
    	populateEventHeap();
    }
	
    /**
    * Seek to a certain time on a certain tracefile but <b>do not</b> read the next event.<p>
    * 
    * This only position the trace, it will not return anything.<p>
    * 
    * @param targetTracefile 	The tracefile object to read from
    * @param seekTime     		The time where we want to seek to
    * 
    * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
    * @see org.eclipse.linuxtools.lttng.jni.common.JniTime
    */
    public void seekToTime(JniTime seekTime, JniTracefile targetTracefile) {
        // Invalidate the current read event
        currentEvent = null;
        
        // Remove from the event related to this tracefile from the event heap, if it exists.
        // WARNING : This is only safe as long getCurrentEvent() never return "null" in any case.
        eventsHeap.remove(targetTracefile.getCurrentEvent() );
        
        // Perform the actual seek on the tracefile
        // Add the event to the heap if it succeed
        if ( targetTracefile.seekToTime(seekTime) == EOK) {
        	// Add back to the heap the read event
            eventsHeap.add(targetTracefile.getCurrentEvent());
        }
    }
	
    /**
    * Seek to a certain timestamp and read the next event.
    * <p>
    * If no more events are available or an error happen, null will be returned.
    * 
    * @param seekTime      The time where we want to seek to.
    * 
    * @return The event just after the seeked time or null if none available.
    * 
    * @see org.eclipse.linuxtools.lttng.jni.JniEvent
    * @see org.eclipse.linuxtools.lttng.jni.common.JniTime
    */
    public JniEvent seekAndRead(JniTime seekTime) { 
         JniEvent returnedEvent = null;
         seekToTime(seekTime);
            
         // The trace should be correctly positionned, let's get the event
         returnedEvent = readNextEvent();
             
         return returnedEvent;
    }
	
    /**
    * Seek to a certain timestamp on a certain tracefile and read the next event.<p>
    * 
    * If no more events are available or an error happen, null will be returned.
    * 
    * Calling  readNextEvent() after this function will consider this tracefile moved and is then consistent.<br>
    * 
    * @param targetTracefile    The tracefile object to read from
    * @param seekTime           The time where we want to seek to
    * 
    * @return The event just after the seeked time or null if none available.
    * 
    * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
    * @see org.eclipse.linuxtools.lttng.jni.common.JniTime
    * @see org.eclipse.linuxtools.lttng.jni.JniEvent
    */
    public JniEvent seekAndRead(JniTime seekTime, JniTracefile targetTracefile) { 
        seekToTime(seekTime, targetTracefile);
        return readNextEvent(targetTracefile);
    }
     
    /**
     * Get a certain tracefile from its given name.<p>
     * 
     * @param tracefileName     The name of the tracefile.
     * 
     * @return The tracefile found or null if none.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    public JniTracefile requestTracefileByName(String tracefileName) {
        return tracefilesMap.get(tracefileName);
    }        
        
    /**
     * Get a certain event associated to a tracefile from the tracefile name.<p>
     * 
     * @param tracefileName     The name of the trace file.
     * 
     * @return Event of the tracefile or null if none found.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent requestEventByName(String tracefileName) {
        JniEvent returnValue = null;

        JniTracefile tmpTracefile = tracefilesMap.get(tracefileName);

        // If the tracefile is found, return the current event
        // There should always be an event linked to a tracefile
        if (tmpTracefile != null) {
            returnValue = tmpTracefile.getCurrentEvent();
        }

        return returnValue;
    }   
        
    // Access to class variable. Most of them doesn't have setter
    public String getTracepath() {
        return tracepath;
    }

    public int getCpuNumber() {
        return cpuNumber;
    }

    public long getArchType() {
        return archType;
    }

    public long getArchVariant() {
        return archVariant;
    }

    public short getArchSize() {
        return archSize;
    }

    public short getLttMajorVersion() {
        return lttMajorVersion;
    }

    public short getLttMinorVersion() {
        return lttMinorVersion;
    }

    public short getFlightRecorder() {
        return flightRecorder;
    }

    public long getFreqScale() {
        return freqScale;
    }

    public long getStartFreq() {
        return startFreq;
    }

    public long getStartTimestampCurrentCounter() {
        return startTimestampCurrentCounter;
    }

    public long getStartMonotonic() {
        return startMonotonic;
    }

    public JniTime getStartTime() {
        return startTime;
    }
    
    public JniTime getEndTime() {
        return endTime;
    }

    public JniTime getStartTimeNoAdjustement() {
        return startTimeNoAdjustement;
    }
    
    public HashMap<String, JniTracefile> getTracefilesMap() {
        return tracefilesMap;
    }        
    
    /**
     * The timestamp of the last read event.<p>
     * 
     * Note : If no event is available, Long.MAX_VALUE is returned.
     * 
     * @return Time of the last event read
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.JniTime
     */
    public JniTime getCurrentEventTimestamp() {
        JniTime returnedTime = null;
        
        // If no event were read or we reach the last event in the trace, 
        //      currentEvent will be null
        if (  currentEvent != null ) {
            returnedTime = currentEvent.getEventTime();
        }
        else {
            returnedTime = new JniTime(Long.MAX_VALUE);
        }
        return returnedTime;
    }
    
    /**
     * Pointer to the LttTrace C structure.<p>
     * 
     * The pointer should only be used <u>INTERNALY</u>, do not use unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public Jni_C_Pointer_And_Library_Id getTracePtr() {
        return thisTracePtr;
    }        
    
    /**
     * Return boolean value saying if the debug is enabled in LTT or not.<p>
     * 
     * Note : this need to be set at construction.
     * 
     * @return If the debug is set or not
     */
    public boolean isPrintingLttDebug() {
        return printLttDebug;
    }
    
    /**
     * Print information for all the tracefiles associated with this trace.
     * <u>Intended to debug</u><p>
     * 
     * This function will call Ltt to print, so information printed will be the
     * one from the C structure, not the one populated in java.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    public void printAllTracefilesInformation() {
        JniTracefile tracefile = null;

        Iterator<String> iterator = tracefilesMap.keySet().iterator();
        while (iterator.hasNext()) {
            tracefile = tracefilesMap.get(iterator.next());
            tracefile.printTracefileInformation();
        }
    }        
        
    /**
     * Print information for this trace. 
     * <u>Intended to debug</u><p>
     * 
     * This function will call Ltt to print, so information printed will be the
     * one from the C structure, not the one populated in java.<p>
     */
    public void printTraceInformation() {
        ltt_printTrace(thisTracePtr.getLibraryId(), thisTracePtr.getPointer());
    }
        
    /**
     * toString() method. 
     * <u>Intended to debug</u><br>
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
	public String toString() {
        String returnData = "";
        returnData += "tracepath                            : " + tracepath + "\n";
        returnData += "cpuNumber                            : " + cpuNumber + "\n";
        returnData += "archType                             : " + archType + "\n";
        returnData += "archVariant                          : " + archVariant + "\n";
        returnData += "archSize                             : " + archSize + "\n";
        returnData += "lttMajorVersion                      : " + lttMajorVersion + "\n";
        returnData += "lttMinorVersion                      : " + lttMinorVersion + "\n";
        returnData += "flightRecorder                       : " + flightRecorder + "\n";
        returnData += "freqScale                            : " + freqScale + "\n";
        returnData += "startFreq                            : " + startFreq + "\n";
        returnData += "startTimestampCurrentCounter         : " + startTimestampCurrentCounter + "\n";
        returnData += "startMonotonic                       : " + startMonotonic + "\n";
        returnData += "startTimeNoAdjustement               : " + startTimeNoAdjustement.getReferenceToString() + "\n";
        returnData += "   seconds                           : " + startTimeNoAdjustement.getSeconds() + "\n";
        returnData += "   nanoSeconds                       : " + startTimeNoAdjustement.getNanoSeconds() + "\n";
        returnData += "startTime                            : " + startTime.getReferenceToString() + "\n";
        returnData += "   seconds                           : " + startTime.getSeconds() + "\n";
        returnData += "   nanoSeconds                       : " + startTime.getNanoSeconds() + "\n";
        returnData += "endTime                              : " + endTime.getReferenceToString() + "\n";
        returnData += "   seconds                           : " + endTime.getSeconds() + "\n";
        returnData += "   nanoSeconds                       : " + endTime.getNanoSeconds() + "\n";
        returnData += "tracefilesMap                        : " + tracefilesMap.keySet() + "\n";      // Hack to avoid ending up with tracefilesMap.toString()

        return returnData;
    }
    
    
    // ****************************
    // **** ABSTRACT FUNCTIONS ****
    // You MUST override those in your version specific implementation
    
    /**
     * Function place holder to load the correct C library.<p>
     * <br>
     * Can be as simple as calling ltt_initializeHandle(LIBRARY_NAME) with the correct .so instead of LIBRARY_NAME.<br>
     * You may also want to perform some check or some additionnal validations.<br>
     * <br>
     * <b>!! Override this with you version specific implementation.</b><br>
     * 
     * @return integer that is the library id, or -1 if the load was unsuccessful
     */
    public abstract int initializeLibrary();
    
    
    /**
     * Function place holder to allocate a new JniTracefile.<p>
     * 
     * JniTracefile constructor is non overridable so we need another overridable function to return the correct version of JniTracefile.<br>
     * Effect of this function should be the same (allocate a fresh new JniTracefile)<br>
     * <br>
     * <b>!! Override this with you version specific implementation.</b><br>
     * 
     * @param newPtr			The pointer of an already opened LttTracefile C Structure
     * @param newParentTrace	The JniTrace parent of this tracefile.
     * 
     * @return					The newly allocated JniTracefile of the correct version
     * 
     * @throws JniException		The construction (allocation) failed.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public abstract JniTracefile allocateNewJniTracefile(Jni_C_Pointer_And_Library_Id newPtr, JniTrace newParentTrace) throws JniException;
    
}
