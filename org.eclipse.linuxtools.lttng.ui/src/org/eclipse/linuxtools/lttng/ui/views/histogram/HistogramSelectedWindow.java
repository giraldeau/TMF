/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.histogram;

public class HistogramSelectedWindow {
	
	private int windowCenterXPosition = 0;
	private long windowTimeWidth = 0;
	
	private boolean isSelectedWindowVisible = false;
	
	private HistogramContent histogramContent = null;

	public HistogramSelectedWindow(HistogramContent newTraceContent) {
		histogramContent = newTraceContent;
	}
	
	public HistogramSelectedWindow(HistogramContent newTraceContent, int centralPosition, long newWindowWidth) {
		histogramContent = newTraceContent;
		windowCenterXPosition = centralPosition;
		windowTimeWidth = newWindowWidth;
	}
	

	public boolean getSelectedWindowVisible() {
		return isSelectedWindowVisible;
	}
	
	public void setSelectedWindowVisible(boolean newIsSelectedWindowVisible) {
		this.isSelectedWindowVisible = newIsSelectedWindowVisible;
	}

	
	public long getWindowTimeWidth() {
		return windowTimeWidth;
	}
	
	public void setWindowTimeWidth(long newWindowTimeWidth) {
		this.windowTimeWidth = newWindowTimeWidth;
	}
	

	public HistogramContent getTraceContent() {
		return histogramContent;
	}
	
	public void setTraceContent(HistogramContent newTraceContent) {
		this.histogramContent = newTraceContent;
	}
	
	
	public int getWindowCenterXPosition() {
		return windowCenterXPosition;
	}
	
	public void setWindowCenterXPosition(int newPosCenter) {
		this.windowCenterXPosition = newPosCenter;
	}
	
	
	public int getWindowPositionLeft() {
		return histogramContent.getXPositionByPositionAndTimeInterval(windowCenterXPosition, -(windowTimeWidth / 2) );
	}
	
	public int getWindowPositionRight() {
		return histogramContent.getXPositionByPositionAndTimeInterval(windowCenterXPosition, +(windowTimeWidth / 2) );
	}
	
	public long getTimestampLeft() {
		return histogramContent.getElementFromXPosition( getWindowPositionLeft() ).firstIntervalTimestamp;
	}
	
	public long getTimestampRight() {
		return histogramContent.getElementFromXPosition( getWindowPositionRight() ).firstIntervalTimestamp;
	}
	
}
