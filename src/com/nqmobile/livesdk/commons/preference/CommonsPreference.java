package com.nqmobile.livesdk.commons.preference;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class CommonsPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String KEY_CHANEL_ID = "channel_id";
    private static final String KEY_NEED_FORE_ACTIVE = "need_fore_active";
    private static final String KEY_FORE_ACTIVE_SUCC = "fore_active_succ";
    
	// ===========================================================
	// Fields
	// ===========================================================
    private static CommonsPreference sInstance = new CommonsPreference();

	// ===========================================================
	// Constructors
	// ===========================================================
	private CommonsPreference() {
	}
	
	public static CommonsPreference getInstance(){
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public String getChannelId() {
        return getStringValue(KEY_CHANEL_ID);
    }

    public void setChannelId(String channelId) {
        setStringValue(KEY_CHANEL_ID, channelId);
    }
    
	public boolean isNeedForegroundActive(){
	    return getBooleanValue(KEY_NEED_FORE_ACTIVE);
	}	
	public void setNeedForegroundActive(boolean need){
        setBooleanValue(KEY_NEED_FORE_ACTIVE, need);
    }
	
	public boolean isForegroundActiveSuccess(){
	    return getBooleanValue(KEY_FORE_ACTIVE_SUCC);
	}	
	public void setForegroundActiveSuccess(boolean success){
        setBooleanValue(KEY_FORE_ACTIVE_SUCC, success);
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
