package com.nqmobile.livesdk.commons.log;

public interface ILogger {
	public void v(String message, Throwable t);

	public void v(String message);

	public void v(String message, Object[] params);

	public void d(String message, Throwable t);

	public void d(String message);

	public void d(String message, Object[] params);

	public void i(String message, Throwable t);

	public void i(String message);

	public void i(String message, Object[] params);

	public void w(String message, Throwable t);

	public void w(String message);

	public void w(String message, Object[] params);

	public void e(Throwable t);

	public void e(String message, Throwable t);

	public void e(String message);

	public void e(String message, Object[] params);

	public void setLogLevel(int logLevel);

	public int getLogLevel();

}
