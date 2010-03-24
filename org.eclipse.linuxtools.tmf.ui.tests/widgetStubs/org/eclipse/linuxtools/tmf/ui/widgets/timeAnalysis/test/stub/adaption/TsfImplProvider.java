package org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.adaption;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.TmfTimeAnalysisProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model.EventImpl;
import org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model.TraceImpl;


public class TsfImplProvider extends TmfTimeAnalysisProvider {

	@Override
	public StateColor getEventColor(ITimeEvent event) {
		if (event instanceof EventImpl) {
			EventImpl devent = (EventImpl) event;
			switch (devent.getType()) {
			case ALARM:
				return StateColor.DARK_GREEN;
			case ERROR:
				return StateColor.DARK_YELLOW;
			case EVENT:
				return StateColor.MAGENTA3;
			case INFORMATION:
				return StateColor.PURPLE1;
			case TIMEADJUSTMENT:
				return StateColor.PINK1;
			case WARNING:
				return StateColor.AQUAMARINE;
			case INFO1:
				return StateColor.RED;
			case INFO2:
				return StateColor.GREEN;
			case INFO3:
				return StateColor.DARK_BLUE;
			case INFO4:
				return StateColor.GOLD;
			case INFO5:
				return StateColor.ORANGE;
			case INFO6:
				return StateColor.GRAY;
			case INFO7:
				return StateColor.LIGHT_BLUE;
			case INFO8:
				return StateColor.CADET_BLUE;
			case INFO9:
				return StateColor.OLIVE;
			}
		}
		return StateColor.BLACK;
	}

	@Override
	public String getStateName(StateColor color) {
		switch (color) {
		case GOLD:
			return "ALARM";
		case RED:
			return "ERROR";
		case DARK_BLUE:
			return "EVENT";
		case GREEN:
			return "INFORMATION";
		case GRAY:
			return "TIME ADJUSTMENT DKDKDKDKL";
		case ORANGE:
			return "WARNING";
		default:
			return "UNKNOWN";
		}
	}

	@Override
	public Map<String, String> getEventHoverToolTipInfo(ITimeEvent revent) {
		Map<String, String> toolTipEventMsgs = new HashMap<String, String>();
		if (revent instanceof EventImpl) {
			toolTipEventMsgs.put("Test Tip1", "Test Value tip1");
			toolTipEventMsgs.put("Test Tip2", "Test Value tip2");
		}

		return toolTipEventMsgs;
	}

	@Override
	public String getEventName(ITimeEvent event, boolean upper, boolean extInfo) {
		String name = "Unknown";
		name = upper ? name : name;
		if (event instanceof EventImpl) {
			EventImpl devent = (EventImpl) event;
			name = devent.getType().toString();
		}
		return name;
	}

	@Override
	public String getTraceClassName(ITmfTimeAnalysisEntry trace) {
		String name = "";
		if (trace instanceof TraceImpl) {
			TraceImpl dTrace = (TraceImpl) trace;
			name = dTrace.getClassName();
		}
		return name;
	}
}
