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

package org.eclipse.linuxtools.tmf.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <b><u>AllTmfCoreTests</u></b>
 * <p>
 * Master test suite for TMF Core.
 */
public class AllTmfCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTmfCoreTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(TmfCorePluginTest.class);
		suite.addTest(org.eclipse.linuxtools.tmf.tests.event.AllTests.suite());
		suite.addTest(org.eclipse.linuxtools.tmf.tests.request.AllTests.suite());
		suite.addTest(org.eclipse.linuxtools.tmf.tests.component.AllTests.suite());
		suite.addTest(org.eclipse.linuxtools.tmf.tests.trace.AllTests.suite());
//		suite.addTest(org.eclipse.linuxtools.tmf.tests.experiment.AllTests.suite());
		//$JUnit-END$
		return suite;
	}

}
