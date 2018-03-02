package com.nqmobile.livesdk.commons.net;

import java.util.HashMap;
import java.util.Map;

import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;


import android.text.format.DateUtils;


public class ServiceLock {
	private static long SERVICE_GAP = 30 * DateUtils.SECOND_IN_MILLIS;
	private static Map<String, ServiceLock> sLocks = new HashMap<String, ServiceLock>();
	private long lastServiceTime;
	
	private ServiceLock(){
		//do nothing
	}
	
	public synchronized static ServiceLock getInstance(String name){
		ServiceLock result = sLocks.get(name);
		if (result == null){
			result = new ServiceLock();
			sLocks.put(name, result);
		}
		
		return result;
	}
	
	public boolean available() {
		boolean result = false;
		
		synchronized (this) {
			long now = SystemFacadeFactory.getSystem().currentTimeMillis();
			if (Math.abs(now - lastServiceTime) > SERVICE_GAP) {
				lastServiceTime = now;
				result = true;
			}
		}

		return result;
	}
	
	/**
	 * 单元测试时，可以设置服务间隔时间
	 * @param gap
	 */
	static void setServiceGap(long gap){
		SERVICE_GAP = gap;
	}
	
}
