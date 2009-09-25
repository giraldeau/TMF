/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.model.trange;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;

public class TimeRangeComposite extends TimeRangeComponent implements
ITmfTimeAnalysisEntry {

	// ========================================================================
	// Data
	// =======================================================================
	/**
	 * Type of Composites or Containers
	 * <p>
	 * PROPERTY: Refers to a sub-composite of a RESOURCE or a PROCESS e.g the
	 * cpu which can vary over time and can have time range events associated to
	 * it, and at the same time PROPERTY is associated to a Composite parent
	 * like a PROCESS
	 * </p>
	 * <p>
	 * PROCESS: A composite of time range events representing a Process
	 * </p>
	 * <p>
	 * RESOURCE: A composite of time range events representing a resource i.g.
	 * irq, softIrq, trap, bdev, cpu
	 * </p>
	 * 
	 * @author alvaro
	 * 
	 */
	public static enum CompositeType {
		UNKNOWN, PROPERTY, PROCESS, RESOURCE
	}

	protected final Vector<TimeRangeComponent> ChildEventLeafs = new Vector<TimeRangeComponent>();
	protected final Vector<TimeRangeComponent> ChildEventComposites = new Vector<TimeRangeComponent>();
	protected Integer id = 0;
	protected String name;
	protected String groupName = "";
	protected String className = "";
	protected CompositeType contType = CompositeType.UNKNOWN;

	// ========================================================================
	// Constructors
	// =======================================================================
	public TimeRangeComposite(Integer id, Long stime, Long etime, String name, CompositeType type) {
		super(stime, etime, null);
		this.id = id;
		this.name = name;
		contType = type;
	}

	public TimeRangeComposite(Integer id, Long stime, Long etime, String name, String groupName, String className, CompositeType type) {
		this(id, stime, etime, name, type);

        this.groupName = groupName;
        this.className = className;

    }
	
	// ========================================================================
	// Methods
	// =======================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeComponent#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry#getGroupName()
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry#getId()
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry#getTraceEvents()
	 */
	@SuppressWarnings("unchecked")
	public Vector<TimeRangeComponent> getTraceEvents() {
		return ChildEventLeafs;
	}

	/**
	 * @return
	 */
	public Vector<TimeRangeComponent> getChildEventComposites() {
		return ChildEventComposites;
	}

}
