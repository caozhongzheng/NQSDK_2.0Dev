package com.nqmobile.livesdk.commons.info;

import java.util.Map;

public class BaseClientInfo implements IClientInfo {
	private static final int BUSSINESS_ID = 138;
	
	private static final String CHANNEL_ID = "1979";
	
	private static final int EDITION_ID = 40025;
	
	private final static String PACKAGE_NAME = "com.booster.launcher.fast.wallpaper.theme";
	
	private static boolean IS_GP = true;

	@Override
	public int getBusinessId() {
		return BUSSINESS_ID;
	}
	
	@Override
	public String getChannelId() {
		return CHANNEL_ID;
	}

	@Override
	public int getEditionId() {
		return EDITION_ID;
	}

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}
	
	@Override
	public boolean isGP() {
		return IS_GP;
	}

	@Override
	public boolean hasLocalTheme() {
		return true;
	}

	@Override
	public Map<String, Boolean> overrideModuleDefaults() {
		return null; //do nothing, follow defaults in InitManager
	}
	
	public boolean isUseBingSearchUrl(){
		return false;
	}

	@Override
	public void onUpgrade(int lastVer) {

	}

	@Override
	public String getWX_APPID() {
		return null;
	}
	
	@Override
	public String getQQ_APPID() {
		return null;
	}
}
