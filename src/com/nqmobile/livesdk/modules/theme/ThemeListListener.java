package com.nqmobile.livesdk.modules.theme;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 主题列表获取监听
 * @time 2013-12-5 下午5:28:56
 */
public interface ThemeListListener extends Listener {
	public void onGetThemeListSucc(List<Theme> themes);
}
