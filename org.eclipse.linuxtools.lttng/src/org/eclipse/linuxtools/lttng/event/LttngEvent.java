package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

/**
 * <b><u>LttngEvent</u></b><p>
 * 
 * Lttng specific TmfEvent implementation.<p>
 * 
 * The main difference from the basic Tmf implementation is that we keep an internal reference to the JniEvent<br>
 * The conversion from this LttngEvent to the JniEvent is then possible.
 */
public class LttngEvent extends TmfEvent {
    
    // Reference to the JNI JniEvent. Should only be used INTERNALLY
    private JniEvent jniEventReference = null;
    
    // Reference to the parent trace that own this event
    private TmfTrace<LttngEvent> parentTrace = null;
    
    /**
     * Constructor with parameters.<p>
     * 
     * @param timestamp  The timestamp of this event   
     * @param source     The source of this event
     * @param type       The type of this event
     * @param content    The content of this event
     * @param reference  The reference of this event
     * @param lttEvent   A reference to a valid JniEvent object
     * 
     * @see org.eclipse.linuxtools.tmf.event.TmfTimestamp
     * @see org.eclipse.linuxtools.tmf.event.TmfEventSource
     * @see org.eclipse.linuxtools.lttng.event.LttngEventType
     * @see org.eclipse.linuxtools.lttng.event.LttngEventContent
     * @see org.eclipse.linuxtools.lttng.event.LttngEventReference
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public LttngEvent(TmfTrace<LttngEvent> parent, LttngTimestamp timestamp, TmfEventSource source, LttngEventType type, LttngEventContent content, LttngEventReference reference, JniEvent lttEvent) { 
        super(timestamp, source, type, reference);
        
        fContent = content;
        jniEventReference = lttEvent;
        setParentTrace(parent);
    }
    
    /**
     * Copy constructor.<p>
     * 
     * @param oldEvent		Event we want to copy from.
     */
    public LttngEvent(LttngEvent oldEvent) {
        this(	
        		oldEvent.getParentTrace(),
        		(LttngTimestamp)oldEvent.getTimestamp(), 
        		(TmfEventSource)oldEvent.getSource(), 
        		(LttngEventType)oldEvent.getType(), 
        		(LttngEventContent)oldEvent.getContent(), 
        		(LttngEventReference)oldEvent.getReference(), 
        		oldEvent.jniEventReference
        	);
    }
    
    
    /**
     * Return the parent trace asoociated with this event
     * 
     * @return Parent trace
     */
    public TmfTrace<LttngEvent> getParentTrace() {
		return parentTrace;
	}
    
    /**
     * Set a new parent trace for this event
     * 
     * @param parentTrace	The new parent
     */
    public void setParentTrace(TmfTrace<LttngEvent> parentTrace) {
		this.parentTrace = parentTrace;
	}
    
    
	/**
     * Return the channel name of this event.<p>
     * 
     * @return Channel (tracefile) for this event
     */
    public String getChannelName() {
        return ( (LttngEventType)this.getType() ).getTracefileName();
    }
    
    /**
     * Cpu id number of this event.<p>
     * 
     * @return CpuId
     */
    public long getCpuId() {
        return ( (LttngEventType)this.getType() ).getCpuId();
    }
    
    /**
     * Marker name of this event.<p>
     * 
     * @return Marker name
     */
    public String getMarkerName() {
        return ( (LttngEventType)this.getType() ).getMarkerName();
    }
    
    @Override
    public LttngEventContent getContent() {
        return (LttngEventContent)fContent;
    }
    
    public void setContent(LttngEventContent newContent) {
        fContent = newContent;
    }
    
    @Override
    public LttngEventType getType() {
        return (LttngEventType)fType;
    }
    
    public void setType(LttngEventType newType) {
        fType = newType;
    }
    
    /**
     * Set a new JniReference for this event.<p>
     * 
     * Note : Reference is used to get back to the Jni during event parsing and need to be consistent.
     * 
     * @param newJniEventReference	New reference
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public synchronized void updateJniEventReference(JniEvent newJniEventReference) {
        this.jniEventReference = newJniEventReference;
    }
    
    /**
     * Convert this event into a Jni JniEvent.<p>
     * 
     * Note : Some verifications are done to make sure the event is still valid on 
     * the Jni side before conversion.<br> If it is not the case, null will be returned.
     * 
     * @return The converted JniEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public synchronized JniEvent convertEventTmfToJni() {
        JniEvent tmpEvent = null;
        
        // ***TODO***
        // Should we remove the check to save some time??
        
        // We don't want to send away events that are outdated as their informations could be invalid
        //  If the timestamp between the event and the trace are not coherent we will not perform the conversion
        if ( jniEventReference.getParentTracefile().getParentTrace().getCurrentEventTimestamp().getTime() == getTimestamp().getValue() ) {
            tmpEvent = jniEventReference;
        }
        else {
            System.out.println("convertEventTmfToJni() failed: Unsynced Timestamp > TMF:" + getTimestamp().getValue() + " <--> JNI:" + jniEventReference.getParentTracefile().getParentTrace().getCurrentEventTimestamp().getTime());
        }
        return tmpEvent;
    }
    
    @Override
	public String toString() {
    	StringBuffer result= new StringBuffer("[LttngEvent(");
    	result.append("Timestamp:" + getTimestamp().getValue());
    	result.append(",Channel:"  + getChannelName());
    	result.append(",CPU:"      + getCpuId());
    	result.append(",Marker:"   + getMarkerName());
    	result.append(",Content:"  + getContent() + ")]");
    	
    	return result.toString();
    }
}
