package com.nqmobile.livesdk.modules.appstub.network;

import com.nqmobile.livesdk.commons.net.AbsService;

public class AppStubService extends AbsService {
	public void getAppStubList(Object tag) {
		getExecutor().submit(new AppStubProtocol(tag));
	}
}
