package com.nqmobile.livesdk.commons.log;

public class NqLog {
	private static final ILogger sLogger = LoggerFactory.getLogger("SDK_test");

	public static void v(String message, Throwable t) {
		sLogger.v(message, t);		
	}

	public static void v(String message) {
		sLogger.v(message);		
	}

	public static void v(String message, Object[] params) {
		sLogger.v(message, params);
	}

	public static void d(String message, Throwable t) {
		sLogger.d(message, t);
	}

	public static void d(String message) {
		sLogger.d(message);
	}

	public static void d(String message, Object[] params) {
		sLogger.d(message, params);
		
	}

	public static void i(String message, Throwable t) {
		sLogger.i(message, t);
	}

	public static void i(String message) {
		sLogger.i(message);
	}

	public static void i(String message, Object[] params) {
		sLogger.i(message, params);
	}

	public static void w(String message, Throwable t) {
		sLogger.w(message, t);
	}

	public static void w(String message) {
		sLogger.w(message);
	}

	public static void w(String message, Object[] params) {
		sLogger.w(message, params);
	}

	public static void e(Throwable t) {
		sLogger.e(t);
		
	}

	public static void e(String message, Throwable t) {
		sLogger.e(message, t);
	}

	public static void e(String message) {
		sLogger.e(message);
	}

	public static void e(String message, Object[] params) {
		sLogger.e(message, params);
	}

	public static void setLogLevel(int logLevel) {
		sLogger.setLogLevel(logLevel);
		
	}

	public static int getLogLevel() {
		return sLogger.getLogLevel();
	}

}
