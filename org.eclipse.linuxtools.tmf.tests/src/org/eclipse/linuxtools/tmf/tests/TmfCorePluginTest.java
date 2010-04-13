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

import org.eclipse.linuxtools.tmf.TmfCorePlugin;

import junit.framework.TestCase;

/**
 * <b><u>TmfCorePluginTest</u></b>
 * <p>
 * Test the TMF core plug-in activator
 */
public class TmfCorePluginTest extends TestCase {

	// ------------------------------------------------------------------------
    // Attributes
	// ------------------------------------------------------------------------

	// Plug-in instantiation
	static final TmfCorePlugin fPlugin = new TmfCorePlugin();
	
	// ------------------------------------------------------------------------
    // Housekeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfCorePluginTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ------------------------------------------------------------------------
    // Test cases
	// ------------------------------------------------------------------------

	public void testTmfCorePluginId() throws Exception {
		assertEquals("Plugin ID", "org.eclipse.linuxtools.tmf", TmfCorePlugin.PLUGIN_ID);
	}

	public void testGetDefault() throws Exception {
		TmfCorePlugin plugin = TmfCorePlugin.getDefault();
		assertEquals("getDefault()", plugin, fPlugin);
	}

}
