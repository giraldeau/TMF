package org.eclipse.linuxtools.lttng.jni_v2_3;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;

public class JniTracefile_v2_3 extends JniTracefile {
	
    protected JniTracefile_v2_3() {
    	super();
    }
    
    public JniTracefile_v2_3(JniTracefile_v2_3 oldTracefile) {
    	super(oldTracefile);
    }
    
    public JniTracefile_v2_3(Jni_C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
    	super(newPtr, newParentTrace);
    }
	
    
    public JniEvent allocateNewJniEvent(Jni_C_Pointer newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {
    	return new JniEvent_v2_3(newEventPtr, newMarkersMap, newParentTracefile);
    }
    
    
    public JniMarker allocateNewJniMarker(Jni_C_Pointer newMarkerPtr) throws JniException {
    	return new JniMarker_v2_3(newMarkerPtr);
    }
    
}
