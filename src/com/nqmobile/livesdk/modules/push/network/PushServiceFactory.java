package com.nqmobile.livesdk.modules.push.network;

/**
 * Push服务工厂
 * 
 * @author HouKangxi
 */
public class PushServiceFactory {
	private static PushService sMock;

	public static void setMock(PushService mock) {
		sMock = mock;
	}

	public static PushService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new PushService();
		}
	}
}
