package com.nqmobile.livesdk.modules.batterypush;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class BatteryPushPreference extends SettingsPreference {
    // ===========================================================
    // Constants
    // ===========================================================
    
    public static final String KEY_BATTERY_PUSH_ENABLE = "battery_push_enable";


    // ===========================================================
    // Fields
    // ===========================================================

    private static BatteryPushPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private BatteryPushPreference() {
    }

    public static BatteryPushPreference getInstance(){
        if(sInstance == null){
            sInstance = new BatteryPushPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public void setBatteryPushEnable(boolean enable){
        setBooleanValue(KEY_BATTERY_PUSH_ENABLE, enable);
    }
    
    public boolean isBatteryPushEnable(){
        return getBooleanValue(KEY_BATTERY_PUSH_ENABLE);
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
