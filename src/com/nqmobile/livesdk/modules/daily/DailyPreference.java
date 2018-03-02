package com.nqmobile.livesdk.modules.daily;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class DailyPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	/** long类型 */
	public static final String KEY_DAILY_LIST_CACHE_TIME = "store_daily_list_cache_time";
	public static final String KEY_DAILY_DISPLAY_SEQ = "daily_display_seq";
	public static final String KEY_DAILY_COUNT = "daily_count";	
	// ===========================================================
	// Fields
	// ===========================================================
	private static DailyPreference sInstance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private DailyPreference() {
	}
	
	public static DailyPreference getInstance(){
		if(sInstance == null){
			sInstance = new DailyPreference();
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
