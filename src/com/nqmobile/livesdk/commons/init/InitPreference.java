package com.nqmobile.livesdk.commons.init;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class InitPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	/* 应用首次初始化是否完成 */
	public static final String KEY_APP_INIT_DONE = "app_init_done";
	public static final String KEY_INIT_FINISH = "init_finish";//兼容之前的KEY
	public static final String KEY_CURRENT_VERSION = "sdk_current_version";
	public static final String KEY_LAST_VERSION = "sdk_last_version";
	// ===========================================================
	// Fields
	// ===========================================================
	private static InitPreference sInstance = new InitPreference();
	// ===========================================================
	// Constructors
	// ===========================================================
	private InitPreference() {
	}
	
	public static InitPreference getInstance(){
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public boolean getAppInitDone(){
	    return getBooleanValue(KEY_APP_INIT_DONE) || getBooleanValue(KEY_INIT_FINISH);//兼容之前的KEY
	}
	
	public void setAppInitDone(boolean appInitDone){
        setBooleanValue(KEY_APP_INIT_DONE, appInitDone);
        setBooleanValue(KEY_INIT_FINISH, appInitDone);//兼容之前的KEY
    }
	
	public int getCurrentVersion(){
	    return getIntValue(KEY_CURRENT_VERSION);
	}
	
	public void setCurrentVersion(int version){
        setIntValue(KEY_CURRENT_VERSION, version);
    }
	
	public int getLastVersion(){
	    return getIntValue(KEY_LAST_VERSION);
	}
	
	public void setLastVersion(int version){
        setIntValue(KEY_LAST_VERSION, version);
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
