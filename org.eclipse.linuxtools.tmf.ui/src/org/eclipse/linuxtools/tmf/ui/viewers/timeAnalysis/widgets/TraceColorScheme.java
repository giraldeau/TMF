/*****************************************************************************
 * Copyright (c) 2008 Intel Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    Ruslan A. Scherbakov, Intel - Initial API and implementation
 *    Alvaro Sanchex-Leon - Udpated for TMF
 *
 * $Id: TraceColorScheme.java,v 1.3 2008/05/09 16:11:24 jkubasta Exp $ 
 *****************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.widgets;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeAnalysisProvider.StateColor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;


public class TraceColorScheme {

	// elements color indices
	static public final int BLACK_STATE = 0;
	static public final int GREEN_STATE = 1;
	static public final int DARK_BLUE_STATE = 2;
	static public final int ORANGE_STATE = 3;
	static public final int GOLD_STATE = 4;
	static public final int RED_STATE = 5;
	static public final int GRAY_STATE = 6;
	static public final int DARK_GREEN_STATE = 7;
	static public final int DARK_YELLOW_STATE = 8;
	static public final int MAGENTA3_STATE = 9;
	static public final int PURPLE1_STATE = 10;
	static public final int PINK1_STATE = 11;
	static public final int AQUAMARINE_STATE = 12;
	static public final int LIGHT_BLUE_STATE = 13;
	static public final int CADET_BLUE_STATE = 14;
	static public final int OLIVE_STATE = 15;
	
	static public final int STATES0 = 0;
	static public final int STATES1 = 15;
	
	// State element index to name mapping, must keep the same order as above
	public static final StateColor stateColors[] = { StateColor.BLACK,
			StateColor.GREEN, StateColor.DARK_BLUE, StateColor.ORANGE,
			StateColor.GOLD, StateColor.RED, StateColor.GRAY, StateColor.DARK_GREEN, StateColor.DARK_YELLOW, StateColor.MAGENTA3, StateColor.PURPLE1, 
			StateColor.PINK1, StateColor.AQUAMARINE, StateColor.LIGHT_BLUE, StateColor.CADET_BLUE, StateColor.OLIVE
			};

	// selected state elements color indices
	static public final int BLACK_STATE_SEL = 16;
	static public final int GREEN_STATE_SEL = 17;
	static public final int DARK_BLUE_STATE_SEL = 18;
	static public final int ORANGE_STATE_SEL = 19;
	static public final int GOLD_STATE_SEL = 20;
	static public final int RED_STATE_SEL = 21;
	static public final int GRAY_STATE_SEL = 22;
	static public final int DARK_GREEN_STATE_SEL = 23;
	static public final int DARK_YELLOW_STATE_SEL = 24;
	static public final int MAGENTA3_STATE_SEL = 25;
	static public final int PURPLE1_STATE_SEL = 26;
	static public final int PINK1_STATE_SEL = 27;
	static public final int AQUAMARINE_STATE_SEL = 28;
	static public final int LIGHT_BLUE_STATE_SEL = 29;
	static public final int CADET_BLUE_STATE_SEL = 30;
	static public final int OLIVE_STATE_SEL = 31;
	
	static public final int STATES_SEL0 = 16;
	static public final int STATES_SEL1 = 31;

	// colors indices for viewer controls
	static public final int BACKGROUND = 32;
	static public final int FOREGROUND = 33;
	static public final int BACKGROUND_SEL = 34;
	static public final int FOREGROUND_SEL = 35;
	static public final int BACKGROUND_SEL_NOFOCUS = 36;
	static public final int FOREGROUND_SEL_NOFOCUS = 37;
	static public final int TOOL_BACKGROUND = 38;
	static public final int TOOL_FOREGROUND = 39;

	// misc colors
	static public final int FIX_COLOR = 40;
	static public final int WHITE = 41;
	static public final int GRAY = 42;
	static public final int BLACK = 43;
	static public final int DARK_GRAY = 44;

	// selected border color indices
	static public final int BLACK_BORDER = 45;
	static public final int GREEN_BORDER = 46;
	static public final int DARK_BLUE_BORDER = 47;
	static public final int ORANGE_BORDER = 48;
	static public final int GOLD_BORDER = 49;
	static public final int RED_BORDER = 50;
	static public final int GRAY_BORDER = 51;
	static public final int DARK_GREEN_BORDER1 = 52;
	static public final int DARK_YELLOW_BORDER1 = 53;
	static public final int MAGENTA3_BORDER1 = 54;
	static public final int PURPLE1_BORDER1 = 55;
	static public final int PINK1_BORDER1 = 56;
	static public final int AQUAMARINE_BORDER1 = 57;
	static public final int LIGHT_BLUE_BORDER1 = 58;
	static public final int CADET_BLUE_STATE_BORDER = 59;
	static public final int OLIVE_BORDER2 = 60;
	
	static public final int STATES_BORDER0 = 45;
	static public final int STATES_BORDER1 = 60;

	static public final int MID_LINE = 61;
	static public final int RED = 62;
	static public final int GREEN = 63;
	static public final int BLUE = 64;
	static public final int YELLOW = 65;
	static public final int CYAN = 66;
	static public final int MAGENTA = 67;

	static public final int SELECTED_TIME = 68;
	static public final int LEGEND_BACKGROUND = 69;
	static public final int LEGEND_FOREGROUND = 70;

	// group items' colors
	static public final int GR_BACKGROUND = 71;
	static public final int GR_FOREGROUND = 72;
	static public final int GR_BACKGROUND_SEL = 73;
	static public final int GR_FOREGROUND_SEL = 74;
	static public final int GR_BACKGROUND_SEL_NOFOCUS = 75;
	static public final int GR_FOREGROUND_SEL_NOFOCUS = 76;

	static public final int LIGHT_LINE = 77;
    static public final int BACKGROUND_NAME = 78;
    static public final int BACKGROUND_NAME_SEL = 79;
    static public final int BACKGROUND_NAME_SEL_NOFOCUS = 80;

	// Interraction's colors
	static public final int TI_START_THREAD = BLACK;
	static public final int TI_HANDOFF_LOCK = BLUE;
	static public final int TI_NOTIFY_ALL = GREEN;
	static public final int TI_NOTIFY = GREEN;
	static public final int TI_NOTIFY_JOINED = DARK_GRAY;
	static public final int TI_INTERRUPT = RED;
	static public final int TI_WAIT_EXCEEDED = BLUE;

	static interface IColorProvider {
		public Color get();
	}

	static class SysCol implements IColorProvider {
		int syscol;

		SysCol(int syscol) {
			this.syscol = syscol;
		}

		public Color get() {
			return Utils.getSysColor(syscol);
		}
	}

	static class RGB implements IColorProvider {
		int r;
		int g;
		int b;

		RGB(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public Color get() {
			return new Color(null, r, g, b);
		}
	}

	static class Mix implements IColorProvider {
		IColorProvider cp1;
		IColorProvider cp2;
		int w1;
		int w2;

		Mix(IColorProvider cp1, IColorProvider cp2, int w1, int w2) {
			this.cp1 = cp1;
			this.cp2 = cp2;
			this.w1 = w1;
			this.w2 = w2;
		}

		Mix(IColorProvider cp1, IColorProvider cp2) {
			this.cp1 = cp1;
			this.cp2 = cp2;
			this.w1 = 1;
			this.w2 = 1;
		}

		public Color get() {
			Color col1 = cp1.get();
			Color col2 = cp2.get();
			Color col = Utils.mixColors(col1, col2, w1, w2);
			return col;
		}
	}

	static private final IColorProvider _providersMap[] = {
			//
			new RGB(100, 100, 100), // UNKNOWN
			new RGB(174, 200, 124), // RUNNING
			new Mix(new SysCol(SWT.COLOR_BLUE), new SysCol(SWT.COLOR_GRAY), 1, 3), // SLEEPING
			new RGB(210, 150, 60), // WAITING
			new RGB(242, 225, 168), // BLOCKED
			new Mix(new SysCol(SWT.COLOR_RED), new SysCol(SWT.COLOR_GRAY), 1, 3), // DEADLOCK
			new RGB(200, 200, 200), // STOPPED
			new RGB(35, 107, 42), // STEEL BLUE
			new RGB(205,205,0), // DARK YELLOW
			new RGB(205, 0, 205), // MAGENTA
			new RGB(171, 130, 255), // PURPLE
			new RGB(255, 181, 197), // PINK
			new RGB(112, 219, 147), // AQUAMARINE
			new RGB(198, 226, 255), // SLATEGRAY
			new RGB(95, 158, 160), // CADET BLUE
			new RGB(107, 142, 35), // OLIVE
			
			
			//TODO: Does not seem to be used, check during clean-up
			new SysCol(SWT.COLOR_WHITE), // UNKNOWN_SEL
			new SysCol(SWT.COLOR_GREEN), // RUNNING_SEL
			new SysCol(SWT.COLOR_BLUE), // SLEEPING_SEL
			new SysCol(SWT.COLOR_CYAN), // WAITING_SEL
			new SysCol(SWT.COLOR_YELLOW), // BLOCKED_SEL
			new SysCol(SWT.COLOR_RED), // DEADLOCK_SEL
			new SysCol(SWT.COLOR_DARK_GRAY), // STOPPED_SEL
			new SysCol(SWT.COLOR_WHITE), 
			new SysCol(SWT.COLOR_GREEN), 
			new SysCol(SWT.COLOR_BLUE), 
			new SysCol(SWT.COLOR_CYAN), 
			new SysCol(SWT.COLOR_YELLOW), 
			new SysCol(SWT.COLOR_RED), 
			new SysCol(SWT.COLOR_DARK_GRAY), 
			new SysCol(SWT.COLOR_WHITE), 
			new SysCol(SWT.COLOR_GREEN), 
			

			new SysCol(SWT.COLOR_LIST_BACKGROUND), // BACKGROUND
			new SysCol(SWT.COLOR_LIST_FOREGROUND), // FOREGROUND
			new RGB(232, 242, 254), // BACKGROUND_SEL
			new SysCol(SWT.COLOR_LIST_FOREGROUND), // FOREGROUND_SEL
			new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // BACKGROUND_SEL_NOFOCUS
			new SysCol(SWT.COLOR_WIDGET_FOREGROUND), // FOREGROUND_SEL_NOFOCUS
			new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // TOOL_BACKGROUND
			new SysCol(SWT.COLOR_WIDGET_DARK_SHADOW), // TOOL_FOREGROUND

			new SysCol(SWT.COLOR_GRAY), // FIX_COLOR
			new SysCol(SWT.COLOR_WHITE), // WHITE
			new SysCol(SWT.COLOR_GRAY), // GRAY
			new SysCol(SWT.COLOR_BLACK), // BLACK
			new SysCol(SWT.COLOR_DARK_GRAY), // DARK_GRAY

			new SysCol(SWT.COLOR_DARK_GRAY), // BLACK_BORDER
			new RGB(75, 115, 120), // GREEN_BORDER
			new SysCol(SWT.COLOR_DARK_BLUE), // DARK_BLUE_BORDER
			new RGB(242, 225, 168), // ORANGE_BORDER
			new RGB(210, 150, 60), // GOLD_BORDER
			new SysCol(SWT.COLOR_DARK_RED), // RED_BORDER
			new SysCol(SWT.COLOR_BLACK), // GRAY_BORDER
			new SysCol(SWT.COLOR_DARK_GRAY), // DARK_GREEN_BORDER
			new RGB(75, 115, 120), // DARK_YELLOW_BORDER
			new SysCol(SWT.COLOR_DARK_BLUE), // MAGENTA3_BORDER
			new RGB(242, 225, 168), // PURPLE1_BORDER
			new RGB(210, 150, 60), // PINK1_BORDER
			new SysCol(SWT.COLOR_DARK_RED), // AQUAMARINE_BORDER
			new SysCol(SWT.COLOR_BLACK), // LIGHT_BLUE_BORDER
			new SysCol(SWT.COLOR_DARK_GRAY), // BLUE_BORDER
			new RGB(75, 115, 120), // OLIVE_BORDER
		

			new SysCol(SWT.COLOR_GRAY), // MID_LINE
			new SysCol(SWT.COLOR_RED), // RED
			new SysCol(SWT.COLOR_GREEN), // GREEN
			new SysCol(SWT.COLOR_BLUE), // BLUE
			new SysCol(SWT.COLOR_YELLOW), // YELLOW
			new SysCol(SWT.COLOR_CYAN), // CYAN
			new SysCol(SWT.COLOR_MAGENTA), // MAGENTA

			new SysCol(SWT.COLOR_BLUE), // SELECTED_TIME
			new SysCol(SWT.COLOR_WIDGET_BACKGROUND), // LEGEND_BACKGROUND
			new SysCol(SWT.COLOR_WIDGET_DARK_SHADOW), // LEGEND_FOREGROUND

			new Mix(new RGB(150, 200, 240), new SysCol(
					SWT.COLOR_LIST_BACKGROUND)),// GR_BACKGROUND
			new RGB(0, 0, 50), // GR_FOREGROUND
			new Mix(new RGB(200, 200, 100),
					new SysCol(SWT.COLOR_LIST_SELECTION)), // GR_BACKGROUND_SEL
			new Mix(new RGB(150, 200, 240), new SysCol(
					SWT.COLOR_LIST_SELECTION_TEXT)), // GR_FOREGROUND_SEL
			new Mix(new RGB(222, 222, 155), new SysCol(
					SWT.COLOR_WIDGET_BACKGROUND)), // GR_BACKGROUND_SEL_NOFOCUS
			new RGB(0, 0, 50), // GR_FOREGROUND_SEL_NOFOCUS

			new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(
					SWT.COLOR_LIST_BACKGROUND), 1, 3), // LIGHT_LINE

			new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_LIST_BACKGROUND), 1, 6),   // BACKGROUND_NAME
		    new Mix(new SysCol(SWT.COLOR_GRAY), new RGB(232, 242, 254), 1, 6),                  // BACKGROUND_NAME_SEL
		    new Mix(new SysCol(SWT.COLOR_GRAY), new SysCol(SWT.COLOR_WIDGET_BACKGROUND), 1, 6), // BACKGROUND_NAME_SEL_NOFOCUS
	};

	private Color _colors[];

	public TraceColorScheme() {
		_colors = new Color[_providersMap.length];
	}

	public void dispose() {
		for (int i = 0; i < _colors.length; i++) {
			Utils.dispose(_colors[i]);
			_colors[i] = null;
		}
	}

	public Color getColor(int idx) {
		if (null == _colors[idx]) {
			if (idx >= STATES_SEL0 && idx <= STATES_SEL1) {
				Color col1 = getColor(idx - STATES_SEL0);
				Color col2 = getColor(BACKGROUND_SEL);
				_colors[idx] = Utils.mixColors(col1, col2, 3, 1);
			} else {
				_colors[idx] = _providersMap[idx].get();
			}
		}
		return _colors[idx];
	}

	public Color getBkColor(boolean selected, boolean focused, boolean name) {
	    if (name) {
	        if (selected && focused)
	            return getColor(BACKGROUND_NAME_SEL);
	        if (selected)
	            return getColor(BACKGROUND_NAME_SEL_NOFOCUS);
	        return getColor(BACKGROUND_NAME);
	    } else {
	        if (selected && focused)
	            return getColor(BACKGROUND_SEL);
	        if (selected)
	            return getColor(BACKGROUND_SEL_NOFOCUS);
	        return getColor(BACKGROUND);
	    }
	}

	public Color getFgColor(boolean selected, boolean focused) {
		if (selected && focused)
			return getColor(FOREGROUND_SEL);
		if (selected)
			return getColor(FOREGROUND_SEL_NOFOCUS);
		return getColor(FOREGROUND);
	}

	public Color getBkColorGroup(boolean selected, boolean focused) {
		if (selected && focused)
			return getColor(GR_BACKGROUND_SEL);
		if (selected)
			return getColor(GR_BACKGROUND_SEL_NOFOCUS);
		return getColor(GR_BACKGROUND);
	}

	public Color getFgColorGroup(boolean selected, boolean focused) {
		if (selected && focused)
			return getColor(GR_FOREGROUND_SEL);
		if (selected)
			return getColor(GR_FOREGROUND_SEL_NOFOCUS);
		return getColor(GR_FOREGROUND);
	}

	public static StateColor[] getStateColors() {
		return stateColors;
	}
}
