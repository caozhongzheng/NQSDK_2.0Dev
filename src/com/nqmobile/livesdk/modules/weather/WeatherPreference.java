package com.nqmobile.livesdk.modules.weather;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class WeatherPreference extends SettingsPreference {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String KEY_SERVERREGION = "serverregion";
    public static final String KEY_LAST_LAT = "last_lat";
    public static final String KEY_LAST_LON = "last_lon";
    public static final String KEY_LAST_LOCATION_TIME = "last_location_time";
	public static final String KEY_LAST_GET_WEATHER_TIME = "last_get_weather_time";
	
    //温度单位是否为华氏
    public static final String KEY_TEMPERATURE_UNIT_FAH = "temperature_unit_fah";
	// ===========================================================
	// Fields
	// ===========================================================
	private static WeatherPreference sInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private WeatherPreference(){
		
	}
	
	public static WeatherPreference getInstance(){
		if (sInstance == null){
			sInstance = new WeatherPreference();
		}
		
		return sInstance;
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
