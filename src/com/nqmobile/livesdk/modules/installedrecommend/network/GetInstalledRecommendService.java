package com.nqmobile.livesdk.modules.installedrecommend.network;

import com.nqmobile.livesdk.commons.net.AbsService;

public class GetInstalledRecommendService extends AbsService {
	public void getInstalledAssociationApp(String packageName, Object tag) {
		getExecutor().submit(new GetInstalledRecommendProtocal(packageName, tag));
	}
}
