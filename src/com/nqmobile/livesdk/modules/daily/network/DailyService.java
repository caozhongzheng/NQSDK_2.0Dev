package com.nqmobile.livesdk.modules.daily.network;

import android.content.Context;

import com.nqmobile.livesdk.commons.net.AbsService;

public class DailyService extends AbsService {
	
	public void getDailyList(Context context, int size, Object tag) {
		getExecutor().submit(new DailyListProtocol(context, size, tag));
	}
}
