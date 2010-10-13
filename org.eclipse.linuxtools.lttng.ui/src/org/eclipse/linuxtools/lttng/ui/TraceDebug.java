package org.eclipse.linuxtools.lttng.ui;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

public class TraceDebug {
	static boolean DEBUG = false;
	static boolean INFO = false;
	static boolean WARN = false;

	static boolean CFV = false;
	static boolean RV = false;

	private static Plugin plugin = LTTngUiPlugin.getDefault();
	private static String pluginID = LTTngUiPlugin.PLUGIN_ID;
	private static SimpleDateFormat stimeformat = new SimpleDateFormat(
			"HH:mm:ss:SSS");

	// Note: files are created in $HOME
	static private PrintWriter fCFVfile = null;
	static private PrintWriter fRVfile  = null;

	public static void init() {
		// Update Trace configuration options
		String debugTrace = Platform.getDebugOption(pluginID + "/debug");
		String infoTrace = Platform.getDebugOption(pluginID + "/info");
		String warnTrace = Platform.getDebugOption(pluginID + "/warn");

		if (debugTrace != null) {
			DEBUG = (new Boolean(debugTrace)).booleanValue();
		}

		if (infoTrace != null) {
			INFO = (new Boolean(infoTrace)).booleanValue();
		}

		if (warnTrace != null) {
			WARN = (new Boolean(warnTrace)).booleanValue();
		}

		String cfvTrace = Platform.getDebugOption(pluginID + "/cfv");
		if (cfvTrace != null) {
			CFV = (new Boolean(cfvTrace)).booleanValue();
			if (CFV) {
				try {
					fCFVfile = new PrintWriter(new FileWriter("CFVTrace.txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		String rvTrace = Platform.getDebugOption(pluginID + "/rv");
		if (rvTrace != null) {
			RV = (new Boolean(rvTrace)).booleanValue();
			if (RV) {
				try {
					fRVfile = new PrintWriter(new FileWriter("RVTrace.txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void stop() {
		if (fCFVfile != null) {
			fCFVfile.close();
			fCFVfile = null;
		}

		if (fRVfile != null) {
			fRVfile.close();
			fRVfile = null;
		}
	}

	public static void traceCFV(String trace) {
		if (CFV && fCFVfile != null) {
			fCFVfile.println(trace);
			fCFVfile.flush();
		}
	}

	public static void traceRV(String trace) {
		if (RV && fRVfile != null) {
			fRVfile.println(trace);
			fRVfile.flush();
		}
	}

	public static void info(String message) {
		if (INFO) {
			ILog logger = plugin.getLog();
			logger.log(new Status(IStatus.INFO, LTTngUiPlugin.PLUGIN_ID,
					IStatus.OK, message, null));
		}
	}

	public static void warn(String message) {
		if (WARN) {
			ILog logger = plugin.getLog();
			logger.log(new Status(IStatus.WARNING, LTTngUiPlugin.PLUGIN_ID,
					IStatus.WARNING, message, null));
		}
	}

	public static void debug(String message) {
		if (DEBUG) {
			String location = getCallingLocation();
			System.out.println(location + "\n\t-> " + message);

		}
	}

	public static void debug(String message, int additionalStackLines) {
		if (DEBUG) {
			String location = getCallingLocation(additionalStackLines);
			System.out.println(location + "\n\t-> " + message);
		}
	}

	public static void throwException(String message) {
		if (DEBUG) {
			try {
				triggerException(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void triggerException(String message) throws Exception {
		throw new Exception(message);
	}

	private static String getCallingLocation() {
		StringBuilder sb = new StringBuilder();
		sb.append(trace(Thread.currentThread().getStackTrace(), 4));
		sb.append("\n" + trace(Thread.currentThread().getStackTrace(), 3));
		return sb.toString();
	}

	private static String getCallingLocation(int numOfStackLines) {
		int stackCalledFromIdx = 3;
		int earliestRequested = numOfStackLines > 0 ? stackCalledFromIdx
				+ numOfStackLines : stackCalledFromIdx;
		StringBuilder sb = new StringBuilder();
		for (int i = earliestRequested; i >= stackCalledFromIdx; i--) {
			sb.append(trace(Thread.currentThread().getStackTrace(), i) + "\n");
		}
		return sb.toString();
	}

	private static String trace(StackTraceElement e[], int level) {
		if (e != null) {
			level = level >= e.length ? e.length - 1 : level;
			StackTraceElement s = e[level];
			if (s != null) {
				String simpleClassName = s.getClassName();
				String[] clsNameSegs = simpleClassName.split("\\.");
				if (clsNameSegs.length > 0)
					simpleClassName = clsNameSegs[clsNameSegs.length - 1];
				return stimeformat.format(new Date()) + " " + simpleClassName
						+ "." + s.getLineNumber() + "." + s.getMethodName();
			}
		}

		return null;
	}

	public static boolean isDEBUG() {
		return DEBUG;
	}

	public static boolean isINFO() {
		return INFO;
	}

	public static boolean isWARN() {
		return WARN;
	}

	public static boolean isCFV() {
		return CFV;
	}

	public static boolean isRV() {
		return RV;
	}
}
