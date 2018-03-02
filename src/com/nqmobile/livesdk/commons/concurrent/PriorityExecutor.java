/**
 * 
 */
package com.nqmobile.livesdk.commons.concurrent;

import java.util.concurrent.ExecutorService;

/**
 * @author chenyanmin
 * @time 2014-2-15 下午9:44:07
 */
public class PriorityExecutor {
	private static final int MAX_POOL_SIZE = 4;
	private static ExecutorService sExecutor;

	public synchronized static ExecutorService getExecutor() {
		if (sExecutor == null) {
			sExecutor = new PriorityThreadPoolExecutor(MAX_POOL_SIZE);
		}

		return sExecutor;
	}
}
