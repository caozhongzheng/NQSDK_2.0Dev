package com.nqmobile.livesdk.modules.wallpaper;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 壁纸详情获取监听
 * @time 2013-12-5 下午5:28:56
 */
public interface WallpaperDetailListener extends Listener {
	public void onGetDetailSucc(Wallpaper wallpaper);
}