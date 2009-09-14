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

package org.eclipse.linuxtools.lttng.jni;

import java.util.HashMap;

/**
 * <b><u>JniTracefile</u></b>
 * <p>
 * A tracefile own an event of a certain type
 * It provides access to the LttTracefile C structure in java.
 * <p>
 * Most important fields in the JniTracefile are :
 * <ul>
 * <li> a JniTracefile path (a tracefile <b>file</b> with a JniTrace directory)
 * <li> a name (basically the name without the directory part)
 * <li> a reference to a single event object
 * <li> a HashMap of marker associated with this tracefile
 * </ul>
 */
public final class JniTracefile extends Jni_C_Common {
        
    // Internal C pointer of the JniTracefile used in LTT
    private C_Pointer thisTracefilePtr = new C_Pointer();
    
    // Reference to the parent trace
    private JniTrace parentTrace = null;
    
    // Data we should populate from LTT
    // Note that all type have been scaled up as there is no "unsigned" in java
    // This might be a problem about "unsigned long" as there is no equivalent in java
    private boolean isCpuOnline = false;
    private String  tracefilePath = "";
    private String  tracefileName = "";
    private long    cpuNumber = 0;
    private long    tid = 0;
    private long    pgid = 0;
    private long    creation = 0;
    private long    tracePtr = 0;
    private long    markerDataPtr = 0;
    private int     CFileDescriptor = 0;
    private long    fileSize = 0;
    private long    blocksNumber = 0;
    private boolean isBytesOrderReversed = false;
    private boolean isFloatWordOrdered = false;
    private long    alignement = 0;
    private long    bufferHeaderSize = 0;
    private int     bitsOfCurrentTimestampCounter = 0;
    private int     bitsOfEvent = 0;
    private long    currentTimestampCounterMask = 0;
    private long    currentTimestampCounterMaskNextBit = 0;
    private long    eventsLost = 0;
    private long    subBufferCorrupt = 0;
    private JniEvent   currentEvent = null;
    private long    bufferPtr = NULL;
    private long    bufferSize = 0;

    // This map will hold markers_info owned by this tracefile
    private HashMap<Integer, JniMarker> tracefileMarkersMap;        

    // Native access functions
    private native boolean  ltt_getIsCpuOnline(long tracefilePtr);
    private native String   ltt_getTracefilepath(long tracefilePtr);
    private native String   ltt_getTracefilename(long tracefilePtr);
    private native long     ltt_getCpuNumber(long tracefilePtr);
    private native long     ltt_getTid(long tracefilePtr);
    private native long     ltt_getPgid(long tracefilePtr);
    private native long     ltt_getCreation(long tracefilePtr);
    private native long     ltt_getTracePtr(long tracefilePtr);
    private native long     ltt_getMarkerDataPtr(long tracefilePtr);
    private native int      ltt_getCFileDescriptor(long tracefilePtr);
    private native long     ltt_getFileSize(long tracefilePtr);
    private native long     ltt_getBlockNumber(long tracefilePtr);
    private native boolean  ltt_getIsBytesOrderReversed(long tracefilePtr);
    private native boolean  ltt_getIsFloatWordOrdered(long tracefilePtr);
    private native long     ltt_getAlignement(long tracefilePtr);
    private native long     ltt_getBufferHeaderSize(long tracefilePtr);
    private native int      ltt_getBitsOfCurrentTimestampCounter(long tracefilePtr);
    private native int      ltt_getBitsOfEvent(long tracefilePtr);
    private native long     ltt_getCurrentTimestampCounterMask(long tracefilePtr);
    private native long     ltt_getCurrentTimestampCounterMaskNextBit(long tracefilePtr);
    private native long     ltt_getEventsLost(long tracefilePtr);
    private native long     ltt_getSubBufferCorrupt(long tracefilePtr);
    private native long     ltt_getEventPtr(long tracefilePtr);
    private native long     ltt_getBufferPtr(long tracefilePtr);
    private native long     ltt_getBufferSize(long tracefilePtr);

    // Method to fill a map with marker object
    private native void ltt_getAllMarkers(long tracefilePtr);

    // Debug native function, ask LTT to print tracefile structure
    private native void ltt_printTracefile(long tracefilePtr);

