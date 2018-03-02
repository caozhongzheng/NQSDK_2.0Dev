package com.nqmobile.livesdk.modules.browserbandge.network;

public class BrowserBandgeServiceFactory {
	private static BrowserBandgeService sMock;

	public static void setMock(BrowserBandgeService mock) {
		sMock = mock;
	}

	public static BrowserBandgeService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new BrowserBandgeService();
		}
	}
}
