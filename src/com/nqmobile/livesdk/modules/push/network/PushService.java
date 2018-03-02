package com.nqmobile.livesdk.modules.push.network;

import com.nqmobile.livesdk.commons.net.AbsService;

/**
 * Push服务类，将外部请求提交到线程池执行
 * 
 * @author HouKangxi
 */
public class PushService extends AbsService {
	public void getPush(Object tag) {
		getExecutor().submit(new PushListProtocol(tag));
	}

}