    static {
        System.loadLibrary("lttvtraceread");
    }
        
    /**
     * Default constructor is forbidden
     */
    @SuppressWarnings("unused")
    private JniTracefile() {
    };

    /**
     * Copy constructor.
     * 
     * @param oldTracefile
     *            A reference to the JniTracefile you want to copy. 
     */
    public JniTracefile(JniTracefile oldTracefile) {
        thisTracefilePtr    = oldTracefile.thisTracefilePtr;
        parentTrace         = oldTracefile.parentTrace;
        tracefileMarkersMap = oldTracefile.tracefileMarkersMap;

        isCpuOnline         = oldTracefile.isCpuOnline;
        tracefilePath       = oldTracefile.tracefilePath;
        tracefileName       = oldTracefile.tracefileName;
        cpuNumber           = oldTracefile.cpuNumber;
        tid                 = oldTracefile.tid;
        pgid                = oldTracefile.pgid;
        creation            = oldTracefile.creation;
        tracePtr            = oldTracefile.tracePtr;
        markerDataPtr       = oldTracefile.markerDataPtr;
        CFileDescriptor     = oldTracefile.CFileDescriptor;
        fileSize            = oldTracefile.fileSize;
        blocksNumber        = oldTracefile.blocksNumber;
        isBytesOrderReversed = oldTracefile.isBytesOrderReversed;
        isFloatWordOrdered  = oldTracefile.isFloatWordOrdered;
        alignement          = oldTracefile.alignement;
        bufferHeaderSize    = oldTracefile.bufferHeaderSize;
        bitsOfCurrentTimestampCounter = oldTracefile.bitsOfCurrentTimestampCounter;
        bitsOfEvent         = oldTracefile.bitsOfEvent;
        currentTimestampCounterMask = oldTracefile.currentTimestampCounterMask;
        currentTimestampCounterMaskNextBit = oldTracefile.currentTimestampCounterMaskNextBit;
        eventsLost          = oldTracefile.eventsLost;
        subBufferCorrupt    = oldTracefile.subBufferCorrupt;
        currentEvent        = oldTracefile.currentEvent;
        bufferPtr           = oldTracefile.bufferPtr;
        bufferSize          = oldTracefile.bufferSize;
    }

    /**
     * Copy constructor, using pointer.
     * 
     * @param newPtr  The pointer to an already opened LttTracefile C Structure
     * 
     * @exception JniException
     */
    public JniTracefile(C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
        thisTracefilePtr = newPtr;
        parentTrace = newParentTrace;
        tracefileMarkersMap = new HashMap<Integer, JniMarker>();

        // Retrieve the trace file information and load the first event.
        try {
            populateTracefileInformation();
        } catch (JniNoSuchEventException e) {
            throw new JniTracefileWithoutEventException(
                    "JniEvent constructor reported that no event of this type are usable. (Jaf_Tracefile)");
        }
    }        

