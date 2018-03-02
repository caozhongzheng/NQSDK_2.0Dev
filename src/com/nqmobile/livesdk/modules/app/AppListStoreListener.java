package com.nqmobile.livesdk.modules.app;

import java.util.List;

import com.nqmobile.livesdk.commons.net.NetworkingListener;

/**
 * 应用列表获取监听
 * @time 2013-12-5 下午5:28:56
 */
public interface AppListStoreListener extends NetworkingListener {
	public void onGetAppListSucc(int column, int offset, List<App> apps);
}
