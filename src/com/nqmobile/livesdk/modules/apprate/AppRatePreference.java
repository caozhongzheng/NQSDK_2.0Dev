package com.nqmobile.livesdk.modules.apprate;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class AppRatePreference extends SettingsPreference {
    // ===========================================================
    // Constants
    // ===========================================================
    public static final String KEY_APP_RATE_ENABLE = "app_rate_enable";//评分模块是否启用
    public static final String KEY_APP_RATE_DISPLAY_COUNT = "app_rate_display_count";//当前版本评分弹框已展示次数
    public static final String KEY_APP_RATE_INIT_TIME = "app_rate_init_time";//当前版本的初始进入时间
    public static final String KEY_APP_RATE_VERSION_NAME = "app_rate_version_name";//当前版本的versionName

    // ===========================================================
    // Fields
    // ===========================================================

    private static AppRatePreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private AppRatePreference() {
    }

    public static AppRatePreference getInstance(){
        if(sInstance == null){
            sInstance = new AppRatePreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public void setAppRateEnable(boolean enable){
        setBooleanValue(KEY_APP_RATE_ENABLE, enable);
    }
    
    public boolean isAppRateEnable(){
        return getBooleanValue(KEY_APP_RATE_ENABLE);
    }
    
    
    public void setDisplayCount(int count){
        setIntValue(KEY_APP_RATE_DISPLAY_COUNT, count);
    }
    
    public int getDisplayCount(){
        return getIntValue(KEY_APP_RATE_DISPLAY_COUNT);
    }
    
    public void setInitTime(long time){
        setLongValue(KEY_APP_RATE_INIT_TIME, time);
    }
    
    public long getInitTime(){
        return getLongValue(KEY_APP_RATE_INIT_TIME);
    }
    
    public void setVersionName(String versionName){
        setStringValue(KEY_APP_RATE_VERSION_NAME, versionName);
    }
    
    public String getVersionName(){
        return getStringValue(KEY_APP_RATE_VERSION_NAME);
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
