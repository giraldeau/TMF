package org.eclipse.linuxtools.tmf.tests.trace;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.linuxtools.tmf.TmfCorePlugin;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for " + TmfCorePlugin.PLUGIN_ID + ".trace"); //$NON-NLS-1$);
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfLocationTest.class);
		suite.addTestSuite(TmfCheckpointTest.class);
		suite.addTestSuite(TmfContextTest.class);
		suite.addTestSuite(TmfTraceTest.class);
		//$JUnit-END$
		return suite;
	}

}