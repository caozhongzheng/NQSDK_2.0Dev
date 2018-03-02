package com.nqmobile.livesdk.modules.wallpaper;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class WallpaperPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
    public static final String KEY_WALLPAPER_ENABLE = "wallpaper_enable";
	/** long类型 */
	public static final String[] KEY_WALLPAPER_LIST_CACHE_TIME = {"store_wallpaper_list_cache_time0", "store_wallpaper_list_cache_time1"};
	public static final String KEY_CURRENT_WALLPAPER = "current_wallpaper";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private static WallpaperPreference sInstance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private WallpaperPreference() {
	}
	
	public static WallpaperPreference getInstance(){
		if(sInstance == null){
			sInstance = new WallpaperPreference();
		}
		return sInstance;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public boolean isWallpaperEnable(){
        return getBooleanValue(KEY_WALLPAPER_ENABLE);
    }
    public void setWallpaperEnable(boolean enable){
        setBooleanValue(KEY_WALLPAPER_ENABLE, enable);
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
