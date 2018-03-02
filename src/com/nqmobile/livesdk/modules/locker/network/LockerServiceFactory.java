package com.nqmobile.livesdk.modules.locker.network;

public class LockerServiceFactory {
	private static LockerService sMock;
	private static LockerService sReal = new LockerService();

	public static void setMock(LockerService mock) {
		sMock = mock;
	}

	public static LockerService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return sReal;
		}
	}
}
