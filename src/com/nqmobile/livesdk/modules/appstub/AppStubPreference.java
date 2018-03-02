package com.nqmobile.livesdk.modules.appstub;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class AppStubPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
    /**虚框应用开关*/
	public static final String KEY_APP_STUB_ENABLE = "app_stub_enable";
    /**虚框应用是否有更新*/
	public static final String KEY_APP_STUB_HAS_UPDATE = "app_stub_has_update";
	// ===========================================================
	// Fields
	// ===========================================================
    private static AppStubPreference sInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private AppStubPreference() {
	}
	
	public static AppStubPreference getInstance(){
		if(sInstance == null){
			sInstance = new AppStubPreference();
		}
		
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public boolean isAppStubEnable(){
        return getBooleanValue(KEY_APP_STUB_ENABLE);
    }
    public void setAppStubEnable(boolean enable){
        setBooleanValue(KEY_APP_STUB_ENABLE, enable);
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
