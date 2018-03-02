package com.nqmobile.livesdk.modules.banner.network;

public class BannerServiceFactory {
	private static BannerService sMock;

	public static void setMock(BannerService mock) {
		sMock = mock;
	}

	public static BannerService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new BannerService();
		}
	}
}
