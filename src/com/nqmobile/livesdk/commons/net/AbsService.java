package com.nqmobile.livesdk.commons.net;

import java.util.concurrent.ExecutorService;

import com.nqmobile.livesdk.commons.concurrent.PriorityExecutor;

public class AbsService {
	private ExecutorService mExecutor;

	public AbsService() {
		mExecutor = PriorityExecutor.getExecutor();
	}

	protected ExecutorService getExecutor() {
		return mExecutor;
	}

	protected void setExecutor(ExecutorService executor) {
		mExecutor = executor;
	}
}
