package com.nqmobile.livesdk.modules.activation;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class ActivePreference extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String KEY_UID = "uid";
    public static final String KEY_NEED_FORE_ACTIVE = "need_fore_active";
    public static final String KEY_FORE_ACTIVE_SUCC = "fore_active_succ";
    /**
     * 覆盖安装标志
     */
    private static final String KEY_OVERRIDE_INSTALL = "override_install";
    
    public static final String KEY_SERVERREGION = "serverregion";

    // ===========================================================
    // Fields
    // ===========================================================

    private static ActivePreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private ActivePreference() {
    }

    public static ActivePreference getInstance(){
        if(sInstance == null){
            sInstance = new ActivePreference();
        }

        return sInstance;
    }

	public boolean isOverrideInstall() {
		return getBooleanValue(KEY_OVERRIDE_INSTALL);
	}

	public void setOverrideInstall(boolean isOverInstall) {
		setBooleanValue(KEY_OVERRIDE_INSTALL, true);
	}
    // ===========================================================
    // Getter & Setter
    // ===========================================================

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