    /**
     * Move the current event to the next one.
     * 
     * @return The read status, as defined in Jni_C_Common
     * @see org.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
    public int readNextEvent() {
        return currentEvent.readNextEvent();
    }        

    /**
     * Move the current event to the time given.<br>
     * 
     * @param seekTime JniTime where to seek to
     * @return The read status, as defined in Jni_C_Common
     * @see org.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
    public int seekToTime(JniTime seekTime) {
        return currentEvent.seekToTime(seekTime);
    }

    /* 
     * This function populates the tracefile data with data from LTT
     * 
     * @throws JafException
     */
    private void populateTracefileInformation() throws JniException {
        if (thisTracefilePtr.getPointer() == NULL) {
            throw new JniTracefileException(
                    "Pointer is NULL, trace closed? (populateTracefileInformation)");
        }

        isCpuOnline = ltt_getIsCpuOnline( thisTracefilePtr.getPointer() );
        tracefilePath = ltt_getTracefilepath( thisTracefilePtr.getPointer() );
        tracefileName = ltt_getTracefilename( thisTracefilePtr.getPointer() );
        cpuNumber = ltt_getCpuNumber( thisTracefilePtr.getPointer() );
        tid = ltt_getTid( thisTracefilePtr.getPointer() );
        pgid = ltt_getPgid( thisTracefilePtr.getPointer() );
        creation = ltt_getCreation( thisTracefilePtr.getPointer() );
        tracePtr = ltt_getTracePtr( thisTracefilePtr.getPointer() );
        markerDataPtr = ltt_getMarkerDataPtr( thisTracefilePtr.getPointer() );
        CFileDescriptor = ltt_getCFileDescriptor( thisTracefilePtr.getPointer() );
        fileSize = ltt_getFileSize( thisTracefilePtr.getPointer() );
        blocksNumber = ltt_getBlockNumber( thisTracefilePtr.getPointer() );
        isBytesOrderReversed = ltt_getIsBytesOrderReversed( thisTracefilePtr.getPointer() );
        isFloatWordOrdered = ltt_getIsFloatWordOrdered( thisTracefilePtr.getPointer() );
        alignement = ltt_getAlignement( thisTracefilePtr.getPointer() );
        bufferHeaderSize = ltt_getBufferHeaderSize( thisTracefilePtr.getPointer() );
        bitsOfCurrentTimestampCounter = ltt_getBitsOfCurrentTimestampCounter( thisTracefilePtr.getPointer() );
        bitsOfEvent = ltt_getBitsOfEvent( thisTracefilePtr.getPointer() );
        currentTimestampCounterMask = ltt_getCurrentTimestampCounterMask( thisTracefilePtr.getPointer() );
        currentTimestampCounterMaskNextBit = ltt_getCurrentTimestampCounterMaskNextBit( thisTracefilePtr.getPointer() );
        eventsLost = ltt_getEventsLost( thisTracefilePtr.getPointer() );
        subBufferCorrupt = ltt_getSubBufferCorrupt( thisTracefilePtr.getPointer() );
        bufferPtr = ltt_getBufferPtr( thisTracefilePtr.getPointer() );
        bufferSize = ltt_getBufferSize( thisTracefilePtr.getPointer() );

        // To fill the map is a bit different
        ltt_getAllMarkers( thisTracefilePtr.getPointer() );

        C_Pointer tmpEventPointer = new C_Pointer(ltt_getEventPtr(thisTracefilePtr.getPointer()));
        currentEvent = new JniEvent(tmpEventPointer , tracefileMarkersMap, this);
    }        
    
    /* 
     * Fills a map of all the markers associated with this tracefile.
     * 
     * Note: This function is called from C and there is no way to propagate
     * exception back to the caller without crashing JNI. Therefore, it MUST
     * catch all exceptions.
     * 
     * @param markerId          Id of the marker (int)
     * @param markerInfoPtr     C Pointer to a marker_info C structure 
     */
    @SuppressWarnings("unused")
    private void addMarkersFromC(int markerId, long markerInfoPtr) {
        // Create a new tracefile object and insert it in the map
        // the tracefile fill itself with LTT data while being constructed
        try {
            JniMarker newMarker = new JniMarker( new C_Pointer(markerInfoPtr) );

            tracefileMarkersMap.put(markerId, newMarker);
        } catch (Exception e) {
            printlnC("Failed to add marker to tracefileMarkersMap!(addMarkersFromC)\n\tException raised : " + e.toString());
        }
    }        

    // Access to class variable. Most of them doesn't have setter
    public boolean getIsCpuOnline() {
        return isCpuOnline;
    }

    public String getTracefilePath() {
        return tracefilePath;
    }

    public String getTracefileName() {
        return tracefileName;
    }

    public long getCpuNumber() {
        return cpuNumber;
    }

    public long getTid() {
        return tid;
    }

    public long getPgid() {
        return pgid;
    }

    public long getCreation() {
        return creation;
    }

    public long getTracePtr() {
        return tracePtr;
    }

    public long getMarkerDataPtr() {
        return markerDataPtr;
    }

