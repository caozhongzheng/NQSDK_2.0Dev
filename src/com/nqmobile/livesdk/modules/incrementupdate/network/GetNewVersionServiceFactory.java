package com.nqmobile.livesdk.modules.incrementupdate.network;

public class GetNewVersionServiceFactory {
	private static GetNewVersionService sMock;

	public static void setMock(GetNewVersionService mock) {
		sMock = mock;
	}

	public static GetNewVersionService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new GetNewVersionService();
		}
	}
}
