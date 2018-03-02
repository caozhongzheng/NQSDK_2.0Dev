package com.nqmobile.livesdk.modules.weather;

import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.moduleframework.IPolicy;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;

public class WeatherPolicy implements IPolicy {
	private static final long WEATHER_UPDATE_INTERVAL = 1 * DateUtils.HOUR_IN_MILLIS;
	private static final long LOCATION_UPDATE_INTERVAL =30 * DateUtils.MINUTE_IN_MILLIS;
	
	public static boolean needUpdateWeather(){
		WeatherPreference preference = WeatherPreference.getInstance();
		
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		long lastWeatehrTime = preference.getLongValue(WeatherPreference.KEY_LAST_GET_WEATHER_TIME);
		
		if (Math.abs(now - lastWeatehrTime) >= WEATHER_UPDATE_INTERVAL) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void initLastGetWeatherTime(){
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		WeatherPreference preference = WeatherPreference.getInstance();
		preference.setLongValue(WeatherPreference.KEY_LAST_GET_WEATHER_TIME, now);
	}

	public static boolean needUpdateLocation() {
		WeatherPreference preference = WeatherPreference.getInstance();
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		long last_location_time = preference.getLongValue(WeatherPreference.KEY_LAST_LOCATION_TIME);
        
		if (Math.abs(now - last_location_time) >= LOCATION_UPDATE_INTERVAL){
			return true;
		} else {
			return false;
		}
	}
}
