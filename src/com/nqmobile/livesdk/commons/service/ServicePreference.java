package com.nqmobile.livesdk.commons.service;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class ServicePreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String KEY_LAST_PERIOD_CHECK = "last_period_check";
	// ===========================================================
	// Fields
	// ===========================================================
	private static ServicePreference sInstance = new ServicePreference();
	// ===========================================================
	// Constructors
	// ===========================================================
	private ServicePreference() {
	}
	
	public static ServicePreference getInstance(){
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public long getLastPeriodCheck(){
	    return getLongValue(KEY_LAST_PERIOD_CHECK);
	}
	
	public void setLastPeriodCheck(long time){
        setLongValue(KEY_LAST_PERIOD_CHECK, time);
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
