package com.nqmobile.livesdk.modules.association.network;

public class AssociationRecommendServiceFactory {
	private static AssociationRecommendService sMock;

	public static void setMock(AssociationRecommendService mock) {
		sMock = mock;
	}

	public static AssociationRecommendService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new AssociationRecommendService();
		}
	}
}
