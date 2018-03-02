package com.nqmobile.livesdk.modules.wallpaper.network;

import android.content.Context;

import com.nqmobile.livesdk.commons.net.AbsService;

public class WallpaperService extends AbsService {
	
	public void getWallpaperDetailt(Context context, String id, Object tag) {
		getExecutor().submit(new WallpaperDetailProtocol(context, id, tag));
	}
	
	public void getWallpaperListList(Context context, int column, int offset, Object tag) {
		getExecutor().submit(new WallpaperListProtocol(context, column, offset, tag));
	}
}
