package com.nqmobile.livesdk.modules.installedrecommend.network;

public class GetInstalledRecommendServiceFactory {
	private static GetInstalledRecommendService sMock;
	
	public static void setMock(GetInstalledRecommendService mock) {
		sMock = mock;
	}

	public static GetInstalledRecommendService getService() {
		if (sMock != null){
			return sMock;
		} else {
			return new GetInstalledRecommendService();
		}
	}
}
