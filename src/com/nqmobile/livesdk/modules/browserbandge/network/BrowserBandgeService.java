package com.nqmobile.livesdk.modules.browserbandge.network;


import com.nqmobile.livesdk.commons.net.AbsService;

/**
 * 
 * @ClassName: BrowserBandgeService 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author handy
 * @date 2014-11-21   
 *
 */
public class BrowserBandgeService extends AbsService {
	
	public void getBandge(Object tag) {
		getExecutor().submit(new GetBrowserBandgeProtocol(tag));
	}
}
