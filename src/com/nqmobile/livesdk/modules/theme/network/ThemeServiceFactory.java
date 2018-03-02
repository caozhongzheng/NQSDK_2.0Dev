package com.nqmobile.livesdk.modules.theme.network;

public class ThemeServiceFactory {
	private static ThemeService sMock;

	public static void setMock(ThemeService mock) {
		sMock = mock;
	}

	public static ThemeService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new ThemeService();
		}
	}
}
