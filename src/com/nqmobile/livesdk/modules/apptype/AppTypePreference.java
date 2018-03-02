package com.nqmobile.livesdk.modules.apptype;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class AppTypePreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
    private static final String KEY_APP_LIB_VERSION = "app_lib_ver";// 应用类别预置库本地版本号
    private static final String KEY_APP_LIB_SERVER_VERSION = "app_lib_ser_ver";// 应用类别预置库服务器版本号
    private static final String KEY_APP_LIB_SERVER_URL = "app_lib_ser_url";// 应用类别预置库服务器URL
    private static final String KEY_APP_LIB_SERVER_MD5 = "app_lib_ser_md5";// 应用类别预置库服务器MD5
    public static final String KEY_APP_LIB_START = "app_lib_down_start";// 应用类别预置库服务器资源下载处理中
	// ===========================================================
	// Fields
	// ===========================================================
    private static AppTypePreference sInstance = new AppTypePreference();

	// ===========================================================
	// Constructors
	// ===========================================================
	private AppTypePreference() {
	}
	
	public static AppTypePreference getInstance(){
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public String getAppLibVersion() {
        return getStringValue(KEY_APP_LIB_VERSION);
    }

    public void setAppLibVersion(String version) {
        setStringValue(KEY_APP_LIB_VERSION, version);
    }
    
    public String getAppLibServerVersion() {
        return getStringValue(KEY_APP_LIB_SERVER_VERSION);
    }

    public void setAppLibServerVersion(String version) {
        setStringValue(KEY_APP_LIB_SERVER_VERSION, version);
    }
    
    public String getAppLibServerUrl() {
        return getStringValue(KEY_APP_LIB_SERVER_URL);
    }

    public void setAppLibServerUrl(String url) {
        setStringValue(KEY_APP_LIB_SERVER_URL, url);
    }
    
    public String getAppLibServerMd5() {
        return getStringValue(KEY_APP_LIB_SERVER_MD5);
    }

    public void setAppLibServerMd5(String url) {
        setStringValue(KEY_APP_LIB_SERVER_MD5, url);
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
