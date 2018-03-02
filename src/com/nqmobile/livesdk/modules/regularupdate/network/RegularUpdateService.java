package com.nqmobile.livesdk.modules.regularupdate.network;

import com.nqmobile.livesdk.commons.net.AbsService;
import com.nqmobile.livesdk.commons.net.ServiceLock;

public class RegularUpdateService extends AbsService {
	
	public void regularUpdate(Object tag){
		ServiceLock lock = ServiceLock.getInstance(RegularUpdateProtocol.class.getName());
		if (!lock.available()) {
			return;
		}
		getExecutor().submit(new RegularUpdateProtocol(tag));
	}
}
