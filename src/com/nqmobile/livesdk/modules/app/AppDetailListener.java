package com.nqmobile.livesdk.modules.app;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 应用详情获取监听
 * @time 2013-12-5 下午5:28:56
 */
public interface AppDetailListener extends Listener {
	public void onGetDetailSucc(App app);
}