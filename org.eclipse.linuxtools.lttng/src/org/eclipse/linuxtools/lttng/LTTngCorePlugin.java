/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * <b><u>LTTngCorePlugin</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
public class LTTngCorePlugin extends Plugin {

    // ========================================================================
    // Attributes
    // ========================================================================

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.lttng";

	// The shared instance
	private static LTTngCorePlugin plugin;
	
    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * The constructor
	 */
	public LTTngCorePlugin() {
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LTTngCorePlugin getDefault() {
		return plugin;
	}

    // ========================================================================
    // Operators
    // ========================================================================

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		LttngFactory.init();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
