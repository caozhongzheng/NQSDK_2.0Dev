package com.nqmobile.livesdk.modules.wallpaper;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 壁纸列表获取监听
 * @time 2013-12-5 下午5:28:56
 */
public interface WallpaperListListener extends Listener {
	public void onGetWallpaperListSucc(List<Wallpaper> wallpapers);
}
