/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfEvent</u></b>
 * <p>
 * The basic event structure in the TMF.
 * <p>
 * In its canonical form, an event has:
 * <ul>
 * <li> a normalized timestamp
 * <li> a source (reporter)
 * <li> a type
 * <li> a content
 * </ul>
 * For convenience, a free-form reference field is also provided. It could be
 * used as e.g. a location marker in the event stream to distinguish between
 * otherwise identical events.
 */
public class TmfEvent {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final TmfTimestamp fTimestamp;
	private final TmfEventSource fSource;
	private final TmfEventType fType;
	private final TmfEventContent fContent;
	private final TmfEventReference fReference;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * @param timestamp
	 * @param source
	 * @param type
	 * @param content
	 * @param reference
	 */
	public TmfEvent(TmfTimestamp timestamp, TmfEventSource source, TmfEventType type,
			TmfEventContent content, TmfEventReference reference)
	{
		fTimestamp = timestamp;
		fSource = source;
		fType = type;
		fContent = content;
		fReference = reference;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public TmfTimestamp getTimestamp() {
		return fTimestamp;
	}

	/**
	 * @return
	 */
	public TmfEventSource getSource() {
		return fSource;
	}

	/**
	 * @return
	 */
	public TmfEventType getType() {
		return fType;
	}

	/**
	 * @return
	 */
	public TmfEventContent getContent() {
		return fContent;
	}

	/**
	 * @return
	 */
	public TmfEventReference getReference() {
		return fReference;
	}
}
