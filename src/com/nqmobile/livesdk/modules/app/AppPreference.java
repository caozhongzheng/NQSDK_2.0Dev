package com.nqmobile.livesdk.modules.app;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class AppPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	/** long类型 */
	public static final String[] KEY_APP_LIST_CACHE_TIME = {"store_app_list_cache_time0", "store_app_list_cache_time1", "store_app_list_cache_time2"};
	public static final String KEY_APP_ENABLE = "app_enable";
	// ===========================================================
	// Fields
	// ===========================================================
	private static AppPreference sInstance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private AppPreference() {
	}
	
	public static AppPreference getInstance(){
		if(sInstance == null){
			sInstance = new AppPreference();
		}
		return sInstance;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public boolean isAppEnable(){
	    return getBooleanValue(KEY_APP_ENABLE);
	}
	public void setAppEnable(boolean enable){
        setBooleanValue(KEY_APP_ENABLE, enable);
    }
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	// ===========================================================
	// Methods
	// ===========================================================
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
