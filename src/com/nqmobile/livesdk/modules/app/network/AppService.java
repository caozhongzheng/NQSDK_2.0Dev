package com.nqmobile.livesdk.modules.app.network;


import android.content.Context;

import com.nqmobile.livesdk.commons.net.AbsService;

public class AppService extends AbsService {
	public void getAppDetail(Context context, String id, Object tag) {
		getExecutor().submit(new AppDetailProtocal(context, id, tag));
	}
	
	public void getAppList(Context context, int column, int offset, int size, Object tag) {
		getExecutor().submit(new AppListProtocol(context, column, offset, size, tag));
	}
}
