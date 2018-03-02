package com.nqmobile.livesdk.modules.regularupdate.network;

public class RegularUpdateServiceFactory {
	private static RegularUpdateService sMock;

	public static void setMock(RegularUpdateService mock) {
		sMock = mock;
	}

	public static RegularUpdateService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new RegularUpdateService();
		}
	}
}
