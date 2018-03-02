package com.nqmobile.livesdk.modules.appstub.network;

public class AppStubServiceFactory {
	private static AppStubService sMock;

	public static void setMock(AppStubService mock) {
		sMock = mock;
	}

	public static AppStubService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new AppStubService();
		}
	}
}
