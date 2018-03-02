package com.nqmobile.livesdk.modules.search.network;

import com.nqmobile.livesdk.commons.net.AbsService;

/**
 * Push服务类，将外部请求提交到线程池执行
 * 
 * @author HouKangxi
 */
public class SearchService extends AbsService {
	public void getSearchHotWordList(Object tag){
		getExecutor().submit(new HotwordListSearchProtocol(tag));
	}

}
