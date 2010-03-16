package org.eclipse.linuxtools.lttng.trace;
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

import org.eclipse.linuxtools.lttng.LttngException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTraceVersionException;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceVersion;

/**
 * <b><u>LTTngTraceVersion</u></b><p>
 * 
 * This class is responsible of handling the version number of a trace.<p>
 * It will return the correct version number and validity information of a trace given its path.<br>
 * 
 */
public class LTTngTraceVersion {
	
	private String tracepath = null;
	private JniTraceVersion traceVersion = new JniTraceVersion();
	
	/*
	 * Default constructor is forbidden
	 */
	@SuppressWarnings("unused")
	private LTTngTraceVersion() {
		// Default constructor forbidden
	}
	
	/**
	 * Default constructor, takes a tracepath as parameter. 
	 * 
	 * @param newPath			(Valid) path to a LTTng trace <b>directory</b>. 
	 * 
	 * @throws LttngException	Throwed if something go wrong (bad tracepath or the C library could not be loaded).
	 */
	public LTTngTraceVersion(String newPath) throws LttngException {
		tracepath = newPath;
		
		// Fill the new traceversion object
		fillJniTraceVersion(tracepath);
	}
	
	/*
	 * Fill (load version numbers) into the JniTraceVersion object.<p>
	 * This need to be done each time the tracepath is changed.  
	 * 
	 * @param newTracepath		(Valid) path to a LTTng trace <b>directory</b>. 
	 * 
	 * @throws LttngException	If something go wrong (bad tracepath or the C library could not be loaded).
	 * 
	 * @see org.eclipse.linuxtools.lttng.jni.factory.JniTraceVersion
	 */
	private void fillJniTraceVersion(String newTracepath) throws LttngException {
		try {
			traceVersion.readVersionFromTrace(newTracepath);
		}
		catch (JniTraceVersionException e) {
			throw new LttngException("Could not get trace version!\nReturned error was : " + e.toString() + " (fillJniTraceVersion)");
		}
	}
	
	/**
	 * Get for the full version number as String
	 * 
	 * @return version number as String
	 */
	public String getTraceVersionString() {
		return traceVersion.getVersionAsString();
	}
	
	/**
	 * Get for the major version number
	 * 
	 * @return major version number as int
	 */
	public int getTraceMinorVersion() {
		return traceVersion.getMinor();
	}
	
	/**
	 * Get for the minor version number
	 * 
	 * @return minor version number as int
	 */
	public int getTraceMajorVersion() {
		return traceVersion.getMajor();
	}
	
	/**
	 * Get for the full version number as float
	 * 
	 * @return version number as float
	 */
	public float getTraceFloatVersion() {
		return traceVersion.getVersionAsFloat();
	}
	
	/**
	 * Verify is the currently loaded path was a valid LTTng tracepath.<p>
	 * 
	 * Internally, the version number will be checked, any number <= 0 is expected to be wrong.
	 * 
	 * @return	A boolean saying if the tracepath appears to be valid or not.
	 */
	public boolean isValidLttngTrace() {
		if ( traceVersion.getVersionAsFloat() > 0 ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Get for the currently loaded tracepath
	 * 
	 * @return	the tracepath currently in use
	 */
	public String getTracepath() {
		return tracepath;
	}
	
	/**
	 * Set a new tracepath<p>
	 * 
	 * Note : Setting this will load the new version information into memory.<br>
	 * Errors will be catched but a warning will be printed if something go wrong.
	 * 
	 * @param newTracepath	The new tracepath to set.
	 */
	public void setTracepath(String newTracepath) {
		try {
			fillJniTraceVersion(newTracepath);
			tracepath = newTracepath;
		}
		catch (LttngException e) {
			System.out.println("Could not get the trace version from the given path." +
							   "Please check that the given path is a valid LTTng trace. (getTracepath)");
		}
	}
	
	@Override
	public String toString() {
		return "LTTngTraceVersion : [" + getTraceFloatVersion() + "]";
	}
	
}
