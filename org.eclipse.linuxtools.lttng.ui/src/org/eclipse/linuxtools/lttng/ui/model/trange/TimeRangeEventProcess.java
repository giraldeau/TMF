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

public class TimeRangeEventProcess extends TimeRangeComposite implements
		Comparable<TimeRangeEventProcess> {
	// ========================================================================
	// Data
	// =======================================================================
	// GUI information
	private Long pid = 0L;
	private Long tgid = 0L;
	private Long ppid = 0L;
	private Long creationTime = 0L;
	private String traceID = "";
	private String processType = "User"; // Kernel or user thread
	private Long cpu = 0L;
	private String brand = "";

	// ========================================================================
	// Constructor
	// =======================================================================
	/**
	 * @param id
	 * @param name
	 * @param sTime
	 *            normally set to the Trace start time
	 * @param stopTime
	 *            normally set to the Trace end time
	 * @param groupName
	 * @param className
	 */
	public TimeRangeEventProcess(int id, String name, long startTime,
			long stopTime, String groupName, String className, Long cpu) {

		super(id, startTime, stopTime, name, CompositeType.PROCESS);
		this.cpu = cpu;
	}

	// ========================================================================
	// Methods
	// =======================================================================
	/**
	 * @return
	 */
	public Long getPid() {
		return pid;
	}

	/**
	 * @param pid
	 */
	public void setPid(Long pid) {
		this.pid = pid;
	}

	/**
	 * @return
	 */
	public Long getTgid() {
		return tgid;
	}

	/**
	 * @param tgid
	 */
	public void setTgid(Long tgid) {
		this.tgid = tgid;
	}

	/**
	 * @return
	 */
	public Long getPpid() {
		return ppid;
	}

	/**
	 * @param ppid
	 */
	public void setPpid(Long ppid) {
		this.ppid = ppid;
	}

	/**
	 * @return
	 */
	public Long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime
	 */
	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	/**
	 * @return
	 */
	public String getTraceID() {
		return traceID;
	}

	/**
	 * @param traceID
	 */
	public void setTraceID(String traceID) {
		if (traceID != null) {
			this.traceID = traceID;
		} else {
			this.traceID = "";
		}
	}

	/**
	 * @return
	 */
	public String getProcessType() {
		return processType;
	}

	/**
	 * @param processType
	 */
	public void setProcessType(String processType) {
		if (processType != null) {
			this.processType = processType;
		}
	}

	/**
	 * @return
	 */
	public Long getCpu() {
		return cpu;
	}

	/**
	 * @param cpu
	 */
	public void setCpu(Long cpu) {
		if (cpu != null) {
			this.cpu = cpu;
		} else {
			cpu = 0L;
		}
	}

	/**
	 * @return
	 */
	public String getBrand() {
        return brand;
    }

	/**
	 * @param brand
	 */
    public void setBrand(String brand) {
        if (brand != null) {
            this.brand = brand;
        } else {
            brand = "";
        }
    }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TimeRangeEventProcess process) {
		if (process != null) {
			int result = 0;
			// Compare by trace first
			String anotherTraceId = process.getTraceID();
			result = traceID.compareTo(anotherTraceId);
			if (result != 0) {
				return result;
			}

			// Then by CPU
			Long anotherCpu = process.getCpu();
			result = cpu.compareTo(anotherCpu);
			if (result != 0) {
				return result;
			}

			// finally Compare by pid
			Long anotherPid = process.getPid();
			return pid.compareTo(anotherPid);
		}

		return 0;
	}
}
