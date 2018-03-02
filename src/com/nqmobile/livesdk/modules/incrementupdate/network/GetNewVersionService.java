package com.nqmobile.livesdk.modules.incrementupdate.network;

import com.nqmobile.livesdk.commons.net.AbsService;

public class GetNewVersionService extends AbsService {
	public void getNewVersion(Object tag) {
		getExecutor().submit(new GetNewVersionProtocol(tag));
	}
}
