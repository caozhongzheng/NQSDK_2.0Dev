package com.nqmobile.livesdk.modules.app.network;

public class AppServiceFactory {
	private static AppService sMock;

	public static void setMock(AppService mock) {
		sMock = mock;
	}

	public static AppService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new AppService();
		}
	}
}
