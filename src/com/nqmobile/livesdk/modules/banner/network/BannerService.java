package com.nqmobile.livesdk.modules.banner.network;

import android.content.Context;

import com.nqmobile.livesdk.commons.net.AbsService;

public class BannerService extends AbsService {
	
	public void getBannerList(Context context, int plate, Object tag) {
		getExecutor().submit(new BannerListProtocol(context, plate, tag));
	}
}
