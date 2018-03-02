package com.nqmobile.livesdk.commons.preference;

public class PreferenceServiceFactory {
	public static final String LIVE_SDK_SETTINGS_PREFERENCE = "LiveSDKSettings";
	public static final String LIVE_SDK_PKGS_PREFERENCE = "LiveSDKPkgs";
	
	private static PreferenceService sMock;

	public static void setMock(PreferenceService mock) {
		sMock = mock;
	}

	public static PreferenceService getService(String name) {
		if (sMock != null) {
			return sMock;
		} 
		else if (LIVE_SDK_SETTINGS_PREFERENCE.equals(name)) {
			return AndroidPreferenceService.getInstance(name);
		} 
		else if (LIVE_SDK_PKGS_PREFERENCE.equals(name)) {
			return AndroidPreferenceService.getInstance(name);
		} 
		else {
			return null;
		}
	}
}
