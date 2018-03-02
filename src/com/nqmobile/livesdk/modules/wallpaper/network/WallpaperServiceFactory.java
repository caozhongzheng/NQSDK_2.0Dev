package com.nqmobile.livesdk.modules.wallpaper.network;

public class WallpaperServiceFactory {
	private static WallpaperService sMock;

	public static void setMock(WallpaperService mock) {
		sMock = mock;
	}

	public static WallpaperService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new WallpaperService();
		}
	}
}
