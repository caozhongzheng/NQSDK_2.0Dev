package com.nqmobile.livesdk.modules.apptype.network;

public class AppTypeServiceFactory {
	private static AppTypeService sMock;

	public static void setMock(AppTypeService mock) {
		sMock = mock;
	}

	public static AppTypeService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new AppTypeService();
		}
	}
}
