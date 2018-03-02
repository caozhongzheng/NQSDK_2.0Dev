package com.nqmobile.livesdk.modules.theme;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class ThemePreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
    public static final String KEY_THEME_ENABLE = "theme_enable";
	/** long类型 */
	public static final String[] KEY_THEME_LIST_CACHE_TIME = {"store_theme_list_cache_time0", "store_theme_list_cache_time1"};
	public static final String KEY_CURRENT_THEME = "current_theme";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private static ThemePreference sInstance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private ThemePreference() {
	}
	
	public static ThemePreference getInstance(){
		if(sInstance == null){
			sInstance = new ThemePreference();
		}
		return sInstance;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public boolean isThemeEnable(){
        return getBooleanValue(KEY_THEME_ENABLE);
    }
    public void setThemeEnable(boolean enable){
        setBooleanValue(KEY_THEME_ENABLE, enable);
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
