package com.nqmobile.livesdk.commons.log;

public abstract class AbstractLogger implements ILogger {
	public static final String DELIM_STR = "{}";
	private final String mTag;
	private int mLogLevel;

	public AbstractLogger(String tag) {
		mTag = tag;
		mLogLevel = LogLevel.DISABLED;
	}
	
	@Override
	public void setLogLevel(int logLevel) {
		mLogLevel = logLevel;
	}
	
	public int getLogLevel() {
		return mLogLevel;
	}
	
	public void v(String message) {
		if (mLogLevel < LogLevel.VERBOSE) return;
		v(mTag, message, null);
	}

	public void v(String message, Throwable t) {
		if (mLogLevel < LogLevel.VERBOSE) return;
		v(mTag, message, t);
	}

	public final void v(String message, Object[] params) {
		if (mLogLevel < LogLevel.VERBOSE) return;
		v(mTag, format(message, params), null);
	}

	public void d(String message) {
		if (mLogLevel < LogLevel.DEBUG) return;
		d(mTag, message, null);
	}

	public void d(String message, Throwable t) {
		if (mLogLevel < LogLevel.DEBUG) return;
		d(mTag, message, t);
	}

	public final void d(String message, Object[] params) {
		if (mLogLevel < LogLevel.DEBUG) return;
		d(mTag, format(message, params), null);
	}

	public void i(String message) {
		if (mLogLevel < LogLevel.INFO) return;
		i(mTag, message, null);
	}

	public void i(String message, Throwable t) {
		if (mLogLevel < LogLevel.INFO) return;
		i(mTag, message, t);
	}

	public final void i(String message, Object[] params) {
		if (mLogLevel < LogLevel.INFO) return;
		i(mTag, format(message, params), null);
	}

	public void w(String message) {
		if (mLogLevel < LogLevel.WARN) return;
		w(mTag, message, null);
	}

	public void w(String message, Throwable t) {
		if (mLogLevel < LogLevel.WARN) return;
		w(mTag, message, t);
	}

	public final void w(String message, Object[] params) {
		if (mLogLevel < LogLevel.WARN) return;
		w(mTag, format(message, params), null);
	}

	public void e(Throwable t) {
		if (mLogLevel < LogLevel.ERROR) return;
		e(mTag, "", t);
	}

	public void e(String message) {
		if (mLogLevel < LogLevel.ERROR) return;
		e(mTag, message, null);
	}

	public void e(String message, Throwable t) {
		if (mLogLevel < LogLevel.ERROR) return;
		e(mTag, message, t);
	}

	public final void e(String message, Object[] params) {
		if (mLogLevel < LogLevel.ERROR) return;
		e(mTag, format(message, params), null);
	}

	private String format(String messagePattern, Object[] array) {
		try {
			int i = 0;
			int j = 0;
			StringBuffer sbuf = new StringBuffer(messagePattern.length() + 50);
			int l = 0;
			for (l = 0; l < array.length; l++) {
				j = messagePattern.indexOf("{}", i);
				if (j == -1)
					break;
				sbuf.append(messagePattern.substring(i, j));
				sbuf.append(array[l]);
				i = j + 2;
			}

			sbuf.append(messagePattern.substring(i, messagePattern.length()));
			return sbuf.toString();
		} catch (Exception e) {
		}
		return messagePattern;
	}
	
	protected abstract void v(String message, String params,
			Throwable t);

	protected abstract void d(String message, String params,
			Throwable t);

	protected abstract void i(String message, String params,
			Throwable t);

	protected abstract void w(String message, String params,
			Throwable t);

	protected abstract void e(String message, String params,
			Throwable t);
}
