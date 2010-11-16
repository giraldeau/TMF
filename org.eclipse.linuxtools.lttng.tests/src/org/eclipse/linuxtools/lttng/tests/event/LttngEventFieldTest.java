package org.eclipse.linuxtools.lttng.tests.event;



import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.tests.LTTngCoreTestPlugin;
import org.eclipse.linuxtools.lttng.trace.LTTngTextTrace;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfLocation;

/*
 Functions tested here :
        public LttngEventField(String name, Object newContent) 
        public LttngEventField(LttngEventField oldField) 
        public String getName() 
        public String toString() 

 */
@SuppressWarnings("nls")
public class LttngEventFieldTest extends TestCase {
    private final static String tracepath1="traceset/trace-15316events_nolost_newformat.txt";
    private final static boolean skipIndexing=true;
    
//    private final static String firstEventName 		= "alignment";
    private final static String firstEventValue 	= "0";
    
    private static LTTngTextTrace testStream = null;
    private LTTngTextTrace initializeEventStream() {
		if (testStream == null) {
			try {
				URL location = FileLocator.find(LTTngCoreTestPlugin.getPlugin().getBundle(), new Path(tracepath1), null);
				File testfile = new File(FileLocator.toFileURL(location).toURI());
				LTTngTextTrace tmpStream = new LTTngTextTrace(testfile.getPath(), skipIndexing);
				testStream = tmpStream;
			} 
			catch (Exception e) {
				System.out.println("ERROR : Could not open " + tracepath1);
				testStream = null;
			}
		}
		else {
			testStream.seekEvent(0);
		}
		
		return testStream;
	}
    
	private LttngEventField prepareToTest() {
		LttngEventField tmpField = null;

		// This trace should be valid
		try {
			LTTngTextTrace tmpStream = initializeEventStream();
			tmpField = (LttngEventField)tmpStream.getNextEvent( new TmfContext(new TmfLocation<Long>(0L), 0) ).getContent().getField(0);
		} 
		catch (Exception e) {
			fail("ERROR : Failed to get field!");
		}

		return tmpField;
	}

	public void testConstructors() {
		LttngEventContent testContent = null;
		LttngEventField testField 	= null;
		@SuppressWarnings("unused")
		LttngEventField testField2 	= null;
        
	    // Default construction with good argument
        try {
        	testField = new LttngEventField(testContent, "test", "test");
        }
        catch( Exception e) { 
        	fail("Default construction failed!");
        }
        
        // Copy constructor with correct parameters
        try {
        	testField = new LttngEventField(testContent, "test", "test");
        	testField2 = new LttngEventField(testField);
        }
        catch( Exception e) { 
        	fail("Copy constructor failed!");
        }
        
	}
	
	public void testGetter() {
    	
    	// *** To "really" test the field, we will get a real field from LTTngTrace
    	LTTngTextTrace tmpStream = initializeEventStream();
    	
    	LttngEventField testField 	= (LttngEventField)tmpStream.getNextEvent( new TmfContext(new TmfLocation<Long>(0L), 0) ).getContent().getField(0);
    	assertNotSame("getField is null!",null,testField);
    	
    	// *** FIXME ***
    	// Depending from the Java version because of the "hashcode()" on String. 
    	// We can't really test that safetly
    	//
    	//assertTrue("getName() returned unexpected result!",firstEventName.equals(testField.getId().toString()));
    	assertNotSame("getName() returned unexpected result!",null, testField.getId());
    	
    	assertTrue("getValue() returned unexpected result!",firstEventValue.equals(testField.getValue().toString()));
    	
    	
    }
    
	public void testToString() {
    	LttngEventField tmpField = prepareToTest();
    	
		// Just make sure toString() does not return null or the java reference
		assertNotSame("toString returned null",null, tmpField.toString() );
		assertNotSame("toString is not overridded!", tmpField.getClass().getName() + '@' + Integer.toHexString(tmpField.hashCode()), tmpField.toString() );
    }
	
}
