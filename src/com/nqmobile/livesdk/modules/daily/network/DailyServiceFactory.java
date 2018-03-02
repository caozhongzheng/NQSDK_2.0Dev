package com.nqmobile.livesdk.modules.daily.network;

public class DailyServiceFactory {
	private static DailyService sMock;

	public static void setMock(DailyService mock) {
		sMock = mock;
	}

	public static DailyService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new DailyService();
		}
	}
}
