/**
 * 
 */
package com.nqmobile.livesdk.utils;


import java.lang.reflect.Method;
import java.net.Socket;

import org.apache.thrift.transport.TSocket;

import android.net.TrafficStats;

import com.nqmobile.livesdk.commons.info.NqTest;
import com.nqmobile.livesdk.commons.log.NqLog;

/**
 * @author chenyanmin
 * @time 2014-1-25 上午11:20:05
 */
public class Stats {
	public static void beginTrafficStats(int tag, Socket socket) {
		if (NqTest.isDebug() && socket != null) {
			setThreadStatsTag(tag);
			tagSocket(socket);
		}
	}
	
	public static void beginTrafficStats(int tag, TSocket socket) {
		if (NqTest.isDebug() && socket != null) {
			setThreadStatsTag(tag);
			Socket s = socket.getSocket();
			if (s != null)
				tagSocket(s);
		}
	}
	
	public static void endTrafficStats(Socket socket) {
		if (NqTest.isDebug() && socket != null) {
			untagSocket(socket);
			clearThreadStatsTag();
		}
	}
	
	public static void endTrafficStats(TSocket socket) {
		if (NqTest.isDebug() && socket != null) {
			Socket s = socket.getSocket();
			if (s != null)
				untagSocket(s);
			clearThreadStatsTag();
		}
	}
	
	public static void beginTrafficStats(int tag) {
		if (NqTest.isDebug()) {
			setThreadStatsTag(tag);
		}
	}
	
	public static void endTrafficStats() {
		if (NqTest.isDebug()) {
			clearThreadStatsTag();
		}
	}
	
	private static void setThreadStatsTag(int tag) {
		Class<?> c = TrafficStats.class;
		try {
			Method method = c.getMethod("setThreadStatsTag",
					new Class[] { int.class });
			if (method != null) {
				method.invoke(c, new Object[] { tag });
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
	}

	private static void tagSocket(Socket socket) {
		Class<?> c = TrafficStats.class;
		try {
			Method method = c.getMethod("tagSocket",
					new Class[] { Socket.class });
			if (method != null) {
				method.invoke(c, new Object[] { socket });
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
	}

	private static void untagSocket(Socket socket) {
		Class<?> c = TrafficStats.class;
		try {
			Method method = c.getMethod("untagSocket",
					new Class[] { Socket.class });
			if (method != null) {
				method.invoke(c, new Object[] { socket });
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
	}

	private static void clearThreadStatsTag() {
		Class<?> c = TrafficStats.class;
		try {
			Method method = c.getMethod("clearThreadStatsTag");
			if (method != null) {
				method.invoke(c);
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
	}
}
