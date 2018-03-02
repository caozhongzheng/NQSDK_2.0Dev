package com.nqmobile.livesdk.commons.log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nqmobile.livesdk.commons.info.NqTest;

public class LoggerFactory {
	private final static String TAG_DEFAULT = "SDK_test";
	
	private static ILogger sMock;
	private static Map<String, ILogger> sLoggers = new ConcurrentHashMap<String, ILogger>();
	
	public static void setMock(ILogger mock){
		sMock = mock;
	}
	
	public static ILogger getLogger(String name){
		if (sMock != null){
			return sMock;
		}
		
		if (name == null || name.isEmpty()){
			name = TAG_DEFAULT;
		}
		ILogger logger = sLoggers.get(name);
		
		if (logger != null) {
			return logger;
		}
		
		logger = new AndroidLogger(name);
		sLoggers.put(name, logger);
		if (NqTest.isDebug()){
			logger.setLogLevel(LogLevel.DEBUG);
		} else {
			logger.setLogLevel(LogLevel.DISABLED);
		}
		
		return logger;
		
	}

}
