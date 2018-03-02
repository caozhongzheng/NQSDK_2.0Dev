package com.nqmobile.livesdk.modules.gamefolder_v2.network;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.commons.net.ServiceLock;

public class GameFolderV2Service extends AbsService {
	public void getGameList(Object tag) {
		ServiceLock lock = ServiceLock.getInstance(GetGameListProtocol.class.getName());
		if (!lock.available()) {
			return;
		}
		getExecutor().submit(new GetGameListProtocol(tag));
	}
}
