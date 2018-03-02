package com.nqmobile.livesdk.modules.banner;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class BannerPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	/** long类型 */
	public static final String KEY_BANNER_LIST_CACHE_TIME = "store_banner_list_cache_time";
	public static final String KEY_CURRENT_THEME = "current_theme";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private static BannerPreference sInstance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private BannerPreference() {
	}
	
	public static BannerPreference getInstance(){
		if(sInstance == null){
			sInstance = new BannerPreference();
		}
		return sInstance;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
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
