/**
 * 
 */
package com.nqmobile.livesdk.modules.search.network;

import android.util.Log;

import com.nq.interfaces.launcher.TLauncherService;
import com.nq.interfaces.launcher.TSearchHotWordReq;
import com.nq.interfaces.launcher.TSearchHotWordResp;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.net.AbsLauncherProtocol;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.commons.thrift.TLauncherServiceClientFactory;
import com.nqmobile.livesdk.modules.search.SearcherModule;
import com.nqmobile.livesdk.modules.search.SearcherPreference;

/**
 * @author HouKangxi
 *
 */
class HotwordListSearchProtocol extends AbsLauncherProtocol {
	private static final ILogger NqLog = LoggerFactory.getLogger(SearcherModule.MODULE_NAME);
	

	public HotwordListSearchProtocol(Object tag) {
		super.setTag(tag);
	}
	
	@Override
	protected int getProtocolId() {
		return 0x5eac4;
	}

	@Override
	protected void process() {
		SearcherPreference xml = SearcherPreference.getInstance();
		try {
			TLauncherService.Iface client = TLauncherServiceClientFactory
					.getClient(getThriftProtocol());
			TSearchHotWordReq req =new TSearchHotWordReq();
			req.searchBoxType = xml.getSearchBoxType();
			req.lastUpdateTime = xml.getLastUpdateTime();
			TSearchHotWordResp resp = client.getSearchHotWordList(getUserInfo(), req);
			NqLog.d("resp = "+resp);
			if (resp!=null&&resp.hotWords != null && resp.hotWords.size()>0) {
				String[] words = new String[resp.hotWords.size()];
				resp.hotWords.toArray(words);
				// 真正取到数据后再设置上次请求的时间
				xml.setLastUpdateTime(SystemFacadeFactory.getSystem().currentTimeMillis());
				
				EventBus.getDefault().post(
						new HotwordSearchListEvent(words)
								.setHotwordUrl(resp.rcUrl)
								.setSoUrl(resp.tgUrl)
						.setTag(getTag()).setSuccess(true));
			}
		} catch (Throwable e) {
			NqLog.e(Log.getStackTraceString(e));onError();
		}
	}
	@Override
	protected void onError() {
		EventBus.getDefault().post(new HotwordSearchListEvent(null).setTag(getTag()).setSuccess(false));
	}

}
