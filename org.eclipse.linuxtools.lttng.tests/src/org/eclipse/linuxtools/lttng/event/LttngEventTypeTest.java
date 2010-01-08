package org.eclipse.linuxtools.lttng.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

/*
 Functions tested here :
    public LttngEventType(String thisChannelName, long thisCpuId, String thisMarkerName, LttngEventFormat thisFormat) 
    public LttngEventType(LttngEventType oldType) 
    public String getChannelName() 
    public long getCpuId() 
    public String getMarkerName() 
    public String toString() 
 */

public class LttngEventTypeTest extends TestCase {
	private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
    private final static String firstEventChannel       = "metadata";
    private final static long firstEventCpu             = 0;
    private final static String firstEventMarker        = "core_marker_id";
    
    private LTTngTextTrace initializeEventStream() {
        LTTngTextTrace tmpStream = null;
        try {
            tmpStream = new LTTngTextTrace(tracepath1, skipIndexing);
        } 
        catch (Exception e) {
            fail("ERROR : Could not open " + tracepath1 + ". Test failed!" );
        }
        
        return tmpStream;
    }
    
    
    private LttngEventType prepareToTest() {
        LttngEventType tmpEventType = null;

        // This trace should be valid
        try {
            LTTngTextTrace tmpStream = initializeEventStream();
            tmpEventType = (LttngEventType)tmpStream.getNextEvent( new TmfTraceContext(null, null, 0) ).getType();
        } 
        catch (Exception e) {
            fail("ERROR : Failed to get reference!");
        }

        return tmpEventType;
    }
    
    public void testConstructors() {
        LttngEventType tmpEventType = null;
        @SuppressWarnings("unused")
        LttngEventType tmpEventType2 = null;
        
        // Default construction with good argument
        try {
            tmpEventType = new LttngEventType("test", 0L, "test",  new String[1]);
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
        
        // Copy constructor
        try {
            tmpEventType = new LttngEventType("test", 0L, "test", new String[1]);
            tmpEventType2 = new LttngEventType(tmpEventType);
        }
        catch( Exception e) { 
            fail("Construction failed!");
        }
    }
    
    
    public void testGetter() {
        LttngEventType tmpEventType = prepareToTest();
        
        assertTrue("Channel name not what was expected!",firstEventChannel.equals((String)tmpEventType.getTracefileName()) );
        assertTrue("Cpu Id not what was expected!",firstEventCpu == tmpEventType.getCpuId() );
        assertTrue("Marker Name not what was expected!",firstEventMarker.equals((String)tmpEventType.getMarkerName()) );
        // Just test the non-nullity of labels
        assertNotSame("getFormat returned null",null, tmpEventType.getLabels() );
    }
    
    public void testToString() {
        LttngEventType tmpEventType = prepareToTest();
        
        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, tmpEventType.toString() );
        assertNotSame("toString is not overridded!", tmpEventType.getClass().getName() + '@' + Integer.toHexString(tmpEventType.hashCode()), tmpEventType.toString() );
    }
    
}
