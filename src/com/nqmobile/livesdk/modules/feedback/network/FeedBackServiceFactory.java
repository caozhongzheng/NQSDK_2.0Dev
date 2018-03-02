package com.nqmobile.livesdk.modules.feedback.network;

public class FeedBackServiceFactory {
	private static FeedBackService sMock;
	private static FeedBackService sReal = new FeedBackService();

	public static void setMock(FeedBackService mock) {
		sMock = mock;
	}

	public static FeedBackService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return sReal;
		}
	}
}
