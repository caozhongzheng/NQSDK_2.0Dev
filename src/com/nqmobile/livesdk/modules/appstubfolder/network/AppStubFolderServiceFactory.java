package com.nqmobile.livesdk.modules.appstubfolder.network;

public class AppStubFolderServiceFactory {
	private static AppStubFolderService sMock;

	public static void setMock(AppStubFolderService mock) {
		sMock = mock;
	}

	public static AppStubFolderService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new AppStubFolderService();
		}
	}
}
