package com.nqmobile.livesdk.modules.gamefolder_v2.network;

public class GameFolderV2ServiceFactory {
	private static GameFolderV2Service sMock;

	public static void setMock(GameFolderV2Service mock) {
		sMock = mock;
	}

	public static GameFolderV2Service getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new GameFolderV2Service();
		}
	}
}