    public int getCFileDescriptor() {
        return CFileDescriptor;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getBlocksNumber() {
        return blocksNumber;
    }

    public boolean getIsBytesOrderReversed() {
        return isBytesOrderReversed;
    }

    public boolean getIsFloatWordOrdered() {
        return isFloatWordOrdered;
    }

    public long getAlignement() {
        return alignement;
    }

    public long getBufferHeaderSize() {
        return bufferHeaderSize;
    }

    public int getBitsOfCurrentTimestampCounter() {
        return bitsOfCurrentTimestampCounter;
    }

    public int getBitsOfEvent() {
        return bitsOfEvent;
    }

    public long getCurrentTimestampCounterMask() {
        return currentTimestampCounterMask;
    }

    public long getCurrentTimestampCounterMaskNextBit() {
        return currentTimestampCounterMaskNextBit;
    }

    public long getEventsLost() {
        return eventsLost;
    }

    public long getSubBufferCorrupt() {
        return subBufferCorrupt;
    }

    public JniEvent getCurrentEvent() {
        return currentEvent;
    }

    public long getBufferPtr() {
        return bufferPtr;
    }

    public long getBufferSize() {
        return bufferSize;
    }

    public HashMap<Integer, JniMarker> getTracefileMarkersMap() {
        return tracefileMarkersMap;
    }

    /**
     * Getter to the parent trace for this tracefile.
     *
     * 
     * @return  the parent trace
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public JniTrace getParentTrace() {
        return parentTrace;
    }
    
    /**
     * Pointer to the LttTracefile C structure<br>
     * <br>
     * The pointer should only be used INTERNALY, do not use these unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL
     */
    public C_Pointer getTracefilePtr() {
        return thisTracefilePtr;
    }

    /**
     * toString() method. <u>Intended to debug</u><br>
     * 
     * @return String Attributes of the object concatenated in String
     */
    public String toString() {
        String returnData = "";
                
        returnData += "isCpuOnline                        : " + isCpuOnline + "\n";
        returnData += "tracefilePath                      : " + tracefilePath + "\n";
        returnData += "tracefileName                      : " + tracefileName + "\n";
        returnData += "cpuNumber                          : " + cpuNumber + "\n";
        returnData += "tid                                : " + tid + "\n";
        returnData += "pgid                               : " + pgid + "\n";
        returnData += "creation                           : " + creation + "\n";
        returnData += "tracePtr                           : 0x" + java.lang.Long.toHexString(tracePtr) + "\n";
        returnData += "markerDataPtr                      : 0x" + java.lang.Long.toHexString(markerDataPtr) + "\n";
        returnData += "CFileDescriptor                    : " + CFileDescriptor + "\n";
        returnData += "fileSize                           : " + fileSize + "\n";
        returnData += "blocksNumber                       : " + blocksNumber + "\n";
        returnData += "isBytesOrderReversed               : " + isBytesOrderReversed + "\n";
        returnData += "isFloatWordOrdered                 : " + isFloatWordOrdered + "\n";
        returnData += "alignement                         : " + alignement + "\n";
        returnData += "bufferHeaderSize                   : " + bufferHeaderSize + "\n";
        returnData += "bitsOfCurrentTimestampCounter      : " + bitsOfCurrentTimestampCounter + "\n";
        returnData += "bitsOfEvent                        : " + bitsOfEvent + "\n";
        returnData += "currentTimestampCounterMask        : " + currentTimestampCounterMask + "\n";
        returnData += "currentTimestampCounterMaskNextBit : " + currentTimestampCounterMaskNextBit + "\n";
        returnData += "eventsLost                         : " + eventsLost + "\n";
        returnData += "subBufferCorrupt                   : " + subBufferCorrupt + "\n";
        returnData += "currentEvent                       : " + currentEvent.getReferenceToString() + "\n"; // Hack to avoid ending up with event.toString()
        returnData += "bufferPtr                          : 0x" + java.lang.Long.toHexString(bufferPtr) + "\n";
        returnData += "bufferSize                         : " + bufferSize + "\n";
        returnData += "tracefileMarkersMap                : " + tracefileMarkersMap.keySet() + "\n"; // Hack to avoid ending up with tracefileMarkersMap.toString()

        return returnData;
    }

    /**
     * Print information for this tracefile. <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the
     * one from the C structure<br>
     * <br>
     * This function will not throw but will complain loudly if pointer is NULL
     */
    public void printTracefileInformation() {

        // If null pointer, print a warning!
        if (thisTracefilePtr.getPointer() == NULL) {
            printlnC("Pointer is NULL, cannot print. (printTracefileInformation)");
        } else {
            ltt_printTracefile( thisTracefilePtr.getPointer() );
        }
    }
}

