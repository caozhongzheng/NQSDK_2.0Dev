package com.nqmobile.livesdk.modules.defaultlauncher;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class DefaultLauncherPreference extends SettingsPreference {
    // ===========================================================
    // Constants
    // ===========================================================
    public static final String KEY_DEFAULT_LAUNCHER_ENABLE = "default_launcher_enable";//默认桌面模块是否启用
    private static final String KEY_LAST_SHOW_DIALOG_TIME = "last_show_dialog_time";
    private static final String KEY_LAST_GUIDE_LAUNCHER_TIME = "last_guide_launcher_time";

    // ===========================================================
    // Fields
    // ===========================================================

    private static DefaultLauncherPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private DefaultLauncherPreference() {
    }

    public static DefaultLauncherPreference getInstance(){
        if(sInstance == null){
            sInstance = new DefaultLauncherPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public void setDefaultLauncherEnable(boolean enable){
        setBooleanValue(KEY_DEFAULT_LAUNCHER_ENABLE, enable);
    }
    
    public boolean isDefaultLauncherEnable(){
        return getBooleanValue(KEY_DEFAULT_LAUNCHER_ENABLE);
    }

    public void setLastShowDialogTime(long time){
        setLongValue(KEY_LAST_SHOW_DIALOG_TIME,time);
    }

    public long getLastShowDialogTime(){
        return getLongValue(KEY_LAST_SHOW_DIALOG_TIME);
    }

    public void setLastGuideLauncherTime(long time){
        setLongValue(KEY_LAST_GUIDE_LAUNCHER_TIME,time);
    }

    public long getLastGuideLauncherTime(){
        return getLongValue(KEY_LAST_GUIDE_LAUNCHER_TIME);
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
