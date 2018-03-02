package com.nqmobile.livesdk.modules.font;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class FontPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String KEY_FONT_ENABLE = "font_enable";
	
	public static final String KEY_CURRENT_FONT= "current_font";
	
	public static final String KEY_CURRENT_FONTID= "current_font_id";
	// ===========================================================
	// Fields
	// ===========================================================
	private static FontPreference sInstance;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private FontPreference() {
	}
	
	public static FontPreference getInstance(){
		if(sInstance == null){
			sInstance = new FontPreference();
		}
		return sInstance;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public boolean isFontEnable(){
	    return getBooleanValue(KEY_FONT_ENABLE);
	}
	public void setFontEnable(boolean enable){
        setBooleanValue(KEY_FONT_ENABLE, enable);
    }
	
	public void setCurrentFont(String fontPath) {
		setStringValue(KEY_CURRENT_FONT, fontPath);
	}
	
	public String getCurrentFont() {
		return getStringValue(KEY_CURRENT_FONT);
	}
	public String getCurrentFontId() {
		return getStringValue(KEY_CURRENT_FONTID);
	}
	public void setCurrentFontId(String fontId) {
		setStringValue(KEY_CURRENT_FONTID, fontId);
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
