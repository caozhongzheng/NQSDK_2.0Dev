package com.nqmobile.livesdk.modules.lqwidget;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class LqWidgetPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String KEY_LQWIDGET_ENABLE = "lqwidget_enable";
	// ===========================================================
	// Fields
	// ===========================================================
	private static LqWidgetPreference sInstance = new LqWidgetPreference();

	// ===========================================================
	// Constructors
	// ===========================================================
	private LqWidgetPreference() {
	}

	public static LqWidgetPreference getInstance() {
		return sInstance;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public boolean isLqWidgetEnable() {
		return getBooleanValue(KEY_LQWIDGET_ENABLE);
	}

	public void setLqWidgetEnable(boolean enable) {
		setBooleanValue(KEY_LQWIDGET_ENABLE, enable);
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
