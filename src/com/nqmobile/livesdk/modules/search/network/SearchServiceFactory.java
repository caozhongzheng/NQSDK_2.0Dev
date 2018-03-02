package com.nqmobile.livesdk.modules.search.network;

/**
 * Push服务工厂
 * 
 * @author HouKangxi
 */
public class SearchServiceFactory {
	private static SearchService sMock;

	public static void setMock(SearchService mock) {
		sMock = mock;
	}

	public static SearchService getService() {
		if (sMock != null) {
			return sMock;
		} else {
			return new SearchService();
		}
	}
}
