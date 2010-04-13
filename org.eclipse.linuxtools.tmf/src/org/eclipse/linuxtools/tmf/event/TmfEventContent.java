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

package org.eclipse.linuxtools.tmf.event;


/**
 * <b><u>TmfEventContent</u></b>
 * <p>
 * The event content.
 */
public class TmfEventContent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	protected TmfEvent fParentEvent;
	protected Object   fRawContent;
	protected Object[] fFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * @param parent the parent event (owner)
	 * @param content the raw content
	 */
	public TmfEventContent(TmfEvent parent, Object content) {
		fParentEvent = parent;
		fRawContent  = content;
	}

    /**
     * @param other the original event content
     */
    public TmfEventContent(TmfEventContent other) {
    	if (other == null)
    		throw new IllegalArgumentException();
    	fParentEvent = other.fParentEvent;
		fRawContent  = other.fRawContent;
		fFields      = other.fFields;
    }

    @SuppressWarnings("unused")
	private TmfEventContent() {
		throw new AssertionError();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	/**
	 * @return the parent (containing) event
	 */
	public TmfEvent getEvent() {
		return fParentEvent;
	}

	/**
	 * @return the event type
	 */
	public TmfEventType getType() {
		return fParentEvent.getType();
	}

	/**
	 * @return the raw content
	 */
	public Object getContent() {
		return fRawContent;
	}

	/**
	 * Returns the list of fields in the same order as TmfEventType.getLabels()
	 * 
	 * @return the ordered set of fields (optional fields might be null)
	 */
	public Object[] getFields() {
		if (fFields == null) {
			parseContent();
		}
		return fFields;
	}

	/**
	 * @param id the field id
	 * @return the corresponding field
	 * @throws TmfNoSuchFieldException
	 */
	public Object getField(String id) throws TmfNoSuchFieldException {
		if (fFields == null) {
			parseContent();
		}
		return fFields[getType().getFieldIndex(id)];
	}

	/**
	 * @param n the field index as per TmfEventType.getLabels()
	 * @return the corresponding field (null if non-existing)
	 */
	public Object getField(int n) {
		if (fFields == null) {
			parseContent();
		}
		if (n >= 0 && n < fFields.length)
			return fFields[n];

		return null;
	}

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

	/**
	 * Parse the content into fields. By default, a single field (the raw
	 * content) is returned. 
	 * Should be overridden.
	 */
	protected void parseContent() {
		fFields = new Object[1];
		fFields[0] = fRawContent;
	}
	
    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

	@Override
    public int hashCode() {
		int result = 17;
		result = 37 * result + ((fParentEvent != null) ? fParentEvent.hashCode() : 0);
		result = 37 * result + ((fRawContent  != null) ? fRawContent.hashCode()  : 0);
        return result;
    }

	@Override
    public boolean equals(Object other) {
		if (!(other instanceof TmfEventContent))
			return false;
		TmfEventContent o = (TmfEventContent) other;
        return fRawContent.equals(o.fRawContent);
    }

    @Override
	public String toString() {
    	Object[] fields = getFields();
    	StringBuilder result = new StringBuilder("[TmfEventContent(");
    	for (int i = 0; i < fields.length; i++) {
    		if (i > 0) result.append(",");
    		result.append(fields[i]);
    	}
    	result.append(")]");
    	return result.toString();
    }

}
