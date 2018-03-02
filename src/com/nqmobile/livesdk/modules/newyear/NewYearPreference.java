package com.nqmobile.livesdk.modules.newyear;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2015/2/9.
 */
public class NewYearPreference extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private static NewYearPreference sInstance;

    private static final String KEY_LOTTERY = "lottery";

    // ===========================================================
    // Constructors
    // ===========================================================

    private NewYearPreference() {
    }

    public static NewYearPreference getInstance(){
        if(sInstance == null){
            sInstance = new NewYearPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public boolean getLottery(){
        return getBooleanValue(KEY_LOTTERY);
    }

    public void setLottery(boolean lottery){
        setBooleanValue(KEY_LOTTERY,lottery);
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
