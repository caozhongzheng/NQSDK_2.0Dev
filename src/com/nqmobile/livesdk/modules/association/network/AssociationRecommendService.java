package com.nqmobile.livesdk.modules.association.network;

import com.nqmobile.livesdk.commons.net.AbsService;

public class AssociationRecommendService  extends AbsService {
	public void getAssociationApp(Object tag) {
		getExecutor().submit(new AssociationRecommendProtocal(tag));
	}
}
