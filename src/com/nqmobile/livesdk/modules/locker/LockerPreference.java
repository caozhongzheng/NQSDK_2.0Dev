package com.nqmobile.livesdk.modules.locker;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class LockerPreference  extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================
    public static final String KEY_LOCKER_ENABLE = "locker_enable";
    // ===========================================================
    // Fields
    // ===========================================================

    private static LockerPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private LockerPreference() {
    }

    public static LockerPreference getInstance(){
        if(sInstance == null){
            sInstance = new LockerPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public boolean isLockerEnable(){
        return getBooleanValue(KEY_LOCKER_ENABLE);
    }
    public void setLockerEnable(boolean enable){
        setBooleanValue(KEY_LOCKER_ENABLE, enable);
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
