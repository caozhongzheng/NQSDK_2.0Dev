package com.nqmobile.livesdk.modules.theme;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 主题详情获取监听
 * @time 2013-12-5 下午5:28:56
 */
public interface ThemeDetailListener extends Listener {
	public void onGetDetailSucc(Theme theme);
}
