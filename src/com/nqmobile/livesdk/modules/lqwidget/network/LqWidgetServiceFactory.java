package com.nqmobile.livesdk.modules.lqwidget.network;

public class LqWidgetServiceFactory {
	private static LqWidgetService sMock;

	public static void setMock(LqWidgetService mock) {
		sMock = mock;
	}

	public static LqWidgetService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new LqWidgetService();
		}
	}
}
